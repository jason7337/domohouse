package com.pdm.domohouse;

import android.app.Application;
import android.content.Context;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Clase Application principal de DomoHouse
 * Maneja la inicialización de componentes globales de la aplicación
 */
public class DomoHouseApplication extends Application {
    
    private static DomoHouseApplication instance;
    
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        
        // Inicializar Firebase
        FirebaseApp.initializeApp(this);
        
        // Habilitar persistencia offline de Firebase
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        } catch (Exception e) {
            // Ya está habilitada
        }
    }
    
    /**
     * Obtiene la instancia de la aplicación
     * @return Instancia de DomoHouseApplication
     */
    public static DomoHouseApplication getInstance() {
        return instance;
    }
    
    /**
     * Obtiene el contexto de la aplicación
     * @return Context de la aplicación
     */
    public static Context getAppContext() {
        return instance != null ? instance.getApplicationContext() : null;
    }
}