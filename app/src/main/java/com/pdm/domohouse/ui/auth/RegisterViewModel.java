package com.pdm.domohouse.ui.auth;

import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Patterns;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseUser;
import com.pdm.domohouse.data.model.UserProfile;
import com.pdm.domohouse.network.FirebaseAuthManager;
import com.pdm.domohouse.network.FirebaseDataManager;
import com.pdm.domohouse.ui.base.BaseAndroidViewModel;
import com.pdm.domohouse.utils.SecurePreferencesManager;

/**
 * ViewModel para la pantalla de Registro
 * Maneja la lógica de registro de nuevos usuarios con Firebase
 */
public class RegisterViewModel extends BaseAndroidViewModel {
    
    // Managers para autenticación y datos
    private final FirebaseAuthManager firebaseAuthManager;
    private final FirebaseDataManager firebaseDataManager;
    private SecurePreferencesManager securePreferencesManager;
    
    // Campos del formulario con Two-Way Data Binding
    private final MutableLiveData<String> _name = new MutableLiveData<>("");
    private final MutableLiveData<String> _email = new MutableLiveData<>("");
    private final MutableLiveData<String> _password = new MutableLiveData<>("");
    private final MutableLiveData<String> _confirmPassword = new MutableLiveData<>("");
    private final MutableLiveData<String> _pin = new MutableLiveData<>("");
    private final MutableLiveData<String> _confirmPin = new MutableLiveData<>("");
    
    public MutableLiveData<String> name = _name;
    public MutableLiveData<String> email = _email;
    public MutableLiveData<String> password = _password;
    public MutableLiveData<String> confirmPassword = _confirmPassword;
    public MutableLiveData<String> pin = _pin;
    public MutableLiveData<String> confirmPin = _confirmPin;
    
    // LiveData para eventos de navegación
    private final MutableLiveData<Boolean> _navigateToHome = new MutableLiveData<>();
    public LiveData<Boolean> navigateToHome = _navigateToHome;
    
    private final MutableLiveData<Boolean> _navigateToLogin = new MutableLiveData<>();
    public LiveData<Boolean> navigateToLogin = _navigateToLogin;
    
    // LiveData para errores del formulario
    private final MutableLiveData<String> _nameError = new MutableLiveData<>();
    public LiveData<String> nameError = _nameError;
    
    private final MutableLiveData<String> _emailError = new MutableLiveData<>();
    public LiveData<String> emailError = _emailError;
    
    private final MutableLiveData<String> _passwordError = new MutableLiveData<>();
    public LiveData<String> passwordError = _passwordError;
    
    private final MutableLiveData<String> _confirmPasswordError = new MutableLiveData<>();
    public LiveData<String> confirmPasswordError = _confirmPasswordError;
    
    private final MutableLiveData<String> _pinError = new MutableLiveData<>();
    public LiveData<String> pinError = _pinError;
    
    private final MutableLiveData<String> _confirmPinError = new MutableLiveData<>();
    public LiveData<String> confirmPinError = _confirmPinError;
    
    // Campo para teléfono (opcional)
    private final MutableLiveData<String> _phoneNumber = new MutableLiveData<>("");
    public MutableLiveData<String> phoneNumber = _phoneNumber;
    
    // Foto de perfil
    private final MutableLiveData<Uri> _profilePhotoUri = new MutableLiveData<>();
    public LiveData<Uri> profilePhotoUri = _profilePhotoUri;
    
    private final MutableLiveData<String> _profilePhotoUrl = new MutableLiveData<>();
    public LiveData<String> profilePhotoUrl = _profilePhotoUrl;
    
    // Estado de la subida de foto
    private final MutableLiveData<Boolean> _isUploadingPhoto = new MutableLiveData<>(false);
    public LiveData<Boolean> isUploadingPhoto = _isUploadingPhoto;
    
    private final MutableLiveData<Integer> _photoUploadProgress = new MutableLiveData<>(0);
    public LiveData<Integer> photoUploadProgress = _photoUploadProgress;
    
    /**
     * Constructor
     */
    public RegisterViewModel(@NonNull Application application) {
        super(application);
        this.firebaseAuthManager = FirebaseAuthManager.getInstance();
        this.firebaseDataManager = FirebaseDataManager.getInstance();
        // Inicializar manager de preferencias seguras
        this.securePreferencesManager = SecurePreferencesManager.getInstance(application);
    }
    
    /**
     * Método legacy para compatibilidad - ya no es necesario llamarlo
     * El manager ahora se inicializa en el constructor
     * @param context Contexto de la aplicación
     * @deprecated Usar el constructor que recibe Application
     */
    @Deprecated
    public void initializeSecurePreferences(Context context) {
        // El manager ya está inicializado en el constructor
    }
    
    /**
     * Maneja el proceso de registro
     * Valida los campos y procede con el registro
     */
    public void register() {
        clearErrors();
        
        String nameValue = _name.getValue();
        String emailValue = _email.getValue();
        String passwordValue = _password.getValue();
        String confirmPasswordValue = _confirmPassword.getValue();
        String pinValue = _pin.getValue();
        String confirmPinValue = _confirmPin.getValue();
        String phoneValue = _phoneNumber.getValue();
        
        // Validar campos
        if (!validateForm(nameValue, emailValue, passwordValue, confirmPasswordValue, pinValue, confirmPinValue)) {
            return;
        }
        
        // Mostrar estado de carga
        setLoading(true);
        
        // Registrar con Firebase
        registerWithFirebase(nameValue, emailValue, passwordValue, pinValue, phoneValue);
    }
    
    /**
     * Registra al usuario con Firebase
     * @param name Nombre del usuario
     * @param email Email del usuario
     * @param password Contraseña del usuario
     * @param pin PIN del usuario
     * @param phoneNumber Teléfono del usuario (opcional)
     */
    private void registerWithFirebase(String name, String email, String password, String pin, String phoneNumber) {
        firebaseAuthManager.createUserWithEmailAndPassword(email, password, new FirebaseAuthManager.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                // Usuario registrado exitosamente, crear perfil completo
                createUserProfile(user, name, email, pin, phoneNumber);
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
     * Crea el perfil completo del usuario en Firebase
     * @param user Usuario de Firebase
     * @param name Nombre del usuario
     * @param email Email del usuario
     * @param pin PIN del usuario
     * @param phoneNumber Teléfono del usuario
     */
    private void createUserProfile(FirebaseUser user, String name, String email, String pin, String phoneNumber) {
        // Crear el perfil del usuario
        UserProfile userProfile = new UserProfile(user.getUid(), name, email);
        userProfile.setPhoneNumber(phoneNumber);
        userProfile.updateLastLogin();
        
        // Si hay foto, subirla primero
        Uri photoUri = _profilePhotoUri.getValue();
        if (photoUri != null) {
            uploadPhotoAndSaveProfile(userProfile, pin, photoUri);
        } else {
            saveUserProfileToFirebase(userProfile, pin);
        }
    }
    
    /**
     * Sube la foto y luego guarda el perfil
     * @param userProfile Perfil del usuario
     * @param pin PIN del usuario
     * @param photoUri URI de la foto
     */
    private void uploadPhotoAndSaveProfile(UserProfile userProfile, String pin, Uri photoUri) {
        _isUploadingPhoto.setValue(true);
        
        firebaseDataManager.uploadProfilePhoto(userProfile.getUserId(), photoUri, new FirebaseDataManager.PhotoUploadCallback() {
            @Override
            public void onSuccess(String downloadUrl) {
                _isUploadingPhoto.setValue(false);
                userProfile.setProfilePhotoUrl(downloadUrl);
                saveUserProfileToFirebase(userProfile, pin);
            }
            
            @Override
            public void onProgress(int progress) {
                _photoUploadProgress.setValue(progress);
            }
            
            @Override
            public void onError(String error) {
                _isUploadingPhoto.setValue(false);
                // Continuar sin la foto si hay error
                setError("No se pudo subir la foto, pero se creó tu cuenta");
                saveUserProfileToFirebase(userProfile, pin);
            }
        });
    }
    
    /**
     * Guarda el perfil del usuario en Firebase y el PIN localmente
     * @param userProfile Perfil del usuario
     * @param pin PIN del usuario
     */
    private void saveUserProfileToFirebase(UserProfile userProfile, String pin) {
        // Primero, cifrar y guardar el PIN como respaldo en el perfil
        try {
            String encryptedPin = securePreferencesManager.encryptPin(pin);
            userProfile.setEncryptedPin(encryptedPin);
        } catch (Exception e) {
            // Continuar sin respaldo del PIN si hay error de cifrado
        }
        
        // Guardar el perfil en Firebase
        firebaseDataManager.saveUserProfile(userProfile, new FirebaseDataManager.DatabaseCallback() {
            @Override
            public void onSuccess() {
                // Perfil guardado, ahora guardar PIN localmente
                savePinSecurely(pin);
            }
            
            @Override
            public void onError(String error) {
                setLoading(false);
                setError("Error al crear perfil: " + error);
            }
        });
    }
    
    /**
     * Guarda el PIN de forma segura localmente
     * @param pin PIN del usuario
     */
    private void savePinSecurely(String pin) {
        if (securePreferencesManager == null) {
            setLoading(false);
            setError("Error del sistema: Preferencias no inicializadas");
            return;
        }
        
        new Thread(() -> {
            try {
                boolean pinSaved = securePreferencesManager.savePin(pin);
                
                if (pinSaved) {
                    setLoading(false);
                    _navigateToHome.postValue(true);
                } else {
                    setLoading(false);
                    setError("Error al configurar PIN de seguridad");
                }
            } catch (Exception e) {
                setLoading(false);
                setError("Error al guardar configuración de seguridad");
            }
        }).start();
    }
    
    /**
     * Navega a la pantalla de login
     */
    public void navigateToLogin() {
        _navigateToLogin.setValue(true);
    }
    
    /**
     * Valida el formulario de registro
     * @param name Nombre del usuario
     * @param email Email del usuario
     * @param password Contraseña del usuario
     * @param confirmPassword Confirmación de contraseña
     * @param pin PIN del usuario
     * @param confirmPin Confirmación de PIN
     * @return true si es válido, false en caso contrario
     */
    private boolean validateForm(String name, String email, String password, 
                                String confirmPassword, String pin, String confirmPin) {
        boolean isValid = true;
        
        // Validar nombre
        if (TextUtils.isEmpty(name)) {
            _nameError.setValue("Por favor ingresa tu nombre");
            isValid = false;
        } else if (name.trim().length() < 2) {
            _nameError.setValue("El nombre debe tener al menos 2 caracteres");
            isValid = false;
        }
        
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
        
        // Validar confirmación de contraseña
        if (TextUtils.isEmpty(confirmPassword)) {
            _confirmPasswordError.setValue("Por favor confirma tu contraseña");
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            _confirmPasswordError.setValue("Las contraseñas no coinciden");
            isValid = false;
        }
        
        // Validar PIN
        if (TextUtils.isEmpty(pin)) {
            _pinError.setValue("Por favor ingresa un PIN de 4 dígitos");
            isValid = false;
        } else if (pin.length() != 4) {
            _pinError.setValue("El PIN debe ser de 4 dígitos");
            isValid = false;
        } else if (!pin.matches("\\d{4}")) {
            _pinError.setValue("El PIN debe contener solo números");
            isValid = false;
        }
        
        // Validar confirmación de PIN
        if (TextUtils.isEmpty(confirmPin)) {
            _confirmPinError.setValue("Por favor confirma tu PIN");
            isValid = false;
        } else if (!pin.equals(confirmPin)) {
            _confirmPinError.setValue("Los PINs no coinciden");
            isValid = false;
        }
        
        return isValid;
    }
    
    /**
     * Establece la URI de la foto de perfil seleccionada
     * @param uri URI de la imagen
     */
    public void setProfilePhotoUri(Uri uri) {
        _profilePhotoUri.setValue(uri);
    }
    
    /**
     * Limpia la foto de perfil seleccionada
     */
    public void clearProfilePhoto() {
        _profilePhotoUri.setValue(null);
        _profilePhotoUrl.setValue(null);
    }
    
    /**
     * Limpia todos los errores
     */
    private void clearErrors() {
        _nameError.setValue(null);
        _emailError.setValue(null);
        _passwordError.setValue(null);
        _confirmPasswordError.setValue(null);
        _pinError.setValue(null);
        _confirmPinError.setValue(null);
        clearError();
    }
    
    /**
     * Marca que ya se navegó para evitar navegaciones múltiples
     */
    public void onNavigationComplete() {
        _navigateToHome.setValue(false);
        _navigateToLogin.setValue(false);
    }
    
    /**
     * Traduce errores de Firebase al español
     * @param error Error original de Firebase
     * @return Error traducido
     */
    private String translateFirebaseError(String error) {
        if (error == null) return "Error desconocido";
        
        if (error.contains("email-already-in-use")) {
            return "Ya existe una cuenta con este correo electrónico";
        } else if (error.contains("weak-password")) {
            return "La contraseña es muy débil";
        } else if (error.contains("invalid-email")) {
            return "Formato de correo electrónico inválido";
        } else if (error.contains("network-request-failed")) {
            return "Error de conexión. Verifica tu internet";
        } else if (error.contains("too-many-requests")) {
            return "Demasiados intentos. Intenta más tarde";
        } else {
            return error;
        }
    }
}