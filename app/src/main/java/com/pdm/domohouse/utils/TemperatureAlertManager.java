package com.pdm.domohouse.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.pdm.domohouse.R;
import com.pdm.domohouse.MainActivity;
import com.pdm.domohouse.data.model.RoomTemperature;
import com.pdm.domohouse.data.model.TemperatureThreshold;

/**
 * Gestor de alertas de temperatura que maneja notificaciones locales
 * cuando las temperaturas están fuera de los rangos normales
 */
public class TemperatureAlertManager {
    
    private static final String TAG = "TemperatureAlertManager";
    private static final String CHANNEL_ID = "temperature_alerts";
    private static final String CHANNEL_NAME = "Alertas de Temperatura";
    private static final String CHANNEL_DESCRIPTION = "Notificaciones cuando la temperatura está fuera del rango normal";
    
    private final Context context;
    private final NotificationManagerCompat notificationManager;
    private final PreferencesManager preferencesManager;
    
    // IDs de notificación basados en el hash del roomId para evitar duplicados
    private static final int NOTIFICATION_ID_BASE = 2000;
    
    /**
     * Constructor privado para patrón Singleton
     */
    private TemperatureAlertManager(Context context) {
        this.context = context.getApplicationContext();
        this.notificationManager = NotificationManagerCompat.from(this.context);
        this.preferencesManager = PreferencesManager.getInstance(context);
        
        createNotificationChannel();
    }
    
    private static TemperatureAlertManager instance;
    
    /**
     * Obtiene la instancia singleton del gestor de alertas
     */
    public static synchronized TemperatureAlertManager getInstance(Context context) {
        if (instance == null) {
            instance = new TemperatureAlertManager(context);
        }
        return instance;
    }
    
    /**
     * Crea el canal de notificación para alertas de temperatura (Android 8.0+)
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
    
    /**
     * Evalúa la temperatura de una habitación y envía alertas si es necesario
     * @param roomTemperature datos de temperatura de la habitación
     * @param threshold configuración de umbrales
     */
    public void evaluateTemperatureAlert(RoomTemperature roomTemperature, TemperatureThreshold threshold) {
        if (!areAlertsEnabled() || roomTemperature == null || threshold == null) {
            return;
        }
        
        if (!roomTemperature.getIsOnline()) {
            // Enviar alerta de dispositivo desconectado si es importante
            if (shouldAlertForOfflineDevice(roomTemperature)) {
                sendOfflineDeviceAlert(roomTemperature);
            }
            return;
        }
        
        float temperature = roomTemperature.getCurrentTemperature();
        RoomTemperature.TemperatureStatus status = threshold.evaluateTemperature(temperature);
        RoomTemperature.TemperatureStatus previousStatus = getPreviousTemperatureStatus(roomTemperature.getRoomId());
        
        // Solo enviar alerta si el estado cambió
        if (status != previousStatus) {
            sendTemperatureAlert(roomTemperature, status, threshold);
            savePreviousTemperatureStatus(roomTemperature.getRoomId(), status);
        }
    }
    
    /**
     * Envía una notificación de alerta de temperatura
     */
    private void sendTemperatureAlert(RoomTemperature roomTemperature, 
                                    RoomTemperature.TemperatureStatus status,
                                    TemperatureThreshold threshold) {
        
        String title;
        String message;
        int iconResource;
        int priority;
        
        switch (status) {
            case CRITICAL_HIGH:
                title = context.getString(R.string.temp_alert_high_title);
                message = context.getString(R.string.temp_alert_high_message, 
                    roomTemperature.getRoomName(), roomTemperature.getCurrentTemperature());
                iconResource = R.drawable.ic_thermometer;
                priority = NotificationCompat.PRIORITY_HIGH;
                break;
                
            case CRITICAL_LOW:
                title = context.getString(R.string.temp_alert_low_title);
                message = context.getString(R.string.temp_alert_low_message,
                    roomTemperature.getRoomName(), roomTemperature.getCurrentTemperature());
                iconResource = R.drawable.ic_thermometer;
                priority = NotificationCompat.PRIORITY_HIGH;
                break;
                
            case HIGH:
                title = "Temperatura Elevada";
                message = String.format("La temperatura en %s es de %.1f°C (por encima del límite de %.1f°C)",
                    roomTemperature.getRoomName(), roomTemperature.getCurrentTemperature(), threshold.getMaxTemperature());
                iconResource = R.drawable.ic_thermometer;
                priority = NotificationCompat.PRIORITY_DEFAULT;
                break;
                
            case LOW:
                title = "Temperatura Baja";
                message = String.format("La temperatura en %s es de %.1f°C (por debajo del límite de %.1f°C)",
                    roomTemperature.getRoomName(), roomTemperature.getCurrentTemperature(), threshold.getMinTemperature());
                iconResource = R.drawable.ic_thermometer;
                priority = NotificationCompat.PRIORITY_DEFAULT;
                break;
                
            case NORMAL:
                title = context.getString(R.string.temp_alert_normal_title);
                message = context.getString(R.string.temp_alert_normal_message,
                    roomTemperature.getRoomName(), roomTemperature.getCurrentTemperature());
                iconResource = R.drawable.ic_check_circle;
                priority = NotificationCompat.PRIORITY_LOW;
                break;
                
            default:
                return; // No enviar notificación para estados desconocidos
        }
        
        sendNotification(roomTemperature.getRoomId(), title, message, iconResource, priority);
    }
    
    /**
     * Envía una alerta de dispositivo desconectado
     */
    private void sendOfflineDeviceAlert(RoomTemperature roomTemperature) {
        String title = "Sensor Desconectado";
        String message = String.format("El sensor de temperatura en %s está desconectado", 
            roomTemperature.getRoomName());
        
        sendNotification(roomTemperature.getRoomId() + "_offline", title, message, 
            R.drawable.ic_wifi_off, NotificationCompat.PRIORITY_DEFAULT);
    }
    
    /**
     * Envía una notificación al sistema
     */
    private void sendNotification(String notificationId, String title, String message, 
                                int iconResource, int priority) {
        
        // Intent para abrir la aplicación al tocar la notificación
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 0, intent, 
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE : 
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        
        // Construir la notificación
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(iconResource)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(priority)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true);
        
        // Configurar vibración y sonido según la prioridad
        if (priority == NotificationCompat.PRIORITY_HIGH) {
            builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        }
        
        // Enviar notificación
        int notificationIdInt = getNotificationId(notificationId);
        notificationManager.notify(notificationIdInt, builder.build());
    }
    
    /**
     * Obtiene el estado de temperatura anterior para una habitación
     */
    private RoomTemperature.TemperatureStatus getPreviousTemperatureStatus(String roomId) {
        String statusString = preferencesManager.getString("temp_status_" + roomId, "NORMAL");
        try {
            return RoomTemperature.TemperatureStatus.valueOf(statusString);
        } catch (IllegalArgumentException e) {
            return RoomTemperature.TemperatureStatus.NORMAL;
        }
    }
    
    /**
     * Guarda el estado de temperatura actual para comparaciones futuras
     */
    private void savePreviousTemperatureStatus(String roomId, RoomTemperature.TemperatureStatus status) {
        preferencesManager.putString("temp_status_" + roomId, status.name());
    }
    
    /**
     * Verifica si las alertas de temperatura están habilitadas
     */
    private boolean areAlertsEnabled() {
        return preferencesManager.getBoolean("temperature_alerts_enabled", true);
    }
    
    /**
     * Verifica si se debe alertar sobre un dispositivo desconectado
     */
    private boolean shouldAlertForOfflineDevice(RoomTemperature roomTemperature) {
        // Solo alertar una vez por dispositivo desconectado
        String key = "offline_alerted_" + roomTemperature.getRoomId();
        long lastAlerted = preferencesManager.getLong(key, 0);
        long now = System.currentTimeMillis();
        
        // Alertar cada 24 horas para dispositivos desconectados
        if (now - lastAlerted > 24 * 60 * 60 * 1000) {
            preferencesManager.putLong(key, now);
            return true;
        }
        
        return false;
    }
    
    /**
     * Genera un ID único para la notificación basado en el identificador
     */
    private int getNotificationId(String identifier) {
        return NOTIFICATION_ID_BASE + Math.abs(identifier.hashCode() % 1000);
    }
    
    /**
     * Habilita o deshabilita las alertas de temperatura
     */
    public void setAlertsEnabled(boolean enabled) {
        preferencesManager.putBoolean("temperature_alerts_enabled", enabled);
    }
    
    /**
     * Cancela todas las notificaciones de temperatura
     */
    public void cancelAllTemperatureAlerts() {
        notificationManager.cancelAll();
    }
    
    /**
     * Cancela una notificación específica para una habitación
     */
    public void cancelAlertForRoom(String roomId) {
        int notificationId = getNotificationId(roomId);
        notificationManager.cancel(notificationId);
    }
}