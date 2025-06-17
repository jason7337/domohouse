package com.pdm.domohouse.data.manager;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.pdm.domohouse.data.model.Device;
import com.pdm.domohouse.data.model.DeviceType;
import com.pdm.domohouse.data.model.Room;
import com.pdm.domohouse.data.model.RoomStatus;
import com.pdm.domohouse.data.model.RoomType;
import com.pdm.domohouse.data.repository.DeviceRepository;
import com.pdm.domohouse.data.repository.RoomRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sistema de gestión de habitaciones para DomoHouse
 * Maneja 3 habitaciones específicas conectadas a 3 Arduinos
 */
public class RoomSystemManager {
    
    private static RoomSystemManager instance;
    
    // Repositorios
    private final RoomRepository roomRepository;
    private final DeviceRepository deviceRepository;
    
    // LiveData para observar cambios
    private final MutableLiveData<Boolean> esp32Connected = new MutableLiveData<>(true);
    private final MutableLiveData<String> esp32Status = new MutableLiveData<>("Conectado - 3/3 Arduinos Activos");
    private final MutableLiveData<List<Room>> rooms = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Map<String, List<Device>>> roomDevices = new MutableLiveData<>(new HashMap<>());
    
    // Configuración de las 3 habitaciones Arduino
    private static final String ROOM_1_ID = "room_arduino_1";
    private static final String ROOM_2_ID = "room_arduino_2";
    private static final String ROOM_3_ID = "room_arduino_3";
    
    // IDs de hardware para los Arduinos
    private static final String ARDUINO_1_HW_ID = "ESP32_ARDUINO_01";
    private static final String ARDUINO_2_HW_ID = "ESP32_ARDUINO_02";
    private static final String ARDUINO_3_HW_ID = "ESP32_ARDUINO_03";
    
    private RoomSystemManager(RoomRepository roomRepository, DeviceRepository deviceRepository) {
        this.roomRepository = roomRepository;
        this.deviceRepository = deviceRepository;
        initializeRoomSystem();
    }
    
    public static synchronized RoomSystemManager getInstance(RoomRepository roomRepository, 
                                                           DeviceRepository deviceRepository) {
        if (instance == null) {
            instance = new RoomSystemManager(roomRepository, deviceRepository);
        }
        return instance;
    }
    
    /**
     * Inicializa el sistema de habitaciones si no existe
     */
    private void initializeRoomSystem() {
        // Verificar si las habitaciones ya existen
        roomRepository.getRoom(ROOM_1_ID).observeForever(room -> {
            if (room == null) {
                createDefaultRoomSystem();
            } else {
                loadExistingRoomSystem();
            }
        });
    }
    
    /**
     * Crea el sistema por defecto de 3 habitaciones
     */
    private void createDefaultRoomSystem() {
        List<Room> defaultRooms = new ArrayList<>();
        
        // Habitación 1 - Arduino 1 (Dormitorio)
        Room room1 = new Room(ROOM_1_ID, "Habitación 1", RoomType.MASTER_BEDROOM, 1);
        room1.setTemperature(23.0f);
        room1.setHumidity(45.0f);
        room1.setActiveDevices(8);
        room1.setPositionX(0.2f);
        room1.setPositionY(0.3f);
        defaultRooms.add(room1);
        
        // Habitación 2 - Arduino 2 (Sala)
        Room room2 = new Room(ROOM_2_ID, "Habitación 2", RoomType.LIVING_ROOM, 1);
        room2.setTemperature(24.0f);
        room2.setHumidity(50.0f);
        room2.setActiveDevices(12);
        room2.setPositionX(0.7f);
        room2.setPositionY(0.3f);
        defaultRooms.add(room2);
        
        // Habitación 3 - Arduino 3 (Cocina/Seguridad)
        Room room3 = new Room(ROOM_3_ID, "Habitación 3", RoomType.KITCHEN, 1);
        room3.setTemperature(22.0f);
        room3.setHumidity(40.0f);
        room3.setActiveDevices(4);
        room3.setPositionX(0.45f);
        room3.setPositionY(0.7f);
        defaultRooms.add(room3);
        
        // Guardar habitaciones
        for (Room room : defaultRooms) {
            roomRepository.addRoom(room).thenAccept(success -> {
                if (success) {
                    createDefaultDevicesForRoom(room);
                }
            });
        }
        
        rooms.setValue(defaultRooms);
    }
    
    /**
     * Crea dispositivos por defecto para cada habitación
     */
    private void createDefaultDevicesForRoom(Room room) {
        String hardwareId = getHardwareIdForRoom(room.getId());
        List<Device> devices = new ArrayList<>();
        
        switch (room.getId()) {
            case ROOM_1_ID: // Habitación 1 - Dormitorio
                devices.add(createDevice("temp_sensor_1", "Sensor Temperatura", DeviceType.TEMPERATURE_SENSOR, room.getId()));
                devices.add(createDevice("light_main_1", "Luz Principal", DeviceType.LIGHT_SWITCH, room.getId()));
                devices.add(createDevice("light_night_1", "Luz Nocturna", DeviceType.LIGHT_DIMMER, room.getId()));
                devices.add(createDevice("fan_1", "Ventilador", DeviceType.FAN_SWITCH, room.getId()));
                devices.add(createDevice("ac_1", "Aire Acondicionado", DeviceType.AIR_CONDITIONING, room.getId()));
                devices.add(createDevice("door_sensor_1", "Sensor Puerta", DeviceType.DOOR_SENSOR, room.getId()));
                devices.add(createDevice("window_sensor_1", "Sensor Ventana", DeviceType.WINDOW_SENSOR, room.getId()));
                devices.add(createDevice("smart_outlet_1", "Enchufe Inteligente", DeviceType.SMART_OUTLET, room.getId()));
                break;
                
            case ROOM_2_ID: // Habitación 2 - Sala
                devices.add(createDevice("temp_sensor_2", "Sensor Temperatura", DeviceType.TEMPERATURE_SENSOR, room.getId()));
                devices.add(createDevice("light_main_2", "Luz Principal", DeviceType.LIGHT_SWITCH, room.getId()));
                devices.add(createDevice("light_accent_2", "Luces Ambientales", DeviceType.RGB_LIGHT, room.getId()));
                devices.add(createDevice("fan_ceiling_2", "Ventilador Techo", DeviceType.FAN_SPEED, room.getId()));
                devices.add(createDevice("tv_2", "TV Inteligente", DeviceType.SMART_TV, room.getId()));
                devices.add(createDevice("sound_system_2", "Sistema Audio", DeviceType.SOUND_SYSTEM, room.getId()));
                devices.add(createDevice("motion_sensor_2", "Sensor Movimiento", DeviceType.MOTION_SENSOR, room.getId()));
                devices.add(createDevice("smart_outlet_2a", "Enchufe 1", DeviceType.SMART_OUTLET, room.getId()));
                devices.add(createDevice("smart_outlet_2b", "Enchufe 2", DeviceType.SMART_OUTLET, room.getId()));
                devices.add(createDevice("smart_outlet_2c", "Enchufe 3", DeviceType.SMART_OUTLET, room.getId()));
                devices.add(createDevice("window_blind_2", "Persianas", DeviceType.WINDOW_BLIND, room.getId()));
                devices.add(createDevice("thermostat_2", "Termostato", DeviceType.THERMOSTAT, room.getId()));
                break;
                
            case ROOM_3_ID: // Habitación 3 - Cocina/Seguridad
                devices.add(createDevice("temp_sensor_3", "Sensor Temperatura", DeviceType.TEMPERATURE_SENSOR, room.getId()));
                devices.add(createDevice("smoke_detector_3", "Detector Humo", DeviceType.SMOKE_DETECTOR, room.getId()));
                devices.add(createDevice("light_main_3", "Luz Principal", DeviceType.LIGHT_SWITCH, room.getId()));
                devices.add(createDevice("security_camera_3", "Cámara Seguridad", DeviceType.CAMERA, room.getId()));
                break;
        }
        
        // Guardar dispositivos
        for (Device device : devices) {
            deviceRepository.addDevice(device).thenAccept(success -> {
                // Actualizar la lista de dispositivos por habitación
                updateRoomDevicesMap();
            });
        }
    }
    
    /**
     * Crea un dispositivo con la configuración especificada
     */
    private Device createDevice(String id, String name, DeviceType type, String roomId) {
        Device device = new Device(id, name, type, roomId);
        device.setEnabled(true);
        device.setConnected(true);
        device.setCurrentValue(0.0f);
        device.setMinValue(0.0f);
        device.setMaxValue(100.0f);
        device.setUnit(getUnitForDeviceType(type));
        device.setLastUpdated(System.currentTimeMillis());
        device.setBatteryLevel(95 + (int)(Math.random() * 5)); // 95-100%
        device.setAutoMode(false);
        device.setSignalStrength(85 + (int)(Math.random() * 15)); // 85-100%
        
        // Configuración específica para sensores de temperatura
        if (type == DeviceType.TEMPERATURE_SENSOR) {
            device.setTemperature(20.0f + (float)(Math.random() * 10)); // 20-30°C
        }
        
        return device;
    }
    
    /**
     * Obtiene el ID de hardware para una habitación
     */
    private String getHardwareIdForRoom(String roomId) {
        switch (roomId) {
            case ROOM_1_ID: return ARDUINO_1_HW_ID;
            case ROOM_2_ID: return ARDUINO_2_HW_ID;
            case ROOM_3_ID: return ARDUINO_3_HW_ID;
            default: return "UNKNOWN_HARDWARE";
        }
    }
    
    /**
     * Obtiene la unidad para un tipo de dispositivo
     */
    private String getUnitForDeviceType(DeviceType type) {
        switch (type) {
            case TEMPERATURE_SENSOR: return "°C";
            case HUMIDITY_SENSOR: return "%";
            case LIGHT_DIMMER:
            case FAN_SPEED: return "%";
            case LIGHT_SENSOR: return "lux";
            default: return "";
        }
    }
    
    /**
     * Carga el sistema de habitaciones existente
     */
    private void loadExistingRoomSystem() {
        roomRepository.getAllRooms().observeForever(roomList -> {
            if (roomList != null) {
                // Filtrar solo las 3 habitaciones del sistema Arduino
                List<Room> systemRooms = new ArrayList<>();
                for (Room room : roomList) {
                    if (room.getId().equals(ROOM_1_ID) || 
                        room.getId().equals(ROOM_2_ID) || 
                        room.getId().equals(ROOM_3_ID)) {
                        systemRooms.add(room);
                    }
                }
                rooms.setValue(systemRooms);
                updateRoomDevicesMap();
            }
        });
    }
    
    /**
     * Actualiza el mapa de dispositivos por habitación
     */
    private void updateRoomDevicesMap() {
        Map<String, List<Device>> deviceMap = new HashMap<>();
        
        deviceRepository.getDevicesByRoom(ROOM_1_ID).observeForever(devices -> {
            deviceMap.put(ROOM_1_ID, devices != null ? devices : new ArrayList<>());
            
            deviceRepository.getDevicesByRoom(ROOM_2_ID).observeForever(devices2 -> {
                deviceMap.put(ROOM_2_ID, devices2 != null ? devices2 : new ArrayList<>());
                
                deviceRepository.getDevicesByRoom(ROOM_3_ID).observeForever(devices3 -> {
                    deviceMap.put(ROOM_3_ID, devices3 != null ? devices3 : new ArrayList<>());
                    roomDevices.setValue(deviceMap);
                });
            });
        });
    }
    
    /**
     * Obtiene información de estado del ESP32
     */
    public void updateESP32Status() {
        // Verificar conectividad de los 3 Arduinos
        int activeArduinos = 0;
        
        // Simular verificación de estado (esto se conectaría con el hardware real)
        Map<String, List<Device>> currentDevices = roomDevices.getValue();
        if (currentDevices != null) {
            for (String roomId : new String[]{ROOM_1_ID, ROOM_2_ID, ROOM_3_ID}) {
                List<Device> devices = currentDevices.get(roomId);
                if (devices != null && !devices.isEmpty()) {
                    boolean roomOnline = devices.stream().anyMatch(Device::isConnected);
                    if (roomOnline) activeArduinos++;
                }
            }
        }
        
        boolean allConnected = activeArduinos == 3;
        esp32Connected.setValue(allConnected);
        
        String status;
        if (allConnected) {
            status = "Conectado - 3/3 Arduinos Activos";
        } else if (activeArduinos > 0) {
            status = "Parcial - " + activeArduinos + "/3 Arduinos Activos";
        } else {
            status = "Desconectado - Sistema Offline";
        }
        esp32Status.setValue(status);
    }
    
    /**
     * Obtiene estadísticas de dispositivos
     */
    public DeviceStats getDeviceStats() {
        Map<String, List<Device>> currentDevices = roomDevices.getValue();
        int total = 0, active = 0, offline = 0;
        
        if (currentDevices != null) {
            for (List<Device> devices : currentDevices.values()) {
                total += devices.size();
                for (Device device : devices) {
                    if (device.isConnected()) {
                        if (device.isEnabled()) {
                            active++;
                        }
                    } else {
                        offline++;
                    }
                }
            }
        }
        
        return new DeviceStats(total, active, offline);
    }
    
    /**
     * Clase para estadísticas de dispositivos
     */
    public static class DeviceStats {
        public final int total;
        public final int active;
        public final int offline;
        
        public DeviceStats(int total, int active, int offline) {
            this.total = total;
            this.active = active;
            this.offline = offline;
        }
    }
    
    // Getters para LiveData
    public LiveData<Boolean> getESP32Connected() { return esp32Connected; }
    public LiveData<String> getESP32Status() { return esp32Status; }
    public LiveData<List<Room>> getRooms() { return rooms; }
    public LiveData<Map<String, List<Device>>> getRoomDevices() { return roomDevices; }
    
    /**
     * Actualiza el estado de una habitación específica
     */
    public void updateRoomTemperature(String roomId, float temperature) {
        List<Room> currentRooms = rooms.getValue();
        if (currentRooms != null) {
            for (Room room : currentRooms) {
                if (room.getId().equals(roomId)) {
                    room.setTemperature(temperature);
                    roomRepository.updateRoom(room).thenAccept(success -> {
                        if (success) {
                            rooms.setValue(currentRooms);
                        }
                    });
                    break;
                }
            }
        }
    }
    
    /**
     * Obtiene el estado de una habitación por ID
     */
    public Room getRoomById(String roomId) {
        List<Room> currentRooms = rooms.getValue();
        if (currentRooms != null) {
            for (Room room : currentRooms) {
                if (room.getId().equals(roomId)) {
                    return room;
                }
            }
        }
        return null;
    }
    
    /**
     * Obtiene los dispositivos de una habitación específica
     */
    public List<Device> getDevicesForRoom(String roomId) {
        Map<String, List<Device>> currentDevices = roomDevices.getValue();
        if (currentDevices != null) {
            return currentDevices.get(roomId);
        }
        return new ArrayList<>();
    }
    
    /**
     * Fuerza la actualización de todos los datos
     */
    public void refreshAllData() {
        loadExistingRoomSystem();
        updateESP32Status();
    }
}