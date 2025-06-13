package com.pdm.domohouse.data.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.pdm.domohouse.data.database.converter.DateConverter;
import com.pdm.domohouse.data.database.dao.DeviceDao;
import com.pdm.domohouse.data.database.dao.DeviceHistoryDao;
import com.pdm.domohouse.data.database.dao.RoomDao;
import com.pdm.domohouse.data.database.dao.UserPreferencesDao;
import com.pdm.domohouse.data.database.dao.UserProfileDao;
import com.pdm.domohouse.data.database.entity.DeviceEntity;
import com.pdm.domohouse.data.database.entity.DeviceHistoryEntity;
import com.pdm.domohouse.data.database.entity.RoomEntity;
import com.pdm.domohouse.data.database.entity.UserPreferencesEntity;
import com.pdm.domohouse.data.database.entity.UserProfileEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Base de datos principal de la aplicación DomoHouse
 * Gestiona todas las entidades y proporciona acceso a los DAOs
 */
@Database(entities = {
        UserProfileEntity.class,
        UserPreferencesEntity.class,
        RoomEntity.class,
        DeviceEntity.class,
        DeviceHistoryEntity.class
}, version = 1, exportSchema = false)
@TypeConverters({DateConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    
    // DAOs
    public abstract UserProfileDao userProfileDao();
    public abstract UserPreferencesDao userPreferencesDao();
    public abstract RoomDao roomDao();
    public abstract DeviceDao deviceDao();
    public abstract DeviceHistoryDao deviceHistoryDao();
    
    // Singleton
    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    
    // Executor para operaciones de base de datos
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    
    /**
     * Obtiene la instancia única de la base de datos
     */
    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "domohouse_database")
                            .addCallback(sRoomDatabaseCallback)
                            .addMigrations(MIGRATION_1_2) // Preparado para futuras migraciones
                            .build();
                }
            }
        }
        return INSTANCE;
    }
    
    /**
     * Alias para getDatabase para compatibilidad
     */
    public static AppDatabase getInstance(final Context context) {
        return getDatabase(context);
    }
    
    /**
     * Callback para inicializar la base de datos con datos por defecto si es necesario
     */
    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            
            // Inicializar con habitaciones por defecto si es necesario
            databaseWriteExecutor.execute(() -> {
                // Aquí se pueden agregar datos iniciales si es necesario
                RoomDao roomDao = INSTANCE.roomDao();
                
                // Crear habitaciones por defecto
                RoomEntity livingRoom = new RoomEntity();
                livingRoom.setRoomId("room_living");
                livingRoom.setName("Sala de Estar");
                livingRoom.setRoomType("LIVING_ROOM");
                livingRoom.setFloor(0);
                livingRoom.setPositionX(0.3f);
                livingRoom.setPositionY(0.5f);
                livingRoom.setColor("#E5C3A0");
                roomDao.insert(livingRoom);
                
                RoomEntity kitchen = new RoomEntity();
                kitchen.setRoomId("room_kitchen");
                kitchen.setName("Cocina");
                kitchen.setRoomType("KITCHEN");
                kitchen.setFloor(0);
                kitchen.setPositionX(0.7f);
                kitchen.setPositionY(0.3f);
                kitchen.setColor("#B8935F");
                roomDao.insert(kitchen);
                
                RoomEntity bedroom1 = new RoomEntity();
                bedroom1.setRoomId("room_bedroom1");
                bedroom1.setName("Dormitorio Principal");
                bedroom1.setRoomType("BEDROOM");
                bedroom1.setFloor(1);
                bedroom1.setPositionX(0.3f);
                bedroom1.setPositionY(0.3f);
                bedroom1.setColor("#D4A574");
                roomDao.insert(bedroom1);
                
                RoomEntity bathroom = new RoomEntity();
                bathroom.setRoomId("room_bathroom");
                bathroom.setName("Baño");
                bathroom.setRoomType("BATHROOM");
                bathroom.setFloor(0);
                bathroom.setPositionX(0.7f);
                bathroom.setPositionY(0.7f);
                bathroom.setColor("#1A1F2E");
                roomDao.insert(bathroom);
            });
        }
    };
    
    /**
     * Migración de ejemplo para futuras versiones
     */
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Ejemplo de migración para agregar una nueva columna en el futuro
            // database.execSQL("ALTER TABLE devices ADD COLUMN new_column TEXT");
        }
    };
    
    /**
     * Limpia toda la base de datos (útil para logout)
     */
    public void clearAllTables() {
        databaseWriteExecutor.execute(() -> {
            deviceHistoryDao().deleteAll();
            deviceDao().deleteAll();
            roomDao().deleteAll();
            userPreferencesDao().delete(getCurrentUserId());
            userProfileDao().deleteAll();
        });
    }
    
    // Método auxiliar para obtener el ID del usuario actual
    // Este método debe ser implementado según el contexto de la aplicación
    private String getCurrentUserId() {
        // TODO: Implementar lógica para obtener el ID del usuario actual
        return "";
    }
}