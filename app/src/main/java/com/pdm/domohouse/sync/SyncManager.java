package com.pdm.domohouse.sync;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pdm.domohouse.data.database.AppDatabase;
import com.pdm.domohouse.data.database.entity.DeviceEntity;
import com.pdm.domohouse.data.database.entity.DeviceHistoryEntity;
import com.pdm.domohouse.data.database.entity.RoomEntity;
import com.pdm.domohouse.data.database.entity.UserPreferencesEntity;
import com.pdm.domohouse.data.database.entity.UserProfileEntity;
import com.pdm.domohouse.data.model.UserPreferences;
import com.pdm.domohouse.data.model.UserProfile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Gestor central de sincronización entre base de datos local y Firebase
 * Maneja conflictos, sincronización automática y manual
 */
public class SyncManager {
    private static final String TAG = "SyncManager";
    
    private static SyncManager instance;
    private final Context context;
    private final AppDatabase database;
    private final FirebaseDatabase firebaseDatabase;
    private final FirebaseAuth firebaseAuth;
    private final ExecutorService executorService;
    
    private final MutableLiveData<SyncStatus> syncStatus = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isOnline = new MutableLiveData<>();
    
    // Estados de sincronización
    public enum SyncStatus {
        IDLE,
        SYNCING,
        SUCCESS,
        ERROR,
        CONFLICT
    }
    
    // Estrategia de resolución de conflictos
    public enum ConflictResolution {
        LOCAL_WINS,      // Los datos locales prevalecen
        REMOTE_WINS,     // Los datos remotos prevalecen
        NEWEST_WINS,     // El más reciente prevalece
        MERGE            // Intenta fusionar cambios
    }
    
    private ConflictResolution defaultStrategy = ConflictResolution.NEWEST_WINS;
    
    private SyncManager(Context context) {
        this.context = context.getApplicationContext();
        this.database = AppDatabase.getDatabase(this.context);
        this.firebaseDatabase = FirebaseDatabase.getInstance();
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.executorService = Executors.newSingleThreadExecutor();
        
        // Monitorear cambios de conectividad
        monitorConnectivity();
    }
    
    /**
     * Obtiene la instancia única del SyncManager
     */
    public static synchronized SyncManager getInstance(Context context) {
        if (instance == null) {
            instance = new SyncManager(context);
        }
        return instance;
    }
    
    /**
     * Monitorea el estado de conectividad
     */
    private void monitorConnectivity() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean connected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        isOnline.setValue(connected);
        
        if (connected) {
            // Sincronizar automáticamente al recuperar conexión
            syncAll();
        }
    }
    
    /**
     * Verifica si hay conexión a internet
     */
    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
    
    /**
     * Sincroniza todos los datos
     */
    public void syncAll() {
        if (!isOnline() || firebaseAuth.getCurrentUser() == null) {
            syncStatus.setValue(SyncStatus.ERROR);
            return;
        }
        
        syncStatus.setValue(SyncStatus.SYNCING);
        executorService.execute(() -> {
            try {
                syncUserProfile();
                syncUserPreferences();
                syncRooms();
                syncDevices();
                syncDeviceHistory();
                
                syncStatus.postValue(SyncStatus.SUCCESS);
            } catch (Exception e) {
                Log.e(TAG, "Error durante sincronización", e);
                syncStatus.postValue(SyncStatus.ERROR);
            }
        });
    }
    
    /**
     * Sincroniza el perfil del usuario
     */
    private void syncUserProfile() {
        String userId = firebaseAuth.getCurrentUser().getUid();
        UserProfileEntity localProfile = database.userProfileDao().getUserProfileSync(userId);
        DatabaseReference profileRef = firebaseDatabase.getReference("users").child(userId).child("profile");
        
        profileRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    UserProfile remoteProfile = snapshot.getValue(UserProfile.class);
                    
                    if (localProfile == null) {
                        // No hay datos locales, usar remotos
                        UserProfileEntity newProfile = convertToEntity(remoteProfile);
                        newProfile.setSynced(true);
                        newProfile.setLastSync(System.currentTimeMillis());
                        database.userProfileDao().insert(newProfile);
                    } else {
                        // Resolver conflictos
                        UserProfileEntity resolvedProfile = resolveProfileConflict(localProfile, remoteProfile);
                        database.userProfileDao().update(resolvedProfile);
                        
                        // Actualizar en Firebase si es necesario
                        if (!localProfile.isSynced()) {
                            profileRef.setValue(convertToModel(resolvedProfile));
                        }
                    }
                } else if (localProfile != null && !localProfile.isSynced()) {
                    // Datos locales sin sincronizar, subir a Firebase
                    profileRef.setValue(convertToModel(localProfile));
                    database.userProfileDao().markAsSynced(userId, System.currentTimeMillis());
                }
            }
            
            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Error sincronizando perfil", error.toException());
            }
        });
    }
    
    /**
     * Sincroniza las preferencias del usuario
     */
    private void syncUserPreferences() {
        String userId = firebaseAuth.getCurrentUser().getUid();
        UserPreferencesEntity localPrefs = database.userPreferencesDao().getUserPreferencesSync(userId);
        DatabaseReference prefsRef = firebaseDatabase.getReference("users").child(userId).child("preferences");
        
        prefsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    UserPreferences remotePrefs = snapshot.getValue(UserPreferences.class);
                    
                    if (localPrefs == null) {
                        UserPreferencesEntity newPrefs = convertToEntity(remotePrefs);
                        newPrefs.setUserId(userId);
                        newPrefs.setSynced(true);
                        newPrefs.setLastSync(System.currentTimeMillis());
                        database.userPreferencesDao().insert(newPrefs);
                    } else {
                        UserPreferencesEntity resolvedPrefs = resolvePreferencesConflict(localPrefs, remotePrefs);
                        database.userPreferencesDao().update(resolvedPrefs);
                        
                        if (!localPrefs.isSynced()) {
                            prefsRef.setValue(convertToModel(resolvedPrefs));
                        }
                    }
                } else if (localPrefs != null && !localPrefs.isSynced()) {
                    prefsRef.setValue(convertToModel(localPrefs));
                    database.userPreferencesDao().markAsSynced(userId, System.currentTimeMillis());
                }
            }
            
            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Error sincronizando preferencias", error.toException());
            }
        });
    }
    
    /**
     * Sincroniza las habitaciones
     */
    private void syncRooms() {
        List<RoomEntity> unsyncedRooms = database.roomDao().getUnsyncedRooms();
        DatabaseReference roomsRef = firebaseDatabase.getReference("rooms");
        
        // Subir habitaciones no sincronizadas
        for (RoomEntity room : unsyncedRooms) {
            roomsRef.child(room.getRoomId()).setValue(convertToMap(room))
                    .addOnSuccessListener(aVoid -> {
                        database.roomDao().markAsSynced(room.getRoomId(), System.currentTimeMillis());
                    });
        }
        
        // Descargar habitaciones de Firebase
        roomsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<RoomEntity> remoteRooms = new ArrayList<>();
                for (DataSnapshot roomSnapshot : snapshot.getChildren()) {
                    RoomEntity room = roomSnapshot.getValue(RoomEntity.class);
                    if (room != null) {
                        room.setSynced(true);
                        room.setLastSync(System.currentTimeMillis());
                        remoteRooms.add(room);
                    }
                }
                
                if (!remoteRooms.isEmpty()) {
                    database.roomDao().insertAll(remoteRooms);
                }
            }
            
            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Error sincronizando habitaciones", error.toException());
            }
        });
    }
    
    /**
     * Sincroniza los dispositivos
     */
    private void syncDevices() {
        List<DeviceEntity> unsyncedDevices = database.deviceDao().getUnsyncedDevices();
        DatabaseReference devicesRef = firebaseDatabase.getReference("devices");
        
        // Subir dispositivos no sincronizados
        for (DeviceEntity device : unsyncedDevices) {
            devicesRef.child(device.getDeviceId()).setValue(convertToMap(device))
                    .addOnSuccessListener(aVoid -> {
                        database.deviceDao().markAsSynced(device.getDeviceId(), System.currentTimeMillis());
                    });
        }
        
        // Descargar dispositivos de Firebase
        devicesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<DeviceEntity> remoteDevices = new ArrayList<>();
                for (DataSnapshot deviceSnapshot : snapshot.getChildren()) {
                    DeviceEntity device = deviceSnapshot.getValue(DeviceEntity.class);
                    if (device != null) {
                        DeviceEntity localDevice = database.deviceDao().getDeviceSync(device.getDeviceId());
                        
                        if (localDevice != null) {
                            // Resolver conflictos
                            DeviceEntity resolved = resolveDeviceConflict(localDevice, device);
                            database.deviceDao().update(resolved);
                        } else {
                            device.setSynced(true);
                            device.setLastSync(System.currentTimeMillis());
                            remoteDevices.add(device);
                        }
                    }
                }
                
                if (!remoteDevices.isEmpty()) {
                    database.deviceDao().insertAll(remoteDevices);
                }
            }
            
            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Error sincronizando dispositivos", error.toException());
            }
        });
    }
    
    /**
     * Sincroniza el historial de dispositivos
     */
    private void syncDeviceHistory() {
        List<DeviceHistoryEntity> unsyncedHistory = database.deviceHistoryDao().getUnsyncedHistory();
        DatabaseReference historyRef = firebaseDatabase.getReference("device_history");
        
        List<Long> syncedIds = new ArrayList<>();
        
        for (DeviceHistoryEntity history : unsyncedHistory) {
            String key = historyRef.push().getKey();
            if (key != null) {
                historyRef.child(key).setValue(convertToMap(history))
                        .addOnSuccessListener(aVoid -> {
                            syncedIds.add(history.getHistoryId());
                            if (syncedIds.size() == unsyncedHistory.size()) {
                                database.deviceHistoryDao().markAsSynced(syncedIds, System.currentTimeMillis());
                            }
                        });
            }
        }
    }
    
    /**
     * Resuelve conflictos en el perfil según la estrategia configurada
     */
    private UserProfileEntity resolveProfileConflict(UserProfileEntity local, UserProfile remote) {
        switch (defaultStrategy) {
            case LOCAL_WINS:
                return local;
                
            case REMOTE_WINS:
                UserProfileEntity remoteEntityWins = convertToEntity(remote);
                remoteEntityWins.setUserId(local.getUserId());
                return remoteEntityWins;
                
            case NEWEST_WINS:
            default:
                if (local.getUpdatedAt() > remote.getUpdatedAt()) {
                    return local;
                } else {
                    UserProfileEntity remoteEntityNewest = convertToEntity(remote);
                    remoteEntityNewest.setUserId(local.getUserId());
                    return remoteEntityNewest;
                }
        }
    }
    
    /**
     * Resuelve conflictos en preferencias
     */
    private UserPreferencesEntity resolvePreferencesConflict(UserPreferencesEntity local, UserPreferences remote) {
        if (local.getUpdatedAt() > remote.getUpdatedAt()) {
            return local;
        } else {
            UserPreferencesEntity remoteEntity = convertToEntity(remote);
            remoteEntity.setUserId(local.getUserId());
            return remoteEntity;
        }
    }
    
    /**
     * Resuelve conflictos en dispositivos
     */
    private DeviceEntity resolveDeviceConflict(DeviceEntity local, DeviceEntity remote) {
        // Para dispositivos, el estado más reciente prevalece
        if (local.getLastStateChange() > remote.getLastStateChange()) {
            return local;
        } else {
            remote.setSynced(true);
            remote.setLastSync(System.currentTimeMillis());
            return remote;
        }
    }
    
    // Métodos de conversión entre entidades y modelos
    private UserProfileEntity convertToEntity(UserProfile model) {
        UserProfileEntity entity = new UserProfileEntity();
        entity.setUserId(model.getUserId());
        entity.setName(model.getName());
        entity.setEmail(model.getEmail());
        entity.setPhotoUrl(model.getPhotoUrl());
        entity.setPinHash(model.getPinHash());
        entity.setCreatedAt(model.getCreatedAt());
        entity.setUpdatedAt(model.getUpdatedAt());
        return entity;
    }
    
    private UserProfile convertToModel(UserProfileEntity entity) {
        UserProfile model = new UserProfile();
        model.setUserId(entity.getUserId());
        model.setName(entity.getName());
        model.setEmail(entity.getEmail());
        model.setPhotoUrl(entity.getPhotoUrl());
        model.setPinHash(entity.getPinHash());
        model.setCreatedAt(entity.getCreatedAt());
        model.setUpdatedAt(entity.getUpdatedAt());
        return model;
    }
    
    private UserPreferencesEntity convertToEntity(UserPreferences model) {
        UserPreferencesEntity entity = new UserPreferencesEntity();
        entity.setLanguage(model.getLanguage());
        entity.setNotificationsEnabled(model.isNotificationsEnabled());
        entity.setMotionAlerts(model.isMotionAlerts());
        entity.setTemperatureAlerts(model.isTemperatureAlerts());
        entity.setSmokeAlerts(model.isSmokeAlerts());
        entity.setSecurityAlerts(model.isSecurityAlerts());
        entity.setAutoLights(model.isAutoLights());
        entity.setAutoTemperature(model.isAutoTemperature());
        entity.setEcoMode(model.isEcoMode());
        entity.setTemperatureUnit(model.getTemperatureUnit());
        entity.setDefaultLightIntensity(model.getDefaultLightIntensity());
        entity.setTemperatureThresholdMin(model.getTemperatureThresholdMin());
        entity.setTemperatureThresholdMax(model.getTemperatureThresholdMax());
        entity.setNightModeStart(model.getNightModeStart());
        entity.setNightModeEnd(model.getNightModeEnd());
        entity.setCreatedAt(model.getCreatedAt());
        entity.setUpdatedAt(model.getUpdatedAt());
        return entity;
    }
    
    private UserPreferences convertToModel(UserPreferencesEntity entity) {
        UserPreferences model = new UserPreferences();
        model.setUserId(entity.getUserId());
        model.setLanguage(entity.getLanguage());
        model.setNotificationsEnabled(entity.isNotificationsEnabled());
        model.setMotionAlerts(entity.isMotionAlerts());
        model.setTemperatureAlerts(entity.isTemperatureAlerts());
        model.setSmokeAlerts(entity.isSmokeAlerts());
        model.setSecurityAlerts(entity.isSecurityAlerts());
        model.setAutoLights(entity.isAutoLights());
        model.setAutoTemperature(entity.isAutoTemperature());
        model.setEcoMode(entity.isEcoMode());
        model.setTemperatureUnit(entity.getTemperatureUnit());
        model.setDefaultLightIntensity(entity.getDefaultLightIntensity());
        model.setTemperatureThresholdMin(entity.getTemperatureThresholdMin());
        model.setTemperatureThresholdMax(entity.getTemperatureThresholdMax());
        model.setNightModeStart(entity.getNightModeStart());
        model.setNightModeEnd(entity.getNightModeEnd());
        model.setCreatedAt(entity.getCreatedAt());
        model.setUpdatedAt(entity.getUpdatedAt());
        return model;
    }
    
    private Map<String, Object> convertToMap(RoomEntity room) {
        Map<String, Object> map = new HashMap<>();
        map.put("roomId", room.getRoomId());
        map.put("name", room.getName());
        map.put("roomType", room.getRoomType());
        map.put("floor", room.getFloor());
        map.put("positionX", room.getPositionX());
        map.put("positionY", room.getPositionY());
        map.put("iconName", room.getIconName());
        map.put("color", room.getColor());
        map.put("createdAt", room.getCreatedAt());
        map.put("updatedAt", room.getUpdatedAt());
        return map;
    }
    
    private Map<String, Object> convertToMap(DeviceEntity device) {
        Map<String, Object> map = new HashMap<>();
        map.put("deviceId", device.getDeviceId());
        map.put("roomId", device.getRoomId());
        map.put("name", device.getName());
        map.put("deviceType", device.getDeviceType());
        map.put("isOn", device.isOn());
        map.put("intensity", device.getIntensity());
        map.put("temperature", device.getTemperature());
        map.put("isOnline", device.isOnline());
        map.put("lastStateChange", device.getLastStateChange());
        map.put("createdAt", device.getCreatedAt());
        map.put("updatedAt", device.getUpdatedAt());
        map.put("hardwareId", device.getHardwareId());
        map.put("pinNumber", device.getPinNumber());
        return map;
    }
    
    private Map<String, Object> convertToMap(DeviceHistoryEntity history) {
        Map<String, Object> map = new HashMap<>();
        map.put("deviceId", history.getDeviceId());
        map.put("action", history.getAction());
        map.put("oldValue", history.getOldValue());
        map.put("newValue", history.getNewValue());
        map.put("timestamp", history.getTimestamp());
        map.put("triggeredBy", history.getTriggeredBy());
        map.put("userId", history.getUserId());
        return map;
    }
    
    // Getters
    public LiveData<SyncStatus> getSyncStatus() {
        return syncStatus;
    }
    
    public LiveData<Boolean> getIsOnline() {
        return isOnline;
    }
    
    public void setConflictResolution(ConflictResolution strategy) {
        this.defaultStrategy = strategy;
    }
}