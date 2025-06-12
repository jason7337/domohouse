package com.pdm.domohouse.ui.home;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.pdm.domohouse.cache.DeviceCache;
import com.pdm.domohouse.data.database.AppDatabase;
import com.pdm.domohouse.data.database.entity.DeviceEntity;
import com.pdm.domohouse.data.database.entity.RoomEntity;
import com.pdm.domohouse.data.model.Device;
import com.pdm.domohouse.data.model.DeviceType;
import com.pdm.domohouse.data.model.Room;
import com.pdm.domohouse.data.model.RoomStatus;
import com.pdm.domohouse.data.model.RoomType;
import com.pdm.domohouse.sync.SyncManager;
import com.pdm.domohouse.ui.base.BaseViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ViewModel para el Dashboard Principal (HomeFragment)
 * Maneja la lógica de negocio para la vista de maqueta de casa y estados de dispositivos
 */
public class HomeViewModel extends AndroidViewModel {

    // Cache y base de datos
    private final DeviceCache deviceCache;
    private final AppDatabase database;
    private final SyncManager syncManager;

    // LiveData para las habitaciones de la casa
    private final LiveData<List<RoomEntity>> roomsFromDb;
    private final MediatorLiveData<List<Room>> _rooms = new MediatorLiveData<>();
    public final LiveData<List<Room>> rooms = _rooms;

    // LiveData para los dispositivos de la casa
    private final LiveData<List<DeviceEntity>> devicesFromDb;
    private final MediatorLiveData<List<Device>> _devices = new MediatorLiveData<>();
    public final LiveData<List<Device>> devices = _devices;

    // LiveData para estadísticas generales
    private final MutableLiveData<DashboardStats> _dashboardStats = new MutableLiveData<>();
    public final LiveData<DashboardStats> dashboardStats = _dashboardStats;

    // LiveData para habitación seleccionada
    private final MutableLiveData<Room> _selectedRoom = new MutableLiveData<>();
    public final LiveData<Room> selectedRoom = _selectedRoom;

    // Estado de carga y error
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public final LiveData<Boolean> isLoading = _isLoading;
    
    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public final LiveData<String> error = _error;

    // Estado de sincronización
    private final MutableLiveData<Boolean> _isSyncing = new MutableLiveData<>(false);
    public final LiveData<Boolean> isSyncing = _isSyncing;

    // Mapa de dispositivos por habitación para acceso rápido
    private Map<String, List<Device>> devicesByRoom = new HashMap<>();

    /**
     * Constructor - Inicializa el ViewModel con cache y base de datos
     */
    public HomeViewModel(@NonNull Application application) {
        super(application);
        Context context = application.getApplicationContext();
        
        // Inicializar componentes
        this.deviceCache = DeviceCache.getInstance(context);
        this.database = AppDatabase.getDatabase(context);
        this.syncManager = SyncManager.getInstance(context);
        
        // Observar datos de la base de datos
        roomsFromDb = database.roomDao().getAllRooms();
        devicesFromDb = deviceCache.getAllDevices();
        
        // Configurar observadores
        setupDataObservers();
        
        // Pre-cargar cache
        deviceCache.preloadDevices();
        
        // Sincronizar si hay conexión
        if (syncManager.isOnline()) {
            syncData();
        }
    }

    /**
     * Configura observadores para datos de la base de datos
     */
    private void setupDataObservers() {
        // Observar cambios en habitaciones
        _rooms.addSource(roomsFromDb, roomEntities -> {
            if (roomEntities != null) {
                List<Room> rooms = convertRoomEntitiesToRooms(roomEntities);
                _rooms.setValue(rooms);
                updateDashboardStats();
            }
        });
        
        // Observar cambios en dispositivos
        _devices.addSource(devicesFromDb, deviceEntities -> {
            if (deviceEntities != null) {
                List<Device> devices = convertDeviceEntitiesToDevices(deviceEntities);
                _devices.setValue(devices);
                organizarDispositivosPorHabitacion(devices);
                updateDashboardStats();
            }
        });
        
        // Observar estado de sincronización
        syncManager.getSyncStatus().observeForever(status -> {
            switch (status) {
                case SYNCING:
                    _isSyncing.setValue(true);
                    break;
                case SUCCESS:
                    _isSyncing.setValue(false);
                    _error.setValue(null);
                    break;
                case ERROR:
                    _isSyncing.setValue(false);
                    _error.setValue("Error de sincronización");
                    break;
                case CONFLICT:
                    _isSyncing.setValue(false);
                    _error.setValue("Conflictos de sincronización detectados");
                    break;
                default:
                    _isSyncing.setValue(false);
                    break;
            }
        });
    }
    
    /**
     * Sincroniza datos con Firebase
     */
    private void syncData() {
        syncManager.syncAll();
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

        // Configurar valores iniciales para sensores
        for (Room room : rooms) {
            room.setTemperature(22.0f); // Temperatura estándar
            room.setHumidity(45.0f);    // Humedad estándar
            room.setLightLevel(50);     // 50% de luz
            room.setFanSpeed(0);        // Ventilador apagado
            room.setDoorOpen(false);
            room.setWindowOpen(false);
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

        // Configurar valores por defecto para los dispositivos
        for (Device device : devices) {
            device.setConnected(true); // Todos conectados por defecto
            device.setSignalStrength(85); // Señal buena
            device.setBatteryLevel(device.getType().isBatteryPowered() ? 85 : 100); // Batería buena

            if (device.getType().isSwitch()) {
                device.setEnabled(false); // Switches apagados por defecto
                device.setCurrentValue(device.isEnabled() ? 1 : 0);
            } else {
                float midValue = (device.getMaxValue() + device.getMinValue()) / 2;
                device.setCurrentValue(midValue);
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
        // Actualizar en cache y base de datos
        deviceCache.updateDeviceState(deviceId, enabled);
        
        // Actualizar lista local para UI inmediata
        List<Device> currentDevices = _devices.getValue();
        if (currentDevices != null) {
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
    }

    /**
     * Actualiza el valor de un dispositivo
     */
    public void updateDeviceValue(String deviceId, float value) {
        // Actualizar intensidad en cache y base de datos
        deviceCache.updateDeviceIntensity(deviceId, (int) value);
        
        // Actualizar lista local para UI inmediata
        List<Device> currentDevices = _devices.getValue();
        if (currentDevices != null) {
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
    }

    /**
     * Refresca datos de sensores desde Firebase o cache local
     */
    public void refreshSensorData() {
        _isLoading.setValue(true);
        
        // Si hay conexión, sincronizar con Firebase
        if (syncManager.isOnline()) {
            syncManager.syncAll();
        } else {
            // Si no hay conexión, refrescar desde cache local
            deviceCache.preloadDevices();
            _isLoading.setValue(false);
        }
    }
    
    /**
     * Cierra la sesión del usuario actual
     */
    public void logout() {
        // Limpiar cache
        deviceCache.invalidateCache();
        _error.setValue("Cerrando sesión...");
    }
    
    /**
     * Convierte entidades de Room a modelos de Room
     */
    private List<Room> convertRoomEntitiesToRooms(List<RoomEntity> entities) {
        List<Room> rooms = new ArrayList<>();
        for (RoomEntity entity : entities) {
            Room room = new Room(
                entity.getRoomId(),
                entity.getName(),
                convertStringToRoomType(entity.getRoomType()),
                entity.getPositionX(),
                entity.getPositionY(),
                0.3f, // width por defecto
                0.3f  // height por defecto
            );
            
            // Configurar valores adicionales desde dispositivos
            List<Device> roomDevices = getDevicesForRoom(entity.getRoomId());
            for (Device device : roomDevices) {
                if (device.getType() == DeviceType.TEMPERATURE_SENSOR) {
                    room.setTemperature(device.getCurrentValue());
                } else if (device.getType() == DeviceType.HUMIDITY_SENSOR) {
                    room.setHumidity(device.getCurrentValue());
                }
            }
            
            rooms.add(room);
        }
        return rooms;
    }
    
    /**
     * Convierte entidades de Device a modelos de Device
     */
    private List<Device> convertDeviceEntitiesToDevices(List<DeviceEntity> entities) {
        List<Device> devices = new ArrayList<>();
        for (DeviceEntity entity : entities) {
            Device device = new Device(
                entity.getDeviceId(),
                entity.getName(),
                convertStringToDeviceType(entity.getDeviceType()),
                entity.getRoomId()
            );
            
            device.setEnabled(entity.isOn());
            device.setOnline(entity.isOnline());
            device.setCurrentValue(entity.getIntensity());
            
            // Para sensores de temperatura
            if (entity.getTemperature() != null) {
                device.setCurrentValue(entity.getTemperature());
            }
            
            devices.add(device);
        }
        return devices;
    }
    
    /**
     * Convierte string a RoomType
     */
    private RoomType convertStringToRoomType(String type) {
        try {
            return RoomType.valueOf(type);
        } catch (Exception e) {
            return RoomType.LIVING_ROOM; // Por defecto
        }
    }
    
    /**
     * Convierte string a DeviceType
     */
    private DeviceType convertStringToDeviceType(String type) {
        try {
            return DeviceType.valueOf(type);
        } catch (Exception e) {
            return DeviceType.LIGHT_SWITCH; // Por defecto
        }
    }
    
    /**
     * Método auxiliar para setMessage (compatibilidad)
     */
    private void setMessage(String message) {
        _error.setValue(message);
    }
    
    /**
     * Método auxiliar para setLoading (compatibilidad)
     */
    private void setLoading(boolean loading) {
        _isLoading.setValue(loading);
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