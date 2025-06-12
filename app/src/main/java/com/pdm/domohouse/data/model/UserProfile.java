package com.pdm.domohouse.data.model;

import com.google.firebase.database.IgnoreExtraProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Modelo de datos para el perfil de usuario
 * Incluye toda la información personal y configuraciones del usuario
 * Diseñado para sincronizarse con Firebase Realtime Database
 */
@IgnoreExtraProperties
public class UserProfile {
    
    // Identificador único del usuario (Firebase UID)
    private String userId;
    
    // Información personal básica
    private String fullName;
    private String email;
    private String phoneNumber;
    
    // Foto de perfil
    private String profilePhotoUrl;
    private String profilePhotoPath; // Ruta local para cache
    
    // Seguridad
    private String encryptedPin; // PIN cifrado para respaldo en la nube
    
    // Metadatos de sesión
    private long lastLoginTimestamp;
    private long registrationTimestamp;
    private long lastSyncTimestamp;
    private long createdAt;
    private long updatedAt;
    
    // Configuraciones personales
    private UserPreferences preferences;
    
    // Información del dispositivo principal
    private String primaryDeviceId;
    private String fcmToken; // Para notificaciones push
    
    /**
     * Constructor vacío requerido por Firebase
     */
    public UserProfile() {
        this.preferences = new UserPreferences();
        this.registrationTimestamp = System.currentTimeMillis();
        this.lastSyncTimestamp = System.currentTimeMillis();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }
    
    /**
     * Constructor con parámetros básicos
     */
    public UserProfile(String userId, String fullName, String email) {
        this();
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
    }
    
    // Getters y Setters
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public String getProfilePhotoUrl() {
        return profilePhotoUrl;
    }
    
    public void setProfilePhotoUrl(String profilePhotoUrl) {
        this.profilePhotoUrl = profilePhotoUrl;
    }
    
    public String getProfilePhotoPath() {
        return profilePhotoPath;
    }
    
    public void setProfilePhotoPath(String profilePhotoPath) {
        this.profilePhotoPath = profilePhotoPath;
    }
    
    public String getEncryptedPin() {
        return encryptedPin;
    }
    
    public void setEncryptedPin(String encryptedPin) {
        this.encryptedPin = encryptedPin;
    }
    
    public long getLastLoginTimestamp() {
        return lastLoginTimestamp;
    }
    
    public void setLastLoginTimestamp(long lastLoginTimestamp) {
        this.lastLoginTimestamp = lastLoginTimestamp;
    }
    
    public long getRegistrationTimestamp() {
        return registrationTimestamp;
    }
    
    public void setRegistrationTimestamp(long registrationTimestamp) {
        this.registrationTimestamp = registrationTimestamp;
    }
    
    public long getLastSyncTimestamp() {
        return lastSyncTimestamp;
    }
    
    public void setLastSyncTimestamp(long lastSyncTimestamp) {
        this.lastSyncTimestamp = lastSyncTimestamp;
    }
    
    public UserPreferences getPreferences() {
        return preferences;
    }
    
    public void setPreferences(UserPreferences preferences) {
        this.preferences = preferences;
    }
    
    public String getPrimaryDeviceId() {
        return primaryDeviceId;
    }
    
    public void setPrimaryDeviceId(String primaryDeviceId) {
        this.primaryDeviceId = primaryDeviceId;
    }
    
    public String getFcmToken() {
        return fcmToken;
    }
    
    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
    
    public long getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // Métodos de compatibilidad para SyncManager
    public String getName() {
        return fullName;
    }
    
    public void setName(String name) {
        this.fullName = name;
    }
    
    public String getPhotoUrl() {
        return profilePhotoUrl;
    }
    
    public void setPhotoUrl(String photoUrl) {
        this.profilePhotoUrl = photoUrl;
    }
    
    public String getPinHash() {
        return encryptedPin;
    }
    
    public void setPinHash(String pinHash) {
        this.encryptedPin = pinHash;
    }
    
    /**
     * Actualiza el timestamp de último acceso
     */
    public void updateLastLogin() {
        this.lastLoginTimestamp = System.currentTimeMillis();
    }
    
    /**
     * Actualiza el timestamp de última sincronización
     */
    public void updateLastSync() {
        this.lastSyncTimestamp = System.currentTimeMillis();
    }
    
    /**
     * Convierte el perfil a un Map para Firebase
     * @return Map con todos los campos del perfil
     */
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("fullName", fullName);
        result.put("email", email);
        result.put("phoneNumber", phoneNumber);
        result.put("profilePhotoUrl", profilePhotoUrl);
        result.put("profilePhotoPath", profilePhotoPath);
        result.put("encryptedPin", encryptedPin);
        result.put("lastLoginTimestamp", lastLoginTimestamp);
        result.put("registrationTimestamp", registrationTimestamp);
        result.put("lastSyncTimestamp", lastSyncTimestamp);
        result.put("primaryDeviceId", primaryDeviceId);
        result.put("fcmToken", fcmToken);
        
        if (preferences != null) {
            result.put("preferences", preferences.toMap());
        }
        
        return result;
    }
    
    /**
     * Valida que el perfil tenga los datos mínimos requeridos
     * @return true si el perfil es válido
     */
    public boolean isValid() {
        return userId != null && !userId.trim().isEmpty() &&
               fullName != null && !fullName.trim().isEmpty() &&
               email != null && !email.trim().isEmpty();
    }
    
    @Override
    public String toString() {
        return "UserProfile{" +
                "userId='" + userId + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", hasProfilePhoto=" + (profilePhotoUrl != null) +
                ", lastLogin=" + lastLoginTimestamp +
                '}';
    }
}