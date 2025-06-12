package com.pdm.domohouse.data.database.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entidad de Room para almacenar el historial de cambios de estado de dispositivos
 * Útil para reportes y análisis de uso
 */
@Entity(tableName = "device_history",
        foreignKeys = @ForeignKey(
                entity = DeviceEntity.class,
                parentColumns = "device_id",
                childColumns = "device_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("device_id"), @Index("timestamp")})
public class DeviceHistoryEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "history_id")
    private long historyId;
    
    @NonNull
    @ColumnInfo(name = "device_id")
    private String deviceId;
    
    @ColumnInfo(name = "action")
    private String action; // ON, OFF, INTENSITY_CHANGE, TEMPERATURE_READING, etc.
    
    @ColumnInfo(name = "old_value")
    private String oldValue;
    
    @ColumnInfo(name = "new_value")
    private String newValue;
    
    @ColumnInfo(name = "timestamp")
    private long timestamp;
    
    @ColumnInfo(name = "triggered_by")
    private String triggeredBy; // USER, SCHEDULE, AUTOMATION, SENSOR
    
    @ColumnInfo(name = "user_id")
    private String userId; // ID del usuario que realizó la acción (si aplica)
    
    @ColumnInfo(name = "is_synced")
    private boolean isSynced;
    
    @ColumnInfo(name = "sync_timestamp")
    private long syncTimestamp;
    
    // Constructor
    public DeviceHistoryEntity() {
        this.timestamp = System.currentTimeMillis();
        this.isSynced = false;
        this.syncTimestamp = 0;
    }
    
    // Getters y Setters
    public long getHistoryId() {
        return historyId;
    }
    
    public void setHistoryId(long historyId) {
        this.historyId = historyId;
    }
    
    @NonNull
    public String getDeviceId() {
        return deviceId;
    }
    
    public void setDeviceId(@NonNull String deviceId) {
        this.deviceId = deviceId;
    }
    
    public String getAction() {
        return action;
    }
    
    public void setAction(String action) {
        this.action = action;
    }
    
    public String getOldValue() {
        return oldValue;
    }
    
    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }
    
    public String getNewValue() {
        return newValue;
    }
    
    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getTriggeredBy() {
        return triggeredBy;
    }
    
    public void setTriggeredBy(String triggeredBy) {
        this.triggeredBy = triggeredBy;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public boolean isSynced() {
        return isSynced;
    }
    
    public void setSynced(boolean synced) {
        isSynced = synced;
    }
    
    public long getSyncTimestamp() {
        return syncTimestamp;
    }
    
    public void setSyncTimestamp(long syncTimestamp) {
        this.syncTimestamp = syncTimestamp;
    }
}