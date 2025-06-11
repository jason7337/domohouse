package com.pdm.domohouse.ui.auth;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.pdm.domohouse.R;
import com.pdm.domohouse.databinding.FragmentLoginBinding;

/**
 * Fragmento de Login
 * Maneja la interfaz de usuario para autenticación con Firebase y PIN
 */
public class LoginFragment extends Fragment {
    
    // Binding para la vista
    private FragmentLoginBinding binding;
    
    // ViewModel
    private LoginViewModel viewModel;
    
    // Referencias a los campos de PIN
    private EditText[] pinFields;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflar el layout usando Data Binding
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Inicializar ViewModel
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        
        // Inicializar preferencias seguras
        viewModel.initializeSecurePreferences(requireContext());
        
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(getViewLifecycleOwner());
        
        // Configurar campos de PIN
        setupPinFields();
        
        // Configurar listeners
        setupClickListeners();
        
        // Configurar observadores
        setupObservers();
    }
    
    /**
     * Configura los campos de PIN
     */
    private void setupPinFields() {
        pinFields = new EditText[]{
                binding.pin1,
                binding.pin2,
                binding.pin3,
                binding.pin4
        };
        
        // Configurar navegación automática entre campos de PIN
        for (int i = 0; i < pinFields.length; i++) {
            final int currentIndex = i;
            
            pinFields[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && currentIndex < pinFields.length - 1) {
                        // Mover al siguiente campo
                        pinFields[currentIndex + 1].requestFocus();
                    } else if (s.length() == 0 && currentIndex > 0) {
                        // Mover al campo anterior si se borra
                        pinFields[currentIndex - 1].requestFocus();
                    }
                    
                    // Si se completó el PIN, intentar login
                    if (currentIndex == pinFields.length - 1 && s.length() == 1) {
                        checkPinComplete();
                    }
                }
                
                @Override
                public void afterTextChanged(Editable s) {}
            });
            
            // Manejo de tecla de retroceso
            pinFields[i].setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == android.view.KeyEvent.KEYCODE_DEL && 
                    currentIndex > 0 && 
                    pinFields[currentIndex].getText().toString().isEmpty()) {
                    pinFields[currentIndex - 1].requestFocus();
                    return true;
                }
                return false;
            });
        }
    }
    
    /**
     * Verifica si el PIN está completo y procede con el login
     */
    private void checkPinComplete() {
        StringBuilder pin = new StringBuilder();
        for (EditText field : pinFields) {
            pin.append(field.getText().toString());
        }
        
        if (pin.length() == 4) {
            viewModel.loginWithPin(pin.toString());
        }
    }
    
    /**
     * Configura los listeners de clicks
     */
    private void setupClickListeners() {
        // Click en "Olvidé mi contraseña"
        binding.forgotPasswordText.setOnClickListener(v -> 
                viewModel.forgotPassword());
        
        // Click en "Registrarse"
        binding.registerText.setOnClickListener(v -> 
                viewModel.navigateToRegister());
    }
    
    /**
     * Configura los observadores del ViewModel
     */
    private void setupObservers() {
        // Observar eventos de navegación a Home
        viewModel.navigateToHome.observe(getViewLifecycleOwner(), shouldNavigate -> {
            if (shouldNavigate != null && shouldNavigate) {
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_login_to_home);
                viewModel.onNavigationComplete();
            }
        });
        
        // Observar eventos de navegación a Registro
        viewModel.navigateToRegister.observe(getViewLifecycleOwner(), shouldNavigate -> {
            if (shouldNavigate != null && shouldNavigate) {
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_login_to_register);
                viewModel.onNavigationComplete();
            }
        });
        
        // Observar errores de validación
        viewModel.emailError.observe(getViewLifecycleOwner(), error -> {
            binding.emailInputLayout.setError(error);
        });
        
        viewModel.passwordError.observe(getViewLifecycleOwner(), error -> {
            binding.passwordInputLayout.setError(error);
        });
        
        viewModel.pinError.observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                // Mostrar error en el primer campo de PIN
                clearPinFields();
                binding.pin1.setError(error);
            }
        });
        
        // Observar mensajes de error generales
        viewModel.errorMessage.observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                // El BaseActivity maneja la visualización del error
                clearPinFields();
            }
        });
    }
    
    /**
     * Limpia todos los campos de PIN
     */
    private void clearPinFields() {
        for (EditText field : pinFields) {
            field.setText("");
            field.setError(null);
        }
        pinFields[0].requestFocus();
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        pinFields = null;
    }
}