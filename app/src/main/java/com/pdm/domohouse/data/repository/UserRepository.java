package com.pdm.domohouse.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.pdm.domohouse.data.database.AppDatabase;
import com.pdm.domohouse.data.database.dao.UserPreferencesDao;
import com.pdm.domohouse.data.database.dao.UserProfileDao;
import com.pdm.domohouse.data.database.entity.UserPreferencesEntity;
import com.pdm.domohouse.data.database.entity.UserProfileEntity;
import com.pdm.domohouse.data.model.UserPreferences;
import com.pdm.domohouse.data.model.UserProfile;
import com.pdm.domohouse.data.sync.ComprehensiveSyncManager;
import com.pdm.domohouse.network.FirebaseDataManager;
import com.pdm.domohouse.utils.DeviceMapper;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repositorio para manejo de datos de usuario
 * Coordina entre datos locales (Room) y remotos (Firebase)
 */
public class UserRepository {
    
    private static final String TAG = "UserRepository";
    
    private final UserProfileDao userProfileDao;
    private final UserPreferencesDao userPreferencesDao;
    private final FirebaseDataManager firebaseDataManager;
    private final ComprehensiveSyncManager syncManager;
    private final ExecutorService executor;
    
    public UserRepository(Context context) {
        AppDatabase database = AppDatabase.getDatabase(context);
        this.userProfileDao = database.userProfileDao();
        this.userPreferencesDao = database.userPreferencesDao();
        this.firebaseDataManager = FirebaseDataManager.getInstance();
        this.syncManager = ComprehensiveSyncManager.getInstance(context);
        this.executor = Executors.newSingleThreadExecutor();
    }
    
    /**
     * Obtiene el perfil de usuario con estrategia cache-first
     */
    public LiveData<UserProfile> getUserProfile(@NonNull String userId) {
        MediatorLiveData<UserProfile> result = new MediatorLiveData<>();
        
        // Primero intentar datos locales
        LiveData<UserProfileEntity> localData = userProfileDao.getUserProfile(userId);
        result.addSource(localData, entity -> {
            if (entity != null) {
                UserProfile profile = mapEntityToModel(entity);
                result.setValue(profile);
                
                // Si los datos están desactualizados, sincronizar en background
                if (isDataStale(entity.getLastSync())) {
                    syncUserProfileInBackground(userId);
                }
            } else {
                // No hay datos locales, intentar cargar desde Firebase
                loadUserProfileFromFirebase(userId, result);
            }
        });
        
        return result;
    }
    
    /**
     * Guarda el perfil de usuario
     */
    public CompletableFuture<Boolean> saveUserProfile(@NonNull UserProfile userProfile) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Convertir a entidad local
                UserProfileEntity entity = mapModelToEntity(userProfile);
                entity.setUpdatedAt(System.currentTimeMillis());
                entity.setSynced(false);
                
                // Guardar localmente primero
                userProfileDao.insert(entity);
                
                // Intentar sincronizar con Firebase si hay conexión
                if (syncManager.isOnline()) {
                    syncUserProfileToFirebase(userProfile);
                }
                
                Log.d(TAG, "Perfil de usuario guardado: " + userProfile.getUserId());
                return true;
                
            } catch (Exception e) {
                Log.e(TAG, "Error al guardar perfil de usuario", e);
                return false;
            }
        }, executor);
    }
    
    /**
     * Obtiene las preferencias de usuario
     */
    public LiveData<UserPreferences> getUserPreferences(@NonNull String userId) {
        MediatorLiveData<UserPreferences> result = new MediatorLiveData<>();
        
        LiveData<UserPreferencesEntity> localData = userPreferencesDao.getUserPreferences(userId);
        result.addSource(localData, entity -> {
            if (entity != null) {
                UserPreferences preferences = mapPreferencesEntityToModel(entity);
                result.setValue(preferences);
            } else {
                // Crear preferencias por defecto
                createDefaultPreferences(userId, result);
            }
        });
        
        return result;
    }
    
    /**
     * Guarda las preferencias de usuario
     */
    public CompletableFuture<Boolean> saveUserPreferences(@NonNull UserPreferences preferences) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                UserPreferencesEntity entity = mapPreferencesModelToEntity(preferences);
                entity.setUpdatedAt(System.currentTimeMillis());
                entity.setSynced(false);
                
                userPreferencesDao.insert(entity);
                
                // Sincronizar con Firebase si hay conexión
                if (syncManager.isOnline()) {
                    syncPreferencesToFirebase(preferences);
                }
                
                Log.d(TAG, "Preferencias guardadas para usuario: " + preferences.getUserId());
                return true;
                
            } catch (Exception e) {
                Log.e(TAG, "Error al guardar preferencias", e);
                return false;
            }
        }, executor);
    }
    
    /**
     * Actualiza el idioma del usuario
     */
    public CompletableFuture<Boolean> updateLanguage(@NonNull String userId, @NonNull String language) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                long timestamp = System.currentTimeMillis();
                userPreferencesDao.updateLanguage(userId, language, timestamp);
                
                Log.d(TAG, "Idioma actualizado a: " + language);
                return true;
                
            } catch (Exception e) {
                Log.e(TAG, "Error al actualizar idioma", e);
                return false;
            }
        }, executor);
    }
    
    /**
     * Actualiza las preferencias de notificaciones
     */
    public CompletableFuture<Boolean> updateNotifications(@NonNull String userId, boolean enabled) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                long timestamp = System.currentTimeMillis();
                userPreferencesDao.updateNotifications(userId, enabled, timestamp);
                
                Log.d(TAG, "Notificaciones " + (enabled ? "habilitadas" : "deshabilitadas"));
                return true;
                
            } catch (Exception e) {
                Log.e(TAG, "Error al actualizar notificaciones", e);
                return false;
            }
        }, executor);
    }
    
    /**
     * Sincroniza el perfil de usuario con Firebase en background
     */
    private void syncUserProfileInBackground(@NonNull String userId) {
        executor.execute(() -> {
            try {
                firebaseDataManager.getUserProfile(userId, new FirebaseDataManager.UserProfileCallback() {
                    @Override
                    public void onSuccess(UserProfile remoteProfile) {
                        // Resolver conflictos y actualizar datos locales
                        resolveAndUpdateLocalProfile(userId, remoteProfile);
                    }
                    
                    @Override
                    public void onError(String error) {
                        Log.w(TAG, "No se pudo sincronizar perfil desde Firebase: " + error);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error en sincronización background de perfil", e);
            }
        });
    }
    
    /**
     * Carga el perfil de usuario desde Firebase
     */
    private void loadUserProfileFromFirebase(@NonNull String userId, MediatorLiveData<UserProfile> result) {
        firebaseDataManager.getUserProfile(userId, new FirebaseDataManager.UserProfileCallback() {
            @Override
            public void onSuccess(UserProfile remoteProfile) {
                // Guardar en cache local
                UserProfileEntity entity = mapModelToEntity(remoteProfile);
                entity.setSynced(true);
                entity.setLastSync(System.currentTimeMillis());
                
                executor.execute(() -> {
                    userProfileDao.insert(entity);
                    Log.d(TAG, "Perfil cargado desde Firebase y guardado localmente");
                });
                
                result.postValue(remoteProfile);
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Error al cargar perfil desde Firebase: " + error);
                result.postValue(null);
            }
        });
    }
    
    /**
     * Sincroniza el perfil con Firebase
     */
    private void syncUserProfileToFirebase(@NonNull UserProfile userProfile) {
        firebaseDataManager.saveUserProfile(userProfile, new FirebaseDataManager.DatabaseCallback() {
            @Override
            public void onSuccess() {
                // Marcar como sincronizado en base de datos local
                executor.execute(() -> {
                    long timestamp = System.currentTimeMillis();
                    userProfileDao.markAsSynced(userProfile.getUserId(), timestamp);
                    Log.d(TAG, "Perfil sincronizado con Firebase");
                });
            }
            
            @Override
            public void onError(String error) {
                Log.w(TAG, "Error al sincronizar perfil con Firebase: " + error);
            }
        });
    }
    
    /**
     * Crea preferencias por defecto para un usuario
     */
    private void createDefaultPreferences(@NonNull String userId, MediatorLiveData<UserPreferences> result) {
        executor.execute(() -> {
            UserPreferencesEntity defaultEntity = new UserPreferencesEntity();
            defaultEntity.setUserId(userId);
            userPreferencesDao.insert(defaultEntity);
            
            UserPreferences defaultPreferences = mapPreferencesEntityToModel(defaultEntity);
            result.postValue(defaultPreferences);
            
            Log.d(TAG, "Preferencias por defecto creadas para usuario: " + userId);
        });
    }
    
    /**
     * Resuelve conflictos y actualiza el perfil local
     */
    private void resolveAndUpdateLocalProfile(@NonNull String userId, @NonNull UserProfile remoteProfile) {
        executor.execute(() -> {
            try {
                UserProfileEntity localEntity = userProfileDao.getUserProfileSync(userId);
                
                if (localEntity == null) {
                    // No hay datos locales, usar los remotos
                    UserProfileEntity newEntity = mapModelToEntity(remoteProfile);
                    newEntity.setSynced(true);
                    newEntity.setLastSync(System.currentTimeMillis());
                    userProfileDao.insert(newEntity);
                    return;
                }
                
                // Comparar timestamps para resolver conflictos
                if (remoteProfile.getLastSyncTimestamp() > localEntity.getUpdatedAt()) {
                    // Datos remotos más recientes
                    UserProfileEntity updatedEntity = mapModelToEntity(remoteProfile);
                    updatedEntity.setSynced(true);
                    updatedEntity.setLastSync(System.currentTimeMillis());
                    userProfileDao.update(updatedEntity);
                    
                    Log.d(TAG, "Perfil local actualizado con datos remotos más recientes");
                } else if (localEntity.getUpdatedAt() > remoteProfile.getLastSyncTimestamp() && !localEntity.isSynced()) {
                    // Datos locales más recientes, sincronizar con Firebase
                    UserProfile localProfile = mapEntityToModel(localEntity);
                    syncUserProfileToFirebase(localProfile);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error al resolver conflictos de perfil", e);
            }
        });
    }
    
    /**
     * Sincroniza preferencias con Firebase
     */
    private void syncPreferencesToFirebase(@NonNull UserPreferences preferences) {
        // TODO: Implementar sincronización de preferencias con Firebase
        // Por ahora solo marcar como sincronizado localmente
        executor.execute(() -> {
            long timestamp = System.currentTimeMillis();
            userPreferencesDao.markAsSynced(preferences.getUserId(), timestamp);
            Log.d(TAG, "Preferencias marcadas como sincronizadas");
        });
    }
    
    /**
     * Verifica si los datos están desactualizados
     */
    private boolean isDataStale(long lastSync) {
        long currentTime = System.currentTimeMillis();
        long timeDiff = currentTime - lastSync;
        // Considerar desactualizados después de 1 hora
        return timeDiff > 60 * 60 * 1000;
    }
    
    // Métodos de mapeo entre entidades y modelos
    
    private UserProfile mapEntityToModel(@NonNull UserProfileEntity entity) {
        UserProfile profile = new UserProfile();
        profile.setUserId(entity.getUserId());
        profile.setName(entity.getName());
        profile.setEmail(entity.getEmail());
        profile.setPhotoUrl(entity.getPhotoUrl());
        profile.setEncryptedPin(entity.getPinHash());
        profile.setLastSyncTimestamp(entity.getLastSync());
        return profile;
    }
    
    private UserProfileEntity mapModelToEntity(@NonNull UserProfile profile) {
        UserProfileEntity entity = new UserProfileEntity();
        entity.setUserId(profile.getUserId());
        entity.setName(profile.getName());
        entity.setEmail(profile.getEmail());
        entity.setPhotoUrl(profile.getPhotoUrl());
        entity.setPinHash(profile.getEncryptedPin());
        entity.setCreatedAt(System.currentTimeMillis());
        entity.setUpdatedAt(System.currentTimeMillis());
        return entity;
    }
    
    private UserPreferences mapPreferencesEntityToModel(@NonNull UserPreferencesEntity entity) {
        UserPreferences preferences = new UserPreferences();
        preferences.setUserId(entity.getUserId());
        preferences.setLanguage(entity.getLanguage());
        preferences.setNotificationsEnabled(entity.isNotificationsEnabled());
        preferences.setMotionAlerts(entity.isMotionAlerts());
        preferences.setTemperatureAlerts(entity.isTemperatureAlerts());
        preferences.setSmokeAlerts(entity.isSmokeAlerts());
        preferences.setSecurityAlerts(entity.isSecurityAlerts());
        preferences.setAutoLights(entity.isAutoLights());
        preferences.setAutoTemperature(entity.isAutoTemperature());
        preferences.setEcoMode(entity.isEcoMode());
        preferences.setTemperatureUnit(entity.getTemperatureUnit());
        preferences.setDefaultLightIntensity(entity.getDefaultLightIntensity());
        preferences.setTemperatureThresholdMin(entity.getTemperatureThresholdMin());
        preferences.setTemperatureThresholdMax(entity.getTemperatureThresholdMax());
        preferences.setNightModeStart(entity.getNightModeStart());
        preferences.setNightModeEnd(entity.getNightModeEnd());
        // Note: UserPreferences model doesn't have lastSync field
        return preferences;
    }
    
    private UserPreferencesEntity mapPreferencesModelToEntity(@NonNull UserPreferences preferences) {
        UserPreferencesEntity entity = new UserPreferencesEntity();
        entity.setUserId(preferences.getUserId());
        entity.setLanguage(preferences.getLanguage());
        entity.setNotificationsEnabled(preferences.isNotificationsEnabled());
        entity.setMotionAlerts(preferences.isMotionAlerts());
        entity.setTemperatureAlerts(preferences.isTemperatureAlerts());
        entity.setSmokeAlerts(preferences.isSmokeAlerts());
        entity.setSecurityAlerts(preferences.isSecurityAlerts());
        entity.setAutoLights(preferences.isAutoLights());
        entity.setAutoTemperature(preferences.isAutoTemperature());
        entity.setEcoMode(preferences.isEcoMode());
        entity.setTemperatureUnit(preferences.getTemperatureUnit());
        entity.setDefaultLightIntensity(preferences.getDefaultLightIntensity());
        entity.setTemperatureThresholdMin(preferences.getTemperatureThresholdMin());
        entity.setTemperatureThresholdMax(preferences.getTemperatureThresholdMax());
        entity.setNightModeStart(preferences.getNightModeStart());
        entity.setNightModeEnd(preferences.getNightModeEnd());
        entity.setCreatedAt(System.currentTimeMillis());
        entity.setUpdatedAt(System.currentTimeMillis());
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