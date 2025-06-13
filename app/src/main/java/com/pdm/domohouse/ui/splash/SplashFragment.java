package com.pdm.domohouse.ui.splash;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.pdm.domohouse.R;
import com.pdm.domohouse.databinding.FragmentSplashBinding;

/**
 * Fragmento de Splash Screen
 * Muestra el logo con animación y navega según el estado de autenticación
 */
public class SplashFragment extends Fragment {
    
    // Binding para la vista
    private FragmentSplashBinding binding;
    
    // ViewModel
    private SplashViewModel viewModel;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflar el layout usando Data Binding
        binding = FragmentSplashBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Inicializar ViewModel con Factory para AndroidViewModel
        viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())).get(SplashViewModel.class);
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(getViewLifecycleOwner());
        
        // Configurar observadores
        setupObservers();
        
        // Iniciar animaciones
        startAnimations();
    }
    
    /**
     * Configura los observadores del ViewModel
     */
    private void setupObservers() {
        // Observar eventos de navegación
        viewModel.navigationEvent.observe(getViewLifecycleOwner(), destination -> {
            if (destination != null) {
                navigateToDestination(destination);
            }
        });
    }
    
    /**
     * Inicia las animaciones del splash
     */
    private void startAnimations() {
        // Preparar vistas para animación
        binding.logoImageView.setAlpha(0f);
        binding.logoImageView.setScaleX(0.5f);
        binding.logoImageView.setScaleY(0.5f);
        
        binding.appNameTextView.setAlpha(0f);
        binding.appNameTextView.setTranslationY(50f);
        
        // Animación del logo
        ObjectAnimator logoAlpha = ObjectAnimator.ofFloat(binding.logoImageView, "alpha", 0f, 1f);
        ObjectAnimator logoScaleX = ObjectAnimator.ofFloat(binding.logoImageView, "scaleX", 0.5f, 1f);
        ObjectAnimator logoScaleY = ObjectAnimator.ofFloat(binding.logoImageView, "scaleY", 0.5f, 1f);
        
        AnimatorSet logoAnimator = new AnimatorSet();
        logoAnimator.playTogether(logoAlpha, logoScaleX, logoScaleY);
        logoAnimator.setDuration(800);
        logoAnimator.setInterpolator(new DecelerateInterpolator());
        
        // Animación del texto
        ObjectAnimator textAlpha = ObjectAnimator.ofFloat(binding.appNameTextView, "alpha", 0f, 1f);
        ObjectAnimator textTranslation = ObjectAnimator.ofFloat(binding.appNameTextView, "translationY", 50f, 0f);
        
        AnimatorSet textAnimator = new AnimatorSet();
        textAnimator.playTogether(textAlpha, textTranslation);
        textAnimator.setDuration(600);
        textAnimator.setStartDelay(400);
        textAnimator.setInterpolator(new DecelerateInterpolator());
        
        // Iniciar animaciones
        logoAnimator.start();
        textAnimator.start();
    }
    
    /**
     * Navega al destino especificado
     * @param destination Destino de navegación
     */
    private void navigateToDestination(SplashViewModel.NavigationDestination destination) {
        switch (destination) {
            case LOGIN:
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_splash_to_login);
                break;
            case HOME:
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_splash_to_home);
                break;
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}