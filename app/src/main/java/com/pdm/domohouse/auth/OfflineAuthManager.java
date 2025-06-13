package com.pdm.domohouse.auth;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.pdm.domohouse.data.database.AppDatabase;
import com.pdm.domohouse.data.database.dao.UserProfileDao;
import com.pdm.domohouse.data.database.entity.UserProfileEntity;
import com.pdm.domohouse.utils.SecurePreferencesManager;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Manager para autenticación offline después de la primera sincronización
 * Permite login con PIN sin conexión a internet usando datos locales
 */
public class OfflineAuthManager {
    
    private static final String TAG = "OfflineAuthManager";
    
    private static OfflineAuthManager instance;
    private final Context context;
    private final SecurePreferencesManager securePreferencesManager;
    private final AppDatabase database;
    private final UserProfileDao userProfileDao;
    private final FirebaseAuth firebaseAuth;
    private final ExecutorService executorService;
    
    // Estado de autenticación offline
    private boolean isOfflineAuthEnabled = false;
    private String currentOfflineUserId = null;
    private UserProfileEntity cachedUserProfile = null;
    
    /**
     * Constructor privado para Singleton
     */
    private OfflineAuthManager(Context context) {
        this.context = context.getApplicationContext();
        this.securePreferencesManager = SecurePreferencesManager.getInstance(context);
        this.database = AppDatabase.getDatabase(context);
        this.userProfileDao = database.userProfileDao();
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.executorService = Executors.newSingleThreadExecutor();
        
        initializeOfflineAuth();
    }
    
    /**
     * Obtiene la instancia única del manager
     */
    public static synchronized OfflineAuthManager getInstance(Context context) {
        if (instance == null) {
            instance = new OfflineAuthManager(context);
        }
        return instance;
    }
    
    /**
     * Inicializa la autenticación offline verificando datos locales
     */
    private void initializeOfflineAuth() {
        executorService.execute(() -> {
            try {
                // Verificar si hay un perfil de usuario en la base de datos local
                int userCount = userProfileDao.getUserCount();
                
                if (userCount > 0) {
                    // Hay datos de usuario locales
                    String savedUserId = securePreferencesManager.getLastUserId();
                    
                    if (savedUserId != null) {
                        UserProfileEntity localProfile = userProfileDao.getUserProfileSync(savedUserId);
                        
                        if (localProfile != null && localProfile.getPinHash() != null) {
                            // Usuario válido con PIN configurado
                            isOfflineAuthEnabled = true;
                            currentOfflineUserId = savedUserId;
                            cachedUserProfile = localProfile;
                            
                            Log.d(TAG, "Autenticación offline habilitada para usuario: " + savedUserId);
                        }
                    }
                }
                
                if (!isOfflineAuthEnabled) {
                    Log.d(TAG, "Autenticación offline no disponible - se requiere primera sincronización");
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error al inicializar autenticación offline", e);
            }
        });
    }
    
    /**
     * Habilita la autenticación offline después de un login exitoso online
     */
    public CompletableFuture<Boolean> enableOfflineAuth(@NonNull String userId, @NonNull String pin) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Verificar que el usuario esté autenticado en Firebase
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                if (currentUser == null || !currentUser.getUid().equals(userId)) {
                    Log.w(TAG, "Usuario no autenticado en Firebase - no se puede habilitar offline auth");
                    return false;
                }
                
                // Guardar o actualizar el perfil en la base de datos local
                UserProfileEntity localProfile = userProfileDao.getUserProfileSync(userId);
                
                if (localProfile == null) {
                    // Crear nuevo perfil local
                    localProfile = new UserProfileEntity();
                    localProfile.setUserId(userId);
                    localProfile.setName(currentUser.getDisplayName());
                    localProfile.setEmail(currentUser.getEmail());
                    localProfile.setPhotoUrl(currentUser.getPhotoUrl() != null ? 
                        currentUser.getPhotoUrl().toString() : null);
                }
                
                // Encriptar y guardar el PIN
                String pinHash = securePreferencesManager.hashPin(pin);
                localProfile.setPinHash(pinHash);
                localProfile.setSynced(true);
                localProfile.setLastSync(System.currentTimeMillis());
                
                // Guardar en la base de datos
                if (userProfileDao.getUserProfileSync(userId) == null) {
                    userProfileDao.insert(localProfile);
                } else {
                    userProfileDao.update(localProfile);
                }
                
                // Guardar ID del último usuario
                securePreferencesManager.saveLastUserId(userId);
                
                // Actualizar estado interno
                isOfflineAuthEnabled = true;
                currentOfflineUserId = userId;
                cachedUserProfile = localProfile;
                
                Log.d(TAG, "Autenticación offline habilitada exitosamente para usuario: " + userId);
                return true;
                
            } catch (Exception e) {
                Log.e(TAG, "Error al habilitar autenticación offline", e);
                return false;
            }
        });
    }
    
    /**
     * Realiza login offline usando PIN
     */
    public CompletableFuture<OfflineAuthResult> loginOffline(@NonNull String pin) {
        return CompletableFuture.supplyAsync(() -> {
            if (!isOfflineAuthEnabled) {
                return new OfflineAuthResult(false, "Autenticación offline no disponible", null);
            }
            
            try {
                // Verificar que tengamos datos locales
                if (currentOfflineUserId == null || cachedUserProfile == null) {
                    Log.w(TAG, "No hay datos de usuario en cache");
                    return new OfflineAuthResult(false, "No hay datos de usuario locales", null);
                }
                
                // Verificar el PIN
                boolean pinValid = securePreferencesManager.verifyPin(pin, cachedUserProfile.getPinHash());
                
                if (!pinValid) {
                    Log.w(TAG, "PIN incorrecto en login offline");
                    return new OfflineAuthResult(false, "PIN incorrecto", null);
                }
                
                // Actualizar timestamp de último acceso
                cachedUserProfile.setUpdatedAt(System.currentTimeMillis());
                userProfileDao.update(cachedUserProfile);
                
                // Guardar sesión offline
                securePreferencesManager.saveOfflineSession(currentOfflineUserId);
                
                Log.d(TAG, "Login offline exitoso para usuario: " + currentOfflineUserId);
                return new OfflineAuthResult(true, "Login offline exitoso", 
                    new OfflineUser(currentOfflineUserId, cachedUserProfile.getName(), 
                        cachedUserProfile.getEmail(), cachedUserProfile.getPhotoUrl()));
                
            } catch (Exception e) {
                Log.e(TAG, "Error en login offline", e);
                return new OfflineAuthResult(false, "Error en login offline: " + e.getMessage(), null);
            }
        });
    }
    
    /**
     * Verifica si hay una sesión offline válida
     */
    public CompletableFuture<OfflineAuthResult> checkOfflineSession() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String sessionUserId = securePreferencesManager.getOfflineSessionUserId();
                
                if (sessionUserId == null) {
                    return new OfflineAuthResult(false, "No hay sesión offline activa", null);
                }
                
                // Verificar que el usuario exista en la base de datos local
                UserProfileEntity localProfile = userProfileDao.getUserProfileSync(sessionUserId);
                
                if (localProfile == null) {
                    securePreferencesManager.clearOfflineSession();
                    return new OfflineAuthResult(false, "Usuario de sesión no encontrado localmente", null);
                }
                
                // Verificar que la sesión no haya expirado (opcional)
                long sessionTime = securePreferencesManager.getOfflineSessionTime();
                long currentTime = System.currentTimeMillis();
                long sessionDuration = currentTime - sessionTime;
                
                // Sesión válida por 7 días
                if (sessionDuration > 7 * 24 * 60 * 60 * 1000L) {
                    securePreferencesManager.clearOfflineSession();
                    return new OfflineAuthResult(false, "Sesión offline expirada", null);
                }
                
                // Actualizar cache interno
                currentOfflineUserId = sessionUserId;
                cachedUserProfile = localProfile;
                isOfflineAuthEnabled = true;
                
                Log.d(TAG, "Sesión offline válida encontrada para usuario: " + sessionUserId);
                return new OfflineAuthResult(true, "Sesión offline válida", 
                    new OfflineUser(sessionUserId, localProfile.getName(), 
                        localProfile.getEmail(), localProfile.getPhotoUrl()));
                
            } catch (Exception e) {
                Log.e(TAG, "Error al verificar sesión offline", e);
                return new OfflineAuthResult(false, "Error al verificar sesión: " + e.getMessage(), null);
            }
        });
    }
    
    /**
     * Cierra la sesión offline
     */
    public void logoutOffline() {
        try {
            securePreferencesManager.clearOfflineSession();
            currentOfflineUserId = null;
            cachedUserProfile = null;
            
            Log.d(TAG, "Sesión offline cerrada");
            
        } catch (Exception e) {
            Log.e(TAG, "Error al cerrar sesión offline", e);
        }
    }
    
    /**
     * Cambia el PIN del usuario actual
     */
    public CompletableFuture<Boolean> changeOfflinePin(@NonNull String currentPin, @NonNull String newPin) {
        return CompletableFuture.supplyAsync(() -> {
            if (!isOfflineAuthEnabled || cachedUserProfile == null) {
                Log.w(TAG, "No hay usuario offline autenticado");
                return false;
            }
            
            try {
                // Verificar PIN actual
                if (!securePreferencesManager.verifyPin(currentPin, cachedUserProfile.getPinHash())) {
                    Log.w(TAG, "PIN actual incorrecto");
                    return false;
                }
                
                // Encriptar nuevo PIN
                String newPinHash = securePreferencesManager.hashPin(newPin);
                
                // Actualizar en base de datos
                long currentTime = System.currentTimeMillis();
                userProfileDao.updatePin(cachedUserProfile.getUserId(), newPinHash, currentTime);
                
                // Actualizar cache
                cachedUserProfile.setPinHash(newPinHash);
                cachedUserProfile.setUpdatedAt(currentTime);
                
                Log.d(TAG, "PIN cambiado exitosamente para usuario offline");
                return true;
                
            } catch (Exception e) {
                Log.e(TAG, "Error al cambiar PIN offline", e);
                return false;
            }
        });
    }
    
    /**
     * Obtiene el usuario offline actual
     */
    public OfflineUser getCurrentOfflineUser() {
        if (cachedUserProfile != null) {
            return new OfflineUser(cachedUserProfile.getUserId(), 
                cachedUserProfile.getName(), 
                cachedUserProfile.getEmail(), 
                cachedUserProfile.getPhotoUrl());
        }
        return null;
    }
    
    /**
     * Verifica si la autenticación offline está disponible
     */
    public boolean isOfflineAuthAvailable() {
        return isOfflineAuthEnabled;
    }
    
    /**
     * Deshabilita la autenticación offline y limpia datos locales
     */
    public CompletableFuture<Boolean> disableOfflineAuth() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Limpiar sesión actual
                logoutOffline();
                
                // Limpiar datos de usuario
                if (currentOfflineUserId != null) {
                    securePreferencesManager.clearUserData(currentOfflineUserId);
                }
                
                // Limpiar base de datos local
                userProfileDao.deleteAll();
                
                // Actualizar estado
                isOfflineAuthEnabled = false;
                currentOfflineUserId = null;
                cachedUserProfile = null;
                
                Log.d(TAG, "Autenticación offline deshabilitada y datos limpiados");
                return true;
                
            } catch (Exception e) {
                Log.e(TAG, "Error al deshabilitar autenticación offline", e);
                return false;
            }
        });
    }
    
    /**
     * Limpia recursos
     */
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
    
    // Clases auxiliares
    
    /**
     * Resultado de operación de autenticación offline
     */
    public static class OfflineAuthResult {
        private final boolean success;
        private final String message;
        private final OfflineUser user;
        
        public OfflineAuthResult(boolean success, String message, OfflineUser user) {
            this.success = success;
            this.message = message;
            this.user = user;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public OfflineUser getUser() { return user; }
    }
    
    /**
     * Representación simplificada de usuario para modo offline
     */
    public static class OfflineUser {
        private final String userId;
        private final String name;
        private final String email;
        private final String photoUrl;
        
        public OfflineUser(String userId, String name, String email, String photoUrl) {
            this.userId = userId;
            this.name = name;
            this.email = email;
            this.photoUrl = photoUrl;
        }
        
        public String getUserId() { return userId; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getPhotoUrl() { return photoUrl; }
    }
}