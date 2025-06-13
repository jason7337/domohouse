package com.pdm.domohouse.data.cache;

import android.content.Context;
import android.util.Log;
import android.util.LruCache;

import androidx.annotation.NonNull;

import com.pdm.domohouse.data.model.Device;
import com.pdm.domohouse.data.model.Room;
import com.pdm.domohouse.data.model.UserPreferences;
import com.pdm.domohouse.data.model.UserProfile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Sistema de cache inteligente para optimizar rendimiento y reducir consultas
 * Implementa múltiples estrategias de cache con gestión automática de memoria
 */
public class IntelligentCacheManager {
    
    private static final String TAG = "IntelligentCacheManager";
    
    // Configuración de cache
    private static final int MAX_MEMORY_CACHE_SIZE = 1024 * 1024 * 4; // 4MB
    private static final long DEFAULT_CACHE_VALIDITY_MS = 15 * 60 * 1000; // 15 minutos
    private static final long DEVICE_STATE_CACHE_VALIDITY_MS = 5 * 60 * 1000; // 5 minutos para estados
    private static final long USER_DATA_CACHE_VALIDITY_MS = 60 * 60 * 1000; // 1 hora para datos de usuario
    private static final long CLEANUP_INTERVAL_MS = 5 * 60 * 1000; // Cleanup cada 5 minutos
    
    private static IntelligentCacheManager instance;
    private final Context context;
    
    // Caches especializados
    private final LruCache<String, CacheEntry<UserProfile>> userProfileCache;
    private final LruCache<String, CacheEntry<UserPreferences>> userPreferencesCache;
    private final LruCache<String, CacheEntry<List<Device>>> deviceListCache;
    private final LruCache<String, CacheEntry<Device>> deviceCache;
    private final LruCache<String, CacheEntry<List<Room>>> roomListCache;
    private final LruCache<String, CacheEntry<Room>> roomCache;
    
    // Cache de estado en tiempo real
    private final Map<String, DeviceStateEntry> deviceStateCache;
    private final Map<String, Long> lastAccessTimes;
    
    // Estadísticas de cache
    private final CacheStats stats;
    
    // Scheduler para limpieza automática
    private final ScheduledExecutorService cleanupScheduler;
    
    /**
     * Configuración de cache por tipo de dato
     */
    public enum CacheStrategy {
        MEMORY_ONLY,        // Solo en memoria
        PERSISTENT,         // Cache persistente
        VOLATILE,           // Cache volátil (se limpia frecuentemente)
        REALTIME            // Cache en tiempo real (alta frecuencia de actualización)
    }
    
    /**
     * Constructor privado para Singleton
     */
    private IntelligentCacheManager(Context context) {
        this.context = context.getApplicationContext();
        
        // Inicializar caches con tamaños calculados
        int cacheSize = MAX_MEMORY_CACHE_SIZE / 6; // Dividir memoria entre los caches
        
        this.userProfileCache = new LruCache<String, CacheEntry<UserProfile>>(cacheSize) {
            @Override
            protected int sizeOf(String key, CacheEntry<UserProfile> entry) {
                return estimateUserProfileSize(entry.getData());
            }
            
            @Override
            protected void entryRemoved(boolean evicted, String key, 
                    CacheEntry<UserProfile> oldValue, CacheEntry<UserProfile> newValue) {
                if (evicted) {
                    Log.d(TAG, "Perfil de usuario evicted del cache: " + key);
                    stats.incrementEvictions();
                }
            }
        };
        
        this.userPreferencesCache = new LruCache<String, CacheEntry<UserPreferences>>(cacheSize) {
            @Override
            protected int sizeOf(String key, CacheEntry<UserPreferences> entry) {
                return estimateUserPreferencesSize(entry.getData());
            }
        };
        
        this.deviceListCache = new LruCache<String, CacheEntry<List<Device>>>(cacheSize) {
            @Override
            protected int sizeOf(String key, CacheEntry<List<Device>> entry) {
                return estimateDeviceListSize(entry.getData());
            }
        };
        
        this.deviceCache = new LruCache<String, CacheEntry<Device>>(cacheSize) {
            @Override
            protected int sizeOf(String key, CacheEntry<Device> entry) {
                return estimateDeviceSize(entry.getData());
            }
        };
        
        this.roomListCache = new LruCache<String, CacheEntry<List<Room>>>(cacheSize);
        this.roomCache = new LruCache<String, CacheEntry<Room>>(cacheSize);
        
        // Cache de estado en tiempo real
        this.deviceStateCache = new ConcurrentHashMap<>();
        this.lastAccessTimes = new ConcurrentHashMap<>();
        
        // Estadísticas
        this.stats = new CacheStats();
        
        // Scheduler para limpieza
        this.cleanupScheduler = Executors.newSingleThreadScheduledExecutor();
        startCleanupScheduler();
        
        Log.d(TAG, "Cache inteligente inicializado con " + (MAX_MEMORY_CACHE_SIZE / 1024) + "KB de memoria");
    }
    
    /**
     * Obtiene la instancia única del cache manager
     */
    public static synchronized IntelligentCacheManager getInstance(Context context) {
        if (instance == null) {
            instance = new IntelligentCacheManager(context);
        }
        return instance;
    }
    
    // Métodos para UserProfile
    
    public void putUserProfile(@NonNull String userId, @NonNull UserProfile profile) {
        CacheEntry<UserProfile> entry = new CacheEntry<>(profile, USER_DATA_CACHE_VALIDITY_MS);
        userProfileCache.put(userId, entry);
        updateLastAccess(userId);
        stats.incrementWrites();
        Log.d(TAG, "Perfil de usuario cacheado: " + userId);
    }
    
    public UserProfile getUserProfile(@NonNull String userId) {
        updateLastAccess(userId);
        CacheEntry<UserProfile> entry = userProfileCache.get(userId);
        
        if (entry != null) {
            if (!entry.isExpired()) {
                stats.incrementHits();
                Log.d(TAG, "Cache hit para perfil de usuario: " + userId);
                return entry.getData();
            } else {
                userProfileCache.remove(userId);
                stats.incrementExpired();
                Log.d(TAG, "Cache expirado para perfil de usuario: " + userId);
            }
        }
        
        stats.incrementMisses();
        return null;
    }
    
    // Métodos para UserPreferences
    
    public void putUserPreferences(@NonNull String userId, @NonNull UserPreferences preferences) {
        CacheEntry<UserPreferences> entry = new CacheEntry<>(preferences, USER_DATA_CACHE_VALIDITY_MS);
        userPreferencesCache.put(userId, entry);
        updateLastAccess(userId);
        stats.incrementWrites();
        Log.d(TAG, "Preferencias de usuario cacheadas: " + userId);
    }
    
    public UserPreferences getUserPreferences(@NonNull String userId) {
        updateLastAccess(userId);
        CacheEntry<UserPreferences> entry = userPreferencesCache.get(userId);
        
        if (entry != null && !entry.isExpired()) {
            stats.incrementHits();
            return entry.getData();
        } else if (entry != null) {
            userPreferencesCache.remove(userId);
            stats.incrementExpired();
        }
        
        stats.incrementMisses();
        return null;
    }
    
    // Métodos para Devices
    
    public void putDeviceList(@NonNull String cacheKey, @NonNull List<Device> devices) {
        CacheEntry<List<Device>> entry = new CacheEntry<>(new ArrayList<>(devices), DEFAULT_CACHE_VALIDITY_MS);
        deviceListCache.put(cacheKey, entry);
        updateLastAccess(cacheKey);
        stats.incrementWrites();
        Log.d(TAG, "Lista de dispositivos cacheada: " + cacheKey + " (" + devices.size() + " dispositivos)");
    }
    
    public List<Device> getDeviceList(@NonNull String cacheKey) {
        updateLastAccess(cacheKey);
        CacheEntry<List<Device>> entry = deviceListCache.get(cacheKey);
        
        if (entry != null && !entry.isExpired()) {
            stats.incrementHits();
            return new ArrayList<>(entry.getData());
        } else if (entry != null) {
            deviceListCache.remove(cacheKey);
            stats.incrementExpired();
        }
        
        stats.incrementMisses();
        return null;
    }
    
    public void putDevice(@NonNull String deviceId, @NonNull Device device) {
        CacheEntry<Device> entry = new CacheEntry<>(device, DEFAULT_CACHE_VALIDITY_MS);
        deviceCache.put(deviceId, entry);
        updateLastAccess(deviceId);
        stats.incrementWrites();
        
        // También actualizar cache de estado en tiempo real
        updateDeviceState(deviceId, device.isOn(), (int)device.getIntensity(), device.getTemperature());
    }
    
    public Device getDevice(@NonNull String deviceId) {
        updateLastAccess(deviceId);
        CacheEntry<Device> entry = deviceCache.get(deviceId);
        
        if (entry != null && !entry.isExpired()) {
            stats.incrementHits();
            
            // Fusionar con estado en tiempo real si existe
            Device cachedDevice = entry.getData();
            DeviceStateEntry stateEntry = deviceStateCache.get(deviceId);
            if (stateEntry != null && !stateEntry.isExpired()) {
                cachedDevice.setOn(stateEntry.isOn);
                cachedDevice.setIntensity(stateEntry.intensity);
                if (stateEntry.temperature != null) {
                    cachedDevice.setTemperature(stateEntry.temperature);
                }
                cachedDevice.setLastStateChange(stateEntry.timestamp);
            }
            
            return cachedDevice;
        } else if (entry != null) {
            deviceCache.remove(deviceId);
            stats.incrementExpired();
        }
        
        stats.incrementMisses();
        return null;
    }
    
    // Métodos para estado de dispositivos en tiempo real
    
    public void updateDeviceState(@NonNull String deviceId, boolean isOn, int intensity, Float temperature) {
        DeviceStateEntry stateEntry = new DeviceStateEntry(isOn, intensity, temperature);
        deviceStateCache.put(deviceId, stateEntry);
        updateLastAccess(deviceId);
        
        // También actualizar el device cache si existe
        CacheEntry<Device> deviceEntry = deviceCache.get(deviceId);
        if (deviceEntry != null && !deviceEntry.isExpired()) {
            Device device = deviceEntry.getData();
            device.setOn(isOn);
            device.setIntensity(intensity);
            if (temperature != null) {
                device.setTemperature(temperature);
            }
            device.setLastStateChange(System.currentTimeMillis());
        }
        
        Log.d(TAG, "Estado de dispositivo actualizado en cache: " + deviceId);
    }
    
    public DeviceStateEntry getDeviceState(@NonNull String deviceId) {
        updateLastAccess(deviceId);
        DeviceStateEntry entry = deviceStateCache.get(deviceId);
        
        if (entry != null && !entry.isExpired()) {
            return entry;
        } else if (entry != null) {
            deviceStateCache.remove(deviceId);
        }
        
        return null;
    }
    
    // Métodos para Rooms
    
    public void putRoomList(@NonNull String cacheKey, @NonNull List<Room> rooms) {
        CacheEntry<List<Room>> entry = new CacheEntry<>(new ArrayList<>(rooms), DEFAULT_CACHE_VALIDITY_MS);
        roomListCache.put(cacheKey, entry);
        updateLastAccess(cacheKey);
        stats.incrementWrites();
        Log.d(TAG, "Lista de habitaciones cacheada: " + cacheKey + " (" + rooms.size() + " habitaciones)");
    }
    
    public List<Room> getRoomList(@NonNull String cacheKey) {
        updateLastAccess(cacheKey);
        CacheEntry<List<Room>> entry = roomListCache.get(cacheKey);
        
        if (entry != null && !entry.isExpired()) {
            stats.incrementHits();
            return new ArrayList<>(entry.getData());
        } else if (entry != null) {
            roomListCache.remove(cacheKey);
            stats.incrementExpired();
        }
        
        stats.incrementMisses();
        return null;
    }
    
    public void putRoom(@NonNull String roomId, @NonNull Room room) {
        CacheEntry<Room> entry = new CacheEntry<>(room, DEFAULT_CACHE_VALIDITY_MS);
        roomCache.put(roomId, entry);
        updateLastAccess(roomId);
        stats.incrementWrites();
    }
    
    public Room getRoom(@NonNull String roomId) {
        updateLastAccess(roomId);
        CacheEntry<Room> entry = roomCache.get(roomId);
        
        if (entry != null && !entry.isExpired()) {
            stats.incrementHits();
            return entry.getData();
        } else if (entry != null) {
            roomCache.remove(roomId);
            stats.incrementExpired();
        }
        
        stats.incrementMisses();
        return null;
    }
    
    // Métodos de gestión de cache
    
    /**
     * Invalida el cache de un usuario específico
     */
    public void invalidateUserCache(@NonNull String userId) {
        userProfileCache.remove(userId);
        userPreferencesCache.remove(userId);
        lastAccessTimes.remove(userId);
        Log.d(TAG, "Cache de usuario invalidado: " + userId);
    }
    
    /**
     * Invalida el cache de un dispositivo específico
     */
    public void invalidateDeviceCache(@NonNull String deviceId) {
        deviceCache.remove(deviceId);
        deviceStateCache.remove(deviceId);
        lastAccessTimes.remove(deviceId);
        
        // También invalida listas que puedan contener este dispositivo
        invalidateDeviceListCaches();
        
        Log.d(TAG, "Cache de dispositivo invalidado: " + deviceId);
    }
    
    /**
     * Invalida todas las listas de dispositivos
     */
    public void invalidateDeviceListCaches() {
        deviceListCache.evictAll();
        Log.d(TAG, "Cache de listas de dispositivos invalidado");
    }
    
    /**
     * Invalida el cache de una habitación específica
     */
    public void invalidateRoomCache(@NonNull String roomId) {
        roomCache.remove(roomId);
        lastAccessTimes.remove(roomId);
        
        // También invalida listas que puedan contener esta habitación
        roomListCache.evictAll();
        
        Log.d(TAG, "Cache de habitación invalidado: " + roomId);
    }
    
    /**
     * Limpia todo el cache
     */
    public void clearAllCache() {
        userProfileCache.evictAll();
        userPreferencesCache.evictAll();
        deviceListCache.evictAll();
        deviceCache.evictAll();
        roomListCache.evictAll();
        roomCache.evictAll();
        deviceStateCache.clear();
        lastAccessTimes.clear();
        
        stats.reset();
        Log.d(TAG, "Todo el cache limpiado");
    }
    
    /**
     * Obtiene estadísticas del cache
     */
    public CacheStats getStats() {
        return stats.copy();
    }
    
    /**
     * Obtiene información de memoria del cache
     */
    public CacheMemoryInfo getMemoryInfo() {
        int userProfileSize = userProfileCache.size();
        int userPreferencesSize = userPreferencesCache.size();
        int deviceListSize = deviceListCache.size();
        int deviceSize = deviceCache.size();
        int roomListSize = roomListCache.size();
        int roomSize = roomCache.size();
        int deviceStateSize = deviceStateCache.size();
        
        return new CacheMemoryInfo(userProfileSize, userPreferencesSize, deviceListSize, 
            deviceSize, roomListSize, roomSize, deviceStateSize);
    }
    
    // Métodos privados
    
    private void updateLastAccess(@NonNull String key) {
        lastAccessTimes.put(key, System.currentTimeMillis());
    }
    
    private void startCleanupScheduler() {
        cleanupScheduler.scheduleAtFixedRate(this::performCleanup, 
            CLEANUP_INTERVAL_MS, CLEANUP_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }
    
    private void performCleanup() {
        try {
            long currentTime = System.currentTimeMillis();
            
            // Limpiar entradas expiradas en cache de estado
            deviceStateCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
            
            // Limpiar tiempos de acceso antiguos
            lastAccessTimes.entrySet().removeIf(entry -> 
                currentTime - entry.getValue() > 24 * 60 * 60 * 1000); // 24 horas
            
            Log.d(TAG, "Cleanup automático completado");
            
        } catch (Exception e) {
            Log.e(TAG, "Error en cleanup automático", e);
        }
    }
    
    // Métodos de estimación de tamaño
    
    private int estimateUserProfileSize(UserProfile profile) {
        if (profile == null) return 0;
        int size = 100; // Base size
        if (profile.getName() != null) size += profile.getName().length() * 2;
        if (profile.getEmail() != null) size += profile.getEmail().length() * 2;
        if (profile.getPhotoUrl() != null) size += profile.getPhotoUrl().length() * 2;
        return size;
    }
    
    private int estimateUserPreferencesSize(UserPreferences preferences) {
        return preferences != null ? 200 : 0; // Estimated size
    }
    
    private int estimateDeviceListSize(List<Device> devices) {
        if (devices == null) return 0;
        return devices.size() * 150; // Estimated size per device
    }
    
    private int estimateDeviceSize(Device device) {
        if (device == null) return 0;
        int size = 100; // Base size
        if (device.getName() != null) size += device.getName().length() * 2;
        return size;
    }
    
    /**
     * Detiene el cache manager y libera recursos
     */
    public void shutdown() {
        if (cleanupScheduler != null && !cleanupScheduler.isShutdown()) {
            cleanupScheduler.shutdown();
        }
        clearAllCache();
        Log.d(TAG, "Cache manager detenido");
    }
    
    // Clases auxiliares
    
    /**
     * Entrada de cache con timestamp y validez
     */
    private static class CacheEntry<T> {
        private final T data;
        private final long timestamp;
        private final long validityDuration;
        
        public CacheEntry(T data, long validityDuration) {
            this.data = data;
            this.timestamp = System.currentTimeMillis();
            this.validityDuration = validityDuration;
        }
        
        public T getData() { return data; }
        
        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > validityDuration;
        }
    }
    
    /**
     * Entrada específica para estado de dispositivos
     */
    public static class DeviceStateEntry {
        public final boolean isOn;
        public final int intensity;
        public final Float temperature;
        public final long timestamp;
        
        public DeviceStateEntry(boolean isOn, int intensity, Float temperature) {
            this.isOn = isOn;
            this.intensity = intensity;
            this.temperature = temperature;
            this.timestamp = System.currentTimeMillis();
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > DEVICE_STATE_CACHE_VALIDITY_MS;
        }
    }
    
    /**
     * Estadísticas de cache
     */
    public static class CacheStats {
        private long hits = 0;
        private long misses = 0;
        private long writes = 0;
        private long evictions = 0;
        private long expired = 0;
        
        public synchronized void incrementHits() { hits++; }
        public synchronized void incrementMisses() { misses++; }
        public synchronized void incrementWrites() { writes++; }
        public synchronized void incrementEvictions() { evictions++; }
        public synchronized void incrementExpired() { expired++; }
        
        public synchronized long getHits() { return hits; }
        public synchronized long getMisses() { return misses; }
        public synchronized long getWrites() { return writes; }
        public synchronized long getEvictions() { return evictions; }
        public synchronized long getExpired() { return expired; }
        
        public synchronized double getHitRatio() {
            long total = hits + misses;
            return total > 0 ? (double) hits / total : 0.0;
        }
        
        public synchronized void reset() {
            hits = misses = writes = evictions = expired = 0;
        }
        
        public synchronized CacheStats copy() {
            CacheStats copy = new CacheStats();
            copy.hits = this.hits;
            copy.misses = this.misses;
            copy.writes = this.writes;
            copy.evictions = this.evictions;
            copy.expired = this.expired;
            return copy;
        }
    }
    
    /**
     * Información de memoria del cache
     */
    public static class CacheMemoryInfo {
        public final int userProfileEntries;
        public final int userPreferencesEntries;
        public final int deviceListEntries;
        public final int deviceEntries;
        public final int roomListEntries;
        public final int roomEntries;
        public final int deviceStateEntries;
        
        public CacheMemoryInfo(int userProfileEntries, int userPreferencesEntries, 
                int deviceListEntries, int deviceEntries, int roomListEntries, 
                int roomEntries, int deviceStateEntries) {
            this.userProfileEntries = userProfileEntries;
            this.userPreferencesEntries = userPreferencesEntries;
            this.deviceListEntries = deviceListEntries;
            this.deviceEntries = deviceEntries;
            this.roomListEntries = roomListEntries;
            this.roomEntries = roomEntries;
            this.deviceStateEntries = deviceStateEntries;
        }
        
        public int getTotalEntries() {
            return userProfileEntries + userPreferencesEntries + deviceListEntries + 
                   deviceEntries + roomListEntries + roomEntries + deviceStateEntries;
        }
    }
}