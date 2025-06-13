package com.pdm.domohouse.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Clase para manejar SharedPreferences generales (no cifradas)
 * Para preferencias no sensibles como configuraciones de temperatura, etc.
 */
public class PreferencesManager {
    
    private static final String TAG = "PreferencesManager";
    private static final String PREFS_FILE_NAME = "domohouse_prefs";
    
    private static PreferencesManager instance;
    private final SharedPreferences sharedPreferences;
    
    /**
     * Constructor privado para Singleton
     * @param context Contexto de la aplicación
     */
    private PreferencesManager(Context context) {
        sharedPreferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE);
    }
    
    /**
     * Obtiene la instancia única del manager
     * @param context Contexto de la aplicación
     * @return La instancia del PreferencesManager
     */
    public static synchronized PreferencesManager getInstance(Context context) {
        if (instance == null) {
            instance = new PreferencesManager(context);
        }
        return instance;
    }
    
    /**
     * Obtiene la instancia usando el contexto de la aplicación
     * @return La instancia del PreferencesManager o null
     */
    public static synchronized PreferencesManager getInstance() {
        if (instance == null) {
            Context appContext = com.pdm.domohouse.DomoHouseApplication.getAppContext();
            if (appContext != null) {
                instance = new PreferencesManager(appContext);
            }
        }
        return instance;
    }
    
    // Métodos para String
    
    public void putString(String key, String value) {
        sharedPreferences.edit().putString(key, value).apply();
    }
    
    public String getString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }
    
    // Métodos para Int
    
    public void putInt(String key, int value) {
        sharedPreferences.edit().putInt(key, value).apply();
    }
    
    public int getInt(String key, int defaultValue) {
        return sharedPreferences.getInt(key, defaultValue);
    }
    
    // Métodos para Long
    
    public void putLong(String key, long value) {
        sharedPreferences.edit().putLong(key, value).apply();
    }
    
    public long getLong(String key, long defaultValue) {
        return sharedPreferences.getLong(key, defaultValue);
    }
    
    // Métodos para Float
    
    public void putFloat(String key, float value) {
        sharedPreferences.edit().putFloat(key, value).apply();
    }
    
    public float getFloat(String key, float defaultValue) {
        return sharedPreferences.getFloat(key, defaultValue);
    }
    
    // Métodos para Boolean
    
    public void putBoolean(String key, boolean value) {
        sharedPreferences.edit().putBoolean(key, value).apply();
    }
    
    public boolean getBoolean(String key, boolean defaultValue) {
        return sharedPreferences.getBoolean(key, defaultValue);
    }
    
    // Métodos de utilidad
    
    public void remove(String key) {
        sharedPreferences.edit().remove(key).apply();
    }
    
    public boolean contains(String key) {
        return sharedPreferences.contains(key);
    }
    
    public void clear() {
        sharedPreferences.edit().clear().apply();
    }
    
    /**
     * Guarda cambios de forma síncrona
     * @return true si se guardó exitosamente
     */
    public boolean commit() {
        return sharedPreferences.edit().commit();
    }
}