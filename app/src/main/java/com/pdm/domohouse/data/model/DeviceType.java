package com.pdm.domohouse.data.model;

/**
 * Enumeración que define los tipos de dispositivos IoT disponibles en la casa inteligente
 * Cada tipo tiene características específicas y valores por defecto
 */
public enum DeviceType {
    
    // SENSORES
    TEMPERATURE_SENSOR("Sensor de Temperatura", "🌡️", true, false, 
                      0, 50, "°C"),
    
    HUMIDITY_SENSOR("Sensor de Humedad", "💧", true, false, 
                   0, 100, "%"),
    
    SMOKE_DETECTOR("Detector de Humo", "🚨", true, false, 
                  0, 1, ""),
    
    MOTION_SENSOR("Sensor de Movimiento", "👁️", true, false, 
                 0, 1, ""),
    
    DOOR_SENSOR("Sensor de Puerta", "🚪", true, false, 
               0, 1, ""),
    
    WINDOW_SENSOR("Sensor de Ventana", "🪟", true, false, 
                 0, 1, ""),
    
    LIGHT_SENSOR("Sensor de Luz", "☀️", true, false, 
                0, 1000, "lux"),
    
    // ACTUADORES DE ILUMINACIÓN
    LIGHT_SWITCH("Interruptor de Luz", "💡", false, true, 
                0, 1, ""),
    
    DIMMER_LIGHT("Luz Regulable", "🔆", false, true, 
                0, 100, "%"),
    
    RGB_LIGHT("Luz RGB", "🌈", false, true, 
             0, 16777215, ""),
    
    // ACTUADORES DE CLIMA
    FAN_SWITCH("Interruptor de Ventilador", "🪭", false, true, 
              0, 1, ""),
    
    FAN_SPEED("Control de Ventilador", "💨", false, true, 
             0, 100, "%"),
    
    AIR_CONDITIONING("Aire Acondicionado", "❄️", false, true, 
                    16, 30, "°C"),
    
    HEATER("Calentador", "🔥", false, true, 
          0, 100, "%"),
    
    // ACTUADORES DE SEGURIDAD
    SMART_LOCK("Cerradura Inteligente", "🔐", false, true, 
              0, 1, ""),
    
    ALARM_SYSTEM("Sistema de Alarma", "🔔", false, true, 
                0, 1, ""),
    
    SECURITY_CAMERA("Cámara de Seguridad", "📹", false, true, 
                   0, 1, ""),
    
    // ACTUADORES GENERALES
    SMART_OUTLET("Enchufe Inteligente", "🔌", false, true, 
                0, 1, ""),
    
    GARAGE_DOOR("Puerta de Garaje", "🏠", false, true, 
               0, 1, ""),
    
    WINDOW_BLIND("Persiana", "🪟", false, true, 
                0, 100, "%"),
    
    // DISPOSITIVOS COMBINADOS
    THERMOSTAT("Termostato", "🎛️", true, true, 
              16, 30, "°C"),
    
    SMART_TV("Televisor Inteligente", "📺", false, true, 
            0, 100, "%"),
    
    SOUND_SYSTEM("Sistema de Sonido", "🔊", false, true, 
                0, 100, "%");

    private final String displayName;
    private final String icon;
    private final boolean isSensor;
    private final boolean isActuator;
    private final float defaultMinValue;
    private final float defaultMaxValue;
    private final String defaultUnit;

    /**
     * Constructor del enum
     */
    DeviceType(String displayName, String icon, boolean isSensor, boolean isActuator,
               float defaultMinValue, float defaultMaxValue, String defaultUnit) {
        this.displayName = displayName;
        this.icon = icon;
        this.isSensor = isSensor;
        this.isActuator = isActuator;
        this.defaultMinValue = defaultMinValue;
        this.defaultMaxValue = defaultMaxValue;
        this.defaultUnit = defaultUnit;
    }

    // Getters
    public String getDisplayName() { return displayName; }
    public String getIcon() { return icon; }
    public boolean isSensor() { return isSensor; }
    public boolean isActuator() { return isActuator; }
    public float getDefaultMinValue() { return defaultMinValue; }
    public float getDefaultMaxValue() { return defaultMaxValue; }
    public String getDefaultUnit() { return defaultUnit; }

    /**
     * Obtiene el nombre completo con icono
     * @return string con icono y nombre
     */
    public String getDisplayNameWithIcon() {
        return icon + " " + displayName;
    }

    /**
     * Verifica si el dispositivo es de tipo interruptor (on/off)
     * @return true si es un interruptor simple
     */
    public boolean isSwitch() {
        return defaultMaxValue == 1 && defaultMinValue == 0 && defaultUnit.isEmpty();
    }

    /**
     * Verifica si el dispositivo maneja rangos de valores
     * @return true si maneja rangos
     */
    public boolean hasRange() {
        return defaultMaxValue > defaultMinValue && defaultMaxValue > 1;
    }

    /**
     * Verifica si el dispositivo es crítico para la seguridad
     * @return true si es crítico para seguridad
     */
    public boolean isSecurityCritical() {
        switch (this) {
            case SMOKE_DETECTOR:
            case SMART_LOCK:
            case ALARM_SYSTEM:
            case SECURITY_CAMERA:
                return true;
            default:
                return false;
        }
    }

    /**
     * Verifica si el dispositivo típicamente usa batería
     * @return true si típicamente usa batería
     */
    public boolean isBatteryPowered() {
        switch (this) {
            case MOTION_SENSOR:
            case DOOR_SENSOR:
            case WINDOW_SENSOR:
            case SMOKE_DETECTOR:
                return true;
            default:
                return false;
        }
    }

    /**
     * Obtiene la categoría del dispositivo
     * @return categoría del dispositivo
     */
    public DeviceCategory getCategory() {
        if (displayName.contains("Sensor") || displayName.contains("Detector")) {
            return DeviceCategory.SENSORS;
        } else if (displayName.contains("Luz") || displayName.contains("Light")) {
            return DeviceCategory.LIGHTING;
        } else if (displayName.contains("Ventilador") || displayName.contains("Aire") || 
                   displayName.contains("Calentador") || displayName.contains("Termostato")) {
            return DeviceCategory.CLIMATE;
        } else if (displayName.contains("Cerradura") || displayName.contains("Alarma") || 
                   displayName.contains("Cámara") || displayName.contains("Security")) {
            return DeviceCategory.SECURITY;
        } else {
            return DeviceCategory.GENERAL;
        }
    }

    /**
     * Categorías de dispositivos para agrupación
     */
    public enum DeviceCategory {
        SENSORS("Sensores", "📊"),
        LIGHTING("Iluminación", "💡"),
        CLIMATE("Clima", "🌡️"),
        SECURITY("Seguridad", "🔒"),
        GENERAL("General", "⚙️");

        private final String displayName;
        private final String icon;

        DeviceCategory(String displayName, String icon) {
            this.displayName = displayName;
            this.icon = icon;
        }

        public String getDisplayName() { return displayName; }
        public String getIcon() { return icon; }
        
        public String getDisplayNameWithIcon() {
            return icon + " " + displayName;
        }
    }
}