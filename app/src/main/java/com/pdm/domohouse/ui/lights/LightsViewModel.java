package com.pdm.domohouse.ui.lights;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.pdm.domohouse.ui.base.BaseAndroidViewModel;
import com.pdm.domohouse.data.model.Device;
import com.pdm.domohouse.data.model.DeviceType;
import com.pdm.domohouse.data.model.Room;
import com.pdm.domohouse.network.FirebaseDataManager;
import com.pdm.domohouse.ui.lights.model.LightStats;
import com.pdm.domohouse.ui.lights.model.RoomWithLights;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ViewModel para la gestión de control de iluminación
 * Maneja la lógica de dispositivos de luz por habitación
 */
public class LightsViewModel extends BaseAndroidViewModel {

    // LiveData para la lista de habitaciones con dispositivos de luz
    private final MutableLiveData<List<RoomWithLights>> roomsWithLights = new MutableLiveData<>();
    
    // LiveData para las estadísticas de luces
    private final MutableLiveData<LightStats> lightsStats = new MutableLiveData<>();
    
    // LiveData para el estado de carga
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    
    // LiveData para mensajes de error
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    // Datos para el funcionamiento
    private List<Room> allRooms = new ArrayList<>();
    private List<Device> allDevices = new ArrayList<>();
    
    // Firebase Data Manager
    private final FirebaseDataManager firebaseDataManager;

    public LightsViewModel(@NonNull Application application) {
        super(application);
        firebaseDataManager = FirebaseDataManager.getInstance();
        initializeSimulatedData(); // Por ahora usamos datos simulados hasta que se integre completamente Firebase
    }

    // Getters para LiveData
    public LiveData<List<RoomWithLights>> getRoomsWithLights() { return roomsWithLights; }
    public LiveData<LightStats> getLightsStats() { return lightsStats; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    /**
     * Carga las habitaciones con sus dispositivos de iluminación
     */
    public void loadRoomsWithLights() {
        isLoading.setValue(true);
        
        try {
            // Simular carga de datos
            List<RoomWithLights> roomsWithLightsList = new ArrayList<>();
            
            for (Room room : allRooms) {
                List<Device> lightDevices = getLightDevicesForRoom(room.getId());
                if (!lightDevices.isEmpty()) {
                    roomsWithLightsList.add(new RoomWithLights(room, lightDevices));
                }
            }
            
            roomsWithLights.setValue(roomsWithLightsList);
            updateLightStats();
            
        } catch (Exception e) {
            errorMessage.setValue("Error al cargar las luces: " + e.getMessage());
        } finally {
            isLoading.setValue(false);
        }
    }

    /**
     * Actualiza las estadísticas de iluminación
     */
    private void updateLightStats() {
        int totalLights = 0;
        int lightsOn = 0;
        int lightsOff = 0;

        for (Device device : allDevices) {
            if (isLightDevice(device.getType())) {
                totalLights++;
                if (device.isEnabled() && device.getCurrentValue() > 0) {
                    lightsOn++;
                } else {
                    lightsOff++;
                }
            }
        }

        lightsStats.setValue(new LightStats(totalLights, lightsOn, lightsOff));
    }

    /**
     * Controla el estado de un dispositivo de luz específico
     */
    public void toggleLight(String deviceId) {
        Device device = findDeviceById(deviceId);
        if (device != null && isLightDevice(device.getType())) {
            boolean newState = !device.isEnabled();
            device.setEnabled(newState);
            
            // Si es un interruptor simple, ajustar el valor
            if (device.getType() == DeviceType.LIGHT_SWITCH) {
                device.setCurrentValue(newState ? 1 : 0);
            }
            
            updateLightStats();
            refreshRoomsData();
        }
    }

    /**
     * Controla la intensidad de una luz regulable
     */
    public void setLightIntensity(String deviceId, float intensity) {
        Device device = findDeviceById(deviceId);
        if (device != null && isLightDevice(device.getType()) && 
            device.getType().hasRange()) {
            
            // Ajustar la intensidad entre el rango permitido
            float adjustedIntensity = Math.max(device.getMinValue(), 
                                             Math.min(device.getMaxValue(), intensity));
            
            device.setCurrentValue(adjustedIntensity);
            device.setEnabled(adjustedIntensity > 0);
            
            updateLightStats();
            refreshRoomsData();
        }
    }

    /**
     * Enciende todas las luces de la casa
     */
    public void turnOnAllLights() {
        for (Device device : allDevices) {
            if (isLightDevice(device.getType())) {
                device.setEnabled(true);
                
                if (device.getType() == DeviceType.LIGHT_SWITCH) {
                    device.setCurrentValue(1);
                } else if (device.getType().hasRange()) {
                    device.setCurrentValue(device.getMaxValue());
                }
            }
        }
        
        updateLightStats();
        refreshRoomsData();
    }

    /**
     * Apaga todas las luces de la casa
     */
    public void turnOffAllLights() {
        for (Device device : allDevices) {
            if (isLightDevice(device.getType())) {
                device.setEnabled(false);
                device.setCurrentValue(0);
            }
        }
        
        updateLightStats();
        refreshRoomsData();
    }

    /**
     * Enciende todas las luces de una habitación específica
     */
    public void turnOnRoomLights(String roomId) {
        List<Device> roomLights = getLightDevicesForRoom(roomId);
        
        for (Device device : roomLights) {
            device.setEnabled(true);
            
            if (device.getType() == DeviceType.LIGHT_SWITCH) {
                device.setCurrentValue(1);
            } else if (device.getType().hasRange()) {
                device.setCurrentValue(device.getMaxValue());
            }
        }
        
        updateLightStats();
        refreshRoomsData();
    }

    /**
     * Apaga todas las luces de una habitación específica
     */
    public void turnOffRoomLights(String roomId) {
        List<Device> roomLights = getLightDevicesForRoom(roomId);
        
        for (Device device : roomLights) {
            device.setEnabled(false);
            device.setCurrentValue(0);
        }
        
        updateLightStats();
        refreshRoomsData();
    }

    /**
     * Refresca los datos de las habitaciones
     */
    public void refreshLights() {
        loadRoomsWithLights();
    }

    /**
     * Obtiene los dispositivos de luz para una habitación específica
     */
    private List<Device> getLightDevicesForRoom(String roomId) {
        List<Device> lightDevices = new ArrayList<>();
        
        for (Device device : allDevices) {
            if (device.getRoomId().equals(roomId) && isLightDevice(device.getType())) {
                lightDevices.add(device);
            }
        }
        
        return lightDevices;
    }

    /**
     * Verifica si un tipo de dispositivo es de iluminación
     */
    private boolean isLightDevice(DeviceType type) {
        return type == DeviceType.LIGHT_SWITCH || 
               type == DeviceType.LIGHT_DIMMER || 
               type == DeviceType.RGB_LIGHT;
    }

    /**
     * Busca un dispositivo por su ID
     */
    private Device findDeviceById(String deviceId) {
        for (Device device : allDevices) {
            if (device.getId().equals(deviceId)) {
                return device;
            }
        }
        return null;
    }

    /**
     * Actualiza los datos mostrados en la interfaz
     */
    private void refreshRoomsData() {
        List<RoomWithLights> currentRooms = new ArrayList<>();
        
        for (Room room : allRooms) {
            List<Device> lightDevices = getLightDevicesForRoom(room.getId());
            if (!lightDevices.isEmpty()) {
                currentRooms.add(new RoomWithLights(room, lightDevices));
            }
        }
        
        roomsWithLights.setValue(currentRooms);
    }

    /**
     * Inicializa datos simulados para el desarrollo
     */
    private void initializeSimulatedData() {
        // Crear habitaciones de ejemplo
        allRooms.add(new Room("room_1", "Sala de Estar", 
                             com.pdm.domohouse.data.model.RoomType.LIVING_ROOM));
        allRooms.add(new Room("room_2", "Cocina", 
                             com.pdm.domohouse.data.model.RoomType.KITCHEN));
        allRooms.add(new Room("room_3", "Dormitorio Principal", 
                             com.pdm.domohouse.data.model.RoomType.BEDROOM));
        allRooms.add(new Room("room_4", "Baño", 
                             com.pdm.domohouse.data.model.RoomType.BATHROOM));

        // Crear dispositivos de luz de ejemplo
        allDevices.add(new Device("light_1", "Lámpara Principal", 
                                DeviceType.LIGHT_DIMMER, "room_1"));
        allDevices.add(new Device("light_2", "Luz Ambiente", 
                                DeviceType.RGB_LIGHT, "room_1"));
        allDevices.add(new Device("light_3", "Lámpara de Cocina", 
                                DeviceType.LIGHT_SWITCH, "room_2"));
        allDevices.add(new Device("light_4", "Luz de Techo", 
                                DeviceType.LIGHT_DIMMER, "room_3"));
        allDevices.add(new Device("light_5", "Luz del Baño", 
                                DeviceType.LIGHT_SWITCH, "room_4"));

        // Configurar algunos valores iniciales
        allDevices.get(0).setEnabled(true);
        allDevices.get(0).setCurrentValue(75);
        allDevices.get(2).setEnabled(true);
        allDevices.get(2).setCurrentValue(1);

        // Marcar todos como conectados
        for (Device device : allDevices) {
            device.setConnected(true);
        }
    }
}