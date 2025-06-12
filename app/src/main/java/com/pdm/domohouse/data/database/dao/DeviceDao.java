package com.pdm.domohouse.data.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.pdm.domohouse.data.database.entity.DeviceEntity;

import java.util.List;

/**
 * DAO para operaciones de base de datos relacionadas con dispositivos
 * Incluye queries para cache inteligente y sincronización
 */
@Dao
public interface DeviceDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(DeviceEntity device);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<DeviceEntity> devices);
    
    @Update
    void update(DeviceEntity device);
    
    @Delete
    void delete(DeviceEntity device);
    
    @Query("SELECT * FROM devices WHERE device_id = :deviceId")
    LiveData<DeviceEntity> getDevice(String deviceId);
    
    @Query("SELECT * FROM devices WHERE device_id = :deviceId")
    DeviceEntity getDeviceSync(String deviceId);
    
    @Query("SELECT * FROM devices WHERE room_id = :roomId ORDER BY name")
    LiveData<List<DeviceEntity>> getDevicesByRoom(String roomId);
    
    @Query("SELECT * FROM devices WHERE room_id = :roomId ORDER BY name")
    List<DeviceEntity> getDevicesByRoomSync(String roomId);
    
    @Query("SELECT * FROM devices ORDER BY room_id, name")
    LiveData<List<DeviceEntity>> getAllDevices();
    
    @Query("SELECT * FROM devices WHERE device_type = :type ORDER BY name")
    LiveData<List<DeviceEntity>> getDevicesByType(String type);
    
    @Query("SELECT * FROM devices WHERE is_on = 1")
    LiveData<List<DeviceEntity>> getActiveDevices();
    
    @Query("SELECT * FROM devices WHERE is_online = 0")
    LiveData<List<DeviceEntity>> getOfflineDevices();
    
    @Query("SELECT * FROM devices WHERE is_synced = 0")
    List<DeviceEntity> getUnsyncedDevices();
    
    @Query("UPDATE devices SET is_synced = 1, last_sync = :timestamp WHERE device_id = :deviceId")
    void markAsSynced(String deviceId, long timestamp);
    
    @Query("UPDATE devices SET is_on = :isOn, last_state_change = :timestamp, updated_at = :timestamp WHERE device_id = :deviceId")
    void updateDeviceState(String deviceId, boolean isOn, long timestamp);
    
    @Query("UPDATE devices SET intensity = :intensity, updated_at = :timestamp WHERE device_id = :deviceId")
    void updateDeviceIntensity(String deviceId, int intensity, long timestamp);
    
    @Query("UPDATE devices SET temperature = :temperature, updated_at = :timestamp WHERE device_id = :deviceId")
    void updateDeviceTemperature(String deviceId, float temperature, long timestamp);
    
    @Query("UPDATE devices SET is_online = :isOnline, updated_at = :timestamp WHERE device_id = :deviceId")
    void updateDeviceOnlineStatus(String deviceId, boolean isOnline, long timestamp);
    
    @Transaction
    @Query("UPDATE devices SET is_on = :isOn, intensity = :intensity, last_state_change = :timestamp, updated_at = :timestamp WHERE device_id = :deviceId")
    void updateDeviceStateAndIntensity(String deviceId, boolean isOn, int intensity, long timestamp);
    
    @Query("SELECT COUNT(*) FROM devices WHERE is_on = 1")
    LiveData<Integer> getActiveDeviceCount();
    
    @Query("SELECT COUNT(*) FROM devices WHERE device_type = :type AND is_on = 1")
    LiveData<Integer> getActiveDeviceCountByType(String type);
    
    @Query("DELETE FROM devices")
    void deleteAll();
    
    @Query("DELETE FROM devices WHERE room_id = :roomId")
    void deleteDevicesByRoom(String roomId);
}