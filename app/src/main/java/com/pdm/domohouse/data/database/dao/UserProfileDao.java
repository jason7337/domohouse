package com.pdm.domohouse.data.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.pdm.domohouse.data.database.entity.UserProfileEntity;

import java.util.List;

/**
 * DAO para operaciones de base de datos relacionadas con perfiles de usuario
 * Soporta operaciones offline y sincronización
 */
@Dao
public interface UserProfileDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserProfileEntity userProfile);
    
    @Update
    void update(UserProfileEntity userProfile);
    
    @Delete
    void delete(UserProfileEntity userProfile);
    
    @Query("SELECT * FROM user_profiles WHERE user_id = :userId")
    LiveData<UserProfileEntity> getUserProfile(String userId);
    
    @Query("SELECT * FROM user_profiles WHERE user_id = :userId")
    UserProfileEntity getUserProfileSync(String userId);
    
    @Query("SELECT * FROM user_profiles WHERE email = :email")
    UserProfileEntity getUserProfileByEmail(String email);
    
    @Query("SELECT * FROM user_profiles WHERE is_synced = 0")
    List<UserProfileEntity> getUnsyncedProfiles();
    
    @Query("UPDATE user_profiles SET is_synced = 1, last_sync = :timestamp WHERE user_id = :userId")
    void markAsSynced(String userId, long timestamp);
    
    @Query("UPDATE user_profiles SET pin_hash = :pinHash, updated_at = :timestamp WHERE user_id = :userId")
    void updatePin(String userId, String pinHash, long timestamp);
    
    @Query("UPDATE user_profiles SET photo_url = :photoUrl, updated_at = :timestamp WHERE user_id = :userId")
    void updatePhotoUrl(String userId, String photoUrl, long timestamp);
    
    @Query("DELETE FROM user_profiles")
    void deleteAll();
    
    @Query("SELECT COUNT(*) FROM user_profiles")
    int getUserCount();
}