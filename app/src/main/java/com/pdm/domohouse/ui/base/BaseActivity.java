package com.pdm.domohouse.ui.base;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.ViewModelProvider;

/**
 * Actividad base para todas las actividades de la aplicación
 * Proporciona funcionalidad común como Data Binding, manejo de ViewModels y estados de carga
 * @param <T> Tipo de ViewDataBinding
 * @param <V> Tipo de ViewModel
 */
public abstract class BaseActivity<T extends ViewDataBinding, V extends BaseViewModel> extends AppCompatActivity {
    
    // Instancia del binding
    protected T binding;
    
    // Instancia del ViewModel
    protected V viewModel;
    
    /**
     * Obtiene el ID del layout de la actividad
     * @return ID del recurso de layout
     */
    @LayoutRes
    protected abstract int getLayoutId();
    
    /**
     * Obtiene la clase del ViewModel
     * @return Clase del ViewModel
     */
    protected abstract Class<V> getViewModelClass();
    
    /**
     * Inicializa la UI después de que el binding esté listo
     */
    protected abstract void setupUI();
    
    /**
     * Configura los observadores de LiveData
     */
    protected abstract void setupObservers();
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Inicializar Data Binding
        binding = DataBindingUtil.setContentView(this, getLayoutId());
        
        // Inicializar ViewModel
        viewModel = new ViewModelProvider(this).get(getViewModelClass());
        
        // Configurar UI
        setupUI();
        
        // Configurar observadores base
        setupBaseObservers();
        
        // Configurar observadores específicos de la actividad
        setupObservers();
    }
    
    /**
     * Configura los observadores base comunes a todas las actividades
     */
    private void setupBaseObservers() {
        // Observar estado de carga
        viewModel.isLoading.observe(this, isLoading -> {
            if (isLoading != null) {
                showLoading(isLoading);
            }
        });
        
        // Observar mensajes de error
        viewModel.errorMessage.observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                showError(errorMessage);
            }
        });
    }
    
    /**
     * Muestra u oculta el indicador de carga
     * @param show true para mostrar, false para ocultar
     */
    protected void showLoading(boolean show) {
        // Las actividades hijas pueden sobrescribir este método
        // para personalizar cómo se muestra el estado de carga
    }
    
    /**
     * Muestra un mensaje de error
     * @param message Mensaje a mostrar
     */
    protected void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
    
    /**
     * Muestra un mensaje informativo
     * @param message Mensaje a mostrar
     */
    protected void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Limpiar binding para evitar fugas de memoria
        if (binding != null) {
            binding.unbind();
        }
    }
}