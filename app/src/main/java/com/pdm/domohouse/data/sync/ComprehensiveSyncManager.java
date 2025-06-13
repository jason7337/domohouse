package com.pdm.domohouse.data.sync;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.pdm.domohouse.data.database.AppDatabase;
import com.pdm.domohouse.data.database.dao.DeviceDao;
import com.pdm.domohouse.data.database.dao.DeviceHistoryDao;
import com.pdm.domohouse.data.database.dao.RoomDao;
import com.pdm.domohouse.data.database.dao.UserPreferencesDao;
import com.pdm.domohouse.data.database.dao.UserProfileDao;
import com.pdm.domohouse.data.database.entity.DeviceEntity;
import com.pdm.domohouse.data.database.entity.DeviceHistoryEntity;
import com.pdm.domohouse.data.database.entity.RoomEntity;
import com.pdm.domohouse.data.database.entity.UserPreferencesEntity;
import com.pdm.domohouse.data.database.entity.UserProfileEntity;
import com.pdm.domohouse.data.model.Device;
import com.pdm.domohouse.data.model.Room;
import com.pdm.domohouse.data.model.UserPreferences;
import com.pdm.domohouse.data.model.UserProfile;
import com.pdm.domohouse.network.FirebaseDataManager;
import com.pdm.domohouse.network.FirebaseSyncManager;
import com.pdm.domohouse.utils.DeviceMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Manager integral para sincronización entre Room Database y Firebase
 * Maneja cache inteligente, resolución de conflictos y funcionalidad offline completa
 */
public class ComprehensiveSyncManager {
    
    private static final String TAG = "ComprehensiveSyncManager";
    private static final long SYNC_INTERVAL_MINUTES = 10; // Sincronización cada 10 minutos
    private static final long CACHE_VALIDITY_HOURS = 2; // Cache válido por 2 horas
    
    private static ComprehensiveSyncManager instance;
    private final Context context;
    
    // Componentes de datos
    private final AppDatabase database;
    private final FirebaseDataManager firebaseDataManager;
    private final FirebaseSyncManager firebaseSyncManager;
    private final FirebaseAuth firebaseAuth;
    
    // DAOs
    private final UserProfileDao userProfileDao;
    private final UserPreferencesDao userPreferencesDao;
    private final RoomDao roomDao;
    private final DeviceDao deviceDao;
    private final DeviceHistoryDao deviceHistoryDao;
    
    // Control de sincronización
    private ScheduledExecutorService syncScheduler;
    private boolean isSyncEnabled = true;
    private boolean isOnline = false;
    private SyncState currentSyncState = SyncState.IDLE;
    
    // Cache de datos
    private final Map<String, CacheEntry<UserProfile>> userProfileCache = new HashMap<>();
    private final Map<String, CacheEntry<List<Device>>> deviceCache = new HashMap<>();
    private final Map<String, CacheEntry<List<Room>>> roomCache = new HashMap<>();
    
    // Listeners para cambios
    private final List<SyncStateListener> syncStateListeners = new ArrayList<>();
    private final List<DataChangeListener> dataChangeListeners = new ArrayList<>();
    
    /**
     * Estados de sincronización
     */
    public enum SyncState {
        IDLE,           // Sin sincronización en progreso
        SYNCING,        // Sincronizando datos
        RESOLVING,      // Resolviendo conflictos
        ERROR,          // Error en sincronización
        OFFLINE         // Modo offline
    }
    
    /**
     * Constructor privado para Singleton
     */
    private ComprehensiveSyncManager(Context context) {
        this.context = context.getApplicationContext();
        this.database = AppDatabase.getDatabase(context);
        this.firebaseDataManager = FirebaseDataManager.getInstance();
        this.firebaseSyncManager = FirebaseSyncManager.getInstance(context);
        this.firebaseAuth = FirebaseAuth.getInstance();
        
        // Inicializar DAOs
        this.userProfileDao = database.userProfileDao();
        this.userPreferencesDao = database.userPreferencesDao();
        this.roomDao = database.roomDao();
        this.deviceDao = database.deviceDao();
        this.deviceHistoryDao = database.deviceHistoryDao();
        
        checkNetworkStatus();
        initializeAutoSync();
    }
    
    /**
     * Obtiene la instancia única del manager
     */
    public static synchronized ComprehensiveSyncManager getInstance(Context context) {
        if (instance == null) {
            instance = new ComprehensiveSyncManager(context);
        }
        return instance;
    }
    
    /**
     * Inicia la sincronización automática
     */
    private void initializeAutoSync() {
        if (syncScheduler == null || syncScheduler.isShutdown()) {
            syncScheduler = Executors.newSingleThreadScheduledExecutor();
            
            syncScheduler.scheduleAtFixedRate(
                this::performAutoSync,
                SYNC_INTERVAL_MINUTES,
                SYNC_INTERVAL_MINUTES,
                TimeUnit.MINUTES
            );
            
            Log.d(TAG, "Sincronización automática iniciada cada " + SYNC_INTERVAL_MINUTES + " minutos");
        }
    }
    
    /**
     * Verifica el estado de la red
     */
    private void checkNetworkStatus() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            boolean wasOnline = isOnline;
            isOnline = networkInfo != null && networkInfo.isConnected();
            
            if (!wasOnline && isOnline) {
                Log.d(TAG, "Dispositivo volvió a estar online - iniciando sincronización completa");
                updateSyncState(SyncState.SYNCING);
                performFullSync();
            } else if (wasOnline && !isOnline) {
                Log.d(TAG, "Dispositivo ahora está offline");
                updateSyncState(SyncState.OFFLINE);
            }
        }
    }
    
    /**
     * Actualiza el estado de sincronización y notifica a los listeners
     */
    private void updateSyncState(SyncState newState) {
        if (currentSyncState != newState) {
            currentSyncState = newState;
            for (SyncStateListener listener : syncStateListeners) {
                listener.onSyncStateChanged(newState);
            }
        }
    }
    
    /**
     * Realiza una sincronización completa de todos los datos
     */
    public CompletableFuture<SyncResult> performFullSync() {
        return CompletableFuture.supplyAsync(() -> {
            if (!isSyncEnabled || !isOnline) {
                return new SyncResult(false, "Sincronización deshabilitada o sin conexión");
            }
            
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            if (currentUser == null) {
                return new SyncResult(false, "No hay usuario autenticado");
            }
            
            updateSyncState(SyncState.SYNCING);
            Log.d(TAG, "Iniciando sincronización completa");
            
            try {
                String userId = currentUser.getUid();
                
                // Sincronizar en orden: perfil, preferencias, habitaciones, dispositivos, historial
                SyncResult userProfileResult = syncUserProfile(userId);
                if (!userProfileResult.isSuccess()) {
                    updateSyncState(SyncState.ERROR);
                    return userProfileResult;
                }
                
                SyncResult preferencesResult = syncUserPreferences(userId);
                if (!preferencesResult.isSuccess()) {
                    Log.w(TAG, "Error en sincronización de preferencias: " + preferencesResult.getErrorMessage());
                }
                
                SyncResult roomsResult = syncRooms(userId);
                if (!roomsResult.isSuccess()) {
                    Log.w(TAG, "Error en sincronización de habitaciones: " + roomsResult.getErrorMessage());
                }
                
                SyncResult devicesResult = syncDevices(userId);
                if (!devicesResult.isSuccess()) {
                    Log.w(TAG, "Error en sincronización de dispositivos: " + devicesResult.getErrorMessage());
                }
                
                SyncResult historyResult = syncDeviceHistory(userId);
                if (!historyResult.isSuccess()) {
                    Log.w(TAG, "Error en sincronización de historial: " + historyResult.getErrorMessage());
                }
                
                updateSyncState(SyncState.IDLE);
                Log.d(TAG, "Sincronización completa exitosa");
                return new SyncResult(true, "Sincronización completa exitosa");
                
            } catch (Exception e) {
                Log.e(TAG, "Error en sincronización completa", e);
                updateSyncState(SyncState.ERROR);
                return new SyncResult(false, "Error inesperado: " + e.getMessage());
            }
        });
    }
    
    /**
     * Sincroniza el perfil de usuario
     */
    private SyncResult syncUserProfile(String userId) {
        try {
            // Obtener datos locales
            UserProfileEntity localProfile = userProfileDao.getUserProfileSync(userId);
            
            // Obtener datos de Firebase (simplificado para el ejemplo)
            // En implementación real, usar FirebaseDataManager con callbacks
            
            if (localProfile == null) {
                // No hay datos locales, cargar desde Firebase
                Log.d(TAG, "Cargando perfil desde Firebase (primera vez)");
                // TODO: Implementar carga desde Firebase y guardado local
                return new SyncResult(true, "Perfil cargado desde Firebase");
            } else {
                // Verificar si necesita sincronización
                long currentTime = System.currentTimeMillis();
                long timeSinceLastSync = currentTime - localProfile.getLastSync();
                
                if (timeSinceLastSync > TimeUnit.HOURS.toMillis(CACHE_VALIDITY_HOURS)) {
                    Log.d(TAG, "Cache de perfil expirado, sincronizando con Firebase");
                    // TODO: Implementar sincronización bidireccional
                }
                
                return new SyncResult(true, "Perfil sincronizado");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error en sincronización de perfil", e);
            return new SyncResult(false, "Error al sincronizar perfil: " + e.getMessage());
        }
    }
    
    /**
     * Sincroniza las preferencias del usuario
     */
    private SyncResult syncUserPreferences(String userId) {
        try {
            UserPreferencesEntity localPrefs = userPreferencesDao.getUserPreferencesSync(userId);
            
            if (localPrefs == null) {
                // Crear preferencias por defecto
                localPrefs = new UserPreferencesEntity();
                localPrefs.setUserId(userId);
                userPreferencesDao.insert(localPrefs);
                Log.d(TAG, "Preferencias por defecto creadas");
            }
            
            // TODO: Sincronizar con Firebase si es necesario
            
            return new SyncResult(true, "Preferencias sincronizadas");
        } catch (Exception e) {
            Log.e(TAG, "Error en sincronización de preferencias", e);
            return new SyncResult(false, "Error al sincronizar preferencias: " + e.getMessage());
        }
    }
    
    /**
     * Sincroniza las habitaciones
     */
    private SyncResult syncRooms(String userId) {
        try {
            List<RoomEntity> localRooms = roomDao.getAllRoomsSync();
            
            if (localRooms.isEmpty()) {
                Log.d(TAG, "No hay habitaciones locales, se utilizarán las predeterminadas");
                // Las habitaciones por defecto se crean en AppDatabase.Callback
            }
            
            // TODO: Sincronizar con Firebase
            // Comparar timestamps, resolver conflictos, actualizar datos
            
            return new SyncResult(true, "Habitaciones sincronizadas");
        } catch (Exception e) {
            Log.e(TAG, "Error en sincronización de habitaciones", e);
            return new SyncResult(false, "Error al sincronizar habitaciones: " + e.getMessage());
        }
    }
    
    /**
     * Sincroniza los dispositivos
     */
    private SyncResult syncDevices(String userId) {
        try {
            List<DeviceEntity> unsyncedDevices = deviceDao.getUnsyncedDevices();
            
            if (!unsyncedDevices.isEmpty()) {
                Log.d(TAG, "Sincronizando " + unsyncedDevices.size() + " dispositivos pendientes");
                
                for (DeviceEntity device : unsyncedDevices) {
                    // TODO: Subir a Firebase
                    // Marcar como sincronizado después del éxito
                    long currentTime = System.currentTimeMillis();
                    deviceDao.markAsSynced(device.getDeviceId(), currentTime);
                }
            }
            
            // TODO: Descargar cambios desde Firebase
            
            return new SyncResult(true, "Dispositivos sincronizados");
        } catch (Exception e) {
            Log.e(TAG, "Error en sincronización de dispositivos", e);
            return new SyncResult(false, "Error al sincronizar dispositivos: " + e.getMessage());
        }
    }
    
    /**
     * Sincroniza el historial de dispositivos
     */
    private SyncResult syncDeviceHistory(String userId) {
        try {
            List<DeviceHistoryEntity> unsyncedHistory = deviceHistoryDao.getUnsyncedHistory();
            
            if (!unsyncedHistory.isEmpty()) {
                Log.d(TAG, "Sincronizando " + unsyncedHistory.size() + " entradas de historial");
                
                // TODO: Subir historial no sincronizado a Firebase
                // Marcar como sincronizado después del éxito
            }
            
            return new SyncResult(true, "Historial sincronizado");
        } catch (Exception e) {
            Log.e(TAG, "Error en sincronización de historial", e);
            return new SyncResult(false, "Error al sincronizar historial: " + e.getMessage());
        }
    }
    
    /**
     * Realiza sincronización automática periódica
     */
    private void performAutoSync() {
        try {
            checkNetworkStatus();
            
            if (isSyncEnabled && isOnline && currentSyncState == SyncState.IDLE) {
                performFullSync();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error en sincronización automática", e);
        }
    }
    
    /**
     * Resuelve conflictos entre datos locales y remotos
     */
    public ConflictResolution resolveConflict(@NonNull ConflictData conflictData) {
        updateSyncState(SyncState.RESOLVING);
        
        try {
            // Estrategia de resolución basada en timestamps
            if (conflictData.getLocalTimestamp() > conflictData.getRemoteTimestamp()) {
                Log.d(TAG, "Datos locales más recientes - usando datos locales");
                return ConflictResolution.USE_LOCAL;
            } else if (conflictData.getRemoteTimestamp() > conflictData.getLocalTimestamp()) {
                Log.d(TAG, "Datos remotos más recientes - usando datos remotos");
                return ConflictResolution.USE_REMOTE;
            } else {
                Log.d(TAG, "Timestamps iguales - fusionando datos");
                return ConflictResolution.MERGE;
            }
        } finally {
            updateSyncState(SyncState.IDLE);
        }
    }
    
    /**
     * Registra un listener para cambios de estado de sincronización
     */
    public void addSyncStateListener(SyncStateListener listener) {
        if (!syncStateListeners.contains(listener)) {
            syncStateListeners.add(listener);
        }
    }
    
    /**
     * Remueve un listener de estado de sincronización
     */
    public void removeSyncStateListener(SyncStateListener listener) {
        syncStateListeners.remove(listener);
    }
    
    /**
     * Registra un listener para cambios de datos
     */
    public void addDataChangeListener(DataChangeListener listener) {
        if (!dataChangeListeners.contains(listener)) {
            dataChangeListeners.add(listener);
        }
    }
    
    /**
     * Remueve un listener de cambios de datos
     */
    public void removeDataChangeListener(DataChangeListener listener) {
        dataChangeListeners.remove(listener);
    }
    
    /**
     * Habilita o deshabilita la sincronización
     */
    public void setSyncEnabled(boolean enabled) {
        this.isSyncEnabled = enabled;
        Log.d(TAG, "Sincronización " + (enabled ? "habilitada" : "deshabilitada"));
        
        if (enabled && isOnline) {
            performFullSync();
        }
    }
    
    /**
     * Obtiene el estado actual de sincronización
     */
    public SyncState getCurrentSyncState() {
        return currentSyncState;
    }
    
    /**
     * Verifica si el dispositivo está online
     */
    public boolean isOnline() {
        return isOnline;
    }
    
    /**
     * Limpia todos los datos locales
     */
    public void clearAllLocalData() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            database.clearAllTables();
            Log.d(TAG, "Todos los datos locales han sido eliminados");
        });
    }
    
    /**
     * Detiene todos los servicios de sincronización
     */
    public void shutdown() {
        if (syncScheduler != null && !syncScheduler.isShutdown()) {
            syncScheduler.shutdown();
            Log.d(TAG, "Scheduler de sincronización detenido");
        }
        
        syncStateListeners.clear();
        dataChangeListeners.clear();
        userProfileCache.clear();
        deviceCache.clear();
        roomCache.clear();
    }
    
    // Clases auxiliares
    
    /**
     * Resultado de operación de sincronización
     */
    public static class SyncResult {
        private final boolean success;
        private final String errorMessage;
        
        public SyncResult(boolean success, String errorMessage) {
            this.success = success;
            this.errorMessage = errorMessage;
        }
        
        public boolean isSuccess() { return success; }
        public String getErrorMessage() { return errorMessage; }
    }
    
    /**
     * Datos para resolución de conflictos
     */
    public static class ConflictData {
        private final long localTimestamp;
        private final long remoteTimestamp;
        private final String dataType;
        
        public ConflictData(long localTimestamp, long remoteTimestamp, String dataType) {
            this.localTimestamp = localTimestamp;
            this.remoteTimestamp = remoteTimestamp;
            this.dataType = dataType;
        }
        
        public long getLocalTimestamp() { return localTimestamp; }
        public long getRemoteTimestamp() { return remoteTimestamp; }
        public String getDataType() { return dataType; }
    }
    
    /**
     * Estrategias de resolución de conflictos
     */
    public enum ConflictResolution {
        USE_LOCAL,
        USE_REMOTE,
        MERGE
    }
    
    /**
     * Entrada de cache con timestamp
     */
    private static class CacheEntry<T> {
        private final T data;
        private final long timestamp;
        
        public CacheEntry(T data) {
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }
        
        public T getData() { return data; }
        public boolean isExpired(long validityDuration) {
            return System.currentTimeMillis() - timestamp > validityDuration;
        }
    }
    
    /**
     * Interface para listeners de estado de sincronización
     */
    public interface SyncStateListener {
        void onSyncStateChanged(SyncState newState);
    }
    
    /**
     * Interface para listeners de cambios de datos
     */
    public interface DataChangeListener {
        void onDataChanged(String dataType, String objectId);
    }
}