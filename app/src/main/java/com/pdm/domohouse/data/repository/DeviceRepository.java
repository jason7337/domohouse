package com.pdm.domohouse.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.pdm.domohouse.data.database.AppDatabase;
import com.pdm.domohouse.data.database.dao.DeviceDao;
import com.pdm.domohouse.data.database.dao.DeviceHistoryDao;
import com.pdm.domohouse.data.database.entity.DeviceEntity;
import com.pdm.domohouse.data.database.entity.DeviceHistoryEntity;
import com.pdm.domohouse.data.model.Device;
import com.pdm.domohouse.data.model.DeviceType;
import com.pdm.domohouse.data.sync.ComprehensiveSyncManager;
import com.pdm.domohouse.utils.DeviceMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repositorio para manejo de dispositivos IoT
 * Coordina entre datos locales (Room) y remotos (Firebase)
 */
public class DeviceRepository {
    
    private static final String TAG = "DeviceRepository";
    
    private final DeviceDao deviceDao;
    private final DeviceHistoryDao deviceHistoryDao;
    private final ComprehensiveSyncManager syncManager;
    private final ExecutorService executor;
    
    public DeviceRepository(Context context) {
        AppDatabase database = AppDatabase.getDatabase(context);
        this.deviceDao = database.deviceDao();
        this.deviceHistoryDao = database.deviceHistoryDao();
        this.syncManager = ComprehensiveSyncManager.getInstance(context);
        this.executor = Executors.newSingleThreadExecutor();
    }
    
    /**
     * Obtiene todos los dispositivos
     */
    public LiveData<List<Device>> getAllDevices() {
        MediatorLiveData<List<Device>> result = new MediatorLiveData<>();
        
        LiveData<List<DeviceEntity>> localDevices = deviceDao.getAllDevices();
        result.addSource(localDevices, entities -> {
            if (entities != null) {
                List<Device> devices = mapEntitiesToModels(entities);
                result.setValue(devices);
                
                // Sincronizar en background si hay dispositivos no sincronizados
                syncUnsyncedDevicesInBackground();
            }
        });
        
        return result;
    }
    
    /**
     * Obtiene dispositivos por habitación
     */
    public LiveData<List<Device>> getDevicesByRoom(@NonNull String roomId) {
        MediatorLiveData<List<Device>> result = new MediatorLiveData<>();
        
        LiveData<List<DeviceEntity>> localDevices = deviceDao.getDevicesByRoom(roomId);
        result.addSource(localDevices, entities -> {
            if (entities != null) {
                List<Device> devices = mapEntitiesToModels(entities);
                result.setValue(devices);
            }
        });
        
        return result;
    }
    
    /**
     * Obtiene un dispositivo específico
     */
    public LiveData<Device> getDevice(@NonNull String deviceId) {
        MediatorLiveData<Device> result = new MediatorLiveData<>();
        
        LiveData<DeviceEntity> localDevice = deviceDao.getDevice(deviceId);
        result.addSource(localDevice, entity -> {
            if (entity != null) {
                Device device = mapEntityToModel(entity);
                result.setValue(device);
            }
        });
        
        return result;
    }
    
    /**
     * Obtiene dispositivos activos (encendidos)
     */
    public LiveData<List<Device>> getActiveDevices() {
        MediatorLiveData<List<Device>> result = new MediatorLiveData<>();
        
        LiveData<List<DeviceEntity>> activeDevices = deviceDao.getActiveDevices();
        result.addSource(activeDevices, entities -> {
            if (entities != null) {
                List<Device> devices = mapEntitiesToModels(entities);
                result.setValue(devices);
            }
        });
        
        return result;
    }
    
    /**
     * Obtiene dispositivos offline
     */
    public LiveData<List<Device>> getOfflineDevices() {
        MediatorLiveData<List<Device>> result = new MediatorLiveData<>();
        
        LiveData<List<DeviceEntity>> offlineDevices = deviceDao.getOfflineDevices();
        result.addSource(offlineDevices, entities -> {
            if (entities != null) {
                List<Device> devices = mapEntitiesToModels(entities);
                result.setValue(devices);
            }
        });
        
        return result;
    }
    
    /**
     * Agrega un nuevo dispositivo
     */
    public CompletableFuture<Boolean> addDevice(@NonNull Device device) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                DeviceEntity entity = mapModelToEntity(device);
                entity.setCreatedAt(System.currentTimeMillis());
                entity.setUpdatedAt(System.currentTimeMillis());
                entity.setSynced(false);
                
                deviceDao.insert(entity);
                
                // Registrar en historial
                logDeviceAction(device.getId(), "DEVICE_ADDED", "", "CREATED", "USER");
                
                // Sincronizar con Firebase si hay conexión
                if (syncManager.isOnline()) {
                    syncDeviceToFirebase(device);
                }
                
                Log.d(TAG, "Dispositivo agregado: " + device.getName());
                return true;
                
            } catch (Exception e) {
                Log.e(TAG, "Error al agregar dispositivo", e);
                return false;
            }
        }, executor);
    }
    
    /**
     * Actualiza un dispositivo
     */
    public CompletableFuture<Boolean> updateDevice(@NonNull Device device) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                DeviceEntity entity = mapModelToEntity(device);
                entity.setUpdatedAt(System.currentTimeMillis());
                entity.setSynced(false);
                
                deviceDao.update(entity);
                
                // Sincronizar con Firebase si hay conexión
                if (syncManager.isOnline()) {
                    syncDeviceToFirebase(device);
                }
                
                Log.d(TAG, "Dispositivo actualizado: " + device.getName());
                return true;
                
            } catch (Exception e) {
                Log.e(TAG, "Error al actualizar dispositivo", e);
                return false;
            }
        }, executor);
    }
    
    /**
     * Cambia el estado de un dispositivo (encendido/apagado)
     */
    public CompletableFuture<Boolean> toggleDeviceState(@NonNull String deviceId, @NonNull String userId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                DeviceEntity entity = deviceDao.getDeviceSync(deviceId);
                if (entity == null) {
                    Log.w(TAG, "Dispositivo no encontrado: " + deviceId);
                    return false;
                }
                
                boolean newState = !entity.isOn();
                long timestamp = System.currentTimeMillis();
                
                deviceDao.updateDeviceState(deviceId, newState, timestamp);
                
                // Registrar en historial
                logDeviceAction(deviceId, newState ? "ON" : "OFF", 
                    String.valueOf(!newState), String.valueOf(newState), "USER", userId);
                
                Log.d(TAG, "Estado del dispositivo cambiado: " + deviceId + " -> " + newState);
                return true;
                
            } catch (Exception e) {
                Log.e(TAG, "Error al cambiar estado del dispositivo", e);
                return false;
            }
        }, executor);
    }
    
    /**
     * Actualiza la intensidad de un dispositivo
     */
    public CompletableFuture<Boolean> updateDeviceIntensity(@NonNull String deviceId, int intensity, @NonNull String userId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (intensity < 0 || intensity > 100) {
                    Log.w(TAG, "Intensidad inválida: " + intensity);
                    return false;
                }
                
                DeviceEntity entity = deviceDao.getDeviceSync(deviceId);
                if (entity == null) {
                    Log.w(TAG, "Dispositivo no encontrado: " + deviceId);
                    return false;
                }
                
                int oldIntensity = entity.getIntensity();
                long timestamp = System.currentTimeMillis();
                
                deviceDao.updateDeviceIntensity(deviceId, intensity, timestamp);
                
                // Registrar en historial
                logDeviceAction(deviceId, "INTENSITY_CHANGE", 
                    String.valueOf(oldIntensity), String.valueOf(intensity), "USER", userId);
                
                Log.d(TAG, "Intensidad del dispositivo actualizada: " + deviceId + " -> " + intensity);
                return true;
                
            } catch (Exception e) {
                Log.e(TAG, "Error al actualizar intensidad del dispositivo", e);
                return false;
            }
        }, executor);
    }
    
    /**
     * Actualiza la temperatura de un sensor
     */
    public CompletableFuture<Boolean> updateDeviceTemperature(@NonNull String deviceId, float temperature) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                DeviceEntity entity = deviceDao.getDeviceSync(deviceId);
                if (entity == null) {
                    Log.w(TAG, "Dispositivo no encontrado: " + deviceId);
                    return false;
                }
                
                Float oldTemperature = entity.getTemperature();
                long timestamp = System.currentTimeMillis();
                
                deviceDao.updateDeviceTemperature(deviceId, temperature, timestamp);
                
                // Registrar en historial solo si hay cambio significativo
                if (oldTemperature == null || Math.abs(temperature - oldTemperature) >= 0.5f) {
                    logDeviceAction(deviceId, "TEMPERATURE_READING", 
                        oldTemperature != null ? String.valueOf(oldTemperature) : "null", 
                        String.valueOf(temperature), "SENSOR");
                }
                
                return true;
                
            } catch (Exception e) {
                Log.e(TAG, "Error al actualizar temperatura del dispositivo", e);
                return false;
            }
        }, executor);
    }
    
    /**
     * Actualiza el estado online de un dispositivo
     */
    public CompletableFuture<Boolean> updateDeviceOnlineStatus(@NonNull String deviceId, boolean isOnline) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                long timestamp = System.currentTimeMillis();
                deviceDao.updateDeviceOnlineStatus(deviceId, isOnline, timestamp);
                
                // Registrar en historial
                logDeviceAction(deviceId, isOnline ? "DEVICE_ONLINE" : "DEVICE_OFFLINE", 
                    String.valueOf(!isOnline), String.valueOf(isOnline), "SYSTEM");
                
                Log.d(TAG, "Estado online del dispositivo actualizado: " + deviceId + " -> " + isOnline);
                return true;
                
            } catch (Exception e) {
                Log.e(TAG, "Error al actualizar estado online del dispositivo", e);
                return false;
            }
        }, executor);
    }
    
    /**
     * Elimina un dispositivo
     */
    public CompletableFuture<Boolean> removeDevice(@NonNull String deviceId, @NonNull String userId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                DeviceEntity entity = deviceDao.getDeviceSync(deviceId);
                if (entity == null) {
                    Log.w(TAG, "Dispositivo no encontrado: " + deviceId);
                    return false;
                }
                
                // Registrar en historial antes de eliminar
                logDeviceAction(deviceId, "DEVICE_REMOVED", "ACTIVE", "REMOVED", "USER", userId);
                
                deviceDao.delete(entity);
                
                Log.d(TAG, "Dispositivo eliminado: " + deviceId);
                return true;
                
            } catch (Exception e) {
                Log.e(TAG, "Error al eliminar dispositivo", e);
                return false;
            }
        }, executor);
    }
    
    /**
     * Obtiene el conteo de dispositivos activos
     */
    public LiveData<Integer> getActiveDeviceCount() {
        return deviceDao.getActiveDeviceCount();
    }
    
    /**
     * Sincroniza dispositivos no sincronizados en background
     */
    private void syncUnsyncedDevicesInBackground() {
        if (!syncManager.isOnline()) {
            return;
        }
        
        executor.execute(() -> {
            try {
                List<DeviceEntity> unsyncedDevices = deviceDao.getUnsyncedDevices();
                
                for (DeviceEntity entity : unsyncedDevices) {
                    Device device = mapEntityToModel(entity);
                    syncDeviceToFirebase(device);
                }
                
                if (!unsyncedDevices.isEmpty()) {
                    Log.d(TAG, "Sincronizados " + unsyncedDevices.size() + " dispositivos pendientes");
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error en sincronización background de dispositivos", e);
            }
        });
    }
    
    /**
     * Sincroniza un dispositivo con Firebase
     */
    private void syncDeviceToFirebase(@NonNull Device device) {
        // TODO: Implementar sincronización con Firebase
        // Por ahora marcar como sincronizado
        executor.execute(() -> {
            long timestamp = System.currentTimeMillis();
            deviceDao.markAsSynced(device.getId(), timestamp);
            Log.d(TAG, "Dispositivo marcado como sincronizado: " + device.getId());
        });
    }
    
    /**
     * Registra una acción del dispositivo en el historial
     */
    private void logDeviceAction(@NonNull String deviceId, @NonNull String action, 
            String oldValue, String newValue, @NonNull String triggeredBy) {
        logDeviceAction(deviceId, action, oldValue, newValue, triggeredBy, null);
    }
    
    /**
     * Registra una acción del dispositivo en el historial con usuario
     */
    private void logDeviceAction(@NonNull String deviceId, @NonNull String action, 
            String oldValue, String newValue, @NonNull String triggeredBy, String userId) {
        
        executor.execute(() -> {
            try {
                DeviceHistoryEntity historyEntity = new DeviceHistoryEntity();
                historyEntity.setDeviceId(deviceId);
                historyEntity.setAction(action);
                historyEntity.setOldValue(oldValue);
                historyEntity.setNewValue(newValue);
                historyEntity.setTriggeredBy(triggeredBy);
                historyEntity.setUserId(userId);
                historyEntity.setTimestamp(System.currentTimeMillis());
                historyEntity.setSynced(false);
                
                deviceHistoryDao.insert(historyEntity);
                
            } catch (Exception e) {
                Log.e(TAG, "Error al registrar acción en historial", e);
            }
        });
    }
    
    // Métodos de mapeo entre entidades y modelos
    
    private List<Device> mapEntitiesToModels(@NonNull List<DeviceEntity> entities) {
        List<Device> devices = new ArrayList<>();
        for (DeviceEntity entity : entities) {
            devices.add(mapEntityToModel(entity));
        }
        return devices;
    }
    
    private Device mapEntityToModel(@NonNull DeviceEntity entity) {
        Device device = new Device(entity.getDeviceId(), entity.getName(), 
                                   DeviceType.valueOf(entity.getDeviceType()), entity.getRoomId());
        device.setEnabled(entity.isOn());
        device.setCurrentValue(entity.getIntensity());
        if (entity.getTemperature() != null) {
            device.setCurrentValue(entity.getTemperature());
        }
        device.setConnected(entity.isOnline());
        device.setLastUpdated(entity.getLastStateChange());
        return device;
    }
    
    private DeviceEntity mapModelToEntity(@NonNull Device device) {
        DeviceEntity entity = new DeviceEntity();
        entity.setDeviceId(device.getId());
        entity.setRoomId(device.getRoomId());
        entity.setName(device.getName());
        entity.setDeviceType(device.getType().name());
        entity.setOn(device.isEnabled());
        entity.setIntensity((int) device.getCurrentValue());
        if (device.getType().name().contains("TEMPERATURE")) {
            entity.setTemperature(device.getCurrentValue());
        }
        entity.setOnline(device.isConnected());
        entity.setLastStateChange(device.getLastUpdated());
        entity.setUpdatedAt(device.getLastUpdated());
        return entity;
    }
    
    /**
     * Limpia recursos
     */
    public void cleanup() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}