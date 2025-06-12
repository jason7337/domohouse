package com.pdm.domohouse.data.database.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entidad de Room para almacenar las preferencias del usuario
 * Incluye configuraciones de notificaciones, idioma, y automatizaciones
 */
@Entity(tableName = "user_preferences")
public class UserPreferencesEntity {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "user_id")
    private String userId;
    
    @ColumnInfo(name = "language")
    private String language; // es, en
    
    @ColumnInfo(name = "notifications_enabled")
    private boolean notificationsEnabled;
    
    @ColumnInfo(name = "motion_alerts")
    private boolean motionAlerts;
    
    @ColumnInfo(name = "temperature_alerts")
    private boolean temperatureAlerts;
    
    @ColumnInfo(name = "smoke_alerts")
    private boolean smokeAlerts;
    
    @ColumnInfo(name = "security_alerts")
    private boolean securityAlerts;
    
    @ColumnInfo(name = "auto_lights")
    private boolean autoLights; // Luces automáticas por movimiento
    
    @ColumnInfo(name = "auto_temperature")
    private boolean autoTemperature; // Control automático de temperatura
    
    @ColumnInfo(name = "eco_mode")
    private boolean ecoMode; // Modo ahorro de energía
    
    @ColumnInfo(name = "temperature_unit")
    private String temperatureUnit; // C o F
    
    @ColumnInfo(name = "default_light_intensity")
    private int defaultLightIntensity; // 0-100
    
    @ColumnInfo(name = "temperature_threshold_min")
    private float temperatureThresholdMin; // Temperatura mínima para alertas
    
    @ColumnInfo(name = "temperature_threshold_max")
    private float temperatureThresholdMax; // Temperatura máxima para alertas
    
    @ColumnInfo(name = "night_mode_start")
    private String nightModeStart; // HH:mm
    
    @ColumnInfo(name = "night_mode_end")
    private String nightModeEnd; // HH:mm
    
    @ColumnInfo(name = "created_at")
    private long createdAt;
    
    @ColumnInfo(name = "updated_at")
    private long updatedAt;
    
    @ColumnInfo(name = "last_sync")
    private long lastSync;
    
    @ColumnInfo(name = "is_synced")
    private boolean isSynced;
    
    // Constructor con valores por defecto
    public UserPreferencesEntity() {
        this.language = "es";
        this.notificationsEnabled = true;
        this.motionAlerts = true;
        this.temperatureAlerts = true;
        this.smokeAlerts = true;
        this.securityAlerts = true;
        this.autoLights = false;
        this.autoTemperature = false;
        this.ecoMode = false;
        this.temperatureUnit = "C";
        this.defaultLightIntensity = 50;
        this.temperatureThresholdMin = 18.0f;
        this.temperatureThresholdMax = 28.0f;
        this.nightModeStart = "22:00";
        this.nightModeEnd = "06:00";
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.lastSync = 0;
        this.isSynced = false;
    }
    
    // Getters y Setters
    @NonNull
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(@NonNull String userId) {
        this.userId = userId;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }
    
    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
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
    
    public String getTemperatureUnit() {
        return temperatureUnit;
    }
    
    public void setTemperatureUnit(String temperatureUnit) {
        this.temperatureUnit = temperatureUnit;
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
    
    public long getLastSync() {
        return lastSync;
    }
    
    public void setLastSync(long lastSync) {
        this.lastSync = lastSync;
    }
    
    public boolean isSynced() {
        return isSynced;
    }
    
    public void setSynced(boolean synced) {
        isSynced = synced;
    }
}