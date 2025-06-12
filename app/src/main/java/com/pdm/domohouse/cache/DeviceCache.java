package com.pdm.domohouse.cache;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.pdm.domohouse.data.database.AppDatabase;
import com.pdm.domohouse.data.database.entity.DeviceEntity;
import com.pdm.domohouse.data.database.entity.DeviceHistoryEntity;
import com.pdm.domohouse.data.database.entity.RoomEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Cache inteligente para dispositivos
 * Gestiona la cache en memoria y coordina con la base de datos local
 */
public class DeviceCache {
    private static final String TAG = "DeviceCache";
    
    // Tiempo de vida del cache (15 minutos)
    private static final long CACHE_VALIDITY_DURATION = TimeUnit.MINUTES.toMillis(15);
    
    private static DeviceCache instance;
    private final AppDatabase database;
    private final Context context;
    
    // Cache en memoria
    private final Map<String, CachedDevice> deviceCache = new HashMap<>();
    private final Map<String, CachedRoom> roomCache = new HashMap<>();
    
    // LiveData para observar cambios
    private final MediatorLiveData<List<DeviceEntity>> allDevicesLiveData = new MediatorLiveData<>();
    private final Map<String, MediatorLiveData<DeviceEntity>> deviceLiveDataMap = new HashMap<>();
    
    // Estado del cache
    private long lastFullSync = 0;
    private boolean isCacheValid = false;
    
    /**
     * Clase interna para almacenar dispositivos con timestamp
     */
    private static class CachedDevice {
        DeviceEntity device;
        long timestamp;
        
        CachedDevice(DeviceEntity device) {
            this.device = device;
            this.timestamp = System.currentTimeMillis();
        }
        
        boolean isValid() {
            return System.currentTimeMillis() - timestamp < CACHE_VALIDITY_DURATION;
        }
    }
    
    /**
     * Clase interna para almacenar habitaciones con timestamp
     */
    private static class CachedRoom {
        RoomEntity room;
        long timestamp;
        
        CachedRoom(RoomEntity room) {
            this.room = room;
            this.timestamp = System.currentTimeMillis();
        }
        
        boolean isValid() {
            return System.currentTimeMillis() - timestamp < CACHE_VALIDITY_DURATION;
        }
    }
    
    private DeviceCache(Context context) {
        this.context = context.getApplicationContext();
        this.database = AppDatabase.getDatabase(this.context);
        
        // Observar cambios en la base de datos
        setupDatabaseObservers();
    }
    
    /**
     * Obtiene la instancia única del cache
     */
    public static synchronized DeviceCache getInstance(Context context) {
        if (instance == null) {
            instance = new DeviceCache(context);
        }
        return instance;
    }
    
    /**
     * Configura observadores para cambios en la base de datos
     */
    private void setupDatabaseObservers() {
        // Observar todos los dispositivos
        LiveData<List<DeviceEntity>> dbDevices = database.deviceDao().getAllDevices();
        allDevicesLiveData.addSource(dbDevices, devices -> {
            // Actualizar cache cuando cambien los datos en la BD
            updateCacheFromDatabase(devices);
            allDevicesLiveData.setValue(devices);
        });
    }
    
    /**
     * Actualiza el cache desde la base de datos
     */
    private void updateCacheFromDatabase(List<DeviceEntity> devices) {
        if (devices != null) {
            for (DeviceEntity device : devices) {
                deviceCache.put(device.getDeviceId(), new CachedDevice(device));
            }
            lastFullSync = System.currentTimeMillis();
            isCacheValid = true;
            Log.d(TAG, "Cache actualizado con " + devices.size() + " dispositivos");
        }
    }
    
    /**
     * Obtiene un dispositivo por ID (primero del cache, luego de la BD)
     */
    public LiveData<DeviceEntity> getDevice(String deviceId) {
        // Verificar si ya tenemos LiveData para este dispositivo
        MediatorLiveData<DeviceEntity> liveData = deviceLiveDataMap.get(deviceId);
        if (liveData == null) {
            liveData = new MediatorLiveData<>();
            
            // Primero intentar desde cache
            CachedDevice cached = deviceCache.get(deviceId);
            if (cached != null && cached.isValid()) {
                liveData.setValue(cached.device);
            }
            
            // Luego observar desde BD
            LiveData<DeviceEntity> dbDevice = database.deviceDao().getDevice(deviceId);
            MediatorLiveData<DeviceEntity> finalLiveData = liveData;
            liveData.addSource(dbDevice, device -> {
                if (device != null) {
                    deviceCache.put(deviceId, new CachedDevice(device));
                }
                finalLiveData.setValue(device);
            });
            
            deviceLiveDataMap.put(deviceId, liveData);
        }
        
        return liveData;
    }
    
    /**
     * Obtiene todos los dispositivos
     */
    public LiveData<List<DeviceEntity>> getAllDevices() {
        return allDevicesLiveData;
    }
    
    /**
     * Obtiene dispositivos por habitación
     */
    public LiveData<List<DeviceEntity>> getDevicesByRoom(String roomId) {
        return database.deviceDao().getDevicesByRoom(roomId);
    }
    
    /**
     * Actualiza el estado de un dispositivo (optimizado para respuesta rápida)
     */
    public void updateDeviceState(String deviceId, boolean isOn) {
        // Actualizar cache inmediatamente
        CachedDevice cached = deviceCache.get(deviceId);
        if (cached != null && cached.device != null) {
            cached.device.setOn(isOn);
            cached.device.setLastStateChange(System.currentTimeMillis());
            cached.device.setUpdatedAt(System.currentTimeMillis());
            cached.device.setSynced(false);
            cached.timestamp = System.currentTimeMillis();
        }
        
        // Actualizar BD en background
        AppDatabase.databaseWriteExecutor.execute(() -> {
            long timestamp = System.currentTimeMillis();
            database.deviceDao().updateDeviceState(deviceId, isOn, timestamp);
            
            // Registrar en historial
            DeviceHistoryEntity history = new DeviceHistoryEntity();
            history.setDeviceId(deviceId);
            history.setAction(isOn ? "ON" : "OFF");
            history.setOldValue(String.valueOf(!isOn));
            history.setNewValue(String.valueOf(isOn));
            history.setTriggeredBy("USER");
            database.deviceHistoryDao().insert(history);
        });
    }
    
    /**
     * Actualiza la intensidad de un dispositivo
     */
    public void updateDeviceIntensity(String deviceId, int intensity) {
        // Actualizar cache
        CachedDevice cached = deviceCache.get(deviceId);
        if (cached != null && cached.device != null) {
            int oldIntensity = cached.device.getIntensity();
            cached.device.setIntensity(intensity);
            cached.device.setUpdatedAt(System.currentTimeMillis());
            cached.device.setSynced(false);
            cached.timestamp = System.currentTimeMillis();
            
            // Si la intensidad es 0, apagar el dispositivo
            if (intensity == 0) {
                cached.device.setOn(false);
            } else if (oldIntensity == 0) {
                cached.device.setOn(true);
            }
        }
        
        // Actualizar BD
        AppDatabase.databaseWriteExecutor.execute(() -> {
            long timestamp = System.currentTimeMillis();
            database.deviceDao().updateDeviceIntensity(deviceId, intensity, timestamp);
            
            // Registrar en historial
            DeviceHistoryEntity history = new DeviceHistoryEntity();
            history.setDeviceId(deviceId);
            history.setAction("INTENSITY_CHANGE");
            history.setOldValue(String.valueOf(cached != null ? cached.device.getIntensity() : 0));
            history.setNewValue(String.valueOf(intensity));
            history.setTriggeredBy("USER");
            database.deviceHistoryDao().insert(history);
        });
    }
    
    /**
     * Actualiza la temperatura de un sensor
     */
    public void updateDeviceTemperature(String deviceId, float temperature) {
        // Actualizar cache
        CachedDevice cached = deviceCache.get(deviceId);
        if (cached != null && cached.device != null) {
            cached.device.setTemperature(temperature);
            cached.device.setUpdatedAt(System.currentTimeMillis());
            cached.timestamp = System.currentTimeMillis();
        }
        
        // Actualizar BD
        AppDatabase.databaseWriteExecutor.execute(() -> {
            long timestamp = System.currentTimeMillis();
            database.deviceDao().updateDeviceTemperature(deviceId, temperature, timestamp);
            
            // Registrar en historial
            DeviceHistoryEntity history = new DeviceHistoryEntity();
            history.setDeviceId(deviceId);
            history.setAction("TEMPERATURE_READING");
            history.setNewValue(String.valueOf(temperature));
            history.setTriggeredBy("SENSOR");
            database.deviceHistoryDao().insert(history);
        });
    }
    
    /**
     * Obtiene una habitación por ID
     */
    public RoomEntity getRoom(String roomId) {
        CachedRoom cached = roomCache.get(roomId);
        if (cached != null && cached.isValid()) {
            return cached.room;
        }
        
        // Si no está en cache, buscar en BD
        RoomEntity room = database.roomDao().getRoomSync(roomId);
        if (room != null) {
            roomCache.put(roomId, new CachedRoom(room));
        }
        return room;
    }
    
    /**
     * Invalida el cache completo
     */
    public void invalidateCache() {
        deviceCache.clear();
        roomCache.clear();
        isCacheValid = false;
        lastFullSync = 0;
        Log.d(TAG, "Cache invalidado");
    }
    
    /**
     * Invalida un dispositivo específico del cache
     */
    public void invalidateDevice(String deviceId) {
        deviceCache.remove(deviceId);
        Log.d(TAG, "Dispositivo " + deviceId + " removido del cache");
    }
    
    /**
     * Pre-carga dispositivos en el cache
     */
    public void preloadDevices() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<DeviceEntity> devices = database.deviceDao().getAllDevices().getValue();
            if (devices != null) {
                updateCacheFromDatabase(devices);
            }
            
            List<RoomEntity> rooms = database.roomDao().getAllRoomsSync();
            if (rooms != null) {
                for (RoomEntity room : rooms) {
                    roomCache.put(room.getRoomId(), new CachedRoom(room));
                }
            }
        });
    }
    
    /**
     * Obtiene estadísticas del cache
     */
    public CacheStats getCacheStats() {
        int validDevices = 0;
        int invalidDevices = 0;
        
        for (CachedDevice cached : deviceCache.values()) {
            if (cached.isValid()) {
                validDevices++;
            } else {
                invalidDevices++;
            }
        }
        
        return new CacheStats(
                deviceCache.size(),
                validDevices,
                invalidDevices,
                roomCache.size(),
                lastFullSync
        );
    }
    
    /**
     * Clase para estadísticas del cache
     */
    public static class CacheStats {
        public final int totalDevices;
        public final int validDevices;
        public final int invalidDevices;
        public final int totalRooms;
        public final long lastSync;
        
        CacheStats(int totalDevices, int validDevices, int invalidDevices, 
                   int totalRooms, long lastSync) {
            this.totalDevices = totalDevices;
            this.validDevices = validDevices;
            this.invalidDevices = invalidDevices;
            this.totalRooms = totalRooms;
            this.lastSync = lastSync;
        }
    }
}