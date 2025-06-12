package com.pdm.domohouse.data.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.pdm.domohouse.data.database.entity.RoomEntity;

import java.util.List;

/**
 * DAO para operaciones de base de datos relacionadas con habitaciones
 * Gestiona la estructura de la casa y ubicación de dispositivos
 */
@Dao
public interface RoomDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(RoomEntity room);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<RoomEntity> rooms);
    
    @Update
    void update(RoomEntity room);
    
    @Delete
    void delete(RoomEntity room);
    
    @Query("SELECT * FROM rooms WHERE room_id = :roomId")
    LiveData<RoomEntity> getRoom(String roomId);
    
    @Query("SELECT * FROM rooms WHERE room_id = :roomId")
    RoomEntity getRoomSync(String roomId);
    
    @Query("SELECT * FROM rooms ORDER BY floor, name")
    LiveData<List<RoomEntity>> getAllRooms();
    
    @Query("SELECT * FROM rooms ORDER BY floor, name")
    List<RoomEntity> getAllRoomsSync();
    
    @Query("SELECT * FROM rooms WHERE floor = :floor ORDER BY name")
    LiveData<List<RoomEntity>> getRoomsByFloor(int floor);
    
    @Query("SELECT * FROM rooms WHERE room_type = :type ORDER BY name")
    LiveData<List<RoomEntity>> getRoomsByType(String type);
    
    @Query("SELECT * FROM rooms WHERE is_synced = 0")
    List<RoomEntity> getUnsyncedRooms();
    
    @Query("UPDATE rooms SET is_synced = 1, last_sync = :timestamp WHERE room_id = :roomId")
    void markAsSynced(String roomId, long timestamp);
    
    @Query("SELECT COUNT(*) FROM rooms")
    LiveData<Integer> getRoomCount();
    
    @Query("SELECT DISTINCT floor FROM rooms ORDER BY floor")
    LiveData<List<Integer>> getFloors();
    
    @Query("DELETE FROM rooms")
    void deleteAll();
}