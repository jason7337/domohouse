package com.pdm.domohouse.data.database.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entidad de Room para almacenar las habitaciones de la casa
 * Cada habitación puede contener múltiples dispositivos
 */
@Entity(tableName = "rooms")
public class RoomEntity {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "room_id")
    private String roomId;
    
    @ColumnInfo(name = "name")
    private String name;
    
    @ColumnInfo(name = "room_type")
    private String roomType; // LIVING_ROOM, BEDROOM, KITCHEN, BATHROOM, etc.
    
    @ColumnInfo(name = "floor")
    private int floor; // Piso donde está la habitación
    
    @ColumnInfo(name = "position_x")
    private float positionX; // Posición X en el mapa de la casa (0-1)
    
    @ColumnInfo(name = "position_y")
    private float positionY; // Posición Y en el mapa de la casa (0-1)
    
    @ColumnInfo(name = "icon_name")
    private String iconName; // Nombre del ícono a mostrar
    
    @ColumnInfo(name = "color")
    private String color; // Color hex de la habitación
    
    @ColumnInfo(name = "created_at")
    private long createdAt;
    
    @ColumnInfo(name = "updated_at")
    private long updatedAt;
    
    @ColumnInfo(name = "last_sync")
    private long lastSync;
    
    @ColumnInfo(name = "is_synced")
    private boolean isSynced;
    
    // Constructor
    public RoomEntity() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.lastSync = 0;
        this.isSynced = false;
        this.floor = 0;
        this.positionX = 0.5f;
        this.positionY = 0.5f;
    }
    
    // Getters y Setters
    @NonNull
    public String getRoomId() {
        return roomId;
    }
    
    public void setRoomId(@NonNull String roomId) {
        this.roomId = roomId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getRoomType() {
        return roomType;
    }
    
    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }
    
    public int getFloor() {
        return floor;
    }
    
    public void setFloor(int floor) {
        this.floor = floor;
    }
    
    public float getPositionX() {
        return positionX;
    }
    
    public void setPositionX(float positionX) {
        this.positionX = positionX;
    }
    
    public float getPositionY() {
        return positionY;
    }
    
    public void setPositionY(float positionY) {
        this.positionY = positionY;
    }
    
    public String getIconName() {
        return iconName;
    }
    
    public void setIconName(String iconName) {
        this.iconName = iconName;
    }
    
    public String getColor() {
        return color;
    }
    
    public void setColor(String color) {
        this.color = color;
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
}