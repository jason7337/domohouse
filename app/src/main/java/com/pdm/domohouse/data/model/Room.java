package com.pdm.domohouse.data.model;

/**
 * Modelo que representa una habitación en la casa inteligente
 * Contiene información sobre la habitación y sus características
 */
public class Room {
    
    // Identificador único de la habitación
    private String id;
    
    // Nombre de la habitación
    private String name;
    
    // Tipo de habitación (sala, cocina, dormitorio, etc.)
    private RoomType type;
    
    // Temperatura actual de la habitación
    private float temperature;
    
    // Humedad actual de la habitación
    private float humidity;
    
    // Estado de iluminación (porcentaje 0-100)
    private int lightLevel;
    
    // Estado de la puerta (abierta/cerrada)
    private boolean doorOpen;
    
    // Estado de la ventana (abierta/cerrada)
    private boolean windowOpen;
    
    // Estado del ventilador (velocidad 0-100)
    private int fanSpeed;
    
    // Número de dispositivos activos en la habitación
    private int activeDevices;
    
    // Posición X en la vista de maqueta (coordenada relativa)
    private float positionX;
    
    // Posición Y en la vista de maqueta (coordenada relativa)
    private float positionY;
    
    // Ancho de la habitación en la vista de maqueta
    private float width;
    
    // Alto de la habitación en la vista de maqueta
    private float height;
    
    // Piso en el que se encuentra la habitación
    private int floor;

    /**
     * Constructor completo para crear una habitación
     */
    public Room(String id, String name, RoomType type, float positionX, float positionY, 
               float width, float height) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.positionX = positionX;
        this.positionY = positionY;
        this.width = width;
        this.height = height;
        
        // Valores por defecto para sensores
        this.temperature = 22.0f;
        this.humidity = 45.0f;
        this.lightLevel = 0;
        this.doorOpen = false;
        this.windowOpen = false;
        this.fanSpeed = 0;
        this.activeDevices = 0;
    }

    /**
     * Constructor simplificado para casos básicos
     */
    public Room(String id, String name, RoomType type) {
        this(id, name, type, 0, 0, 1, 1);
    }
    
    /**
     * Constructor con piso (para compatibilidad con AddDeviceViewModel)
     */
    public Room(String id, String name, RoomType type, int floor) {
        this(id, name, type, 0, 0, 1, 1);
        this.floor = floor;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public RoomType getType() { return type; }
    public void setType(RoomType type) { this.type = type; }

    public float getTemperature() { return temperature; }
    public void setTemperature(float temperature) { this.temperature = temperature; }

    public float getHumidity() { return humidity; }
    public void setHumidity(float humidity) { this.humidity = humidity; }

    public int getLightLevel() { return lightLevel; }
    public void setLightLevel(int lightLevel) { 
        this.lightLevel = Math.max(0, Math.min(100, lightLevel)); 
    }

    public boolean isDoorOpen() { return doorOpen; }
    public void setDoorOpen(boolean doorOpen) { this.doorOpen = doorOpen; }

    public boolean isWindowOpen() { return windowOpen; }
    public void setWindowOpen(boolean windowOpen) { this.windowOpen = windowOpen; }

    public int getFanSpeed() { return fanSpeed; }
    public void setFanSpeed(int fanSpeed) { 
        this.fanSpeed = Math.max(0, Math.min(100, fanSpeed)); 
    }

    public int getActiveDevices() { return activeDevices; }
    public void setActiveDevices(int activeDevices) { this.activeDevices = activeDevices; }

    public float getPositionX() { return positionX; }
    public void setPositionX(float positionX) { this.positionX = positionX; }

    public float getPositionY() { return positionY; }
    public void setPositionY(float positionY) { this.positionY = positionY; }

    public float getWidth() { return width; }
    public void setWidth(float width) { this.width = width; }

    public float getHeight() { return height; }
    public void setHeight(float height) { this.height = height; }

    /**
     * Verifica si la habitación tiene algún dispositivo activo
     * @return true si hay dispositivos activos
     */
    public boolean hasActiveDevices() {
        return lightLevel > 0 || fanSpeed > 0 || activeDevices > 0;
    }

    /**
     * Calcula el estado general de la habitación basado en los sensores
     * @return estado de la habitación
     */
    public RoomStatus getOverallStatus() {
        if (temperature > 28 || humidity > 70) {
            return RoomStatus.WARNING;
        } else if (hasActiveDevices()) {
            return RoomStatus.ACTIVE;
        } else {
            return RoomStatus.NORMAL;
        }
    }
    
    /**
     * Obtiene el piso de la habitación
     * @return número de piso
     */
    public int getFloor() {
        return floor;
    }
    
    /**
     * Establece el piso de la habitación
     * @param floor número de piso
     */
    public void setFloor(int floor) {
        this.floor = floor;
    }

    @Override
    public String toString() {
        return "Room{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", temperature=" + temperature +
                ", lightLevel=" + lightLevel +
                ", activeDevices=" + activeDevices +
                '}';
    }
}