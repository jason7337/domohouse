package com.pdm.domohouse.ui.lights.model;

/**
 * Modelo que contiene las estadísticas de iluminación de la casa
 * Utilizado para mostrar resúmenes en la interfaz
 */
public class LightStats {
    
    private final int totalLights;
    private final int lightsOn;
    private final int lightsOff;

    /**
     * Constructor para crear estadísticas de luces
     * @param totalLights Total de luces en la casa
     * @param lightsOn Número de luces encendidas
     * @param lightsOff Número de luces apagadas
     */
    public LightStats(int totalLights, int lightsOn, int lightsOff) {
        this.totalLights = totalLights;
        this.lightsOn = lightsOn;
        this.lightsOff = lightsOff;
    }

    // Getters
    public int getTotalLights() { return totalLights; }
    public int getLightsOn() { return lightsOn; }
    public int getLightsOff() { return lightsOff; }

    /**
     * Obtiene el porcentaje de luces encendidas
     * @return porcentaje (0-100)
     */
    public int getOnPercentage() {
        if (totalLights == 0) return 0;
        return (lightsOn * 100) / totalLights;
    }

    /**
     * Obtiene el porcentaje de luces apagadas
     * @return porcentaje (0-100)
     */
    public int getOffPercentage() {
        if (totalLights == 0) return 0;
        return (lightsOff * 100) / totalLights;
    }

    /**
     * Verifica si hay luces encendidas
     * @return true si hay al menos una luz encendida
     */
    public boolean hasLightsOn() {
        return lightsOn > 0;
    }

    /**
     * Verifica si todas las luces están apagadas
     * @return true si todas las luces están apagadas
     */
    public boolean allLightsOff() {
        return lightsOn == 0;
    }

    /**
     * Verifica si todas las luces están encendidas
     * @return true si todas las luces están encendidas
     */
    public boolean allLightsOn() {
        return lightsOff == 0 && totalLights > 0;
    }

    @Override
    public String toString() {
        return "LightStats{" +
                "totalLights=" + totalLights +
                ", lightsOn=" + lightsOn +
                ", lightsOff=" + lightsOff +
                '}';
    }
}