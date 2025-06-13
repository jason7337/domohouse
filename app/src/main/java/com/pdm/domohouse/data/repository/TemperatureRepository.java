package com.pdm.domohouse.data.repository;

import com.pdm.domohouse.data.model.*;
import com.pdm.domohouse.utils.PreferencesManager;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

/**
 * Repositorio para manejar datos de temperatura, ventiladores y umbrales
 * Simula datos reales para demostración y pruebas
 */
public class TemperatureRepository {
    
    private static final String TAG = "TemperatureRepository";
    
    private final PreferencesManager preferencesManager;
    private final Random random = new Random();
    
    public TemperatureRepository() {
        this.preferencesManager = PreferencesManager.getInstance();
    }
    
    /**
     * Obtiene las temperaturas de todas las habitaciones
     * @return lista de temperaturas por habitación
     */
    public List<RoomTemperature> getAllRoomTemperatures() {
        List<RoomTemperature> temperatures = new ArrayList<>();
        
        // Simular datos de temperatura para diferentes habitaciones
        temperatures.add(createRoomTemperature("room_living", "Sala de Estar", 
                RoomType.LIVING_ROOM, 24.5f, 65f, true, 2));
        
        temperatures.add(createRoomTemperature("room_bedroom_main", "Dormitorio Principal", 
                RoomType.BEDROOM, 22.8f, 58f, true, 1));
        
        temperatures.add(createRoomTemperature("room_kitchen", "Cocina", 
                RoomType.KITCHEN, 26.2f, 70f, true, 1));
        
        temperatures.add(createRoomTemperature("room_bathroom", "Baño Principal", 
                RoomType.BATHROOM, 23.1f, 85f, true, 1));
        
        temperatures.add(createRoomTemperature("room_office", "Oficina", 
                RoomType.OFFICE, 21.9f, 55f, true, 1));
        
        temperatures.add(createRoomTemperature("room_garage", "Garaje", 
                RoomType.GARAGE, 18.5f, 45f, false, 1));
        
        return temperatures;
    }
    
    /**
     * Obtiene todos los controles de ventiladores disponibles
     * @return lista de controles de ventiladores
     */
    public List<FanControl> getAllFanControls() {
        List<FanControl> fanControls = new ArrayList<>();
        
        // Simular ventiladores en diferentes habitaciones
        fanControls.add(createFanControl("fan_living_001", "Ventilador Sala", 
                "Sala de Estar", "room_living", DeviceType.FAN_SPEED, true, 65f));
        
        fanControls.add(createFanControl("fan_bedroom_001", "Ventilador Dormitorio", 
                "Dormitorio Principal", "room_bedroom_main", DeviceType.FAN_SPEED, false, 0f));
        
        fanControls.add(createFanControl("fan_kitchen_001", "Extractor Cocina", 
                "Cocina", "room_kitchen", DeviceType.FAN_SWITCH, true, 80f));
        
        fanControls.add(createFanControl("fan_office_001", "Ventilador Oficina", 
                "Oficina", "room_office", DeviceType.FAN_SPEED, true, 45f));
        
        return fanControls;
    }
    
    /**
     * Obtiene la configuración de umbrales de temperatura
     * @return configuración de umbrales
     */
    public TemperatureThreshold getTemperatureThresholds() {
        // Obtener desde preferencias o crear valores por defecto
        float minTemp = preferencesManager.getFloat("temp_threshold_min", 18f);
        float maxTemp = preferencesManager.getFloat("temp_threshold_max", 28f);
        boolean autoControl = preferencesManager.getBoolean("temp_auto_control", false);
        
        TemperatureThreshold threshold = new TemperatureThreshold();
        threshold.setId("main_threshold");
        threshold.setRoomId("all");
        threshold.setRoomName("Todas las habitaciones");
        threshold.setMinTemperature(minTemp);
        threshold.setMaxTemperature(maxTemp);
        threshold.setCriticalLow(minTemp - 5);
        threshold.setCriticalHigh(maxTemp + 5);
        threshold.setIsAutomaticControlEnabled(autoControl);
        threshold.setEnableCooling(true);
        threshold.setEnableHeating(true);
        threshold.setLastUpdated(System.currentTimeMillis());
        
        return threshold;
    }
    
    /**
     * Cambia el estado de encendido/apagado de un ventilador
     * @param deviceId ID del dispositivo
     * @param isOn true para encender, false para apagar
     * @return true si la operación fue exitosa
     */
    public boolean setFanPower(String deviceId, boolean isOn) {
        try {
            // Simular delay de red
            Thread.sleep(random.nextInt(500) + 200);
            
            // Simular falla ocasional (10% de probabilidad)
            if (random.nextFloat() < 0.1f) {
                return false;
            }
            
            // Guardar estado en preferencias
            preferencesManager.putBoolean("fan_power_" + deviceId, isOn);
            
            return true;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Cambia la velocidad de un ventilador
     * @param deviceId ID del dispositivo
     * @param speedPercentage velocidad como porcentaje (0-100)
     * @return true si la operación fue exitosa
     */
    public boolean setFanSpeed(String deviceId, float speedPercentage) {
        try {
            // Simular delay de red
            Thread.sleep(random.nextInt(300) + 100);
            
            // Simular falla ocasional (5% de probabilidad)
            if (random.nextFloat() < 0.05f) {
                return false;
            }
            
            // Validar rango
            if (speedPercentage < 0 || speedPercentage > 100) {
                return false;
            }
            
            // Guardar velocidad en preferencias
            preferencesManager.putFloat("fan_speed_" + deviceId, speedPercentage);
            
            return true;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Guarda la configuración de umbrales de temperatura
     * @param threshold configuración de umbrales
     * @return true si se guardó exitosamente
     */
    public boolean saveTemperatureThreshold(TemperatureThreshold threshold) {
        try {
            if (!threshold.isValid()) {
                return false;
            }
            
            // Simular delay de guardado
            Thread.sleep(random.nextInt(200) + 100);
            
            // Guardar en preferencias
            preferencesManager.putFloat("temp_threshold_min", threshold.getMinTemperature());
            preferencesManager.putFloat("temp_threshold_max", threshold.getMaxTemperature());
            preferencesManager.putBoolean("temp_auto_control", threshold.getIsAutomaticControlEnabled());
            
            return true;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Crea una instancia de RoomTemperature con datos simulados
     */
    private RoomTemperature createRoomTemperature(String roomId, String roomName, RoomType roomType,
                                                 float baseTemp, float humidity, boolean isOnline, int sensorCount) {
        
        // Simular variación de temperatura (±2°C)
        float currentTemp = baseTemp + (random.nextFloat() * 4f - 2f);
        
        // Determinar estado basado en temperatura
        RoomTemperature.TemperatureStatus status;
        if (currentTemp < 16) {
            status = RoomTemperature.TemperatureStatus.CRITICAL_LOW;
        } else if (currentTemp < 18) {
            status = RoomTemperature.TemperatureStatus.LOW;
        } else if (currentTemp > 32) {
            status = RoomTemperature.TemperatureStatus.CRITICAL_HIGH;
        } else if (currentTemp > 28) {
            status = RoomTemperature.TemperatureStatus.HIGH;
        } else {
            status = RoomTemperature.TemperatureStatus.NORMAL;
        }
        
        return new RoomTemperature(
                roomId,
                roomName,
                roomType,
                currentTemp,
                humidity + (random.nextFloat() * 10f - 5f), // Variación de humedad
                true, // hasHumidity
                sensorCount,
                status,
                System.currentTimeMillis() - random.nextInt(300000), // últimos 5 minutos
                isOnline && random.nextFloat() > 0.05f // 95% probabilidad de estar online
        );
    }
    
    /**
     * Crea una instancia de FanControl con datos simulados
     */
    private FanControl createFanControl(String deviceId, String deviceName, String roomName,
                                       String roomId, DeviceType deviceType, boolean defaultOn, float defaultSpeed) {
        
        // Obtener estado guardado o usar valores por defecto
        boolean isOn = preferencesManager.getBoolean("fan_power_" + deviceId, defaultOn);
        float speed = preferencesManager.getFloat("fan_speed_" + deviceId, defaultSpeed);
        
        // Simular consumo de energía basado en la velocidad
        float powerConsumption = isOn ? (speed / 100f) * 50f + 5f : 0f; // 5-55W
        
        return new FanControl(
                deviceId,
                deviceName,
                roomName,
                roomId,
                deviceType,
                isOn,
                speed,
                random.nextFloat() > 0.02f, // 98% probabilidad de estar conectado
                powerConsumption,
                System.currentTimeMillis() - random.nextInt(600000), // últimos 10 minutos
                false, // modo automático desactivado por defecto
                24f // temperatura objetivo por defecto
        );
    }
}