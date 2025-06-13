package com.pdm.domohouse.data.model;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import com.pdm.domohouse.BR;

/**
 * Modelo que representa el control de un ventilador con sus estados y configuraciones
 */
public class FanControl extends BaseObservable {
    
    private String deviceId;
    private String deviceName;
    private String roomName;
    private String roomId;
    private DeviceType deviceType;
    private boolean isOn;
    private float speedPercentage;
    private boolean isConnected;
    private float powerConsumption;
    private long lastUpdated;
    private boolean isAutomaticMode;
    private float targetTemperature;
    
    // Constructor vacío para Firebase
    public FanControl() {}
    
    /**
     * Constructor completo
     */
    public FanControl(String deviceId, String deviceName, String roomName, String roomId,
                     DeviceType deviceType, boolean isOn, float speedPercentage,
                     boolean isConnected, float powerConsumption, long lastUpdated,
                     boolean isAutomaticMode, float targetTemperature) {
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.roomName = roomName;
        this.roomId = roomId;
        this.deviceType = deviceType;
        this.isOn = isOn;
        this.speedPercentage = speedPercentage;
        this.isConnected = isConnected;
        this.powerConsumption = powerConsumption;
        this.lastUpdated = lastUpdated;
        this.isAutomaticMode = isAutomaticMode;
        this.targetTemperature = targetTemperature;
    }
    
    // Getters y Setters con Bindable para Data Binding
    
    @Bindable
    public String getDeviceId() { return deviceId; }
    
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
        notifyPropertyChanged(BR.deviceId);
    }
    
    @Bindable
    public String getDeviceName() { return deviceName; }
    
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
        notifyPropertyChanged(BR.deviceName);
    }
    
    @Bindable
    public String getRoomName() { return roomName; }
    
    public void setRoomName(String roomName) {
        this.roomName = roomName;
        notifyPropertyChanged(BR.roomName);
    }
    
    @Bindable
    public String getRoomId() { return roomId; }
    
    public void setRoomId(String roomId) {
        this.roomId = roomId;
        notifyPropertyChanged(BR.roomId);
    }
    
    @Bindable
    public DeviceType getDeviceType() { return deviceType; }
    
    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
        notifyPropertyChanged(BR.deviceType);
    }
    
    @Bindable
    public boolean getIsOn() { return isOn; }
    
    public void setIsOn(boolean isOn) {
        this.isOn = isOn;
        notifyPropertyChanged(BR.isOn);
    }
    
    @Bindable
    public float getSpeedPercentage() { return speedPercentage; }
    
    public void setSpeedPercentage(float speedPercentage) {
        this.speedPercentage = speedPercentage;
        notifyPropertyChanged(BR.speedPercentage);
    }
    
    @Bindable
    public boolean getIsConnected() { return isConnected; }
    
    public void setIsConnected(boolean isConnected) {
        this.isConnected = isConnected;
        notifyPropertyChanged(BR.isConnected);
    }
    
    @Bindable
    public float getPowerConsumption() { return powerConsumption; }
    
    public void setPowerConsumption(float powerConsumption) {
        this.powerConsumption = powerConsumption;
        notifyPropertyChanged(BR.powerConsumption);
    }
    
    @Bindable
    public long getLastUpdated() { return lastUpdated; }
    
    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
        notifyPropertyChanged(BR.lastUpdated);
    }
    
    @Bindable
    public boolean getIsAutomaticMode() { return isAutomaticMode; }
    
    public void setIsAutomaticMode(boolean isAutomaticMode) {
        this.isAutomaticMode = isAutomaticMode;
        notifyPropertyChanged(BR.isAutomaticMode);
    }
    
    @Bindable
    public float getTargetTemperature() { return targetTemperature; }
    
    public void setTargetTemperature(float targetTemperature) {
        this.targetTemperature = targetTemperature;
        notifyPropertyChanged(BR.targetTemperature);
    }
    
    /**
     * Calcula la velocidad del ventilador basada en el porcentaje
     * @return velocidad de 1 a 5
     */
    public int getSpeedLevel() {
        if (!isOn || speedPercentage <= 0) return 0;
        if (speedPercentage <= 20) return 1;
        if (speedPercentage <= 40) return 2;
        if (speedPercentage <= 60) return 3;
        if (speedPercentage <= 80) return 4;
        return 5;
    }
    
    /**
     * Obtiene el texto descriptivo de la velocidad
     * @return descripción de la velocidad
     */
    public String getSpeedDescription() {
        if (!isOn) return "Apagado";
        
        int level = getSpeedLevel();
        switch (level) {
            case 1: return "Muy baja";
            case 2: return "Baja";
            case 3: return "Media";
            case 4: return "Alta";
            case 5: return "Máxima";
            default: return "Apagado";
        }
    }
    
    /**
     * Verifica si el ventilador está funcionando eficientemente
     * @return true si está funcionando bien
     */
    public boolean isEfficient() {
        return isConnected && isOn && speedPercentage > 0 && powerConsumption > 0;
    }
    
    /**
     * Calcula el consumo estimado de energía por hora
     * @return consumo en Wh
     */
    public float getHourlyPowerConsumption() {
        if (!isOn) return 0f;
        return powerConsumption; // Ya está en W, equivale a Wh por hora
    }
    
    /**
     * Obtiene el estado general del ventilador
     * @return estado como string
     */
    public String getStatusText() {
        if (!isConnected) return "Desconectado";
        if (!isOn) return "Apagado";
        if (isAutomaticMode) return "Automático (" + getSpeedDescription() + ")";
        return getSpeedDescription();
    }
    
    /**
     * Verifica si el ventilador necesita atención
     * @return true si necesita atención
     */
    public boolean needsAttention() {
        return !isConnected || (isOn && powerConsumption == 0);
    }
    
    @Override
    public String toString() {
        return "FanControl{" +
                "deviceName='" + deviceName + '\'' +
                ", roomName='" + roomName + '\'' +
                ", isOn=" + isOn +
                ", speedPercentage=" + speedPercentage +
                ", isConnected=" + isConnected +
                '}';
    }
}