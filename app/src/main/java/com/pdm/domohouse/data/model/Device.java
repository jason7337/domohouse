package com.pdm.domohouse.data.model;

/**
 * Modelo que representa un dispositivo IoT en la casa inteligente
 * Puede ser un sensor, actuador o dispositivo combinado
 */
public class Device {
    
    // Identificador único del dispositivo
    private String id;
    
    // Nombre personalizado del dispositivo
    private String name;
    
    // Tipo de dispositivo
    private DeviceType type;
    
    // ID de la habitación donde está ubicado
    private String roomId;
    
    // Estado actual del dispositivo (encendido/apagado)
    private boolean enabled;
    
    // Estado de conexión del dispositivo
    private boolean connected;
    
    // Valor actual del dispositivo (porcentaje, temperatura, etc.)
    private float currentValue;
    
    // Valor mínimo que puede tomar
    private float minValue;
    
    // Valor máximo que puede tomar
    private float maxValue;
    
    // Unidad de medida del valor
    private String unit;
    
    // Última vez que se actualizó el estado
    private long lastUpdated;
    
    // Nivel de batería (para dispositivos inalámbricos)
    private int batteryLevel;
    
    // Indica si el dispositivo está en modo automático
    private boolean autoMode;
    
    // Nivel de señal de comunicación (0-100)
    private int signalStrength;

    /**
     * Constructor completo para crear un dispositivo
     */
    public Device(String id, String name, DeviceType type, String roomId) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.roomId = roomId;
        
        // Valores por defecto
        this.enabled = false;
        this.connected = false;
        this.currentValue = 0;
        this.minValue = type.getDefaultMinValue();
        this.maxValue = type.getDefaultMaxValue();
        this.unit = type.getDefaultUnit();
        this.lastUpdated = System.currentTimeMillis();
        this.batteryLevel = 100;
        this.autoMode = false;
        this.signalStrength = 100;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public DeviceType getType() { return type; }
    public void setType(DeviceType type) { this.type = type; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { 
        this.enabled = enabled;
        updateLastUpdated();
    }

    public boolean isConnected() { return connected; }
    public void setConnected(boolean connected) { 
        this.connected = connected;
        updateLastUpdated();
    }

    public float getCurrentValue() { return currentValue; }
    public void setCurrentValue(float currentValue) { 
        this.currentValue = Math.max(minValue, Math.min(maxValue, currentValue));
        updateLastUpdated();
    }

    public float getMinValue() { return minValue; }
    public void setMinValue(float minValue) { this.minValue = minValue; }

    public float getMaxValue() { return maxValue; }
    public void setMaxValue(float maxValue) { this.maxValue = maxValue; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public long getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(long lastUpdated) { this.lastUpdated = lastUpdated; }

    public int getBatteryLevel() { return batteryLevel; }
    public void setBatteryLevel(int batteryLevel) { 
        this.batteryLevel = Math.max(0, Math.min(100, batteryLevel)); 
    }

    public boolean isAutoMode() { return autoMode; }
    public void setAutoMode(boolean autoMode) { this.autoMode = autoMode; }

    public int getSignalStrength() { return signalStrength; }
    public void setSignalStrength(int signalStrength) { 
        this.signalStrength = Math.max(0, Math.min(100, signalStrength)); 
    }
    
    // Métodos de compatibilidad para sincronización
    public boolean isOnline() { return connected; }
    public void setOnline(boolean online) { 
        this.connected = online;
        updateLastUpdated();
    }

    /**
     * Actualiza el timestamp de última actualización
     */
    private void updateLastUpdated() {
        this.lastUpdated = System.currentTimeMillis();
    }

    /**
     * Obtiene el estado general del dispositivo
     * @return estado del dispositivo
     */
    public RoomStatus getDeviceStatus() {
        if (!connected) {
            return RoomStatus.OFFLINE;
        } else if (batteryLevel < 20) {
            return RoomStatus.WARNING;
        } else if (enabled && currentValue > 0) {
            return RoomStatus.ACTIVE;
        } else {
            return RoomStatus.NORMAL;
        }
    }

    /**
     * Verifica si el dispositivo necesita atención
     * @return true si necesita atención
     */
    public boolean needsAttention() {
        return !connected || batteryLevel < 20 || signalStrength < 30;
    }

    /**
     * Obtiene el valor actual como string formateado
     * @return valor formateado con unidad
     */
    public String getFormattedValue() {
        if (type == DeviceType.LIGHT_SWITCH || type == DeviceType.FAN_SWITCH) {
            return enabled ? "Encendido" : "Apagado";
        } else {
            return String.format("%.1f %s", currentValue, unit);
        }
    }

    /**
     * Obtiene el porcentaje del valor actual respecto al rango
     * @return porcentaje (0-100)
     */
    public int getValuePercentage() {
        if (maxValue == minValue) return 0;
        return (int) ((currentValue - minValue) / (maxValue - minValue) * 100);
    }

    /**
     * Verifica si el dispositivo es de tipo sensor
     * @return true si es sensor
     */
    public boolean isSensor() {
        return type.isSensor();
    }

    /**
     * Verifica si el dispositivo es de tipo actuador
     * @return true si es actuador
     */
    public boolean isActuator() {
        return type.isActuator();
    }

    /**
     * Obtiene la edad de la última actualización en minutos
     * @return minutos desde la última actualización
     */
    public long getMinutesSinceLastUpdate() {
        return (System.currentTimeMillis() - lastUpdated) / (1000 * 60);
    }

    @Override
    public String toString() {
        return "Device{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", enabled=" + enabled +
                ", connected=" + connected +
                ", currentValue=" + currentValue +
                '}';
    }
}