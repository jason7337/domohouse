package com.pdm.domohouse.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.pdm.domohouse.data.database.AppDatabase;
import com.pdm.domohouse.data.database.dao.RoomDao;
import com.pdm.domohouse.data.database.entity.RoomEntity;
import com.pdm.domohouse.data.model.Room;
import com.pdm.domohouse.data.model.RoomType;
import com.pdm.domohouse.data.sync.ComprehensiveSyncManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repositorio para manejo de habitaciones de la casa
 * Coordina entre datos locales (Room) y remotos (Firebase)
 */
public class RoomRepository {
    
    private static final String TAG = "RoomRepository";
    
    private final RoomDao roomDao;
    private final ComprehensiveSyncManager syncManager;
    private final ExecutorService executor;
    
    public RoomRepository(Context context) {
        AppDatabase database = AppDatabase.getDatabase(context);
        this.roomDao = database.roomDao();
        this.syncManager = ComprehensiveSyncManager.getInstance(context);
        this.executor = Executors.newSingleThreadExecutor();
    }
    
    /**
     * Obtiene todas las habitaciones
     */
    public LiveData<List<Room>> getAllRooms() {
        MediatorLiveData<List<Room>> result = new MediatorLiveData<>();
        
        LiveData<List<RoomEntity>> localRooms = roomDao.getAllRooms();
        result.addSource(localRooms, entities -> {
            if (entities != null) {
                List<Room> rooms = mapEntitiesToModels(entities);
                result.setValue(rooms);
                
                // Sincronizar en background si hay habitaciones no sincronizadas
                syncUnsyncedRoomsInBackground();
            }
        });
        
        return result;
    }
    
    /**
     * Obtiene una habitación específica
     */
    public LiveData<Room> getRoom(@NonNull String roomId) {
        MediatorLiveData<Room> result = new MediatorLiveData<>();
        
        LiveData<RoomEntity> localRoom = roomDao.getRoom(roomId);
        result.addSource(localRoom, entity -> {
            if (entity != null) {
                Room room = mapEntityToModel(entity);
                result.setValue(room);
            }
        });
        
        return result;
    }
    
    /**
     * Obtiene habitaciones por piso
     */
    public LiveData<List<Room>> getRoomsByFloor(int floor) {
        MediatorLiveData<List<Room>> result = new MediatorLiveData<>();
        
        LiveData<List<RoomEntity>> roomsByFloor = roomDao.getRoomsByFloor(floor);
        result.addSource(roomsByFloor, entities -> {
            if (entities != null) {
                List<Room> rooms = mapEntitiesToModels(entities);
                result.setValue(rooms);
            }
        });
        
        return result;
    }
    
    /**
     * Obtiene habitaciones por tipo
     */
    public LiveData<List<Room>> getRoomsByType(@NonNull RoomType type) {
        MediatorLiveData<List<Room>> result = new MediatorLiveData<>();
        
        LiveData<List<RoomEntity>> roomsByType = roomDao.getRoomsByType(type.name());
        result.addSource(roomsByType, entities -> {
            if (entities != null) {
                List<Room> rooms = mapEntitiesToModels(entities);
                result.setValue(rooms);
            }
        });
        
        return result;
    }
    
    /**
     * Obtiene la lista de pisos disponibles
     */
    public LiveData<List<Integer>> getFloors() {
        return roomDao.getFloors();
    }
    
    /**
     * Obtiene el conteo total de habitaciones
     */
    public LiveData<Integer> getRoomCount() {
        return roomDao.getRoomCount();
    }
    
    /**
     * Agrega una nueva habitación
     */
    public CompletableFuture<Boolean> addRoom(@NonNull Room room) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Validar datos de entrada
                if (!isValidRoom(room)) {
                    Log.w(TAG, "Datos de habitación inválidos");
                    return false;
                }
                
                RoomEntity entity = mapModelToEntity(room);
                entity.setCreatedAt(System.currentTimeMillis());
                entity.setUpdatedAt(System.currentTimeMillis());
                entity.setSynced(false);
                
                roomDao.insert(entity);
                
                // Sincronizar con Firebase si hay conexión
                if (syncManager.isOnline()) {
                    syncRoomToFirebase(room);
                }
                
                Log.d(TAG, "Habitación agregada: " + room.getName());
                return true;
                
            } catch (Exception e) {
                Log.e(TAG, "Error al agregar habitación", e);
                return false;
            }
        }, executor);
    }
    
    /**
     * Actualiza una habitación existente
     */
    public CompletableFuture<Boolean> updateRoom(@NonNull Room room) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (!isValidRoom(room)) {
                    Log.w(TAG, "Datos de habitación inválidos");
                    return false;
                }
                
                RoomEntity entity = mapModelToEntity(room);
                entity.setUpdatedAt(System.currentTimeMillis());
                entity.setSynced(false);
                
                roomDao.update(entity);
                
                // Sincronizar con Firebase si hay conexión
                if (syncManager.isOnline()) {
                    syncRoomToFirebase(room);
                }
                
                Log.d(TAG, "Habitación actualizada: " + room.getName());
                return true;
                
            } catch (Exception e) {
                Log.e(TAG, "Error al actualizar habitación", e);
                return false;
            }
        }, executor);
    }
    
    /**
     * Elimina una habitación
     */
    public CompletableFuture<Boolean> removeRoom(@NonNull String roomId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                RoomEntity entity = roomDao.getRoomSync(roomId);
                if (entity == null) {
                    Log.w(TAG, "Habitación no encontrada: " + roomId);
                    return false;
                }
                
                // Verificar si la habitación tiene dispositivos asociados
                // (Esto se manejará automáticamente por CASCADE en la foreign key)
                
                roomDao.delete(entity);
                
                Log.d(TAG, "Habitación eliminada: " + roomId);
                return true;
                
            } catch (Exception e) {
                Log.e(TAG, "Error al eliminar habitación", e);
                return false;
            }
        }, executor);
    }
    
    /**
     * Actualiza la posición de una habitación en el mapa
     */
    public CompletableFuture<Boolean> updateRoomPosition(@NonNull String roomId, float x, float y) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Validar coordenadas (deben estar entre 0 y 1)
                if (x < 0.0f || x > 1.0f || y < 0.0f || y > 1.0f) {
                    Log.w(TAG, "Coordenadas de posición inválidas: (" + x + ", " + y + ")");
                    return false;
                }
                
                RoomEntity entity = roomDao.getRoomSync(roomId);
                if (entity == null) {
                    Log.w(TAG, "Habitación no encontrada: " + roomId);
                    return false;
                }
                
                entity.setPositionX(x);
                entity.setPositionY(y);
                entity.setUpdatedAt(System.currentTimeMillis());
                entity.setSynced(false);
                
                roomDao.update(entity);
                
                Log.d(TAG, "Posición de habitación actualizada: " + roomId + " -> (" + x + ", " + y + ")");
                return true;
                
            } catch (Exception e) {
                Log.e(TAG, "Error al actualizar posición de habitación", e);
                return false;
            }
        }, executor);
    }
    
    /**
     * Actualiza el color de una habitación
     */
    public CompletableFuture<Boolean> updateRoomColor(@NonNull String roomId, @NonNull String color) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Validar formato de color hex
                if (!isValidColorHex(color)) {
                    Log.w(TAG, "Formato de color inválido: " + color);
                    return false;
                }
                
                RoomEntity entity = roomDao.getRoomSync(roomId);
                if (entity == null) {
                    Log.w(TAG, "Habitación no encontrada: " + roomId);
                    return false;
                }
                
                entity.setColor(color);
                entity.setUpdatedAt(System.currentTimeMillis());
                entity.setSynced(false);
                
                roomDao.update(entity);
                
                Log.d(TAG, "Color de habitación actualizado: " + roomId + " -> " + color);
                return true;
                
            } catch (Exception e) {
                Log.e(TAG, "Error al actualizar color de habitación", e);
                return false;
            }
        }, executor);
    }
    
    /**
     * Crea habitaciones por defecto si no existen
     */
    public CompletableFuture<Boolean> createDefaultRoomsIfNeeded() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<RoomEntity> existingRooms = roomDao.getAllRoomsSync();
                
                if (!existingRooms.isEmpty()) {
                    Log.d(TAG, "Ya existen habitaciones, no se crearán las por defecto");
                    return true;
                }
                
                // Crear habitaciones por defecto
                List<RoomEntity> defaultRooms = createDefaultRoomEntities();
                
                for (RoomEntity room : defaultRooms) {
                    roomDao.insert(room);
                }
                
                Log.d(TAG, "Habitaciones por defecto creadas: " + defaultRooms.size());
                return true;
                
            } catch (Exception e) {
                Log.e(TAG, "Error al crear habitaciones por defecto", e);
                return false;
            }
        }, executor);
    }
    
    /**
     * Sincroniza habitaciones no sincronizadas en background
     */
    private void syncUnsyncedRoomsInBackground() {
        if (!syncManager.isOnline()) {
            return;
        }
        
        executor.execute(() -> {
            try {
                List<RoomEntity> unsyncedRooms = roomDao.getUnsyncedRooms();
                
                for (RoomEntity entity : unsyncedRooms) {
                    Room room = mapEntityToModel(entity);
                    syncRoomToFirebase(room);
                }
                
                if (!unsyncedRooms.isEmpty()) {
                    Log.d(TAG, "Sincronizadas " + unsyncedRooms.size() + " habitaciones pendientes");
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error en sincronización background de habitaciones", e);
            }
        });
    }
    
    /**
     * Sincroniza una habitación con Firebase
     */
    private void syncRoomToFirebase(@NonNull Room room) {
        // TODO: Implementar sincronización con Firebase
        // Por ahora marcar como sincronizado
        executor.execute(() -> {
            long timestamp = System.currentTimeMillis();
            roomDao.markAsSynced(room.getId(), timestamp);
            Log.d(TAG, "Habitación marcada como sincronizada: " + room.getId());
        });
    }
    
    /**
     * Valida los datos de una habitación
     */
    private boolean isValidRoom(@NonNull Room room) {
        if (room.getId() == null || room.getId().trim().isEmpty()) {
            return false;
        }
        
        if (room.getName() == null || room.getName().trim().isEmpty()) {
            return false;
        }
        
        if (room.getType() == null) {
            return false;
        }
        
        if (room.getPositionX() < 0.0f || room.getPositionX() > 1.0f) {
            return false;
        }
        
        if (room.getPositionY() < 0.0f || room.getPositionY() > 1.0f) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Valida el formato de color hexadecimal
     */
    private boolean isValidColorHex(@NonNull String color) {
        return color.matches("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$");
    }
    
    /**
     * Crea las entidades de habitaciones por defecto
     */
    private List<RoomEntity> createDefaultRoomEntities() {
        List<RoomEntity> defaultRooms = new ArrayList<>();
        
        // Sala de estar
        RoomEntity livingRoom = new RoomEntity();
        livingRoom.setRoomId("room_living");
        livingRoom.setName("Sala de Estar");
        livingRoom.setRoomType(RoomType.LIVING_ROOM.name());
        livingRoom.setFloor(0);
        livingRoom.setPositionX(0.3f);
        livingRoom.setPositionY(0.5f);
        livingRoom.setColor("#E5C3A0");
        livingRoom.setIconName("ic_living_room");
        defaultRooms.add(livingRoom);
        
        // Cocina
        RoomEntity kitchen = new RoomEntity();
        kitchen.setRoomId("room_kitchen");
        kitchen.setName("Cocina");
        kitchen.setRoomType(RoomType.KITCHEN.name());
        kitchen.setFloor(0);
        kitchen.setPositionX(0.7f);
        kitchen.setPositionY(0.3f);
        kitchen.setColor("#B8935F");
        kitchen.setIconName("ic_kitchen");
        defaultRooms.add(kitchen);
        
        // Dormitorio principal
        RoomEntity bedroom1 = new RoomEntity();
        bedroom1.setRoomId("room_bedroom1");
        bedroom1.setName("Dormitorio Principal");
        bedroom1.setRoomType(RoomType.BEDROOM.name());
        bedroom1.setFloor(1);
        bedroom1.setPositionX(0.3f);
        bedroom1.setPositionY(0.3f);
        bedroom1.setColor("#D4A574");
        bedroom1.setIconName("ic_bedroom");
        defaultRooms.add(bedroom1);
        
        // Baño
        RoomEntity bathroom = new RoomEntity();
        bathroom.setRoomId("room_bathroom");
        bathroom.setName("Baño");
        bathroom.setRoomType(RoomType.BATHROOM.name());
        bathroom.setFloor(0);
        bathroom.setPositionX(0.7f);
        bathroom.setPositionY(0.7f);
        bathroom.setColor("#1A1F2E");
        bathroom.setIconName("ic_bathroom");
        defaultRooms.add(bathroom);
        
        return defaultRooms;
    }
    
    // Métodos de mapeo entre entidades y modelos
    
    private List<Room> mapEntitiesToModels(@NonNull List<RoomEntity> entities) {
        List<Room> rooms = new ArrayList<>();
        for (RoomEntity entity : entities) {
            rooms.add(mapEntityToModel(entity));
        }
        return rooms;
    }
    
    private Room mapEntityToModel(@NonNull RoomEntity entity) {
        Room room = new Room(entity.getRoomId(), entity.getName(), 
                             RoomType.valueOf(entity.getRoomType()), 
                             entity.getPositionX(), entity.getPositionY(), 1.0f, 1.0f);
        // Note: Room model doesn't have all the same fields as RoomEntity
        // This is a simplified mapping for the core functionality
        return room;
    }
    
    private RoomEntity mapModelToEntity(@NonNull Room room) {
        RoomEntity entity = new RoomEntity();
        entity.setRoomId(room.getId());
        entity.setName(room.getName());
        entity.setRoomType(room.getType().name());
        entity.setFloor(0); // Default floor
        entity.setPositionX(room.getPositionX());
        entity.setPositionY(room.getPositionY());
        entity.setIconName("ic_" + room.getType().name().toLowerCase());
        entity.setColor("#E5C3A0"); // Default color
        entity.setUpdatedAt(System.currentTimeMillis());
        entity.setLastSync(0);
        return entity;
    }
    
    /**
     * Limpia recursos
     */
    public void cleanup() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}