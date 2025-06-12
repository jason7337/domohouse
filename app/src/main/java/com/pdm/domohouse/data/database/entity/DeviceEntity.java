package com.pdm.domohouse.data.database.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entidad de Room para almacenar dispositivos del hogar
 * Cada dispositivo pertenece a una habitación específica
 */
@Entity(tableName = "devices",
        foreignKeys = @ForeignKey(
                entity = RoomEntity.class,
                parentColumns = "room_id",
                childColumns = "room_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("room_id"), @Index("device_type")})
public class DeviceEntity {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "device_id")
    private String deviceId;
    
    @ColumnInfo(name = "room_id")
    private String roomId;
    
    @ColumnInfo(name = "name")
    private String name;
    
    @ColumnInfo(name = "device_type")
    private String deviceType; // LIGHT, FAN, DOOR, WINDOW, SMOKE_SENSOR, etc.
    
    @ColumnInfo(name = "is_on")
    private boolean isOn;
    
    @ColumnInfo(name = "intensity")
    private int intensity; // 0-100 para luces/ventiladores
    
    @ColumnInfo(name = "temperature")
    private Float temperature; // Para sensores de temperatura
    
    @ColumnInfo(name = "is_online")
    private boolean isOnline;
    
    @ColumnInfo(name = "last_state_change")
    private long lastStateChange;
    
    @ColumnInfo(name = "created_at")
    private long createdAt;
    
    @ColumnInfo(name = "updated_at")
    private long updatedAt;
    
    @ColumnInfo(name = "last_sync")
    private long lastSync;
    
    @ColumnInfo(name = "is_synced")
    private boolean isSynced;
    
    @ColumnInfo(name = "hardware_id")
    private String hardwareId; // ID del ESP32 asociado
    
    @ColumnInfo(name = "pin_number")
    private Integer pinNumber; // Pin GPIO en el ESP32
    
    // Constructor
    public DeviceEntity() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.lastStateChange = System.currentTimeMillis();
        this.lastSync = 0;
        this.isSynced = false;
        this.isOnline = false;
        this.isOn = false;
        this.intensity = 0;
    }
    
    // Getters y Setters
    @NonNull
    public String getDeviceId() {
        return deviceId;
    }
    
    public void setDeviceId(@NonNull String deviceId) {
        this.deviceId = deviceId;
    }
    
    public String getRoomId() {
        return roomId;
    }
    
    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDeviceType() {
        return deviceType;
    }
    
    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }
    
    public boolean isOn() {
        return isOn;
    }
    
    public void setOn(boolean on) {
        isOn = on;
    }
    
    public int getIntensity() {
        return intensity;
    }
    
    public void setIntensity(int intensity) {
        this.intensity = intensity;
    }
    
    public Float getTemperature() {
        return temperature;
    }
    
    public void setTemperature(Float temperature) {
        this.temperature = temperature;
    }
    
    public boolean isOnline() {
        return isOnline;
    }
    
    public void setOnline(boolean online) {
        isOnline = online;
    }
    
    public long getLastStateChange() {
        return lastStateChange;
    }
    
    public void setLastStateChange(long lastStateChange) {
        this.lastStateChange = lastStateChange;
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
    
    public long getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public long getLastSync() {
        return lastSync;
    }
    
    public void setLastSync(long lastSync) {
        this.lastSync = lastSync;
    }
    
    public boolean isSynced() {
        return isSynced;
    }
    
    public void setSynced(boolean synced) {
        isSynced = synced;
    }
    
    public String getHardwareId() {
        return hardwareId;
    }
    
    public void setHardwareId(String hardwareId) {
        this.hardwareId = hardwareId;
    }
    
    public Integer getPinNumber() {
        return pinNumber;
    }
    
    public void setPinNumber(Integer pinNumber) {
        this.pinNumber = pinNumber;
    }
}