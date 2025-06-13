package com.pdm.domohouse.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Clase para manejar SharedPreferences cifradas
 * Maneja el almacenamiento seguro del PIN y otras preferencias sensibles
 */
public class SecurePreferencesManager {
    
    private static final String TAG = "SecurePreferencesManager";
    private static final String PREFS_FILE_NAME = "secure_domohouse_prefs";
    private static final String PIN_KEY = "user_pin";
    private static final String PIN_ENABLED_KEY = "pin_enabled";
    private static final String CURRENT_USER_ID_KEY = "current_user_id";
    private static final String LAST_USER_ID_KEY = "last_user_id";
    private static final String OFFLINE_SESSION_USER_KEY = "offline_session_user";
    private static final String OFFLINE_SESSION_TIME_KEY = "offline_session_time";
    
    private static SecurePreferencesManager instance;
    private SharedPreferences sharedPreferences;
    private final Context context;
    
    /**
     * Constructor privado para Singleton
     * @param context Contexto de la aplicación
     */
    private SecurePreferencesManager(Context context) {
        this.context = context.getApplicationContext();
        initializeEncryptedPreferences();
    }
    
    /**
     * Obtiene la instancia única del manager
     * @param context Contexto de la aplicación
     * @return La instancia del SecurePreferencesManager
     */
    public static synchronized SecurePreferencesManager getInstance(Context context) {
        if (instance == null) {
            instance = new SecurePreferencesManager(context);
        }
        return instance;
    }
    
    /**
     * Obtiene la instancia única del manager usando el contexto de la aplicación
     * Este método solo debe usarse cuando no se tiene acceso directo al contexto
     * @return La instancia del SecurePreferencesManager o null si la aplicación no está inicializada
     */
    public static synchronized SecurePreferencesManager getInstance() {
        if (instance == null) {
            Context appContext = com.pdm.domohouse.DomoHouseApplication.getAppContext();
            if (appContext != null) {
                instance = new SecurePreferencesManager(appContext);
            }
        }
        return instance;
    }
    
    /**
     * Inicializa las SharedPreferences cifradas
     */
    private void initializeEncryptedPreferences() {
        try {
            // Crear la MasterKey para cifrado
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            
            // Crear las SharedPreferences cifradas
            sharedPreferences = EncryptedSharedPreferences.create(
                    context,
                    PREFS_FILE_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
            
            Log.d(TAG, "SharedPreferences cifradas inicializadas exitosamente");
            
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "Error al inicializar SharedPreferences cifradas", e);
            // Fallback a SharedPreferences normales (no recomendado para producción)
            sharedPreferences = context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE);
        }
    }
    
    /**
     * Guarda el PIN del usuario de forma cifrada
     * @param pin PIN de 4 dígitos
     * @return true si se guardó exitosamente, false en caso contrario
     */
    public boolean savePin(String pin) {
        if (pin == null || pin.length() != 4 || !pin.matches("\\d{4}")) {
            Log.e(TAG, "PIN inválido: debe ser de 4 dígitos numéricos");
            return false;
        }
        
        try {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(PIN_KEY, pin);
            editor.putBoolean(PIN_ENABLED_KEY, true);
            boolean saved = editor.commit();
            
            if (saved) {
                Log.d(TAG, "PIN guardado exitosamente");
            } else {
                Log.e(TAG, "Error al guardar PIN");
            }
            
            return saved;
        } catch (Exception e) {
            Log.e(TAG, "Error al guardar PIN", e);
            return false;
        }
    }
    
    /**
     * Verifica si el PIN proporcionado es correcto
     * @param inputPin PIN ingresado por el usuario
     * @return true si el PIN es correcto, false en caso contrario
     */
    public boolean verifyPin(String inputPin) {
        if (inputPin == null || !isPinEnabled()) {
            return false;
        }
        
        try {
            String storedPin = sharedPreferences.getString(PIN_KEY, null);
            boolean isValid = inputPin.equals(storedPin);
            
            Log.d(TAG, "Verificación de PIN: " + (isValid ? "exitosa" : "fallida"));
            return isValid;
            
        } catch (Exception e) {
            Log.e(TAG, "Error al verificar PIN", e);
            return false;
        }
    }
    
    /**
     * Verifica si el PIN está habilitado
     * @return true si el PIN está habilitado, false en caso contrario
     */
    public boolean isPinEnabled() {
        try {
            return sharedPreferences.getBoolean(PIN_ENABLED_KEY, false);
        } catch (Exception e) {
            Log.e(TAG, "Error al verificar si PIN está habilitado", e);
            return false;
        }
    }
    
    /**
     * Deshabilita el PIN del usuario
     * @return true si se deshabilitó exitosamente, false en caso contrario
     */
    public boolean disablePin() {
        try {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(PIN_KEY);
            editor.putBoolean(PIN_ENABLED_KEY, false);
            boolean disabled = editor.commit();
            
            if (disabled) {
                Log.d(TAG, "PIN deshabilitado exitosamente");
            } else {
                Log.e(TAG, "Error al deshabilitar PIN");
            }
            
            return disabled;
        } catch (Exception e) {
            Log.e(TAG, "Error al deshabilitar PIN", e);
            return false;
        }
    }
    
    /**
     * Cambia el PIN del usuario
     * @param currentPin PIN actual
     * @param newPin Nuevo PIN
     * @return true si se cambió exitosamente, false en caso contrario
     */
    public boolean changePin(String currentPin, String newPin) {
        if (!verifyPin(currentPin)) {
            Log.e(TAG, "PIN actual incorrecto");
            return false;
        }
        
        return savePin(newPin);
    }
    
    /**
     * Cifra un PIN para respaldo en la nube (diferente del almacenamiento local)
     * @param pin PIN a cifrar
     * @return PIN cifrado como string base64, o null si hay error
     */
    public String encryptPin(String pin) {
        if (pin == null || pin.length() != 4 || !pin.matches("\\d{4}")) {
            Log.e(TAG, "PIN inválido para cifrado");
            return null;
        }
        
        try {
            // Simple cifrado para respaldo (en producción usar algoritmo más robusto)
            // Por ahora usamos una simple transformación reversible
            StringBuilder encrypted = new StringBuilder();
            for (char c : pin.toCharArray()) {
                int digit = Character.getNumericValue(c);
                int transformed = (digit + 5) % 10; // Desplazamiento simple
                encrypted.append(transformed);
            }
            
            // Agregar un prefijo para identificar el método de cifrado
            String result = "V1_" + encrypted.toString();
            Log.d(TAG, "PIN cifrado exitosamente para respaldo");
            return result;
            
        } catch (Exception e) {
            Log.e(TAG, "Error al cifrar PIN para respaldo", e);
            return null;
        }
    }
    
    /**
     * Descifra un PIN desde el respaldo en la nube
     * @param encryptedPin PIN cifrado
     * @return PIN descifrado, o null si hay error
     */
    public String decryptPin(String encryptedPin) {
        if (encryptedPin == null || !encryptedPin.startsWith("V1_")) {
            Log.e(TAG, "Formato de PIN cifrado inválido");
            return null;
        }
        
        try {
            // Quitar el prefijo
            String encrypted = encryptedPin.substring(3);
            
            if (encrypted.length() != 4) {
                Log.e(TAG, "Longitud de PIN cifrado inválida");
                return null;
            }
            
            // Descifrar usando la transformación inversa
            StringBuilder decrypted = new StringBuilder();
            for (char c : encrypted.toCharArray()) {
                int digit = Character.getNumericValue(c);
                int original = (digit - 5 + 10) % 10; // Transformación inversa correcta
                decrypted.append(original);
            }
            
            Log.d(TAG, "PIN descifrado exitosamente desde respaldo");
            return decrypted.toString();
            
        } catch (Exception e) {
            Log.e(TAG, "Error al descifrar PIN desde respaldo", e);
            return null;
        }
    }
    
    /**
     * Limpia todas las preferencias cifradas
     * @return true si se limpiaron exitosamente, false en caso contrario
     */
    public boolean clearAllPreferences() {
        try {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            boolean cleared = editor.commit();
            
            if (cleared) {
                Log.d(TAG, "Preferencias limpiadas exitosamente");
            } else {
                Log.e(TAG, "Error al limpiar preferencias");
            }
            
            return cleared;
        } catch (Exception e) {
            Log.e(TAG, "Error al limpiar preferencias", e);
            return false;
        }
    }
    
    /**
     * Obtiene el PIN almacenado (solo para uso interno y sincronización)
     * @return PIN almacenado o null si no existe
     */
    public String getStoredPin() {
        try {
            return sharedPreferences.getString(PIN_KEY, null);
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener PIN almacenado", e);
            return null;
        }
    }
    
    /**
     * Guarda el ID del usuario actual
     * @param userId ID del usuario
     * @return true si se guardó exitosamente
     */
    public boolean setCurrentUserId(String userId) {
        try {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(CURRENT_USER_ID_KEY, userId);
            return editor.commit();
        } catch (Exception e) {
            Log.e(TAG, "Error al guardar ID del usuario", e);
            return false;
        }
    }
    
    /**
     * Obtiene el ID del usuario actual
     * @return ID del usuario o null si no existe
     */
    public String getCurrentUserId() {
        try {
            return sharedPreferences.getString(CURRENT_USER_ID_KEY, null);
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener ID del usuario", e);
            return null;
        }
    }
    
    /**
     * Guarda el ID del último usuario que se autenticó
     * @param userId ID del usuario
     * @return true si se guardó exitosamente
     */
    public boolean saveLastUserId(String userId) {
        try {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(LAST_USER_ID_KEY, userId);
            return editor.commit();
        } catch (Exception e) {
            Log.e(TAG, "Error al guardar último ID de usuario", e);
            return false;
        }
    }
    
    /**
     * Obtiene el ID del último usuario que se autenticó
     * @return ID del último usuario o null si no existe
     */
    public String getLastUserId() {
        try {
            return sharedPreferences.getString(LAST_USER_ID_KEY, null);
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener último ID de usuario", e);
            return null;
        }
    }
    
    /**
     * Guarda una sesión offline activa
     * @param userId ID del usuario de la sesión
     * @return true si se guardó exitosamente
     */
    public boolean saveOfflineSession(String userId) {
        try {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(OFFLINE_SESSION_USER_KEY, userId);
            editor.putLong(OFFLINE_SESSION_TIME_KEY, System.currentTimeMillis());
            return editor.commit();
        } catch (Exception e) {
            Log.e(TAG, "Error al guardar sesión offline", e);
            return false;
        }
    }
    
    /**
     * Obtiene el ID del usuario de la sesión offline activa
     * @return ID del usuario o null si no hay sesión activa
     */
    public String getOfflineSessionUserId() {
        try {
            return sharedPreferences.getString(OFFLINE_SESSION_USER_KEY, null);
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener usuario de sesión offline", e);
            return null;
        }
    }
    
    /**
     * Obtiene el timestamp de inicio de la sesión offline
     * @return timestamp de la sesión o 0 si no hay sesión
     */
    public long getOfflineSessionTime() {
        try {
            return sharedPreferences.getLong(OFFLINE_SESSION_TIME_KEY, 0);
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener tiempo de sesión offline", e);
            return 0;
        }
    }
    
    /**
     * Limpia la sesión offline activa
     */
    public void clearOfflineSession() {
        try {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(OFFLINE_SESSION_USER_KEY);
            editor.remove(OFFLINE_SESSION_TIME_KEY);
            editor.commit();
            Log.d(TAG, "Sesión offline limpiada");
        } catch (Exception e) {
            Log.e(TAG, "Error al limpiar sesión offline", e);
        }
    }
    
    /**
     * Genera un hash del PIN para almacenamiento seguro
     * @param pin PIN a hashear
     * @return Hash del PIN
     */
    public String hashPin(String pin) {
        if (pin == null || pin.length() != 4 || !pin.matches("\\d{4}")) {
            Log.e(TAG, "PIN inválido para hashear");
            return null;
        }
        
        try {
            // Simple hash para almacenamiento local (en producción usar bcrypt o similar)
            int hash = pin.hashCode();
            String hashedPin = "HASH_" + Math.abs(hash);
            Log.d(TAG, "PIN hasheado exitosamente");
            return hashedPin;
        } catch (Exception e) {
            Log.e(TAG, "Error al hashear PIN", e);
            return null;
        }
    }
    
    /**
     * Verifica un PIN contra su hash almacenado
     * @param pin PIN a verificar
     * @param storedHash Hash almacenado
     * @return true si el PIN es correcto
     */
    public boolean verifyPin(String pin, String storedHash) {
        if (pin == null || storedHash == null) {
            return false;
        }
        
        try {
            String pinHash = hashPin(pin);
            boolean isValid = pinHash != null && pinHash.equals(storedHash);
            Log.d(TAG, "Verificación de PIN con hash: " + (isValid ? "exitosa" : "fallida"));
            return isValid;
        } catch (Exception e) {
            Log.e(TAG, "Error al verificar PIN con hash", e);
            return false;
        }
    }
    
    /**
     * Limpia todos los datos de un usuario específico
     * @param userId ID del usuario
     */
    public void clearUserData(String userId) {
        try {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            
            // Limpiar datos específicos del usuario si fuera necesario
            // Por ahora, limpiar datos generales
            if (userId.equals(getCurrentUserId())) {
                editor.remove(CURRENT_USER_ID_KEY);
            }
            
            if (userId.equals(getLastUserId())) {
                editor.remove(LAST_USER_ID_KEY);
            }
            
            if (userId.equals(getOfflineSessionUserId())) {
                clearOfflineSession();
            }
            
            editor.commit();
            Log.d(TAG, "Datos del usuario " + userId + " limpiados");
        } catch (Exception e) {
            Log.e(TAG, "Error al limpiar datos del usuario", e);
        }
    }
}