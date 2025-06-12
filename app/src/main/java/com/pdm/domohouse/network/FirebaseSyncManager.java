package com.pdm.domohouse.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ValueEventListener;
import com.pdm.domohouse.data.model.UserProfile;
import com.pdm.domohouse.utils.SecurePreferencesManager;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Manager para sincronización bidireccional entre datos locales y Firebase
 * Maneja sincronización automática, resolución de conflictos y modo offline
 */
public class FirebaseSyncManager {
    
    private static final String TAG = "FirebaseSyncManager";
    private static final long SYNC_INTERVAL_MINUTES = 15; // Sincronización cada 15 minutos
    
    private static FirebaseSyncManager instance;
    private final Context context;
    private final FirebaseDataManager firebaseDataManager;
    private final FirebaseAuth firebaseAuth;
    private final SecurePreferencesManager securePreferencesManager;
    
    // Control de sincronización
    private ScheduledExecutorService syncScheduler;
    private ValueEventListener userProfileListener;
    private boolean isSyncEnabled = true;
    private boolean isOnline = false;
    
    // Cache local del perfil
    private UserProfile cachedUserProfile;
    private long lastLocalUpdateTimestamp = 0;
    
    /**
     * Constructor privado para Singleton
     */
    private FirebaseSyncManager(Context context) {
        this.context = context.getApplicationContext();
        this.firebaseDataManager = FirebaseDataManager.getInstance();
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.securePreferencesManager = SecurePreferencesManager.getInstance(context);
        
        checkNetworkStatus();
        initializeAutoSync();
    }
    
    /**
     * Obtiene la instancia única del manager
     * @param context Contexto de la aplicación
     * @return La instancia del FirebaseSyncManager
     */
    public static synchronized FirebaseSyncManager getInstance(Context context) {
        if (instance == null) {
            instance = new FirebaseSyncManager(context);
        }
        return instance;
    }
    
    /**
     * Inicia la sincronización automática
     */
    private void initializeAutoSync() {
        if (syncScheduler == null || syncScheduler.isShutdown()) {
            syncScheduler = Executors.newSingleThreadScheduledExecutor();
            
            // Programar sincronización periódica
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
                Log.d(TAG, "Dispositivo volvió a estar online - iniciando sincronización");
                performFullSync();
            } else if (wasOnline && !isOnline) {
                Log.d(TAG, "Dispositivo ahora está offline");
            }
        }
    }
    
    /**
     * Realiza una sincronización completa
     */
    public void performFullSync() {
        if (!isSyncEnabled) {
            Log.d(TAG, "Sincronización deshabilitada");
            return;
        }
        
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Log.d(TAG, "No hay usuario autenticado - no se puede sincronizar");
            return;
        }
        
        if (!isOnline) {
            Log.d(TAG, "Dispositivo offline - posponiendo sincronización");
            return;
        }
        
        Log.d(TAG, "Iniciando sincronización completa");
        syncUserProfileFromFirebase(currentUser.getUid());
    }
    
    /**
     * Realiza sincronización automática periódica
     */
    private void performAutoSync() {
        try {
            checkNetworkStatus();
            
            if (isSyncEnabled && isOnline) {
                performFullSync();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error en sincronización automática", e);
        }
    }
    
    /**
     * Sincroniza el perfil de usuario desde Firebase
     * @param userId ID del usuario
     */
    private void syncUserProfileFromFirebase(String userId) {
        firebaseDataManager.getUserProfile(userId, new FirebaseDataManager.UserProfileCallback() {
            @Override
            public void onSuccess(UserProfile firebaseProfile) {
                Log.d(TAG, "Perfil obtenido de Firebase");
                resolveProfileConflicts(firebaseProfile);
            }
            
            @Override
            public void onError(String error) {
                Log.w(TAG, "Error al obtener perfil de Firebase: " + error);
                // No es crítico - el usuario puede seguir trabajando offline
            }
        });
    }
    
    /**
     * Resuelve conflictos entre el perfil local y el de Firebase
     * @param firebaseProfile Perfil desde Firebase
     */
    private void resolveProfileConflicts(UserProfile firebaseProfile) {
        if (cachedUserProfile == null) {
            // No hay perfil local, usar el de Firebase
            cachedUserProfile = firebaseProfile;
            Log.d(TAG, "Perfil local actualizado desde Firebase");
            return;
        }
        
        // Comparar timestamps para resolver conflictos
        long localTimestamp = lastLocalUpdateTimestamp;
        long firebaseTimestamp = firebaseProfile.getLastSyncTimestamp();
        
        if (firebaseTimestamp > localTimestamp) {
            // Firebase tiene datos más recientes
            Log.d(TAG, "Firebase tiene datos más recientes - actualizando perfil local");
            
            // Sincronizar PIN si es necesario
            syncPinFromFirebase(firebaseProfile);
            
            cachedUserProfile = firebaseProfile;
            lastLocalUpdateTimestamp = firebaseTimestamp;
        } else if (localTimestamp > firebaseTimestamp) {
            // Datos locales más recientes, subir a Firebase
            Log.d(TAG, "Datos locales más recientes - subiendo a Firebase");
            uploadLocalProfileToFirebase();
        } else {
            Log.d(TAG, "Perfiles sincronizados - no hay conflictos");
        }
    }
    
    /**
     * Sincroniza el PIN desde Firebase como respaldo
     * @param firebaseProfile Perfil de Firebase
     */
    private void syncPinFromFirebase(UserProfile firebaseProfile) {
        String encryptedPin = firebaseProfile.getEncryptedPin();
        if (encryptedPin != null && !securePreferencesManager.isPinEnabled()) {
            // Intentar restaurar PIN desde respaldo
            String decryptedPin = securePreferencesManager.decryptPin(encryptedPin);
            if (decryptedPin != null) {
                boolean pinSaved = securePreferencesManager.savePin(decryptedPin);
                if (pinSaved) {
                    Log.d(TAG, "PIN restaurado exitosamente desde respaldo de Firebase");
                } else {
                    Log.w(TAG, "Error al restaurar PIN desde respaldo");
                }
            }
        }
    }
    
    /**
     * Sube el perfil local a Firebase
     */
    private void uploadLocalProfileToFirebase() {
        if (cachedUserProfile == null) {
            Log.w(TAG, "No hay perfil local para subir");
            return;
        }
        
        cachedUserProfile.updateLastSync();
        
        firebaseDataManager.saveUserProfile(cachedUserProfile, new FirebaseDataManager.DatabaseCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Perfil local subido exitosamente a Firebase");
                lastLocalUpdateTimestamp = cachedUserProfile.getLastSyncTimestamp();
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Error al subir perfil local a Firebase: " + error);
            }
        });
    }
    
    /**
     * Actualiza el perfil de usuario local y lo sincroniza con Firebase
     * @param userProfile Perfil actualizado
     * @param callback Callback para el resultado
     */
    public void updateUserProfile(UserProfile userProfile, SyncCallback callback) {
        if (userProfile == null) {
            callback.onError("Perfil de usuario inválido");
            return;
        }
        
        // Actualizar cache local
        cachedUserProfile = userProfile;
        lastLocalUpdateTimestamp = System.currentTimeMillis();
        cachedUserProfile.setLastSyncTimestamp(lastLocalUpdateTimestamp);
        
        if (isOnline && isSyncEnabled) {
            // Subir a Firebase inmediatamente si estamos online
            firebaseDataManager.saveUserProfile(userProfile, new FirebaseDataManager.DatabaseCallback() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "Perfil actualizado y sincronizado con Firebase");
                    callback.onSuccess();
                }
                
                @Override
                public void onError(String error) {
                    Log.w(TAG, "Error al sincronizar con Firebase, guardado localmente: " + error);
                    // Aún así consideramos exitoso porque está guardado localmente
                    callback.onSuccess();
                }
            });
        } else {
            // Offline o sincronización deshabilitada - solo guardar localmente
            Log.d(TAG, "Perfil guardado localmente (offline o sync deshabilitado)");
            callback.onSuccess();
        }
    }
    
    /**
     * Obtiene el perfil de usuario (local o Firebase)
     * @param callback Callback para el resultado
     */
    public void getUserProfile(SyncCallback callback) {
        if (cachedUserProfile != null) {
            callback.onSuccess();
            return;
        }
        
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onError("No hay usuario autenticado");
            return;
        }
        
        if (isOnline) {
            // Intentar obtener desde Firebase
            syncUserProfileFromFirebase(currentUser.getUid());
            if (cachedUserProfile != null) {
                callback.onSuccess();
            } else {
                callback.onError("No se pudo obtener el perfil");
            }
        } else {
            callback.onError("Sin conexión y sin cache local");
        }
    }
    
    /**
     * Habilita o deshabilita la sincronización automática
     * @param enabled true para habilitar, false para deshabilitar
     */
    public void setSyncEnabled(boolean enabled) {
        this.isSyncEnabled = enabled;
        Log.d(TAG, "Sincronización " + (enabled ? "habilitada" : "deshabilitada"));
        
        if (enabled && isOnline) {
            performFullSync();
        }
    }
    
    /**
     * Obtiene el perfil en cache
     * @return El perfil en cache o null
     */
    public UserProfile getCachedUserProfile() {
        return cachedUserProfile;
    }
    
    /**
     * Inicia el listener en tiempo real para el perfil del usuario
     * @param userId ID del usuario
     */
    public void startRealtimeSync(String userId) {
        if (userProfileListener != null) {
            stopRealtimeSync(userId);
        }
        
        userProfileListener = firebaseDataManager.listenToUserProfile(userId, 
                new FirebaseDataManager.UserProfileListener() {
                    @Override
                    public void onUserProfileChanged(UserProfile userProfile) {
                        Log.d(TAG, "Perfil actualizado en tiempo real desde Firebase");
                        resolveProfileConflicts(userProfile);
                    }
                    
                    @Override
                    public void onError(String error) {
                        Log.w(TAG, "Error en sincronización en tiempo real: " + error);
                    }
                });
    }
    
    /**
     * Detiene el listener en tiempo real
     * @param userId ID del usuario
     */
    public void stopRealtimeSync(String userId) {
        if (userProfileListener != null) {
            firebaseDataManager.removeUserProfileListener(userId, userProfileListener);
            userProfileListener = null;
            Log.d(TAG, "Sincronización en tiempo real detenida");
        }
    }
    
    /**
     * Detiene todos los servicios de sincronización
     */
    public void shutdown() {
        if (syncScheduler != null && !syncScheduler.isShutdown()) {
            syncScheduler.shutdown();
            Log.d(TAG, "Scheduler de sincronización detenido");
        }
        
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            stopRealtimeSync(currentUser.getUid());
        }
    }
    
    /**
     * Interface para callbacks de sincronización
     */
    public interface SyncCallback {
        void onSuccess();
        void onError(String error);
    }
}