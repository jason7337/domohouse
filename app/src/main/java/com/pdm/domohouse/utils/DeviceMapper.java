package com.pdm.domohouse.utils;

import com.pdm.domohouse.data.database.entity.DeviceEntity;
import com.pdm.domohouse.data.model.Device;
import com.pdm.domohouse.data.model.DeviceType;

/**
 * Utilidad para mapear entre modelos Device y entidades DeviceEntity
 * Mapea entre las diferentes estructuras de datos
 */
public class DeviceMapper {
    
    /**
     * Convierte un modelo Device a entidad DeviceEntity
     */
    public static DeviceEntity toEntity(Device device) {
        if (device == null) return null;
        
        DeviceEntity entity = new DeviceEntity();
        entity.setDeviceId(device.getId());
        entity.setName(device.getName());
        entity.setDeviceType(device.getType().name());
        entity.setRoomId(device.getRoomId());
        
        // Mapear campos comunes
        entity.setOn(device.isEnabled());
        entity.setIntensity((int) device.getCurrentValue());
        entity.setOnline(device.isConnected());
        entity.setLastStateChange(device.getLastUpdated());
        entity.setUpdatedAt(System.currentTimeMillis());
        entity.setSynced(false); // Por defecto no sincronizado
        
        // Para sensores de temperatura
        if (device.getType() == DeviceType.TEMPERATURE_SENSOR) {
            entity.setTemperature(device.getCurrentValue());
        }
        
        return entity;
    }
    
    /**
     * Convierte una entidad DeviceEntity a modelo Device
     */
    public static Device toModel(DeviceEntity entity) {
        if (entity == null) return null;
        
        try {
            DeviceType type = DeviceType.valueOf(entity.getDeviceType());
            Device device = new Device(entity.getDeviceId(), entity.getName(), type, entity.getRoomId());
            
            // Mapear campos comunes
            device.setEnabled(entity.isOn());
            device.setConnected(entity.isOnline());
            device.setCurrentValue(entity.getIntensity());
            device.setLastUpdated(entity.getLastStateChange());
            
            // Para sensores de temperatura
            if (type == DeviceType.TEMPERATURE_SENSOR && entity.getTemperature() != null) {
                device.setCurrentValue(entity.getTemperature());
            }
            
            // Establecer valores por defecto
            device.setMinValue(0);
            device.setMaxValue(100);
            device.setUnit("%");
            device.setBatteryLevel(100);
            device.setSignalStrength(80);
            device.setAutoMode(false);
            
            return device;
        } catch (IllegalArgumentException e) {
            // Si el tipo de dispositivo no es válido, crear un dispositivo por defecto
            Device device = new Device(entity.getDeviceId(), entity.getName(), DeviceType.SMART_OUTLET, entity.getRoomId());
            device.setEnabled(entity.isOn());
            device.setConnected(entity.isOnline());
            device.setCurrentValue(entity.getIntensity());
            return device;
        }
    }
}