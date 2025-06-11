package com.pdm.domohouse.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}