package com.pdm.domohouse.ui.profile;

import android.app.Application;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.pdm.domohouse.data.model.UserProfile;
import com.pdm.domohouse.ui.base.BaseAndroidViewModel;

/**
 * ViewModel para la gestión del perfil de usuario
 * Maneja la información personal, configuraciones y seguridad
 * Versión simplificada para Sesión 5B
 */
public class ProfileViewModel extends BaseAndroidViewModel {
    
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
        
        // Cargar el perfil del usuario con datos de ejemplo
        loadUserProfile();
    }
    
    /**
     * Carga el perfil del usuario (datos reales del usuario en sesión)
     */
    public void loadUserProfile() {
        setLoading(true);
        
        // TODO: En producción, obtener datos del usuario autenticado desde Firebase Auth
        // Para esta demo, simular datos del usuario actual de la sesión
        
        // Obtener datos del usuario autenticado (simulado)
        String currentUserId = getCurrentUserId();
        String currentUserEmail = getCurrentUserEmail();
        String currentUserName = getCurrentUserName();
        
        // Crear perfil con datos del usuario real
        UserProfile realProfile = new UserProfile(currentUserId, currentUserName, currentUserEmail);
        
        // Obtener foto de perfil si existe
        String photoUrl = getCurrentUserPhotoUrl();
        if (photoUrl != null && !photoUrl.isEmpty()) {
            realProfile.setProfilePhotoUrl(photoUrl);
        }
        
        // Cargar preferencias guardadas
        loadUserPreferences(realProfile);
        
        _userProfile.setValue(realProfile);
        originalProfile = cloneProfile(realProfile);
        
        setLoading(false);
    }
    
    /**
     * Obtiene el ID del usuario actual (simulado - en producción usar Firebase Auth)
     */
    private String getCurrentUserId() {
        // TODO: Implementar con Firebase Auth
        return "user_" + System.currentTimeMillis();
    }
    
    /**
     * Obtiene el email del usuario actual (simulado - en producción usar Firebase Auth)
     */
    private String getCurrentUserEmail() {
        // TODO: Implementar con Firebase Auth
        return "usuario@domohouse.com";
    }
    
    /**
     * Obtiene el nombre del usuario actual (simulado - en producción usar Firebase Auth)
     */
    private String getCurrentUserName() {
        // TODO: Implementar con Firebase Auth
        return "Usuario DomoHouse";
    }
    
    /**
     * Obtiene la URL de la foto del usuario actual (simulado - en producción usar Firebase Auth)
     */
    private String getCurrentUserPhotoUrl() {
        // TODO: Implementar con Firebase Auth
        return null; // Por defecto sin foto
    }
    
    /**
     * Carga las preferencias del usuario desde almacenamiento local
     */
    private void loadUserPreferences(UserProfile profile) {
        // TODO: Implementar carga desde SharedPreferences o base de datos local
        // Por ahora usar valores por defecto
        profile.getPreferences().setNotificationsEnabled(true);
        profile.getPreferences().setAutoModeEnabled(false);
        profile.getPreferences().setAnalyticsEnabled(true);
    }
    
    /**
     * Guarda los cambios del perfil
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
        
        // Simular guardado (en futuras sesiones se integrará con Firebase)
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            _isSaving.setValue(false);
            _hasUnsavedChanges.setValue(false);
            originalProfile = cloneProfile(currentProfile);
            setMessage("Perfil actualizado correctamente");
        }, 1000);
    }
    
    /**
     * Actualiza la foto de perfil
     */
    public void updateProfilePhoto(Uri imageUri) {
        UserProfile currentProfile = _userProfile.getValue();
        if (currentProfile == null || imageUri == null) {
            setError("No se puede actualizar la foto de perfil");
            return;
        }
        
        _isUploadingImage.setValue(true);
        
        // Simular subida de imagen (en futuras sesiones se integrará con Firebase Storage)
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            currentProfile.setProfilePhotoUrl(imageUri.toString());
            currentProfile.updateLastSync();
            
            _isUploadingImage.setValue(false);
            _userProfile.setValue(currentProfile);
            setMessage("Foto de perfil actualizada");
        }, 2000);
    }
    
    /**
     * Cambia el PIN de acceso
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
        
        // Validar PIN actual (simulado)
        if (!"1234".equals(currentPin)) {
            setError("PIN actual incorrecto");
            return;
        }
        
        UserProfile currentProfile = _userProfile.getValue();
        if (currentProfile == null) {
            setError("No hay perfil disponible");
            return;
        }
        
        _isSaving.setValue(true);
        
        // Simular cambio de PIN (en futuras sesiones se integrará con SecurePreferences)
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            currentProfile.setEncryptedPin("encrypted_" + newPin);
            currentProfile.updateLastSync();
            
            _isSaving.setValue(false);
            setMessage("PIN actualizado correctamente");
        }, 1500);
    }
    
    /**
     * Actualiza las preferencias del usuario
     */
    public void updatePreferences(boolean notificationsEnabled, boolean autoModeEnabled, boolean analyticsEnabled) {
        UserProfile currentProfile = _userProfile.getValue();
        if (currentProfile == null || currentProfile.getPreferences() == null) {
            setError("No hay preferencias para actualizar");
            return;
        }
        
        // Actualizar preferencias
        currentProfile.getPreferences().setNotificationsEnabled(notificationsEnabled);
        currentProfile.getPreferences().setAutoModeEnabled(autoModeEnabled);
        currentProfile.getPreferences().setAnalyticsEnabled(analyticsEnabled);
        currentProfile.updateLastSync();
        
        _isSaving.setValue(true);
        
        // Simular guardado de preferencias
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            _isSaving.setValue(false);
            _userProfile.setValue(currentProfile);
            setMessage("Preferencias actualizadas");
        }, 500);
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
     * Cierra la sesión del usuario
     */
    public void logout() {
        setLoading(true);
        
        // Simular cierre de sesión
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            setLoading(false);
            setMessage("Sesión cerrada correctamente");
        }, 500);
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