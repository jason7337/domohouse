package com.pdm.domohouse.ui.splash;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.pdm.domohouse.network.FirebaseAuthManager;
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
     * Verifica si el usuario está autenticado con Firebase
     */
    private void checkUserAuthentication() {
        try {
            FirebaseAuthManager authManager = FirebaseAuthManager.getInstance();
            
            // Verificar si hay un usuario autenticado
            if (authManager.isUserSignedIn()) {
                android.util.Log.d("SplashViewModel", "Usuario autenticado encontrado, navegando a Home");
                _navigationEvent.setValue(NavigationDestination.HOME);
            } else {
                android.util.Log.d("SplashViewModel", "No hay usuario autenticado, navegando a Login");
                _navigationEvent.setValue(NavigationDestination.LOGIN);
            }
        } catch (Exception e) {
            android.util.Log.e("SplashViewModel", "Error verificando autenticación: " + e.getMessage());
            // En caso de error, ir a login por seguridad
            _navigationEvent.setValue(NavigationDestination.LOGIN);
        }
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        // Cancelar cualquier callback pendiente
        handler.removeCallbacksAndMessages(null);
    }
}