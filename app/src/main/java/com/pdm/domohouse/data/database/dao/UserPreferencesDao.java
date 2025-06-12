package com.pdm.domohouse.data.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.pdm.domohouse.data.database.entity.UserPreferencesEntity;

/**
 * DAO para operaciones de preferencias de usuario
 * Gestiona configuraciones personalizadas y automatizaciones
 */
@Dao
public interface UserPreferencesDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserPreferencesEntity preferences);
    
    @Update
    void update(UserPreferencesEntity preferences);
    
    @Query("SELECT * FROM user_preferences WHERE user_id = :userId")
    LiveData<UserPreferencesEntity> getUserPreferences(String userId);
    
    @Query("SELECT * FROM user_preferences WHERE user_id = :userId")
    UserPreferencesEntity getUserPreferencesSync(String userId);
    
    @Query("UPDATE user_preferences SET language = :language, updated_at = :timestamp WHERE user_id = :userId")
    void updateLanguage(String userId, String language, long timestamp);
    
    @Query("UPDATE user_preferences SET notifications_enabled = :enabled, updated_at = :timestamp WHERE user_id = :userId")
    void updateNotifications(String userId, boolean enabled, long timestamp);
    
    @Query("UPDATE user_preferences SET eco_mode = :enabled, updated_at = :timestamp WHERE user_id = :userId")
    void updateEcoMode(String userId, boolean enabled, long timestamp);
    
    @Query("UPDATE user_preferences SET temperature_unit = :unit, updated_at = :timestamp WHERE user_id = :userId")
    void updateTemperatureUnit(String userId, String unit, long timestamp);
    
    @Query("UPDATE user_preferences SET is_synced = 1, last_sync = :timestamp WHERE user_id = :userId")
    void markAsSynced(String userId, long timestamp);
    
    @Query("SELECT * FROM user_preferences WHERE is_synced = 0")
    UserPreferencesEntity getUnsyncedPreferences();
    
    @Query("DELETE FROM user_preferences WHERE user_id = :userId")
    void delete(String userId);
}