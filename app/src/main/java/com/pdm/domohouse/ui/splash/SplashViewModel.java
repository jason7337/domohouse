package com.pdm.domohouse.ui.splash;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.pdm.domohouse.ui.base.BaseAndroidViewModel;

/**
 * ViewModel para la pantalla Splash
 * Maneja la lógica de inicio y navegación inicial
 */
public class SplashViewModel extends BaseAndroidViewModel {
    
    // Tiempo de duración del splash en milisegundos
    private static final long SPLASH_DURATION = 2000;
    
    // LiveData para indicar cuándo navegar
    private final MutableLiveData<NavigationDestination> _navigationEvent = new MutableLiveData<>();
    public LiveData<NavigationDestination> navigationEvent = _navigationEvent;
    
    // Handler para manejar el delay
    private final Handler handler = new Handler(Looper.getMainLooper());
    
    /**
     * Enum para los destinos de navegación
     */
    public enum NavigationDestination {
        LOGIN,
        HOME
    }
    
    /**
     * Constructor
     */
    public SplashViewModel(@NonNull Application application) {
        super(application);
        startSplashTimer();
    }
    
    /**
     * Inicia el temporizador del splash
     */
    private void startSplashTimer() {
        handler.postDelayed(() -> {
            // Verificar si el usuario está autenticado
            checkUserAuthentication();
        }, SPLASH_DURATION);
    }
    
    /**
     * Verifica si el usuario está autenticado
     * Por ahora siempre navega a Login
     * TODO: Implementar verificación real con Firebase en sesiones futuras
     */
    private void checkUserAuthentication() {
        // Por ahora siempre ir a Login
        // En futuras sesiones se verificará con Firebase
        _navigationEvent.setValue(NavigationDestination.LOGIN);
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        // Cancelar cualquier callback pendiente
        handler.removeCallbacksAndMessages(null);
    }
}