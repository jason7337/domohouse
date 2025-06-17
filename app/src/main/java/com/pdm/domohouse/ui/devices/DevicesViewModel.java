package com.pdm.domohouse.ui.devices;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.pdm.domohouse.data.manager.RoomSystemManager;
import com.pdm.domohouse.data.model.Device;
import com.pdm.domohouse.data.model.Room;
import com.pdm.domohouse.data.repository.DeviceRepository;
import com.pdm.domohouse.data.repository.RoomRepository;

import java.util.List;
import java.util.Map;

/**
 * ViewModel para la página de dispositivos
 * Maneja el sistema de 3 habitaciones con Arduino
 */
public class DevicesViewModel extends AndroidViewModel {
    
    private final RoomSystemManager roomSystemManager;
    
    // LiveData del sistema de habitaciones
    private final LiveData<Boolean> esp32Connected;
    private final LiveData<String> esp32Status;
    private final LiveData<List<Room>> rooms;
    private final LiveData<Map<String, List<Device>>> roomDevices;
    
    // Estadísticas de dispositivos
    private final MutableLiveData<Integer> totalDevicesCount = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> activeDevicesCount = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> offlineDevicesCount = new MutableLiveData<>(0);
    
    // Estados de las habitaciones individuales
    private final MutableLiveData<RoomData> room1Data = new MutableLiveData<>();
    private final MutableLiveData<RoomData> room2Data = new MutableLiveData<>();
    private final MutableLiveData<RoomData> room3Data = new MutableLiveData<>();
    
    // Control de carga
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    
    public DevicesViewModel(@NonNull Application application) {
        super(application);
        
        // Inicializar repositorios (normalmente inyectados por DI)
        RoomRepository roomRepository = new RoomRepository(application);
        DeviceRepository deviceRepository = new DeviceRepository(application);
        
        // Inicializar el manager del sistema de habitaciones
        roomSystemManager = RoomSystemManager.getInstance(roomRepository, deviceRepository);
        
        // Obtener LiveData del manager
        esp32Connected = roomSystemManager.getESP32Connected();
        esp32Status = roomSystemManager.getESP32Status();
        rooms = roomSystemManager.getRooms();
        roomDevices = roomSystemManager.getRoomDevices();
        
        // Configurar observadores para actualizar estadísticas
        setupObservers();
        
        // Cargar datos iniciales
        refreshData();
    }
    
    /**
     * Configura los observadores para actualizar estadísticas automáticamente
     */
    private void setupObservers() {
        // Observar cambios en dispositivos para actualizar estadísticas
        roomDevices.observeForever(deviceMap -> {
            if (deviceMap != null) {
                updateDeviceStatistics(deviceMap);
                updateRoomData(deviceMap);
            }
        });
        
        // Observar cambios en habitaciones
        rooms.observeForever(roomList -> {
            if (roomList != null) {
                updateRoomData(roomDevices.getValue());
            }
        });
    }
    
    /**
     * Actualiza las estadísticas de dispositivos
     */
    private void updateDeviceStatistics(Map<String, List<Device>> deviceMap) {
        RoomSystemManager.DeviceStats stats = roomSystemManager.getDeviceStats();
        
        totalDevicesCount.setValue(stats.total);
        activeDevicesCount.setValue(stats.active);
        offlineDevicesCount.setValue(stats.offline);
    }
    
    /**
     * Actualiza los datos de las habitaciones individuales
     */
    private void updateRoomData(Map<String, List<Device>> deviceMap) {
        List<Room> roomList = rooms.getValue();
        
        if (roomList != null && deviceMap != null) {
            for (Room room : roomList) {
                List<Device> devices = deviceMap.get(room.getId());
                RoomData roomData = createRoomData(room, devices);
                
                switch (room.getId()) {
                    case "room_arduino_1":
                        room1Data.setValue(roomData);
                        break;
                    case "room_arduino_2":
                        room2Data.setValue(roomData);
                        break;
                    case "room_arduino_3":
                        room3Data.setValue(roomData);
                        break;
                }
            }
        }
    }
    
    /**
     * Crea un objeto RoomData con la información de la habitación
     */
    private RoomData createRoomData(Room room, List<Device> devices) {
        int deviceCount = devices != null ? devices.size() : 0;
        int activeCount = 0;
        boolean isOnline = true;
        
        if (devices != null) {
            for (Device device : devices) {
                if (device.isConnected() && device.isEnabled()) {
                    activeCount++;
                }
                if (!device.isConnected()) {
                    isOnline = false;
                }
            }
        }
        
        return new RoomData(
            room.getName(),
            room.getTemperature(),
            deviceCount,
            activeCount,
            isOnline ? "Activo" : "Offline",
            isOnline
        );
    }
    
    /**
     * Refresca todos los datos del sistema
     */
    public void refreshData() {
        isLoading.setValue(true);
        
        try {
            roomSystemManager.refreshAllData();
            roomSystemManager.updateESP32Status();
            
            // Simular delay de carga
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                isLoading.setValue(false);
            }, 1000);
            
        } catch (Exception e) {
            isLoading.setValue(false);
            errorMessage.setValue("Error al cargar datos: " + e.getMessage());
        }
    }
    
    /**
     * Navega a los detalles de una habitación específica
     */
    public void navigateToRoomDetails(String roomId) {
        // Implementar navegación a detalles de habitación
        // Por ahora, simular carga de datos específicos
        Room room = roomSystemManager.getRoomById(roomId);
        if (room != null) {
            // Aquí se implementaría la navegación real
            // navigationController.navigate(RoomDetailsFragment.newInstance(roomId));
        }
    }
    
    /**
     * Actualiza la temperatura de una habitación
     */
    public void updateRoomTemperature(String roomId, float temperature) {
        roomSystemManager.updateRoomTemperature(roomId, temperature);
    }
    
    /**
     * Controla un dispositivo específico
     */
    public void toggleDevice(String deviceId, boolean enabled) {
        // Implementar control de dispositivo
        // deviceRepository.updateDeviceState(deviceId, enabled, success -> {
        //     if (success) {
        //         refreshData();
        //     }
        // });
    }
    
    /**
     * Abre el panel de control general
     */
    public void openGeneralControl() {
        // Implementar navegación al control general
    }
    
    /**
     * Abre el sistema de programación
     */
    public void openScheduleSystem() {
        // Implementar navegación al sistema de programación
    }
    
    /**
     * Agrega un nuevo dispositivo
     */
    public void addNewDevice() {
        // Implementar navegación para agregar dispositivo
    }
    
    // Getters para LiveData
    public LiveData<Boolean> getESP32Connected() { return esp32Connected; }
    public LiveData<String> getESP32Status() { return esp32Status; }
    public LiveData<List<Room>> getRooms() { return rooms; }
    public LiveData<Map<String, List<Device>>> getRoomDevices() { return roomDevices; }
    
    public LiveData<Integer> getTotalDevicesCount() { return totalDevicesCount; }
    public LiveData<Integer> getActiveDevicesCount() { return activeDevicesCount; }
    public LiveData<Integer> getOfflineDevicesCount() { return offlineDevicesCount; }
    
    public LiveData<RoomData> getRoom1Data() { return room1Data; }
    public LiveData<RoomData> getRoom2Data() { return room2Data; }
    public LiveData<RoomData> getRoom3Data() { return room3Data; }
    
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    
    /**
     * Clase para encapsular datos de habitación
     */
    public static class RoomData {
        public final String name;
        public final float temperature;
        public final int deviceCount;
        public final int activeCount;
        public final String status;
        public final boolean isOnline;
        
        public RoomData(String name, float temperature, int deviceCount, 
                       int activeCount, String status, boolean isOnline) {
            this.name = name;
            this.temperature = temperature;
            this.deviceCount = deviceCount;
            this.activeCount = activeCount;
            this.status = status;
            this.isOnline = isOnline;
        }
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        // Limpiar recursos si es necesario
    }
}