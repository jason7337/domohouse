package com.pdm.domohouse.ui.auth;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.pdm.domohouse.R;
import com.pdm.domohouse.databinding.FragmentRegisterBinding;

/**
 * Fragmento de Registro
 * Maneja la interfaz de usuario para registro de nuevos usuarios
 */
public class RegisterFragment extends Fragment {
    
    // Binding para la vista
    private FragmentRegisterBinding binding;
    
    // ViewModel
    private RegisterViewModel viewModel;
    
    // Launchers para selección de imágenes
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<String> permissionLauncher;
    private Uri cameraImageUri;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeActivityLaunchers();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflar el layout usando Data Binding
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Inicializar ViewModel
        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);
        
        // Inicializar preferencias seguras
        viewModel.initializeSecurePreferences(requireContext());
        
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(getViewLifecycleOwner());
        
        // Configurar listeners
        setupClickListeners();
        
        // Configurar observadores
        setupObservers();
    }
    
    /**
     * Configura los listeners de click
     */
    private void setupClickListeners() {
        // Botón de registro
        binding.registerButton.setOnClickListener(v -> viewModel.register());
        
        // Enlace a login
        binding.loginLinkTextView.setOnClickListener(v -> viewModel.navigateToLogin());
        
        // Botones para selección de foto
        binding.selectFromGalleryButton.setOnClickListener(v -> selectFromGallery());
        binding.takePhotoButton.setOnClickListener(v -> takePhoto());
        binding.profilePhotoCard.setOnClickListener(v -> selectFromGallery());
    }
    
    /**
     * Configura los observadores del ViewModel
     */
    private void setupObservers() {
        // Observar navegación al home
        viewModel.navigateToHome.observe(getViewLifecycleOwner(), navigate -> {
            if (navigate != null && navigate) {
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_registerFragment_to_homeFragment);
                viewModel.onNavigationComplete();
            }
        });
        
        // Observar navegación al login
        viewModel.navigateToLogin.observe(getViewLifecycleOwner(), navigate -> {
            if (navigate != null && navigate) {
                Navigation.findNavController(requireView()).navigateUp();
                viewModel.onNavigationComplete();
            }
        });
        
        // Observar mensajes de error
        viewModel.errorMessage.observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
            }
        });
        
        // Observar cambios en la foto de perfil
        viewModel.profilePhotoUri.observe(getViewLifecycleOwner(), uri -> {
            if (uri != null) {
                binding.profilePhotoImageView.setImageURI(uri);
            }
        });
        
        // Observar progreso de subida de foto
        viewModel.photoUploadProgress.observe(getViewLifecycleOwner(), progress -> {
            if (progress != null && progress > 0) {
                // Mostrar progreso si es necesario
                Toast.makeText(requireContext(), "Subiendo foto: " + progress + "%", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Inicializa los launchers para actividades de selección de imágenes
     */
    private void initializeActivityLaunchers() {
        // Launcher para galería
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            viewModel.setProfilePhotoUri(selectedImageUri);
                        }
                    }
                }
        );
        
        // Launcher para cámara
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && cameraImageUri != null) {
                        viewModel.setProfilePhotoUri(cameraImageUri);
                    }
                }
        );
        
        // Launcher para permisos
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openCamera();
                    } else {
                        Toast.makeText(requireContext(), 
                                "Permiso de cámara necesario para tomar fotos", 
                                Toast.LENGTH_LONG).show();
                    }
                }
        );
    }
    
    /**
     * Abre la galería para seleccionar una imagen
     */
    private void selectFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }
    
    /**
     * Toma una foto con la cámara
     */
    private void takePhoto() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) 
                    != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.CAMERA);
                return;
            }
        }
        
        openCamera();
    }
    
    /**
     * Abre la cámara para tomar una foto
     */
    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(requireContext().getPackageManager()) != null) {
            // Crear un archivo temporal para la imagen
            try {
                cameraImageUri = createImageFileUri();
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
                cameraLauncher.launch(cameraIntent);
            } catch (Exception e) {
                Toast.makeText(requireContext(), 
                        "Error al abrir la cámara: " + e.getMessage(), 
                        Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(requireContext(), 
                    "No hay aplicación de cámara disponible", 
                    Toast.LENGTH_LONG).show();
        }
    }
    
    /**
     * Crea un URI temporal para la imagen de la cámara
     * @return URI del archivo temporal
     */
    private Uri createImageFileUri() {
        String fileName = "profile_photo_" + System.currentTimeMillis() + ".jpg";
        return Uri.fromFile(new java.io.File(requireContext().getExternalFilesDir(null), fileName));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}