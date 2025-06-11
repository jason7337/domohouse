package com.pdm.domohouse.ui.base;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * Clase base para todos los ViewModels de la aplicación
 * Proporciona funcionalidad común para el manejo de estados y errores
 */
public abstract class BaseViewModel extends ViewModel {
    
    // LiveData para manejar el estado de carga
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;
    
    // LiveData para manejar mensajes de error
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> errorMessage = _errorMessage;
    
    /**
     * Constructor base
     */
    public BaseViewModel() {
        super();
    }
    
    /**
     * Establece el estado de carga (thread-safe)
     * @param loading true si está cargando, false en caso contrario
     */
    protected void setLoading(boolean loading) {
        _isLoading.postValue(loading);
    }
    
    /**
     * Establece un mensaje de error (thread-safe)
     * @param message El mensaje de error a mostrar
     */
    protected void setError(String message) {
        _errorMessage.postValue(message);
    }
    
    /**
     * Limpia el mensaje de error (thread-safe)
     */
    protected void clearError() {
        _errorMessage.postValue(null);
    }
    
    /**
     * Método llamado cuando el ViewModel va a ser destruido
     * Limpia todos los recursos
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        // Aquí se pueden limpiar recursos si es necesario
    }
}