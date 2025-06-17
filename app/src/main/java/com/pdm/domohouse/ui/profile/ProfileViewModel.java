package com.pdm.domohouse.ui.profile;

import android.app.Application;
import android.net.Uri;
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
 * ViewModel para la gestión del perfil de usuario
 * Maneja la información personal, configuraciones y seguridad
 * Integrado con Firebase Realtime Database y Firebase Auth
 */
public class ProfileViewModel extends BaseAndroidViewModel {
    
    // Managers de Firebase
    private final FirebaseAuthManager authManager;
    private final FirebaseDataManager dataManager;
    private final SecurePreferencesManager preferencesManager;
    
    // LiveData para el perfil del usuario
    private final MutableLiveData<UserProfile> _userProfile = new MutableLiveData<>();
    public final LiveData<UserProfile> userProfile = _userProfile;
    
    // LiveData para el estado de guardado
    private final MutableLiveData<Boolean> _isSaving = new MutableLiveData<>(false);
    public final LiveData<Boolean> isSaving = _isSaving;
    
    // LiveData para cambios no guardados
    private final MutableLiveData<Boolean> _hasUnsavedChanges = new MutableLiveData<>(false);
    public final LiveData<Boolean> hasUnsavedChanges = _hasUnsavedChanges;
    
    // LiveData para el estado de la carga de imagen
    private final MutableLiveData<Boolean> _isUploadingImage = new MutableLiveData<>(false);
    public final LiveData<Boolean> isUploadingImage = _isUploadingImage;
    
    // Perfil original para comparar cambios
    private UserProfile originalProfile;
    
    public ProfileViewModel(@NonNull Application application) {
        super(application);
        
        // Inicializar managers
        authManager = FirebaseAuthManager.getInstance();
        dataManager = FirebaseDataManager.getInstance();
        preferencesManager = SecurePreferencesManager.getInstance(application);
        
        android.util.Log.d("ProfileViewModel", "ProfileViewModel creado, iniciando carga de perfil");
        
        // Cargar el perfil del usuario real desde Firebase
        loadUserProfile();
    }
    
    /**
     * Carga el perfil del usuario desde Firebase Auth y Realtime Database
     */
    public void loadUserProfile() {
        setLoading(true);
        
        // Obtener usuario autenticado de Firebase
        FirebaseUser currentUser = authManager.getCurrentUser();
        if (currentUser == null) {
            android.util.Log.e("ProfileViewModel", "No hay usuario autenticado");
            setError("No hay usuario autenticado");
            setLoading(false);
            return;
        }
        
        android.util.Log.d("ProfileViewModel", "Usuario autenticado encontrado: " + currentUser.getUid() + ", Email: " + currentUser.getEmail());
        
        // Obtener perfil desde Firebase Realtime Database
        dataManager.getUserProfile(currentUser.getUid(), new FirebaseDataManager.UserProfileCallback() {
            @Override
            public void onSuccess(UserProfile profile) {
                android.util.Log.d("ProfileViewModel", "getUserProfile onSuccess llamado");
                if (profile != null) {
                    android.util.Log.d("ProfileViewModel", "Perfil encontrado en Firebase: " + profile.getFullName() + ", Email: " + profile.getEmail() + ", Photo URL: " + profile.getProfilePhotoUrl());
                    // Perfil encontrado en Firebase
                    _userProfile.setValue(profile);
                    originalProfile = cloneProfile(profile);
                } else {
                    android.util.Log.w("ProfileViewModel", "Perfil es null, creando nuevo perfil");
                    // Crear perfil nuevo si no existe
                    createNewUserProfile(currentUser);
                }
                setLoading(false);
            }
            
            @Override
            public void onError(String error) {
                android.util.Log.e("ProfileViewModel", "Error al obtener perfil: " + error);
                // Si falla la carga, intentar crear perfil desde datos de Auth
                createNewUserProfile(currentUser);
                setLoading(false);
            }
        });
    }
    
    /**
     * Crea un nuevo perfil de usuario basado en los datos de Firebase Auth
     */
    private void createNewUserProfile(FirebaseUser firebaseUser) {
        android.util.Log.d("ProfileViewModel", "Creando nuevo perfil para usuario: " + firebaseUser.getUid());
        // Obtener datos del usuario de Firebase Auth
        String userId = firebaseUser.getUid();
        String displayName = firebaseUser.getDisplayName();
        String email = firebaseUser.getEmail();
        
        android.util.Log.d("ProfileViewModel", "Datos de Firebase Auth - UID: " + userId + ", DisplayName: " + displayName + ", Email: " + email);
        
        // Crear perfil con datos seguros
        String finalName = displayName != null && !displayName.trim().isEmpty() ? displayName.trim() : "Usuario DomoHouse";
        String finalEmail = email != null && !email.trim().isEmpty() ? email.trim() : "usuario@domohouse.com";
        
        UserProfile newProfile = new UserProfile(userId, finalName, finalEmail);
        
        android.util.Log.d("ProfileViewModel", "Nuevo perfil creado - Nombre: " + newProfile.getFullName() + ", Email: " + newProfile.getEmail());
        
        // Establecer foto de perfil si existe
        if (firebaseUser.getPhotoUrl() != null) {
            String photoUrl = firebaseUser.getPhotoUrl().toString();
            android.util.Log.d("ProfileViewModel", "Photo URL encontrada: " + photoUrl);
            newProfile.setProfilePhotoUrl(photoUrl);
        } else {
            android.util.Log.d("ProfileViewModel", "No hay photo URL en Firebase Auth");
        }
        
        // Cargar preferencias locales
        loadUserPreferences(newProfile);
        
        // Guardar en Firebase para futuras cargas
        dataManager.saveUserProfile(newProfile, new FirebaseDataManager.DatabaseCallback() {
            @Override
            public void onSuccess() {
                setMessage("Perfil creado correctamente");
            }
            
            @Override
            public void onError(String error) {
                // No es crítico si falla el guardado inicial
            }
        });
        
        // Asegurar que los datos del perfil están correctamente establecidos antes de asignar
        if (newProfile.getFullName() == null || newProfile.getFullName().isEmpty()) {
            newProfile.setFullName("Usuario DomoHouse");
        }
        if (newProfile.getEmail() == null || newProfile.getEmail().isEmpty()) {
            newProfile.setEmail("usuario@domohouse.com");
        }
        
        android.util.Log.d("ProfileViewModel", "Asignando perfil final - Nombre: " + newProfile.getFullName() + ", Email: " + newProfile.getEmail());
        _userProfile.setValue(newProfile);
        originalProfile = cloneProfile(newProfile);
    }
    
    /**
     * Carga las preferencias del usuario desde almacenamiento seguro local
     */
    private void loadUserPreferences(UserProfile profile) {
        if (profile.getPreferences() == null) {
            profile.setPreferences(new com.pdm.domohouse.data.model.UserPreferences());
        }
        
        // Por ahora usar valores por defecto para las preferencias
        // TODO: Implementar carga de preferencias desde sistema de almacenamiento local
        try {
            profile.getPreferences().setNotificationsEnabled(true);
            profile.getPreferences().setAutoModeEnabled(false);
            profile.getPreferences().setAnalyticsEnabled(true);
        } catch (Exception e) {
            // Si falla la carga, usar valores por defecto
            profile.getPreferences().setNotificationsEnabled(true);
            profile.getPreferences().setAutoModeEnabled(false);
            profile.getPreferences().setAnalyticsEnabled(true);
        }
    }
    
    /**
     * Guarda los cambios del perfil en Firebase Realtime Database
     */
    public void saveProfile(String newFullName) {
        UserProfile currentProfile = _userProfile.getValue();
        if (currentProfile == null) {
            setError("No hay perfil para guardar");
            return;
        }
        
        // Validar campos requeridos
        if (newFullName == null || newFullName.trim().isEmpty()) {
            setError("El nombre completo es requerido");
            return;
        }
        
        _isSaving.setValue(true);
        
        // Actualizar el perfil con los nuevos datos
        currentProfile.setFullName(newFullName.trim());
        currentProfile.updateLastSync();
        
        // Guardar en Firebase Realtime Database
        dataManager.saveUserProfile(currentProfile, new FirebaseDataManager.DatabaseCallback() {
            @Override
            public void onSuccess() {
                _isSaving.setValue(false);
                _hasUnsavedChanges.setValue(false);
                originalProfile = cloneProfile(currentProfile);
                setMessage("Perfil actualizado correctamente");
                
                // Actualizar la UI con el perfil guardado
                _userProfile.setValue(currentProfile);
            }
            
            @Override
            public void onError(String error) {
                _isSaving.setValue(false);
                setError("Error al guardar el perfil: " + error);
            }
        });
    }
    
    /**
     * Actualiza la foto de perfil usando Firebase Storage
     */
    public void updateProfilePhoto(Uri imageUri) {
        UserProfile currentProfile = _userProfile.getValue();
        if (currentProfile == null || imageUri == null) {
            setError("No se puede actualizar la foto de perfil");
            return;
        }
        
        _isUploadingImage.setValue(true);
        
        // Subir imagen a Firebase Storage
        dataManager.uploadProfilePhoto(currentProfile.getUserId(), imageUri, 
            new FirebaseDataManager.PhotoUploadCallback() {
                @Override
                public void onSuccess(String downloadUrl) {
                    // Actualizar perfil con la nueva URL
                    currentProfile.setProfilePhotoUrl(downloadUrl);
                    currentProfile.updateLastSync();
                    
                    // Guardar perfil actualizado en Firebase
                    dataManager.saveUserProfile(currentProfile, new FirebaseDataManager.DatabaseCallback() {
                        @Override
                        public void onSuccess() {
                            _isUploadingImage.setValue(false);
                            _userProfile.setValue(currentProfile);
                            setMessage("Foto de perfil actualizada");
                        }
                        
                        @Override
                        public void onError(String error) {
                            _isUploadingImage.setValue(false);
                            setError("Error al guardar la foto: " + error);
                        }
                    });
                }
                
                @Override
                public void onProgress(int progress) {
                    // Podríamos mostrar el progreso de subida aquí
                }
                
                @Override
                public void onError(String error) {
                    _isUploadingImage.setValue(false);
                    setError("Error al subir la imagen: " + error);
                }
            });
    }
    
    /**
     * Cambia el PIN de acceso usando SecurePreferencesManager
     */
    public void changePin(String currentPin, String newPin, String confirmPin) {
        // Validar nuevo PIN
        if (newPin == null || newPin.length() != 4) {
            setError("El nuevo PIN debe tener 4 dígitos");
            return;
        }
        
        if (!newPin.equals(confirmPin)) {
            setError("Los PINs no coinciden");
            return;
        }
        
        UserProfile currentProfile = _userProfile.getValue();
        if (currentProfile == null) {
            setError("No hay perfil disponible");
            return;
        }
        
        _isSaving.setValue(true);
        
        try {
            // Verificar PIN actual usando SecurePreferencesManager
            if (!preferencesManager.verifyPin(currentPin)) {
                _isSaving.setValue(false);
                setError("PIN actual incorrecto");
                return;
            }
            
            // Cambiar el PIN usando el método del manager
            boolean pinChanged = preferencesManager.changePin(currentPin, newPin);
            if (!pinChanged) {
                _isSaving.setValue(false);
                setError("Error al cambiar el PIN");
                return;
            }
            
            // Actualizar perfil con PIN cifrado para respaldo en Firebase
            String encryptedPin = preferencesManager.encryptPin(newPin);
            currentProfile.setEncryptedPin(encryptedPin);
            currentProfile.updateLastSync();
            
            // Guardar en Firebase
            dataManager.saveUserProfile(currentProfile, new FirebaseDataManager.DatabaseCallback() {
                @Override
                public void onSuccess() {
                    _isSaving.setValue(false);
                    setMessage("PIN actualizado correctamente");
                }
                
                @Override
                public void onError(String error) {
                    _isSaving.setValue(false);
                    setError("PIN actualizado localmente, pero error en respaldo: " + error);
                }
            });
            
        } catch (Exception e) {
            _isSaving.setValue(false);
            setError("Error al cambiar el PIN: " + e.getMessage());
        }
    }
    
    /**
     * Actualiza las preferencias del usuario en almacenamiento local y Firebase
     */
    public void updatePreferences(boolean notificationsEnabled, boolean autoModeEnabled, boolean analyticsEnabled) {
        UserProfile currentProfile = _userProfile.getValue();
        if (currentProfile == null || currentProfile.getPreferences() == null) {
            setError("No hay preferencias para actualizar");
            return;
        }
        
        // Actualizar preferencias en el perfil
        currentProfile.getPreferences().setNotificationsEnabled(notificationsEnabled);
        currentProfile.getPreferences().setAutoModeEnabled(autoModeEnabled);
        currentProfile.getPreferences().setAnalyticsEnabled(analyticsEnabled);
        currentProfile.updateLastSync();
        
        _isSaving.setValue(true);
        
        try {
            // Por ahora solo actualizar en Firebase
            // TODO: Implementar guardado de preferencias en almacenamiento local seguro
            
            // Guardar en Firebase para sincronización entre dispositivos
            dataManager.saveUserProfile(currentProfile, new FirebaseDataManager.DatabaseCallback() {
                @Override
                public void onSuccess() {
                    _isSaving.setValue(false);
                    _userProfile.setValue(currentProfile);
                    setMessage("Preferencias actualizadas");
                }
                
                @Override
                public void onError(String error) {
                    _isSaving.setValue(false);
                    // Mantener cambios locales aunque falle Firebase
                    _userProfile.setValue(currentProfile);
                    setMessage("Preferencias actualizadas localmente");
                }
            });
            
        } catch (Exception e) {
            _isSaving.setValue(false);
            setError("Error al guardar preferencias: " + e.getMessage());
        }
    }
    
    /**
     * Marca que hay cambios no guardados
     */
    public void markAsChanged() {
        _hasUnsavedChanges.setValue(true);
    }
    
    /**
     * Establece el perfil de usuario (para testing)
     */
    public void setUserProfile(UserProfile profile) {
        _userProfile.setValue(profile);
    }
    
    /**
     * Fuerza la recarga del perfil (para debugging)
     */
    public void forceReloadProfile() {
        android.util.Log.d("ProfileViewModel", "Forzando recarga del perfil");
        loadUserProfile();
    }
    
    /**
     * Verifica si hay cambios no guardados
     */
    public boolean hasChanges() {
        UserProfile current = _userProfile.getValue();
        if (current == null || originalProfile == null) {
            return false;
        }
        
        return !current.getFullName().equals(originalProfile.getFullName()) ||
               current.getPreferences().isNotificationsEnabled() != originalProfile.getPreferences().isNotificationsEnabled() ||
               current.getPreferences().isAutoModeEnabled() != originalProfile.getPreferences().isAutoModeEnabled() ||
               current.getPreferences().isAnalyticsEnabled() != originalProfile.getPreferences().isAnalyticsEnabled();
    }
    
    /**
     * Cierra la sesión del usuario usando Firebase Auth
     */
    public void logout() {
        setLoading(true);
        
        // Cerrar sesión en Firebase Auth (método sin parámetros)
        authManager.signOut();
        
        // Limpiar datos locales inmediatamente
        _userProfile.setValue(null);
        originalProfile = null;
        
        setLoading(false);
        setMessage("Sesión cerrada correctamente");
    }
    
    /**
     * Crea una copia del perfil para comparar cambios
     */
    private UserProfile cloneProfile(UserProfile original) {
        UserProfile clone = new UserProfile();
        clone.setUserId(original.getUserId());
        clone.setFullName(original.getFullName());
        clone.setEmail(original.getEmail());
        clone.setPhoneNumber(original.getPhoneNumber());
        clone.setProfilePhotoUrl(original.getProfilePhotoUrl());
        clone.setProfilePhotoPath(original.getProfilePhotoPath());
        clone.setEncryptedPin(original.getEncryptedPin());
        clone.setLastLoginTimestamp(original.getLastLoginTimestamp());
        clone.setRegistrationTimestamp(original.getRegistrationTimestamp());
        clone.setLastSyncTimestamp(original.getLastSyncTimestamp());
        clone.setPrimaryDeviceId(original.getPrimaryDeviceId());
        clone.setFcmToken(original.getFcmToken());
        
        if (original.getPreferences() != null) {
            clone.getPreferences().copyFrom(original.getPreferences());
        }
        
        return clone;
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        // Limpiar recursos si es necesario
    }
}