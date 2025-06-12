package com.pdm.domohouse.data.model;

import com.google.firebase.database.IgnoreExtraProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Modelo de datos para las preferencias y configuraciones del usuario
 * Incluye configuraciones personalizadas de la aplicación y del hogar inteligente
 */
@IgnoreExtraProperties
public class UserPreferences {
    
    // Configuraciones de la aplicación
    private boolean notificationsEnabled;
    private boolean pushNotificationsEnabled;
    private boolean soundEnabled;
    private boolean vibrationEnabled;
    
    // Configuraciones de seguridad
    private boolean biometricEnabled;
    private boolean autoLockEnabled;
    private int autoLockTimeoutMinutes;
    
    // Configuraciones de la casa
    private String temperatureUnit; // "C" o "F"
    private boolean autoModeEnabled;
    private int defaultTemperature;
    
    // Configuraciones de usuario
    private String language; // "es", "en", etc.
    private String theme; // "light" (oscuro eliminado por diseño)
    
    // Configuraciones de dispositivos
    private boolean offlineModeEnabled;
    private int syncFrequencyMinutes;
    
    // Configuraciones avanzadas
    private boolean developerModeEnabled;
    private boolean analyticsEnabled;
    
    // Campos adicionales para sincronización
    private String userId;
    private boolean motionAlerts;
    private boolean temperatureAlerts;
    private boolean smokeAlerts;
    private boolean securityAlerts;
    private boolean autoLights;
    private boolean autoTemperature;
    private boolean ecoMode;
    private int defaultLightIntensity;
    private float temperatureThresholdMin;
    private float temperatureThresholdMax;
    private String nightModeStart;
    private String nightModeEnd;
    private long createdAt;
    private long updatedAt;
    
    /**
     * Constructor con valores por defecto
     */
    public UserPreferences() {
        // Configuraciones por defecto
        this.notificationsEnabled = true;
        this.pushNotificationsEnabled = true;
        this.soundEnabled = true;
        this.vibrationEnabled = true;
        
        this.biometricEnabled = false;
        this.autoLockEnabled = true;
        this.autoLockTimeoutMinutes = 5;
        
        this.temperatureUnit = "C";
        this.autoModeEnabled = true;
        this.defaultTemperature = 22;
        
        this.language = "es";
        this.theme = "light";
        
        this.offlineModeEnabled = true;
        this.syncFrequencyMinutes = 15;
        
        this.developerModeEnabled = false;
        this.analyticsEnabled = true;
        
        // Inicializar campos adicionales
        this.motionAlerts = true;
        this.temperatureAlerts = true;
        this.smokeAlerts = true;
        this.securityAlerts = true;
        this.autoLights = false;
        this.autoTemperature = false;
        this.ecoMode = false;
        this.defaultLightIntensity = 50;
        this.temperatureThresholdMin = 18.0f;
        this.temperatureThresholdMax = 28.0f;
        this.nightModeStart = "22:00";
        this.nightModeEnd = "06:00";
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }
    
    // Getters y Setters para configuraciones de la aplicación
    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }
    
    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }
    
    public boolean isPushNotificationsEnabled() {
        return pushNotificationsEnabled;
    }
    
    public void setPushNotificationsEnabled(boolean pushNotificationsEnabled) {
        this.pushNotificationsEnabled = pushNotificationsEnabled;
    }
    
    public boolean isSoundEnabled() {
        return soundEnabled;
    }
    
    public void setSoundEnabled(boolean soundEnabled) {
        this.soundEnabled = soundEnabled;
    }
    
    public boolean isVibrationEnabled() {
        return vibrationEnabled;
    }
    
    public void setVibrationEnabled(boolean vibrationEnabled) {
        this.vibrationEnabled = vibrationEnabled;
    }
    
    // Getters y Setters para configuraciones de seguridad
    public boolean isBiometricEnabled() {
        return biometricEnabled;
    }
    
    public void setBiometricEnabled(boolean biometricEnabled) {
        this.biometricEnabled = biometricEnabled;
    }
    
    public boolean isAutoLockEnabled() {
        return autoLockEnabled;
    }
    
    public void setAutoLockEnabled(boolean autoLockEnabled) {
        this.autoLockEnabled = autoLockEnabled;
    }
    
    public int getAutoLockTimeoutMinutes() {
        return autoLockTimeoutMinutes;
    }
    
    public void setAutoLockTimeoutMinutes(int autoLockTimeoutMinutes) {
        this.autoLockTimeoutMinutes = autoLockTimeoutMinutes;
    }
    
    // Getters y Setters para configuraciones de la casa
    public String getTemperatureUnit() {
        return temperatureUnit;
    }
    
    public void setTemperatureUnit(String temperatureUnit) {
        this.temperatureUnit = temperatureUnit;
    }
    
    public boolean isAutoModeEnabled() {
        return autoModeEnabled;
    }
    
    public void setAutoModeEnabled(boolean autoModeEnabled) {
        this.autoModeEnabled = autoModeEnabled;
    }
    
    public int getDefaultTemperature() {
        return defaultTemperature;
    }
    
    public void setDefaultTemperature(int defaultTemperature) {
        this.defaultTemperature = defaultTemperature;
    }
    
    // Getters y Setters para configuraciones de usuario
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    public String getTheme() {
        return theme;
    }
    
    public void setTheme(String theme) {
        this.theme = theme;
    }
    
    // Getters y Setters para configuraciones de dispositivos
    public boolean isOfflineModeEnabled() {
        return offlineModeEnabled;
    }
    
    public void setOfflineModeEnabled(boolean offlineModeEnabled) {
        this.offlineModeEnabled = offlineModeEnabled;
    }
    
    public int getSyncFrequencyMinutes() {
        return syncFrequencyMinutes;
    }
    
    public void setSyncFrequencyMinutes(int syncFrequencyMinutes) {
        this.syncFrequencyMinutes = syncFrequencyMinutes;
    }
    
    // Getters y Setters para configuraciones avanzadas
    public boolean isDeveloperModeEnabled() {
        return developerModeEnabled;
    }
    
    public void setDeveloperModeEnabled(boolean developerModeEnabled) {
        this.developerModeEnabled = developerModeEnabled;
    }
    
    public boolean isAnalyticsEnabled() {
        return analyticsEnabled;
    }
    
    public void setAnalyticsEnabled(boolean analyticsEnabled) {
        this.analyticsEnabled = analyticsEnabled;
    }
    
    // Getters y setters para campos adicionales
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public boolean isMotionAlerts() {
        return motionAlerts;
    }
    
    public void setMotionAlerts(boolean motionAlerts) {
        this.motionAlerts = motionAlerts;
    }
    
    public boolean isTemperatureAlerts() {
        return temperatureAlerts;
    }
    
    public void setTemperatureAlerts(boolean temperatureAlerts) {
        this.temperatureAlerts = temperatureAlerts;
    }
    
    public boolean isSmokeAlerts() {
        return smokeAlerts;
    }
    
    public void setSmokeAlerts(boolean smokeAlerts) {
        this.smokeAlerts = smokeAlerts;
    }
    
    public boolean isSecurityAlerts() {
        return securityAlerts;
    }
    
    public void setSecurityAlerts(boolean securityAlerts) {
        this.securityAlerts = securityAlerts;
    }
    
    public boolean isAutoLights() {
        return autoLights;
    }
    
    public void setAutoLights(boolean autoLights) {
        this.autoLights = autoLights;
    }
    
    public boolean isAutoTemperature() {
        return autoTemperature;
    }
    
    public void setAutoTemperature(boolean autoTemperature) {
        this.autoTemperature = autoTemperature;
    }
    
    public boolean isEcoMode() {
        return ecoMode;
    }
    
    public void setEcoMode(boolean ecoMode) {
        this.ecoMode = ecoMode;
    }
    
    public int getDefaultLightIntensity() {
        return defaultLightIntensity;
    }
    
    public void setDefaultLightIntensity(int defaultLightIntensity) {
        this.defaultLightIntensity = defaultLightIntensity;
    }
    
    public float getTemperatureThresholdMin() {
        return temperatureThresholdMin;
    }
    
    public void setTemperatureThresholdMin(float temperatureThresholdMin) {
        this.temperatureThresholdMin = temperatureThresholdMin;
    }
    
    public float getTemperatureThresholdMax() {
        return temperatureThresholdMax;
    }
    
    public void setTemperatureThresholdMax(float temperatureThresholdMax) {
        this.temperatureThresholdMax = temperatureThresholdMax;
    }
    
    public String getNightModeStart() {
        return nightModeStart;
    }
    
    public void setNightModeStart(String nightModeStart) {
        this.nightModeStart = nightModeStart;
    }
    
    public String getNightModeEnd() {
        return nightModeEnd;
    }
    
    public void setNightModeEnd(String nightModeEnd) {
        this.nightModeEnd = nightModeEnd;
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
    
    /**
     * Convierte las preferencias a un Map para Firebase
     * @return Map con todas las preferencias
     */
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        
        // Configuraciones de la aplicación
        result.put("notificationsEnabled", notificationsEnabled);
        result.put("pushNotificationsEnabled", pushNotificationsEnabled);
        result.put("soundEnabled", soundEnabled);
        result.put("vibrationEnabled", vibrationEnabled);
        
        // Configuraciones de seguridad
        result.put("biometricEnabled", biometricEnabled);
        result.put("autoLockEnabled", autoLockEnabled);
        result.put("autoLockTimeoutMinutes", autoLockTimeoutMinutes);
        
        // Configuraciones de la casa
        result.put("temperatureUnit", temperatureUnit);
        result.put("autoModeEnabled", autoModeEnabled);
        result.put("defaultTemperature", defaultTemperature);
        
        // Configuraciones de usuario
        result.put("language", language);
        result.put("theme", theme);
        
        // Configuraciones de dispositivos
        result.put("offlineModeEnabled", offlineModeEnabled);
        result.put("syncFrequencyMinutes", syncFrequencyMinutes);
        
        // Configuraciones avanzadas
        result.put("developerModeEnabled", developerModeEnabled);
        result.put("analyticsEnabled", analyticsEnabled);
        
        return result;
    }
    
    /**
     * Restaura valores por defecto de fábrica
     */
    public void resetToDefaults() {
        UserPreferences defaults = new UserPreferences();
        copyFrom(defaults);
    }
    
    /**
     * Copia configuraciones desde otra instancia
     * @param other Las preferencias desde las que copiar
     */
    public void copyFrom(UserPreferences other) {
        this.notificationsEnabled = other.notificationsEnabled;
        this.pushNotificationsEnabled = other.pushNotificationsEnabled;
        this.soundEnabled = other.soundEnabled;
        this.vibrationEnabled = other.vibrationEnabled;
        
        this.biometricEnabled = other.biometricEnabled;
        this.autoLockEnabled = other.autoLockEnabled;
        this.autoLockTimeoutMinutes = other.autoLockTimeoutMinutes;
        
        this.temperatureUnit = other.temperatureUnit;
        this.autoModeEnabled = other.autoModeEnabled;
        this.defaultTemperature = other.defaultTemperature;
        
        this.language = other.language;
        this.theme = other.theme;
        
        this.offlineModeEnabled = other.offlineModeEnabled;
        this.syncFrequencyMinutes = other.syncFrequencyMinutes;
        
        this.developerModeEnabled = other.developerModeEnabled;
        this.analyticsEnabled = other.analyticsEnabled;
    }
}