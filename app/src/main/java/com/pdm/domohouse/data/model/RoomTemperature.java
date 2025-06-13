package com.pdm.domohouse.data.model;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import com.pdm.domohouse.BR;

/**
 * Modelo que representa la temperatura de una habitación con información agregada
 * de múltiples sensores y estado de los dispositivos
 */
public class RoomTemperature extends BaseObservable {
    
    private String roomId;
    private String roomName;
    private RoomType roomType;
    private float currentTemperature;
    private float humidity;
    private boolean hasHumidity;
    private int sensorCount;
    private TemperatureStatus status;
    private long lastUpdated;
    private boolean isOnline;
    
    // Constructor vacío para Firebase
    public RoomTemperature() {}
    
    /**
     * Constructor completo
     */
    public RoomTemperature(String roomId, String roomName, RoomType roomType,
                          float currentTemperature, float humidity, boolean hasHumidity,
                          int sensorCount, TemperatureStatus status, long lastUpdated, boolean isOnline) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.roomType = roomType;
        this.currentTemperature = currentTemperature;
        this.humidity = humidity;
        this.hasHumidity = hasHumidity;
        this.sensorCount = sensorCount;
        this.status = status;
        this.lastUpdated = lastUpdated;
        this.isOnline = isOnline;
    }
    
    // Getters y Setters con Bindable para Data Binding
    
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
    public RoomType getRoomType() { return roomType; }
    
    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
        notifyPropertyChanged(BR.roomType);
    }
    
    @Bindable
    public float getCurrentTemperature() { return currentTemperature; }
    
    public void setCurrentTemperature(float currentTemperature) {
        this.currentTemperature = currentTemperature;
        notifyPropertyChanged(BR.currentTemperature);
    }
    
    @Bindable
    public float getHumidity() { return humidity; }
    
    public void setHumidity(float humidity) {
        this.humidity = humidity;
        notifyPropertyChanged(BR.humidity);
    }
    
    @Bindable
    public boolean getHasHumidity() { return hasHumidity; }
    
    public void setHasHumidity(boolean hasHumidity) {
        this.hasHumidity = hasHumidity;
        notifyPropertyChanged(BR.hasHumidity);
    }
    
    @Bindable
    public int getSensorCount() { return sensorCount; }
    
    public void setSensorCount(int sensorCount) {
        this.sensorCount = sensorCount;
        notifyPropertyChanged(BR.sensorCount);
    }
    
    @Bindable
    public TemperatureStatus getStatus() { return status; }
    
    public void setStatus(TemperatureStatus status) {
        this.status = status;
        notifyPropertyChanged(BR.status);
    }
    
    @Bindable
    public long getLastUpdated() { return lastUpdated; }
    
    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
        notifyPropertyChanged(BR.lastUpdated);
    }
    
    @Bindable
    public boolean getIsOnline() { return isOnline; }
    
    public void setIsOnline(boolean isOnline) {
        this.isOnline = isOnline;
        notifyPropertyChanged(BR.isOnline);
    }
    
    /**
     * Obtiene el color basado en el estado de la temperatura
     * @return color resource ID
     */
    public int getTemperatureColor() {
        if (status == null) return android.R.color.black;
        
        switch (status) {
            case NORMAL:
                return com.pdm.domohouse.R.color.success;
            case HIGH:
                return com.pdm.domohouse.R.color.warning;
            case LOW:
                return com.pdm.domohouse.R.color.info;
            case CRITICAL_HIGH:
            case CRITICAL_LOW:
                return com.pdm.domohouse.R.color.error;
            default:
                return com.pdm.domohouse.R.color.text_primary;
        }
    }
    
    /**
     * Obtiene el color del indicador de estado
     * @return color resource ID
     */
    public int getStatusColor() {
        if (!isOnline) {
            return com.pdm.domohouse.R.color.text_secondary;
        }
        return getTemperatureColor();
    }
    
    /**
     * Verifica si la temperatura está en rango normal
     * @return true si está en rango normal
     */
    public boolean isTemperatureNormal() {
        return status == TemperatureStatus.NORMAL;
    }
    
    /**
     * Verifica si la habitación necesita atención
     * @return true si necesita atención
     */
    public boolean needsAttention() {
        return !isOnline || status == TemperatureStatus.CRITICAL_HIGH || 
               status == TemperatureStatus.CRITICAL_LOW;
    }
    
    /**
     * Obtiene el texto del estado de temperatura
     * @return string con el estado
     */
    public String getStatusText() {
        if (!isOnline) return "Sin conexión";
        
        if (status == null) return "Desconocido";
        
        switch (status) {
            case NORMAL:
                return "Normal";
            case HIGH:
                return "Alta";
            case LOW:
                return "Baja";
            case CRITICAL_HIGH:
                return "Crítica Alta";
            case CRITICAL_LOW:
                return "Crítica Baja";
            default:
                return "Desconocido";
        }
    }
    
    @Override
    public String toString() {
        return "RoomTemperature{" +
                "roomName='" + roomName + '\'' +
                ", currentTemperature=" + currentTemperature +
                ", status=" + status +
                ", isOnline=" + isOnline +
                '}';
    }
    
    /**
     * Enumeración para el estado de la temperatura
     */
    public enum TemperatureStatus {
        NORMAL,        // Temperatura en rango normal
        HIGH,          // Temperatura alta pero no crítica
        LOW,           // Temperatura baja pero no crítica
        CRITICAL_HIGH, // Temperatura crítica alta
        CRITICAL_LOW   // Temperatura crítica baja
    }
}