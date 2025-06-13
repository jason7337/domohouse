package com.pdm.domohouse.ui.temperature;

/**
 * Interfaz para manejar las interacciones del usuario con los controles de ventiladores
 */
public interface FanControlListener {
    
    /**
     * Se llama cuando el usuario cambia el estado de encendido/apagado de un ventilador
     * @param deviceId ID del dispositivo ventilador
     * @param isOn true para encender, false para apagar
     */
    void onFanPowerChanged(String deviceId, boolean isOn);
    
    /**
     * Se llama cuando el usuario cambia la velocidad de un ventilador
     * @param deviceId ID del dispositivo ventilador
     * @param speedPercentage velocidad como porcentaje (0-100)
     */
    void onFanSpeedChanged(String deviceId, float speedPercentage);
    
    /**
     * Se llama cuando el usuario toca el ventilador para ver más detalles
     * @param deviceId ID del dispositivo ventilador
     */
    void onFanClicked(String deviceId);
    
    /**
     * Se llama cuando el usuario quiere configurar el ventilador
     * @param deviceId ID del dispositivo ventilador
     */
    void onFanConfigClicked(String deviceId);
}