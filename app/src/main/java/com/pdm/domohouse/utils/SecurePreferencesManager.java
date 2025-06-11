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
}