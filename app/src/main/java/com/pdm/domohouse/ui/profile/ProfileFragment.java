package com.pdm.domohouse.ui.profile;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.bumptech.glide.Glide;
import com.pdm.domohouse.R;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import com.pdm.domohouse.databinding.FragmentProfileBinding;
import com.pdm.domohouse.data.model.UserProfile;

/**
 * Fragmento para la gestión del perfil de usuario
 * Permite editar información personal, cambiar configuraciones y gestionar la cuenta
 */
public class ProfileFragment extends Fragment {
    
    private FragmentProfileBinding binding;
    private ProfileViewModel viewModel;
    
    // Launcher para seleccionar imagen de perfil
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Configurar el launcher para seleccionar imagen
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        viewModel.updateProfilePhoto(imageUri);
                    }
                }
            }
        );
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Inicializar ViewModel con Factory para AndroidViewModel
        viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())).get(ProfileViewModel.class);
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);
        
        // Configurar UI
        setupUI();
        
        // Observar datos del ViewModel
        observeViewModel();
    }
    
    /**
     * Configura los elementos de la interfaz de usuario
     */
    private void setupUI() {
        // Botón de cámara para cambiar foto de perfil
        binding.cameraButton.setOnClickListener(v -> openImagePicker());
        
        // Botón de guardar cambios
        binding.saveChangesButton.setOnClickListener(v -> saveProfileChanges());
        
        // Botón de cambiar PIN
        binding.changePinButton.setOnClickListener(v -> showChangePinDialog());
        
        // Botón de cambiar contraseña
        binding.changePasswordButton.setOnClickListener(v -> showChangePasswordInfo());
        
        // Botón de cerrar sesión
        binding.logoutButton.setOnClickListener(v -> showLogoutConfirmation());
        
        // DEBUG: Agregar un listener de click largo en el header para forzar recarga
        binding.profileHeaderCard.setOnLongClickListener(v -> {
            android.util.Log.d("ProfileFragment", "Long click detectado, forzando recarga del perfil");
            viewModel.forceReloadProfile();
            Toast.makeText(getContext(), "Recargando perfil...", Toast.LENGTH_SHORT).show();
            return true;
        });
        
        // Listeners para detectar cambios en preferencias
        binding.notificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) { // Solo si es interacción del usuario
                viewModel.markAsChanged();
                updatePreferences();
            }
        });
        
        binding.autoModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) { // Solo si es interacción del usuario
                viewModel.markAsChanged();
                updatePreferences();
            }
        });
        
        binding.temperatureAlertsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) { // Solo si es interacción del usuario
                viewModel.markAsChanged();
                updatePreferences();
            }
        });
        
        // Listener para detectar cambios en el nombre
        binding.nameEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                viewModel.markAsChanged();
            }
        });
    }
    
    /**
     * Observa los datos del ViewModel
     */
    private void observeViewModel() {
        // Observar el perfil del usuario
        viewModel.userProfile.observe(getViewLifecycleOwner(), this::updateProfileUI);
        
        // Observar estado de carga
        viewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            // Mostrar/ocultar indicador de carga
            binding.saveChangesButton.setEnabled(!isLoading);
        });
        
        // Observar estado de guardado
        viewModel.isSaving.observe(getViewLifecycleOwner(), isSaving -> {
            binding.saveChangesButton.setText(isSaving ? "Guardando..." : getString(R.string.save_changes));
            binding.saveChangesButton.setEnabled(!isSaving);
        });
        
        // Observar estado de subida de imagen
        viewModel.isUploadingImage.observe(getViewLifecycleOwner(), isUploading -> {
            binding.cameraButton.setEnabled(!isUploading);
        });
        
        // Observar cambios no guardados
        viewModel.hasUnsavedChanges.observe(getViewLifecycleOwner(), hasChanges -> {
            binding.saveChangesButton.setVisibility(hasChanges ? View.VISIBLE : View.GONE);
        });
        
        // Observar mensajes
        viewModel.message.observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
        
        // Observar errores
        viewModel.errorMessage.observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            }
        });
    }
    
    /**
     * Actualiza la UI con los datos del perfil
     */
    private void updateProfileUI(UserProfile profile) {
        if (profile == null) {
            android.util.Log.w("ProfileFragment", "Profile is null, cannot update UI");
            return;
        }
        
        android.util.Log.d("ProfileFragment", "Updating UI with profile: " + profile.getFullName() + ", " + profile.getEmail());
        
        // Actualizar nombre y email manualmente para asegurar que se muestren
        if (profile.getFullName() != null && !profile.getFullName().isEmpty()) {
            binding.userNameText.setText(profile.getFullName());
            binding.nameEditText.setText(profile.getFullName());
        }
        
        if (profile.getEmail() != null && !profile.getEmail().isEmpty()) {
            binding.userEmailText.setText(profile.getEmail());
            binding.emailEditText.setText(profile.getEmail());
        }
        
        // Actualizar preferencias si existen
        if (profile.getPreferences() != null) {
            binding.notificationsSwitch.setChecked(profile.getPreferences().isNotificationsEnabled());
            binding.autoModeSwitch.setChecked(profile.getPreferences().isAutoModeEnabled());
            binding.temperatureAlertsSwitch.setChecked(profile.getPreferences().isAnalyticsEnabled());
        }
        
        // Cargar foto de perfil
        loadProfileImage(profile.getProfilePhotoUrl());
    }
    
    /**
     * Carga la imagen de perfil usando Glide
     */
    private void loadProfileImage(String profilePhotoUrl) {
        android.util.Log.d("ProfileFragment", "Loading profile image: " + profilePhotoUrl);
        
        if (profilePhotoUrl != null && !profilePhotoUrl.isEmpty()) {
            // Cargar imagen desde URL
            android.util.Log.d("ProfileFragment", "Cargando imagen desde URL: " + profilePhotoUrl);
            Glide.with(this)
                .load(profilePhotoUrl)
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .circleCrop()
                .into(binding.profileImage);
        } else {
            // Intentar cargar desde Gravatar usando el email del usuario
            UserProfile profile = viewModel.userProfile.getValue();
            if (profile != null && profile.getEmail() != null && !profile.getEmail().isEmpty()) {
                String gravatarUrl = getGravatarUrl(profile.getEmail());
                android.util.Log.d("ProfileFragment", "Intentando cargar Gravatar: " + gravatarUrl);
                
                Glide.with(this)
                    .load(gravatarUrl)
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .circleCrop()
                    .into(binding.profileImage);
            } else {
                // Mostrar imagen por defecto
                android.util.Log.d("ProfileFragment", "No hay email para Gravatar, usando imagen por defecto");
                binding.profileImage.setImageResource(R.drawable.ic_person);
                binding.profileImage.setColorFilter(null);
            }
        }
    }
    
    /**
     * Genera la URL de Gravatar para el email del usuario
     */
    private String getGravatarUrl(String email) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(email.trim().toLowerCase().getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return "https://www.gravatar.com/avatar/" + sb.toString() + "?s=200&d=404";
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }
    
    /**
     * Abre un diálogo para seleccionar foto de perfil
     */
    private void openImagePicker() {
        new AlertDialog.Builder(getContext())
            .setTitle("Cambiar foto de perfil")
            .setMessage("¿Cómo quieres actualizar tu foto de perfil?")
            .setPositiveButton("Galería", (dialog, which) -> openGallery())
            .setNegativeButton("Cámara", (dialog, which) -> openCamera())
            .setNeutralButton("Cancelar", null)
            .show();
    }
    
    /**
     * Abre la galería para seleccionar imagen
     */
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(Intent.createChooser(intent, "Seleccionar foto de perfil"));
    }
    
    /**
     * Abre la cámara para tomar una foto
     */
    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            imagePickerLauncher.launch(intent);
        } else {
            Toast.makeText(getContext(), "No se encontró una aplicación de cámara", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Guarda los cambios del perfil
     */
    private void saveProfileChanges() {
        String newName = binding.nameEditText.getText().toString().trim();
        viewModel.saveProfile(newName);
    }
    
    /**
     * Actualiza las preferencias del usuario
     */
    private void updatePreferences() {
        boolean notificationsEnabled = binding.notificationsSwitch.isChecked();
        boolean autoModeEnabled = binding.autoModeSwitch.isChecked();
        boolean analyticsEnabled = binding.temperatureAlertsSwitch.isChecked();
        
        viewModel.updatePreferences(notificationsEnabled, autoModeEnabled, analyticsEnabled);
    }
    
    /**
     * Muestra el diálogo para cambiar PIN
     */
    private void showChangePinDialog() {
        new ChangePinDialog(getContext(), viewModel).show();
    }
    
    /**
     * Muestra información sobre cambio de contraseña
     */
    private void showChangePasswordInfo() {
        new AlertDialog.Builder(getContext())
            .setTitle("Cambiar Contraseña")
            .setMessage("Para cambiar tu contraseña, puedes usar la opción 'Olvidé mi contraseña' en la pantalla de inicio de sesión.")
            .setPositiveButton("Entendido", null)
            .show();
    }
    
    /**
     * Muestra confirmación para cerrar sesión
     */
    private void showLogoutConfirmation() {
        new AlertDialog.Builder(getContext())
            .setTitle("Cerrar Sesión")
            .setMessage("¿Estás seguro de que quieres cerrar sesión?")
            .setPositiveButton("Sí", (dialog, which) -> {
                viewModel.logout();
                // Navegar a la pantalla de login
                Navigation.findNavController(binding.getRoot())
                    .navigate(R.id.action_profile_to_login);
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}