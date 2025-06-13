package com.pdm.domohouse.ui.lights.model;

import com.pdm.domohouse.data.model.Device;
import com.pdm.domohouse.data.model.DeviceType;
import com.pdm.domohouse.data.model.Room;
import java.util.List;

/**
 * Modelo que representa una habitación con sus dispositivos de iluminación
 * Utilizado para organizar los datos en la interfaz de control de luces
 */
public class RoomWithLights {
    
    private final Room room;
    private final List<Device> lightDevices;

    /**
     * Constructor
     * @param room La habitación
     * @param lightDevices Lista de dispositivos de iluminación en la habitación
     */
    public RoomWithLights(Room room, List<Device> lightDevices) {
        this.room = room;
        this.lightDevices = lightDevices;
    }

    // Getters
    public Room getRoom() { return room; }
    public List<Device> getLightDevices() { return lightDevices; }

    /**
     * Obtiene el número total de luces en la habitación
     * @return número de luces
     */
    public int getTotalLights() {
        return lightDevices.size();
    }

    /**
     * Obtiene el número de luces encendidas en la habitación
     * @return número de luces encendidas
     */
    public int getLightsOn() {
        int count = 0;
        for (Device device : lightDevices) {
            if (device.isEnabled() && device.getCurrentValue() > 0) {
                count++;
            }
        }
        return count;
    }

    /**
     * Obtiene el número de luces apagadas en la habitación
     * @return número de luces apagadas
     */
    public int getLightsOff() {
        return getTotalLights() - getLightsOn();
    }

    /**
     * Verifica si hay luces encendidas en la habitación
     * @return true si hay al menos una luz encendida
     */
    public boolean hasLightsOn() {
        return getLightsOn() > 0;
    }

    /**
     * Verifica si todas las luces están apagadas
     * @return true si todas las luces están apagadas
     */
    public boolean allLightsOff() {
        return getLightsOn() == 0;
    }

    /**
     * Verifica si todas las luces están encendidas
     * @return true si todas las luces están encendidas
     */
    public boolean allLightsOn() {
        return getLightsOff() == 0 && getTotalLights() > 0;
    }

    /**
     * Obtiene el porcentaje promedio de intensidad de las luces encendidas
     * @return porcentaje promedio (0-100)
     */
    public int getAverageLightIntensity() {
        if (lightDevices.isEmpty()) return 0;
        
        float total = 0;
        int count = 0;
        
        for (Device device : lightDevices) {
            if (device.isEnabled() && device.getCurrentValue() > 0) {
                if (device.getType() == DeviceType.LIGHT_SWITCH) {
                    total += 100; // Interruptor simple cuenta como 100%
                } else {
                    total += device.getValuePercentage();
                }
                count++;
            }
        }
        
        return count > 0 ? (int) (total / count) : 0;
    }

    /**
     * Obtiene el estado general de iluminación de la habitación
     * @return descripción del estado
     */
    public String getLightingStatus() {
        if (allLightsOff()) {
            return "Todas apagadas";
        } else if (allLightsOn()) {
            return "Todas encendidas";
        } else {
            return getLightsOn() + " de " + getTotalLights() + " encendidas";
        }
    }

    /**
     * Cuenta cuántas luces son regulables (dimmer)
     * @return número de luces regulables
     */
    public int getDimmableLights() {
        int count = 0;
        for (Device device : lightDevices) {
            if (device.getType().hasRange()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Cuenta cuántas luces son interruptores simples
     * @return número de interruptores simples
     */
    public int getSimpleSwitches() {
        int count = 0;
        for (Device device : lightDevices) {
            if (device.getType() == DeviceType.LIGHT_SWITCH) {
                count++;
            }
        }
        return count;
    }

    /**
     * Cuenta cuántas luces RGB hay
     * @return número de luces RGB
     */
    public int getRgbLights() {
        int count = 0;
        for (Device device : lightDevices) {
            if (device.getType() == DeviceType.RGB_LIGHT) {
                count++;
            }
        }
        return count;
    }

    /**
     * Verifica si la habitación tiene luces desconectadas
     * @return true si hay luces desconectadas
     */
    public boolean hasDisconnectedLights() {
        for (Device device : lightDevices) {
            if (!device.isConnected()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Obtiene el número de luces desconectadas
     * @return número de luces desconectadas
     */
    public int getDisconnectedLights() {
        int count = 0;
        for (Device device : lightDevices) {
            if (!device.isConnected()) {
                count++;
            }
        }
        return count;
    }

    @Override
    public String toString() {
        return "RoomWithLights{" +
                "room=" + room.getName() +
                ", lightDevices=" + lightDevices.size() +
                ", lightsOn=" + getLightsOn() +
                '}';
    }
}