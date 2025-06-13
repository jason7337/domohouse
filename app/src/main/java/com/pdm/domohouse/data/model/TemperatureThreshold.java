package com.pdm.domohouse.data.model;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import com.pdm.domohouse.BR;

/**
 * Modelo que representa los umbrales de temperatura para el control automático
 */
public class TemperatureThreshold extends BaseObservable {
    
    private String id;
    private String roomId;
    private String roomName;
    private float minTemperature;
    private float maxTemperature;
    private float criticalLow;
    private float criticalHigh;
    private boolean isAutomaticControlEnabled;
    private boolean enableCooling;
    private boolean enableHeating;
    private String coolingDeviceId;
    private String heatingDeviceId;
    private long lastUpdated;
    private String userId;
    
    // Constructor vacío para Firebase
    public TemperatureThreshold() {}
    
    /**
     * Constructor completo
     */
    public TemperatureThreshold(String id, String roomId, String roomName,
                               float minTemperature, float maxTemperature,
                               float criticalLow, float criticalHigh,
                               boolean isAutomaticControlEnabled,
                               boolean enableCooling, boolean enableHeating,
                               String coolingDeviceId, String heatingDeviceId,
                               long lastUpdated, String userId) {
        this.id = id;
        this.roomId = roomId;
        this.roomName = roomName;
        this.minTemperature = minTemperature;
        this.maxTemperature = maxTemperature;
        this.criticalLow = criticalLow;
        this.criticalHigh = criticalHigh;
        this.isAutomaticControlEnabled = isAutomaticControlEnabled;
        this.enableCooling = enableCooling;
        this.enableHeating = enableHeating;
        this.coolingDeviceId = coolingDeviceId;
        this.heatingDeviceId = heatingDeviceId;
        this.lastUpdated = lastUpdated;
        this.userId = userId;
    }
    
    // Getters y Setters con Bindable para Data Binding
    
    @Bindable
    public String getId() { return id; }
    
    public void setId(String id) {
        this.id = id;
        notifyPropertyChanged(BR.id);
    }
    
    @Bindable
    public String getRoomId() { return roomId; }
    
    public void setRoomId(String roomId) {
        this.roomId = roomId;
        notifyPropertyChanged(BR.roomId);
    }
    
    @Bindable
    public String getRoomName() { return roomName; }
    
    public void setRoomName(String roomName) {
        this.roomName = roomName;
        notifyPropertyChanged(BR.roomName);
    }
    
    @Bindable
    public float getMinTemperature() { return minTemperature; }
    
    public void setMinTemperature(float minTemperature) {
        this.minTemperature = minTemperature;
        notifyPropertyChanged(BR.minTemperature);
    }
    
    @Bindable
    public float getMaxTemperature() { return maxTemperature; }
    
    public void setMaxTemperature(float maxTemperature) {
        this.maxTemperature = maxTemperature;
        notifyPropertyChanged(BR.maxTemperature);
    }
    
    @Bindable
    public float getCriticalLow() { return criticalLow; }
    
    public void setCriticalLow(float criticalLow) {
        this.criticalLow = criticalLow;
        notifyPropertyChanged(BR.criticalLow);
    }
    
    @Bindable
    public float getCriticalHigh() { return criticalHigh; }
    
    public void setCriticalHigh(float criticalHigh) {
        this.criticalHigh = criticalHigh;
        notifyPropertyChanged(BR.criticalHigh);
    }
    
    @Bindable
    public boolean getIsAutomaticControlEnabled() { return isAutomaticControlEnabled; }
    
    public void setIsAutomaticControlEnabled(boolean isAutomaticControlEnabled) {
        this.isAutomaticControlEnabled = isAutomaticControlEnabled;
        notifyPropertyChanged(BR.isAutomaticControlEnabled);
    }
    
    @Bindable
    public boolean getEnableCooling() { return enableCooling; }
    
    public void setEnableCooling(boolean enableCooling) {
        this.enableCooling = enableCooling;
        notifyPropertyChanged(BR.enableCooling);
    }
    
    @Bindable
    public boolean getEnableHeating() { return enableHeating; }
    
    public void setEnableHeating(boolean enableHeating) {
        this.enableHeating = enableHeating;
        notifyPropertyChanged(BR.enableHeating);
    }
    
    @Bindable
    public String getCoolingDeviceId() { return coolingDeviceId; }
    
    public void setCoolingDeviceId(String coolingDeviceId) {
        this.coolingDeviceId = coolingDeviceId;
        notifyPropertyChanged(BR.coolingDeviceId);
    }
    
    @Bindable
    public String getHeatingDeviceId() { return heatingDeviceId; }
    
    public void setHeatingDeviceId(String heatingDeviceId) {
        this.heatingDeviceId = heatingDeviceId;
        notifyPropertyChanged(BR.heatingDeviceId);
    }
    
    @Bindable
    public long getLastUpdated() { return lastUpdated; }
    
    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
        notifyPropertyChanged(BR.lastUpdated);
    }
    
    @Bindable
    public String getUserId() { return userId; }
    
    public void setUserId(String userId) {
        this.userId = userId;
        notifyPropertyChanged(BR.userId);
    }
    
    /**
     * Evalúa el estado de una temperatura dada
     * @param temperature temperatura a evaluar
     * @return estado de la temperatura
     */
    public RoomTemperature.TemperatureStatus evaluateTemperature(float temperature) {
        if (temperature <= criticalLow) {
            return RoomTemperature.TemperatureStatus.CRITICAL_LOW;
        } else if (temperature < minTemperature) {
            return RoomTemperature.TemperatureStatus.LOW;
        } else if (temperature > criticalHigh) {
            return RoomTemperature.TemperatureStatus.CRITICAL_HIGH;
        } else if (temperature > maxTemperature) {
            return RoomTemperature.TemperatureStatus.HIGH;
        } else {
            return RoomTemperature.TemperatureStatus.NORMAL;
        }
    }
    
    /**
     * Verifica si la temperatura está en rango crítico
     * @param temperature temperatura a verificar
     * @return true si está en rango crítico
     */
    public boolean isCriticalTemperature(float temperature) {
        RoomTemperature.TemperatureStatus status = evaluateTemperature(temperature);
        return status == RoomTemperature.TemperatureStatus.CRITICAL_HIGH ||
               status == RoomTemperature.TemperatureStatus.CRITICAL_LOW;
    }
    
    /**
     * Verifica si se necesita enfriamiento
     * @param temperature temperatura actual
     * @return true si se necesita enfriamiento
     */
    public boolean needsCooling(float temperature) {
        return isAutomaticControlEnabled && enableCooling && 
               temperature > maxTemperature && coolingDeviceId != null;
    }
    
    /**
     * Verifica si se necesita calefacción
     * @param temperature temperatura actual
     * @return true si se necesita calefacción
     */
    public boolean needsHeating(float temperature) {
        return isAutomaticControlEnabled && enableHeating && 
               temperature < minTemperature && heatingDeviceId != null;
    }
    
    /**
     * Calcula la intensidad necesaria de enfriamiento/calefacción (0-100%)
     * @param temperature temperatura actual
     * @return intensidad como porcentaje
     */
    public float calculateIntensity(float temperature) {
        if (needsCooling(temperature)) {
            float range = criticalHigh - maxTemperature;
            float excess = temperature - maxTemperature;
            return Math.min(100f, (excess / range) * 100f);
        } else if (needsHeating(temperature)) {
            float range = minTemperature - criticalLow;
            float deficit = minTemperature - temperature;
            return Math.min(100f, (deficit / range) * 100f);
        }
        return 0f;
    }
    
    /**
     * Verifica si la configuración es válida
     * @return true si es válida
     */
    public boolean isValid() {
        return criticalLow < minTemperature && 
               minTemperature < maxTemperature && 
               maxTemperature < criticalHigh &&
               roomId != null && !roomId.isEmpty();
    }
    
    /**
     * Obtiene el rango de temperatura como string
     * @return rango formateado
     */
    public String getTemperatureRange() {
        return String.format("%.1f°C - %.1f°C", minTemperature, maxTemperature);
    }
    
    @Override
    public String toString() {
        return "TemperatureThreshold{" +
                "roomName='" + roomName + '\'' +
                ", range=" + getTemperatureRange() +
                ", autoControl=" + isAutomaticControlEnabled +
                '}';
    }
}