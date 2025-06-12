package com.pdm.domohouse.data.database.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entidad de Room para almacenar el perfil del usuario localmente
 * Permite funcionalidad offline después de la primera sincronización
 */
@Entity(tableName = "user_profiles")
public class UserProfileEntity {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "user_id")
    private String userId;
    
    @ColumnInfo(name = "name")
    private String name;
    
    @ColumnInfo(name = "email")
    private String email;
    
    @ColumnInfo(name = "photo_url")
    private String photoUrl;
    
    @ColumnInfo(name = "pin_hash")
    private String pinHash;
    
    @ColumnInfo(name = "created_at")
    private long createdAt;
    
    @ColumnInfo(name = "updated_at")
    private long updatedAt;
    
    @ColumnInfo(name = "last_sync")
    private long lastSync;
    
    @ColumnInfo(name = "is_synced")
    private boolean isSynced;
    
    // Constructor
    public UserProfileEntity() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.lastSync = 0;
        this.isSynced = false;
    }
    
    // Getters y Setters
    @NonNull
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(@NonNull String userId) {
        this.userId = userId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhotoUrl() {
        return photoUrl;
    }
    
    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
    
    public String getPinHash() {
        return pinHash;
    }
    
    public void setPinHash(String pinHash) {
        this.pinHash = pinHash;
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