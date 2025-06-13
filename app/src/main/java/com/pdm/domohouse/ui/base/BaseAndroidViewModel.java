package com.pdm.domohouse.ui.base;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * ViewModel base que extiende AndroidViewModel para tener acceso al Context
 * Proporciona funcionalidad común para todos los ViewModels que necesitan Context
 */
public abstract class BaseAndroidViewModel extends AndroidViewModel {
    
    protected final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public final LiveData<Boolean> isLoading = _isLoading;
    
    protected final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public final LiveData<String> errorMessage = _errorMessage;
    
    public BaseAndroidViewModel(@NonNull Application application) {
        super(application);
    }
    
    /**
     * Obtiene el contexto de la aplicación
     * @return Application context
     */
    protected Application getContext() {
        return getApplication();
    }
    
    /**
     * Establece el estado de carga
     * @param loading true si está cargando, false en caso contrario
     */
    protected void setLoading(boolean loading) {
        _isLoading.setValue(loading);
    }
    
    /**
     * Establece un mensaje de error
     * @param message Mensaje de error a mostrar
     */
    protected void setError(String message) {
        _errorMessage.setValue(message);
    }
    
    /**
     * Limpia el mensaje de error
     */
    protected void clearError() {
        _errorMessage.setValue(null);
    }
    
    // LiveData para manejar mensajes informativos
    protected final MutableLiveData<String> _message = new MutableLiveData<>();
    public final LiveData<String> message = _message;
    
    /**
     * Establece un mensaje informativo
     * @param message El mensaje informativo a mostrar
     */
    protected void setMessage(String message) {
        _message.setValue(message);
    }
}