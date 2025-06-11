package com.pdm.domohouse.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.pdm.domohouse.data.model.Device;
import com.pdm.domohouse.data.model.DeviceType;
import com.pdm.domohouse.data.model.Room;
import com.pdm.domohouse.data.model.RoomStatus;
import com.pdm.domohouse.data.model.RoomType;
import com.pdm.domohouse.ui.base.BaseViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * ViewModel para el Dashboard Principal (HomeFragment)
 * Maneja la lógica de negocio para la vista de maqueta de casa y estados de dispositivos
 */
public class HomeViewModel extends BaseViewModel {

    // LiveData para las habitaciones de la casa
    private final MutableLiveData<List<Room>> _rooms = new MutableLiveData<>();
    public final LiveData<List<Room>> rooms = _rooms;

    // LiveData para los dispositivos de la casa
    private final MutableLiveData<List<Device>> _devices = new MutableLiveData<>();
    public final LiveData<List<Device>> devices = _devices;

    // LiveData para estadísticas generales
    private final MutableLiveData<DashboardStats> _dashboardStats = new MutableLiveData<>();
    public final LiveData<DashboardStats> dashboardStats = _dashboardStats;

    // LiveData para habitación seleccionada
    private final MutableLiveData<Room> _selectedRoom = new MutableLiveData<>();
    public final LiveData<Room> selectedRoom = _selectedRoom;

    // Mapa de dispositivos por habitación para acceso rápido
    private Map<String, List<Device>> devicesByRoom = new HashMap<>();

    // Random para simular datos cambiantes
    private Random random = new Random();

    /**
     * Constructor - Inicializa el ViewModel con datos de ejemplo
     */
    public HomeViewModel() {
        initializeMockData();
        updateDashboardStats();
    }

    /**
     * Inicializa datos de ejemplo para el dashboard
     * En una implementación real, estos datos vendrían del backend/base de datos
     */
    private void initializeMockData() {
        setLoading(true);

        // Crear habitaciones de ejemplo
        List<Room> mockRooms = createMockRooms();
        _rooms.postValue(mockRooms);

        // Crear dispositivos de ejemplo
        List<Device> mockDevices = createMockDevices(mockRooms);
        _devices.postValue(mockDevices);

        // Organizar dispositivos por habitación
        organizarDispositivosPorHabitacion(mockDevices);

        setLoading(false);
    }

    /**
     * Crea habitaciones de ejemplo con posiciones para la maqueta
     */
    private List<Room> createMockRooms() {
        List<Room> rooms = new ArrayList<>();

        // Planta baja
        rooms.add(new Room("sala", "Sala Principal", RoomType.LIVING_ROOM, 0.1f, 0.3f, 0.4f, 0.4f));
        rooms.add(new Room("cocina", "Cocina", RoomType.KITCHEN, 0.6f, 0.3f, 0.3f, 0.3f));
        rooms.add(new Room("comedor", "Comedor", RoomType.DINING_ROOM, 0.1f, 0.8f, 0.3f, 0.15f));
        rooms.add(new Room("bano1", "Baño Principal", RoomType.BATHROOM, 0.5f, 0.8f, 0.15f, 0.15f));

        // Planta alta
        rooms.add(new Room("dormitorio1", "Dormitorio Principal", RoomType.MASTER_BEDROOM, 0.1f, 0.05f, 0.35f, 0.2f));
        rooms.add(new Room("dormitorio2", "Dormitorio 2", RoomType.BEDROOM, 0.5f, 0.05f, 0.25f, 0.2f));
        rooms.add(new Room("oficina", "Oficina", RoomType.OFFICE, 0.8f, 0.05f, 0.15f, 0.2f));

        // Exterior
        rooms.add(new Room("garage", "Garaje", RoomType.GARAGE, 0.7f, 0.7f, 0.25f, 0.25f));

        // Configurar valores iniciales aleatorios para sensores
        for (Room room : rooms) {
            room.setTemperature(20 + random.nextFloat() * 8); // 20-28°C
            room.setHumidity(40 + random.nextFloat() * 30);   // 40-70%
            room.setLightLevel(random.nextInt(101));          // 0-100%
            room.setFanSpeed(random.nextInt(51));             // 0-50%
            room.setDoorOpen(random.nextBoolean());
            room.setWindowOpen(random.nextBoolean());
        }

        return rooms;
    }

    /**
     * Crea dispositivos de ejemplo para las habitaciones
     */
    private List<Device> createMockDevices(List<Room> rooms) {
        List<Device> devices = new ArrayList<>();

        for (Room room : rooms) {
            // Agregar sensores básicos a cada habitación
            devices.add(createDevice(room.getId() + "_temp", "Termómetro", 
                        DeviceType.TEMPERATURE_SENSOR, room.getId()));
            devices.add(createDevice(room.getId() + "_hum", "Sensor Humedad", 
                        DeviceType.HUMIDITY_SENSOR, room.getId()));

            // Agregar dispositivos específicos por tipo de habitación
            switch (room.getType()) {
                case LIVING_ROOM:
                    devices.add(createDevice(room.getId() + "_light1", "Luz Principal", 
                               DeviceType.DIMMER_LIGHT, room.getId()));
                    devices.add(createDevice(room.getId() + "_light2", "Luz Ambiente", 
                               DeviceType.RGB_LIGHT, room.getId()));
                    devices.add(createDevice(room.getId() + "_tv", "Smart TV", 
                               DeviceType.SMART_TV, room.getId()));
                    devices.add(createDevice(room.getId() + "_sound", "Sistema de Audio", 
                               DeviceType.SOUND_SYSTEM, room.getId()));
                    break;

                case KITCHEN:
                    devices.add(createDevice(room.getId() + "_light", "Luz Cocina", 
                               DeviceType.LIGHT_SWITCH, room.getId()));
                    devices.add(createDevice(room.getId() + "_smoke", "Detector Humo", 
                               DeviceType.SMOKE_DETECTOR, room.getId()));
                    devices.add(createDevice(room.getId() + "_fan", "Extractor", 
                               DeviceType.FAN_SWITCH, room.getId()));
                    break;

                case MASTER_BEDROOM:
                case BEDROOM:
                    devices.add(createDevice(room.getId() + "_light", "Luz Principal", 
                               DeviceType.DIMMER_LIGHT, room.getId()));
                    devices.add(createDevice(room.getId() + "_ac", "Aire Acondicionado", 
                               DeviceType.AIR_CONDITIONING, room.getId()));
                    devices.add(createDevice(room.getId() + "_blind", "Persiana", 
                               DeviceType.WINDOW_BLIND, room.getId()));
                    break;

                case BATHROOM:
                    devices.add(createDevice(room.getId() + "_light", "Luz Baño", 
                               DeviceType.LIGHT_SWITCH, room.getId()));
                    devices.add(createDevice(room.getId() + "_fan", "Ventilador", 
                               DeviceType.FAN_SWITCH, room.getId()));
                    devices.add(createDevice(room.getId() + "_heater", "Calentador", 
                               DeviceType.HEATER, room.getId()));
                    break;

                case OFFICE:
                    devices.add(createDevice(room.getId() + "_light", "Luz Escritorio", 
                               DeviceType.DIMMER_LIGHT, room.getId()));
                    devices.add(createDevice(room.getId() + "_outlet", "Enchufe Inteligente", 
                               DeviceType.SMART_OUTLET, room.getId()));
                    break;

                case GARAGE:
                    devices.add(createDevice(room.getId() + "_light", "Luz Garaje", 
                               DeviceType.LIGHT_SWITCH, room.getId()));
                    devices.add(createDevice(room.getId() + "_door", "Puerta Garaje", 
                               DeviceType.GARAGE_DOOR, room.getId()));
                    devices.add(createDevice(room.getId() + "_motion", "Sensor Movimiento", 
                               DeviceType.MOTION_SENSOR, room.getId()));
                    break;
            }

            // Agregar sensores de seguridad en habitaciones principales
            if (room.getType().hasSecuritySensors()) {
                devices.add(createDevice(room.getId() + "_door_sensor", "Sensor Puerta", 
                           DeviceType.DOOR_SENSOR, room.getId()));
                if (room.getType() != RoomType.GARAGE) {
                    devices.add(createDevice(room.getId() + "_window_sensor", "Sensor Ventana", 
                               DeviceType.WINDOW_SENSOR, room.getId()));
                }
            }
        }

        // Configurar valores aleatorios para los dispositivos
        for (Device device : devices) {
            device.setConnected(random.nextFloat() > 0.1f); // 90% conectados
            device.setSignalStrength(70 + random.nextInt(31)); // 70-100%
            device.setBatteryLevel(device.getType().isBatteryPowered() ? 
                                 20 + random.nextInt(81) : 100); // 20-100% o 100%

            if (device.getType().isSwitch()) {
                device.setEnabled(random.nextBoolean());
                device.setCurrentValue(device.isEnabled() ? 1 : 0);
            } else {
                float range = device.getMaxValue() - device.getMinValue();
                device.setCurrentValue(device.getMinValue() + random.nextFloat() * range);
                device.setEnabled(device.getCurrentValue() > device.getMinValue());
            }
        }

        return devices;
    }

    /**
     * Crea un dispositivo con configuración inicial
     */
    private Device createDevice(String id, String name, DeviceType type, String roomId) {
        Device device = new Device(id, name, type, roomId);
        device.setConnected(true);
        device.setSignalStrength(100);
        return device;
    }

    /**
     * Organiza los dispositivos por habitación para acceso rápido
     */
    private void organizarDispositivosPorHabitacion(List<Device> devices) {
        devicesByRoom.clear();
        for (Device device : devices) {
            String roomId = device.getRoomId();
            if (!devicesByRoom.containsKey(roomId)) {
                devicesByRoom.put(roomId, new ArrayList<>());
            }
            devicesByRoom.get(roomId).add(device);
        }

        // Actualizar contadores de dispositivos activos en habitaciones
        List<Room> currentRooms = _rooms.getValue();
        if (currentRooms != null) {
            for (Room room : currentRooms) {
                List<Device> roomDevices = devicesByRoom.get(room.getId());
                if (roomDevices != null) {
                    int activeCount = 0;
                    for (Device device : roomDevices) {
                        if (device.isEnabled() && device.isConnected()) {
                            activeCount++;
                        }
                    }
                    room.setActiveDevices(activeCount);
                }
            }
            _rooms.postValue(currentRooms);
        }
    }

    /**
     * Actualiza las estadísticas generales del dashboard
     */
    private void updateDashboardStats() {
        List<Room> currentRooms = _rooms.getValue();
        List<Device> currentDevices = _devices.getValue();

        if (currentRooms == null || currentDevices == null) return;

        int totalRooms = currentRooms.size();
        int activeRooms = 0;
        int totalDevices = currentDevices.size();
        int connectedDevices = 0;
        int activeDevices = 0;
        int alertsCount = 0;
        float avgTemperature = 0;

        // Contar habitaciones activas y calcular temperatura promedio
        for (Room room : currentRooms) {
            if (room.hasActiveDevices()) {
                activeRooms++;
            }
            if (room.getOverallStatus().requiresAttention()) {
                alertsCount++;
            }
            avgTemperature += room.getTemperature();
        }
        avgTemperature /= totalRooms;

        // Contar dispositivos conectados y activos
        for (Device device : currentDevices) {
            if (device.isConnected()) {
                connectedDevices++;
                if (device.isEnabled()) {
                    activeDevices++;
                }
            }
            if (device.needsAttention()) {
                alertsCount++;
            }
        }

        DashboardStats stats = new DashboardStats(
            totalRooms, activeRooms, totalDevices, connectedDevices, 
            activeDevices, alertsCount, avgTemperature
        );

        _dashboardStats.postValue(stats);
    }

    /**
     * Selecciona una habitación para mostrar detalles
     */
    public void selectRoom(Room room) {
        _selectedRoom.postValue(room);
    }

    /**
     * Obtiene los dispositivos de una habitación específica
     */
    public List<Device> getDevicesForRoom(String roomId) {
        return devicesByRoom.getOrDefault(roomId, new ArrayList<>());
    }

    /**
     * Actualiza el estado de un dispositivo
     */
    public void updateDeviceState(String deviceId, boolean enabled) {
        List<Device> currentDevices = _devices.getValue();
        if (currentDevices == null) return;

        for (Device device : currentDevices) {
            if (device.getId().equals(deviceId)) {
                device.setEnabled(enabled);
                if (device.getType().isSwitch()) {
                    device.setCurrentValue(enabled ? 1 : 0);
                }
                break;
            }
        }

        _devices.postValue(currentDevices);
        organizarDispositivosPorHabitacion(currentDevices);
        updateDashboardStats();
    }

    /**
     * Actualiza el valor de un dispositivo
     */
    public void updateDeviceValue(String deviceId, float value) {
        List<Device> currentDevices = _devices.getValue();
        if (currentDevices == null) return;

        for (Device device : currentDevices) {
            if (device.getId().equals(deviceId)) {
                device.setCurrentValue(value);
                device.setEnabled(value > device.getMinValue());
                break;
            }
        }

        _devices.postValue(currentDevices);
        organizarDispositivosPorHabitacion(currentDevices);
        updateDashboardStats();
    }

    /**
     * Simula actualización de datos de sensores
     * En una implementación real, esto vendría del backend en tiempo real
     */
    public void refreshSensorData() {
        setLoading(true);

        List<Room> currentRooms = _rooms.getValue();
        List<Device> currentDevices = _devices.getValue();

        if (currentRooms != null) {
            // Simular cambios en sensores de habitaciones
            for (Room room : currentRooms) {
                if (random.nextFloat() > 0.7f) { // 30% de probabilidad de cambio
                    room.setTemperature(room.getTemperature() + (random.nextFloat() - 0.5f) * 2);
                    room.setHumidity(room.getHumidity() + (random.nextFloat() - 0.5f) * 5);
                }
            }
            _rooms.postValue(currentRooms);
        }

        if (currentDevices != null) {
            // Simular cambios en dispositivos sensores
            for (Device device : currentDevices) {
                if (device.isSensor() && random.nextFloat() > 0.8f) { // 20% probabilidad
                    float range = device.getMaxValue() - device.getMinValue();
                    float newValue = device.getCurrentValue() + (random.nextFloat() - 0.5f) * range * 0.1f;
                    device.setCurrentValue(newValue);
                }
            }
            _devices.postValue(currentDevices);
        }

        updateDashboardStats();
        setLoading(false);
    }

    /**
     * Clase interna para estadísticas del dashboard
     */
    public static class DashboardStats {
        public final int totalRooms;
        public final int activeRooms;
        public final int totalDevices;
        public final int connectedDevices;
        public final int activeDevices;
        public final int alertsCount;
        public final float avgTemperature;

        public DashboardStats(int totalRooms, int activeRooms, int totalDevices, 
                            int connectedDevices, int activeDevices, int alertsCount, 
                            float avgTemperature) {
            this.totalRooms = totalRooms;
            this.activeRooms = activeRooms;
            this.totalDevices = totalDevices;
            this.connectedDevices = connectedDevices;
            this.activeDevices = activeDevices;
            this.alertsCount = alertsCount;
            this.avgTemperature = avgTemperature;
        }

        /**
         * Obtiene el porcentaje de habitaciones activas
         */
        public int getActiveRoomsPercentage() {
            return totalRooms > 0 ? (activeRooms * 100) / totalRooms : 0;
        }

        /**
         * Obtiene el porcentaje de dispositivos conectados
         */
        public int getConnectedDevicesPercentage() {
            return totalDevices > 0 ? (connectedDevices * 100) / totalDevices : 0;
        }

        /**
         * Verifica si hay alertas activas
         */
        public boolean hasAlerts() {
            return alertsCount > 0;
        }
    }
}