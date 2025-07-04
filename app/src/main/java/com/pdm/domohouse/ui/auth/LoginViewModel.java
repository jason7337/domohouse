package com.pdm.domohouse.ui.auth;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseUser;
import com.pdm.domohouse.network.FirebaseAuthManager;
import com.pdm.domohouse.data.database.AppDatabase;
import com.pdm.domohouse.data.database.entity.UserProfileEntity;
import com.pdm.domohouse.sync.SyncManager;
import com.pdm.domohouse.ui.base.BaseAndroidViewModel;
import com.pdm.domohouse.utils.SecurePreferencesManager;

/**
 * ViewModel para la pantalla de Login
 * Maneja la lógica de autenticación con Firebase y PIN local
 */
public class LoginViewModel extends BaseAndroidViewModel {
    
    // Managers para autenticación
    private final FirebaseAuthManager firebaseAuthManager;
    private SecurePreferencesManager securePreferencesManager;
    private AppDatabase database;
    private SyncManager syncManager;
    private Context appContext;
    
    // Campos del formulario con Two-Way Data Binding
    private final MutableLiveData<String> _email = new MutableLiveData<>("");
    private final MutableLiveData<String> _password = new MutableLiveData<>("");
    
    public MutableLiveData<String> email = _email;
    public MutableLiveData<String> password = _password;
    
    // LiveData para eventos de navegación
    private final MutableLiveData<Boolean> _navigateToHome = new MutableLiveData<>();
    public LiveData<Boolean> navigateToHome = _navigateToHome;
    
    private final MutableLiveData<Boolean> _navigateToRegister = new MutableLiveData<>();
    public LiveData<Boolean> navigateToRegister = _navigateToRegister;
    
    // LiveData para el estado del formulario
    private final MutableLiveData<String> _emailError = new MutableLiveData<>();
    public LiveData<String> emailError = _emailError;
    
    private final MutableLiveData<String> _passwordError = new MutableLiveData<>();
    public LiveData<String> passwordError = _passwordError;
    
    private final MutableLiveData<String> _pinError = new MutableLiveData<>();
    public LiveData<String> pinError = _pinError;
    
    /**
     * Constructor
     */
    public LoginViewModel(@NonNull Application application) {
        super(application);
        this.firebaseAuthManager = FirebaseAuthManager.getInstance();
        // Inicializar managers que requieren contexto
        this.securePreferencesManager = SecurePreferencesManager.getInstance(application);
        this.database = AppDatabase.getDatabase(application);
        this.syncManager = SyncManager.getInstance(application);
        this.appContext = application;
    }
    
    /**
     * Método legacy para compatibilidad - ya no es necesario llamarlo
     * Los managers ahora se inicializan en el constructor
     * @param context Contexto de la aplicación
     * @deprecated Usar el constructor que recibe Application
     */
    @Deprecated
    public void initializeSecurePreferences(Context context) {
        // Los managers ya están inicializados en el constructor
    }
    
    /**
     * Maneja el proceso de login
     * Valida los campos y procede con la autenticación
     */
    public void login() {
        clearErrors();
        
        String emailValue = _email.getValue();
        String passwordValue = _password.getValue();
        
        // Validar campos
        if (!validateForm(emailValue, passwordValue)) {
            return;
        }
        
        // Mostrar estado de carga
        setLoading(true);
        
        // Verificar si hay conexión a internet
        if (syncManager != null && syncManager.isOnline()) {
            // Autenticar con Firebase (online)
            authenticateWithFirebase(emailValue, passwordValue);
        } else {
            // Intentar login offline
            attemptOfflineLogin(emailValue, passwordValue);
        }
    }
    
    /**
     * Autentica al usuario con Firebase
     * @param email Email del usuario
     * @param password Contraseña del usuario
     */
    private void authenticateWithFirebase(String email, String password) {
        firebaseAuthManager.signInWithEmailAndPassword(email, password, new FirebaseAuthManager.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                // Guardar perfil en base de datos local para acceso offline
                saveUserProfileLocally(user);
                // Sincronizar datos
                syncManager.syncAll();
                setLoading(false);
                _navigateToHome.postValue(true);
            }
            
            @Override
            public void onError(String error) {
                setLoading(false);
                // Traducir algunos errores comunes de Firebase al español
                String translatedError = translateFirebaseError(error);
                setError(translatedError);
            }
        });
    }
    
    /**
     * Maneja el login con PIN
     * @param pin PIN de 4 dígitos ingresado por el usuario
     */
    public void loginWithPin(String pin) {
        clearErrors();
        
        if (!validatePin(pin)) {
            return;
        }
        
        if (securePreferencesManager == null) {
            setError("Error del sistema: Preferencias no inicializadas");
            return;
        }
        
        setLoading(true);
        
        // Verificar PIN en background thread
        new Thread(() -> {
            try {
                Thread.sleep(500); // Simular delay de verificación
                
                if (securePreferencesManager.verifyPin(pin)) {
                    setLoading(false);
                    _navigateToHome.postValue(true);
                } else {
                    setLoading(false);
                    setError("PIN incorrecto");
                }
            } catch (Exception e) {
                setLoading(false);
                setError("Error al verificar PIN");
            }
        }).start();
    }
    
    /**
     * Navega a la pantalla de registro
     */
    public void navigateToRegister() {
        _navigateToRegister.setValue(true);
    }
    
    /**
     * Valida el formulario de login
     * @param email Email a validar
     * @param password Contraseña a validar
     * @return true si es válido, false en caso contrario
     */
    private boolean validateForm(String email, String password) {
        boolean isValid = true;
        
        // Validar email
        if (TextUtils.isEmpty(email)) {
            _emailError.setValue("Por favor ingresa tu correo electrónico");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailError.setValue("Por favor ingresa un correo válido");
            isValid = false;
        }
        
        // Validar contraseña
        if (TextUtils.isEmpty(password)) {
            _passwordError.setValue("Por favor ingresa tu contraseña");
            isValid = false;
        } else if (password.length() < 6) {
            _passwordError.setValue("La contraseña debe tener al menos 6 caracteres");
            isValid = false;
        }
        
        return isValid;
    }
    
    /**
     * Valida el PIN
     * @param pin PIN a validar
     * @return true si es válido, false en caso contrario
     */
    private boolean validatePin(String pin) {
        if (TextUtils.isEmpty(pin)) {
            _pinError.setValue("Por favor ingresa tu PIN");
            return false;
        }
        
        if (pin.length() != 4) {
            _pinError.setValue("El PIN debe ser de 4 dígitos");
            return false;
        }
        
        if (!pin.matches("\\d{4}")) {
            _pinError.setValue("El PIN debe contener solo números");
            return false;
        }
        
        return true;
    }
    
    /**
     * Limpia todos los errores
     */
    private void clearErrors() {
        _emailError.setValue(null);
        _passwordError.setValue(null);
        _pinError.setValue(null);
        clearError();
    }
    
    /**
     * Maneja el "olvidé mi contraseña"
     */
    public void forgotPassword() {
        String emailValue = _email.getValue();
        
        if (TextUtils.isEmpty(emailValue)) {
            _emailError.setValue("Por favor ingresa tu correo para recuperar la contraseña");
            return;
        }
        
        if (!Patterns.EMAIL_ADDRESS.matcher(emailValue).matches()) {
            _emailError.setValue("Por favor ingresa un correo válido");
            return;
        }
        
        setLoading(true);
        
        // Enviar email de recuperación con Firebase
        firebaseAuthManager.sendPasswordResetEmail(emailValue, new FirebaseAuthManager.ResetPasswordCallback() {
            @Override
            public void onSuccess() {
                setLoading(false);
                setError("Se ha enviado un enlace de recuperación a tu correo electrónico");
            }
            
            @Override
            public void onError(String error) {
                setLoading(false);
                String translatedError = translateFirebaseError(error);
                setError(translatedError);
            }
        });
    }
    
    /**
     * Marca que ya se navegó para evitar navegaciones múltiples
     */
    public void onNavigationComplete() {
        _navigateToHome.setValue(false);
        _navigateToRegister.setValue(false);
    }
    
    /**
     * Traduce errores de Firebase al español
     * @param error Error original de Firebase
     * @return Error traducido
     */
    private String translateFirebaseError(String error) {
        if (error == null) return "Error desconocido";
        
        if (error.contains("user-not-found")) {
            return "No existe una cuenta con este correo electrónico";
        } else if (error.contains("wrong-password")) {
            return "Contraseña incorrecta";
        } else if (error.contains("invalid-email")) {
            return "Formato de correo electrónico inválido";
        } else if (error.contains("user-disabled")) {
            return "Esta cuenta ha sido deshabilitada";
        } else if (error.contains("too-many-requests")) {
            return "Demasiados intentos fallidos. Intenta más tarde";
        } else if (error.contains("network-request-failed")) {
            return "Error de conexión. Verifica tu internet";
        } else if (error.contains("weak-password")) {
            return "La contraseña es muy débil";
        } else if (error.contains("email-already-in-use")) {
            return "Ya existe una cuenta con este correo electrónico";
        } else {
            return error;
        }
    }
    
    /**
     * Verifica si el PIN está habilitado
     * @return true si el PIN está habilitado, false en caso contrario
     */
    public boolean isPinEnabled() {
        return securePreferencesManager != null && securePreferencesManager.isPinEnabled();
    }
    
    /**
     * Verifica si hay un usuario autenticado con Firebase
     * @return true si hay un usuario autenticado, false en caso contrario
     */
    public boolean isUserSignedIn() {
        return firebaseAuthManager.isUserSignedIn();
    }
    
    /**
     * Intenta login offline con credenciales almacenadas localmente
     * @param email Email del usuario
     * @param password Contraseña del usuario (se verificará con hash almacenado)
     */
    private void attemptOfflineLogin(String email, String password) {
        if (database == null) {
            setLoading(false);
            setError("No se puede acceder sin conexión en este momento");
            return;
        }
        
        // Buscar en base de datos local en background thread
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                UserProfileEntity userProfile = database.userProfileDao().getUserProfileByEmail(email);
                
                if (userProfile != null) {
                    // Verificar si tiene PIN almacenado
                    String storedPin = securePreferencesManager.getStoredPin();
                    if (storedPin != null && !storedPin.isEmpty()) {
                        // Usuario encontrado localmente
                        securePreferencesManager.setCurrentUserId(userProfile.getUserId());
                        setLoading(false);
                        _navigateToHome.postValue(true);
                        setError("Acceso sin conexión - Funcionalidad limitada");
                    } else {
                        setLoading(false);
                        setError("Se requiere conexión a internet para el primer acceso");
                    }
                } else {
                    setLoading(false);
                    setError("Usuario no encontrado. Se requiere conexión a internet");
                }
            } catch (Exception e) {
                setLoading(false);
                setError("Error al acceder sin conexión");
            }
        });
    }
    
    /**
     * Guarda el perfil del usuario localmente para acceso offline
     * @param user Usuario de Firebase autenticado
     */
    private void saveUserProfileLocally(FirebaseUser user) {
        if (database == null || user == null) return;
        
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                UserProfileEntity existingProfile = database.userProfileDao().getUserProfileSync(user.getUid());
                
                if (existingProfile == null) {
                    // Crear nuevo perfil local
                    UserProfileEntity newProfile = new UserProfileEntity();
                    newProfile.setUserId(user.getUid());
                    newProfile.setEmail(user.getEmail());
                    newProfile.setName(user.getDisplayName() != null ? user.getDisplayName() : "");
                    newProfile.setPhotoUrl(user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "");
                    newProfile.setSynced(true);
                    newProfile.setLastSync(System.currentTimeMillis());
                    
                    database.userProfileDao().insert(newProfile);
                } else {
                    // Actualizar perfil existente
                    existingProfile.setEmail(user.getEmail());
                    existingProfile.setLastSync(System.currentTimeMillis());
                    existingProfile.setSynced(true);
                    
                    database.userProfileDao().update(existingProfile);
                }
                
                // Guardar ID del usuario actual
                securePreferencesManager.setCurrentUserId(user.getUid());
            } catch (Exception e) {
                Log.e("LoginViewModel", "Error guardando perfil localmente", e);
            }
        });
    }
}