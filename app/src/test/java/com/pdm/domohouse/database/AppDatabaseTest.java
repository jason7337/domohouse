package com.pdm.domohouse.database;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.pdm.domohouse.data.database.AppDatabase;
import com.pdm.domohouse.data.database.dao.DeviceDao;
import com.pdm.domohouse.data.database.dao.RoomDao;
import com.pdm.domohouse.data.database.dao.UserProfileDao;
import com.pdm.domohouse.data.database.entity.DeviceEntity;
import com.pdm.domohouse.data.database.entity.RoomEntity;
import com.pdm.domohouse.data.database.entity.UserProfileEntity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests para la base de datos Room
 * Verifica CRUD operations y relaciones entre entidades
 */
@RunWith(AndroidJUnit4.class)
public class AppDatabaseTest {
    
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();
    
    private AppDatabase database;
    private UserProfileDao userProfileDao;
    private RoomDao roomDao;
    private DeviceDao deviceDao;
    
    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        
        userProfileDao = database.userProfileDao();
        roomDao = database.roomDao();
        deviceDao = database.deviceDao();
    }
    
    @After
    public void closeDb() throws IOException {
        database.close();
    }
    
    @Test
    public void insertAndRetrieveUserProfile() throws Exception {
        // Crear perfil de usuario
        UserProfileEntity user = new UserProfileEntity();
        user.setUserId("test_user_1");
        user.setName("Usuario Test");
        user.setEmail("test@domohouse.com");
        user.setPinHash("encrypted_pin_hash");
        
        // Insertar
        userProfileDao.insert(user);
        
        // Recuperar
        UserProfileEntity retrieved = userProfileDao.getUserProfileSync("test_user_1");
        
        // Verificar
        assertNotNull(retrieved);
        assertEquals("test_user_1", retrieved.getUserId());
        assertEquals("Usuario Test", retrieved.getName());
        assertEquals("test@domohouse.com", retrieved.getEmail());
        assertEquals("encrypted_pin_hash", retrieved.getPinHash());
    }
    
    @Test
    public void insertAndRetrieveRoom() throws Exception {
        // Crear habitación
        RoomEntity room = new RoomEntity();
        room.setRoomId("room_living");
        room.setName("Sala de Estar");
        room.setRoomType("LIVING_ROOM");
        room.setFloor(0);
        room.setPositionX(0.3f);
        room.setPositionY(0.5f);
        
        // Insertar
        roomDao.insert(room);
        
        // Recuperar
        RoomEntity retrieved = roomDao.getRoomSync("room_living");
        
        // Verificar
        assertNotNull(retrieved);
        assertEquals("room_living", retrieved.getRoomId());
        assertEquals("Sala de Estar", retrieved.getName());
        assertEquals("LIVING_ROOM", retrieved.getRoomType());
        assertEquals(0, retrieved.getFloor());
        assertEquals(0.3f, retrieved.getPositionX(), 0.01f);
        assertEquals(0.5f, retrieved.getPositionY(), 0.01f);
    }
    
    @Test
    public void insertDeviceWithRoomRelation() throws Exception {
        // Crear habitación primero
        RoomEntity room = new RoomEntity();
        room.setRoomId("room_bedroom");
        room.setName("Dormitorio");
        room.setRoomType("BEDROOM");
        roomDao.insert(room);
        
        // Crear dispositivo
        DeviceEntity device = new DeviceEntity();
        device.setDeviceId("device_light_1");
        device.setRoomId("room_bedroom");
        device.setName("Luz Principal");
        device.setDeviceType("LIGHT_SWITCH");
        device.setOn(true);
        device.setIntensity(75);
        device.setOnline(true);
        
        // Insertar dispositivo
        deviceDao.insert(device);
        
        // Recuperar dispositivo
        DeviceEntity retrieved = deviceDao.getDeviceSync("device_light_1");
        
        // Verificar
        assertNotNull(retrieved);
        assertEquals("device_light_1", retrieved.getDeviceId());
        assertEquals("room_bedroom", retrieved.getRoomId());
        assertEquals("Luz Principal", retrieved.getName());
        assertEquals("LIGHT_SWITCH", retrieved.getDeviceType());
        assertTrue(retrieved.isOn());
        assertEquals(75, retrieved.getIntensity());
        assertTrue(retrieved.isOnline());
    }
    
    @Test
    public void getDevicesByRoom() throws Exception {
        // Crear habitación
        RoomEntity room = new RoomEntity();
        room.setRoomId("room_kitchen");
        room.setName("Cocina");
        room.setRoomType("KITCHEN");
        roomDao.insert(room);
        
        // Crear múltiples dispositivos para la misma habitación
        DeviceEntity light = new DeviceEntity();
        light.setDeviceId("kitchen_light");
        light.setRoomId("room_kitchen");
        light.setName("Luz Cocina");
        light.setDeviceType("LIGHT_SWITCH");
        deviceDao.insert(light);
        
        DeviceEntity fan = new DeviceEntity();
        fan.setDeviceId("kitchen_fan");
        fan.setRoomId("room_kitchen");
        fan.setName("Extractor");
        fan.setDeviceType("FAN_SWITCH");
        deviceDao.insert(fan);
        
        DeviceEntity smoke = new DeviceEntity();
        smoke.setDeviceId("kitchen_smoke");
        smoke.setRoomId("room_kitchen");
        smoke.setName("Detector Humo");
        smoke.setDeviceType("SMOKE_DETECTOR");
        deviceDao.insert(smoke);
        
        // Recuperar dispositivos por habitación
        List<DeviceEntity> kitchenDevices = deviceDao.getDevicesByRoomSync("room_kitchen");
        
        // Verificar
        assertNotNull(kitchenDevices);
        assertEquals(3, kitchenDevices.size());
        
        // Verificar que todos pertenecen a la cocina
        for (DeviceEntity device : kitchenDevices) {
            assertEquals("room_kitchen", device.getRoomId());
        }
    }
    
    @Test
    public void updateDeviceState() throws Exception {
        // Crear y insertar dispositivo
        DeviceEntity device = new DeviceEntity();
        device.setDeviceId("test_device");
        device.setRoomId("test_room");
        device.setName("Test Device");
        device.setDeviceType("LIGHT_SWITCH");
        device.setOn(false);
        device.setIntensity(0);
        deviceDao.insert(device);
        
        // Actualizar estado
        long timestamp = System.currentTimeMillis();
        deviceDao.updateDeviceState("test_device", true, timestamp);
        
        // Verificar actualización
        DeviceEntity updated = deviceDao.getDeviceSync("test_device");
        assertNotNull(updated);
        assertTrue(updated.isOn());
        assertEquals(timestamp, updated.getLastStateChange());
        assertEquals(timestamp, updated.getUpdatedAt());
    }
    
    @Test
    public void updateDeviceIntensity() throws Exception {
        // Crear y insertar dispositivo
        DeviceEntity device = new DeviceEntity();
        device.setDeviceId("dimmer_device");
        device.setRoomId("test_room");
        device.setName("Luz Regulable");
        device.setDeviceType("DIMMER_LIGHT");
        device.setIntensity(0);
        deviceDao.insert(device);
        
        // Actualizar intensidad
        long timestamp = System.currentTimeMillis();
        deviceDao.updateDeviceIntensity("dimmer_device", 85, timestamp);
        
        // Verificar actualización
        DeviceEntity updated = deviceDao.getDeviceSync("dimmer_device");
        assertNotNull(updated);
        assertEquals(85, updated.getIntensity());
        assertEquals(timestamp, updated.getUpdatedAt());
    }
    
    @Test
    public void markDeviceAsSynced() throws Exception {
        // Crear dispositivo sin sincronizar
        DeviceEntity device = new DeviceEntity();
        device.setDeviceId("unsynced_device");
        device.setRoomId("test_room");
        device.setName("Dispositivo no sincronizado");
        device.setDeviceType("TEMPERATURE_SENSOR");
        device.setSynced(false);
        device.setLastSync(0);
        deviceDao.insert(device);
        
        // Marcar como sincronizado
        long syncTime = System.currentTimeMillis();
        deviceDao.markAsSynced("unsynced_device", syncTime);
        
        // Verificar sincronización
        DeviceEntity synced = deviceDao.getDeviceSync("unsynced_device");
        assertNotNull(synced);
        assertTrue(synced.isSynced());
        assertEquals(syncTime, synced.getLastSync());
    }
    
    @Test
    public void getUnsyncedDevices() throws Exception {
        // Crear dispositivos sincronizados y no sincronizados
        DeviceEntity synced = new DeviceEntity();
        synced.setDeviceId("synced_device");
        synced.setRoomId("test_room");
        synced.setName("Dispositivo Sincronizado");
        synced.setDeviceType("LIGHT_SWITCH");
        synced.setSynced(true);
        deviceDao.insert(synced);
        
        DeviceEntity unsynced1 = new DeviceEntity();
        unsynced1.setDeviceId("unsynced_1");
        unsynced1.setRoomId("test_room");
        unsynced1.setName("No Sincronizado 1");
        unsynced1.setDeviceType("FAN_SWITCH");
        unsynced1.setSynced(false);
        deviceDao.insert(unsynced1);
        
        DeviceEntity unsynced2 = new DeviceEntity();
        unsynced2.setDeviceId("unsynced_2");
        unsynced2.setRoomId("test_room");
        unsynced2.setName("No Sincronizado 2");
        unsynced2.setDeviceType("SMOKE_DETECTOR");
        unsynced2.setSynced(false);
        deviceDao.insert(unsynced2);
        
        // Obtener dispositivos no sincronizados
        List<DeviceEntity> unsyncedDevices = deviceDao.getUnsyncedDevices();
        
        // Verificar
        assertNotNull(unsyncedDevices);
        assertEquals(2, unsyncedDevices.size());
        
        for (DeviceEntity device : unsyncedDevices) {
            assertFalse(device.isSynced());
        }
    }
    
    @Test
    public void cascadeDeleteFromRoom() throws Exception {
        // Crear habitación
        RoomEntity room = new RoomEntity();
        room.setRoomId("room_to_delete");
        room.setName("Habitación a Eliminar");
        room.setRoomType("OFFICE");
        roomDao.insert(room);
        
        // Crear dispositivos en la habitación
        DeviceEntity device1 = new DeviceEntity();
        device1.setDeviceId("device_1");
        device1.setRoomId("room_to_delete");
        device1.setName("Dispositivo 1");
        device1.setDeviceType("LIGHT_SWITCH");
        deviceDao.insert(device1);
        
        DeviceEntity device2 = new DeviceEntity();
        device2.setDeviceId("device_2");
        device2.setRoomId("room_to_delete");
        device2.setName("Dispositivo 2");
        device2.setDeviceType("FAN_SWITCH");
        deviceDao.insert(device2);
        
        // Verificar que los dispositivos existen
        List<DeviceEntity> devicesBefore = deviceDao.getDevicesByRoomSync("room_to_delete");
        assertEquals(2, devicesBefore.size());
        
        // Eliminar habitación (debe eliminar dispositivos por cascade)
        roomDao.delete(room);
        
        // Verificar que los dispositivos fueron eliminados
        List<DeviceEntity> devicesAfter = deviceDao.getDevicesByRoomSync("room_to_delete");
        assertEquals(0, devicesAfter.size());
        
        // Verificar que la habitación fue eliminada
        RoomEntity deletedRoom = roomDao.getRoomSync("room_to_delete");
        assertNull(deletedRoom);
    }
}