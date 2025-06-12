package com.pdm.domohouse.data.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.pdm.domohouse.data.database.entity.DeviceHistoryEntity;

import java.util.List;

/**
 * DAO para operaciones del historial de dispositivos
 * Permite análisis de uso y generación de reportes
 */
@Dao
public interface DeviceHistoryDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(DeviceHistoryEntity history);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<DeviceHistoryEntity> historyList);
    
    @Query("SELECT * FROM device_history WHERE device_id = :deviceId ORDER BY timestamp DESC LIMIT :limit")
    LiveData<List<DeviceHistoryEntity>> getDeviceHistory(String deviceId, int limit);
    
    @Query("SELECT * FROM device_history WHERE device_id = :deviceId AND timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    LiveData<List<DeviceHistoryEntity>> getDeviceHistoryByDateRange(String deviceId, long startTime, long endTime);
    
    @Query("SELECT * FROM device_history WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    LiveData<List<DeviceHistoryEntity>> getRecentHistory(long startTime);
    
    @Query("SELECT * FROM device_history WHERE action = :action ORDER BY timestamp DESC LIMIT :limit")
    LiveData<List<DeviceHistoryEntity>> getHistoryByAction(String action, int limit);
    
    @Query("SELECT * FROM device_history WHERE user_id = :userId ORDER BY timestamp DESC LIMIT :limit")
    LiveData<List<DeviceHistoryEntity>> getUserHistory(String userId, int limit);
    
    @Query("SELECT * FROM device_history WHERE is_synced = 0 ORDER BY timestamp")
    List<DeviceHistoryEntity> getUnsyncedHistory();
    
    @Query("UPDATE device_history SET is_synced = 1, sync_timestamp = :timestamp WHERE history_id IN (:historyIds)")
    void markAsSynced(List<Long> historyIds, long timestamp);
    
    @Query("DELETE FROM device_history WHERE timestamp < :timestamp")
    void deleteOldHistory(long timestamp);
    
    @Query("SELECT COUNT(*) FROM device_history WHERE device_id = :deviceId AND action = :action AND timestamp >= :startTime")
    int getActionCount(String deviceId, String action, long startTime);
    
    @Query("SELECT AVG(CAST(new_value AS FLOAT)) FROM device_history WHERE device_id = :deviceId AND action = 'TEMPERATURE_READING' AND timestamp >= :startTime")
    Float getAverageTemperature(String deviceId, long startTime);
    
    @Query("DELETE FROM device_history")
    void deleteAll();
}