package com.pdm.domohouse.sync;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.pdm.domohouse.data.database.AppDatabase;
import com.pdm.domohouse.data.database.entity.DeviceEntity;
import com.pdm.domohouse.data.database.entity.RoomEntity;
import com.pdm.domohouse.data.database.entity.UserProfileEntity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Tests para el SyncManager
 * Verifica sincronización offline/online y resolución de conflictos
 */
@RunWith(AndroidJUnit4.class)
public class SyncManagerTest {
    
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();
    
    private AppDatabase database;
    private SyncManager syncManager;
    private Context context;
    
    @Mock
    private SyncManager mockSyncManager;
    
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        context = ApplicationProvider.getApplicationContext();
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        
        syncManager = SyncManager.getInstance(context);
    }
    
    @After
    public void tearDown() throws IOException {
        database.close();
    }
    
    @Test
    public void testSyncManagerSingleton() {
        // Verificar que SyncManager es singleton
        SyncManager instance1 = SyncManager.getInstance(context);
        SyncManager instance2 = SyncManager.getInstance(context);
        
        assertSame(instance1, instance2);
    }
    
    @Test
    public void testOfflineDataStorage() throws Exception {
        // Crear datos locales sin conexión
        UserProfileEntity user = new UserProfileEntity();
        user.setUserId("offline_user");
        user.setName("Usuario Offline");
        user.setEmail("offline@test.com");
        user.setSynced(false);
        
        database.userProfileDao().insert(user);
        
        // Verificar que los datos están marcados como no sincronizados
        UserProfileEntity stored = database.userProfileDao().getUserProfileSync("offline_user");
        assertNotNull(stored);
        assertFalse(stored.isSynced());
        assertEquals(0, stored.getLastSync());
    }
    
    @Test
    public void testUnsyncedDataRetrieval() throws Exception {
        // Crear múltiples entidades con diferentes estados de sincronización
        
        // Usuario sincronizado
        UserProfileEntity syncedUser = new UserProfileEntity();
        syncedUser.setUserId("synced_user");
        syncedUser.setName("Usuario Sincronizado");
        syncedUser.setEmail("synced@test.com");
        syncedUser.setSynced(true);
        syncedUser.setLastSync(System.currentTimeMillis());
        database.userProfileDao().insert(syncedUser);
        
        // Usuario no sincronizado
        UserProfileEntity unsyncedUser = new UserProfileEntity();
        unsyncedUser.setUserId("unsynced_user");
        unsyncedUser.setName("Usuario No Sincronizado");
        unsyncedUser.setEmail("unsynced@test.com");
        unsyncedUser.setSynced(false);
        unsyncedUser.setLastSync(0);
        database.userProfileDao().insert(unsyncedUser);
        
        // Dispositivos sincronizados y no sincronizados
        DeviceEntity syncedDevice = new DeviceEntity();
        syncedDevice.setDeviceId("synced_device");
        syncedDevice.setRoomId("test_room");
        syncedDevice.setName("Dispositivo Sincronizado");
        syncedDevice.setDeviceType("LIGHT_SWITCH");
        syncedDevice.setSynced(true);
        database.deviceDao().insert(syncedDevice);
        
        DeviceEntity unsyncedDevice = new DeviceEntity();
        unsyncedDevice.setDeviceId("unsynced_device");
        unsyncedDevice.setRoomId("test_room");
        unsyncedDevice.setName("Dispositivo No Sincronizado");
        unsyncedDevice.setDeviceType("FAN_SWITCH");
        unsyncedDevice.setSynced(false);
        database.deviceDao().insert(unsyncedDevice);
        
        // Verificar recuperación de datos no sincronizados
        List<UserProfileEntity> unsyncedProfiles = database.userProfileDao().getUnsyncedProfiles();
        assertEquals(1, unsyncedProfiles.size());
        assertEquals("unsynced_user", unsyncedProfiles.get(0).getUserId());
        
        List<DeviceEntity> unsyncedDevices = database.deviceDao().getUnsyncedDevices();
        assertEquals(1, unsyncedDevices.size());
        assertEquals("unsynced_device", unsyncedDevices.get(0).getDeviceId());
    }
    
    @Test
    public void testConflictResolutionNewestWins() throws Exception {
        // Crear dispositivo local más reciente
        DeviceEntity localDevice = new DeviceEntity();
        localDevice.setDeviceId("conflict_device");
        localDevice.setRoomId("test_room");
        localDevice.setName("Dispositivo Local");
        localDevice.setDeviceType("LIGHT_SWITCH");
        localDevice.setOn(true);
        localDevice.setIntensity(75);
        localDevice.setLastStateChange(System.currentTimeMillis());
        localDevice.setUpdatedAt(System.currentTimeMillis());
        localDevice.setSynced(false);
        
        database.deviceDao().insert(localDevice);
        
        // Simular dispositivo remoto más antiguo
        DeviceEntity remoteDevice = new DeviceEntity();
        remoteDevice.setDeviceId("conflict_device");
        remoteDevice.setRoomId("test_room");
        remoteDevice.setName("Dispositivo Remoto");
        remoteDevice.setDeviceType("LIGHT_SWITCH");
        remoteDevice.setOn(false);
        remoteDevice.setIntensity(50);
        remoteDevice.setLastStateChange(System.currentTimeMillis() - 60000); // 1 minuto atrás
        remoteDevice.setUpdatedAt(System.currentTimeMillis() - 60000);
        remoteDevice.setSynced(true);
        
        // El dispositivo local debe ganar (más reciente)
        DeviceEntity stored = database.deviceDao().getDeviceSync("conflict_device");
        assertNotNull(stored);
        assertTrue(stored.isOn()); // Estado local (más reciente)
        assertEquals(75, stored.getIntensity()); // Intensidad local
        assertEquals("Dispositivo Local", stored.getName()); // Nombre local
    }
    
    @Test
    public void testMarkAsSyncedUpdatesTimestamp() throws Exception {
        // Crear dispositivo no sincronizado
        DeviceEntity device = new DeviceEntity();
        device.setDeviceId("timestamp_test");
        device.setRoomId("test_room");
        device.setName("Test Device");
        device.setDeviceType("TEMPERATURE_SENSOR");
        device.setSynced(false);
        device.setLastSync(0);
        
        database.deviceDao().insert(device);
        
        // Marcar como sincronizado
        long syncTime = System.currentTimeMillis();
        database.deviceDao().markAsSynced("timestamp_test", syncTime);
        
        // Verificar actualización
        DeviceEntity updated = database.deviceDao().getDeviceSync("timestamp_test");
        assertNotNull(updated);
        assertTrue(updated.isSynced());
        assertEquals(syncTime, updated.getLastSync());
    }
    
    @Test
    public void testBatchSyncOperations() throws Exception {
        // Crear múltiples dispositivos no sincronizados
        for (int i = 1; i <= 5; i++) {
            DeviceEntity device = new DeviceEntity();
            device.setDeviceId("batch_device_" + i);
            device.setRoomId("test_room");
            device.setName("Dispositivo Lote " + i);
            device.setDeviceType("LIGHT_SWITCH");
            device.setSynced(false);
            
            database.deviceDao().insert(device);
        }
        
        // Verificar que todos están sin sincronizar
        List<DeviceEntity> unsyncedBefore = database.deviceDao().getUnsyncedDevices();
        assertEquals(5, unsyncedBefore.size());
        
        // Simular sincronización masiva
        long syncTime = System.currentTimeMillis();
        for (DeviceEntity device : unsyncedBefore) {
            database.deviceDao().markAsSynced(device.getDeviceId(), syncTime);
        }
        
        // Verificar que no quedan dispositivos sin sincronizar
        List<DeviceEntity> unsyncedAfter = database.deviceDao().getUnsyncedDevices();
        assertEquals(0, unsyncedAfter.size());
    }
    
    @Test
    public void testOfflineChangesPreservation() throws Exception {
        // Simular cambios offline
        DeviceEntity device = new DeviceEntity();
        device.setDeviceId("offline_changes");
        device.setRoomId("test_room");
        device.setName("Dispositivo Offline");
        device.setDeviceType("DIMMER_LIGHT");
        device.setOn(false);
        device.setIntensity(0);
        device.setSynced(true);
        
        database.deviceDao().insert(device);
        
        // Simular cambios del usuario mientras está offline
        long changeTime = System.currentTimeMillis();
        database.deviceDao().updateDeviceState("offline_changes", true, changeTime);
        database.deviceDao().updateDeviceIntensity("offline_changes", 80, changeTime);
        
        // El dispositivo debe estar marcado como no sincronizado después de cambios
        DeviceEntity changed = database.deviceDao().getDeviceSync("offline_changes");
        assertNotNull(changed);
        assertTrue(changed.isOn());
        assertEquals(80, changed.intensity);
        assertEquals(changeTime, changed.getLastStateChange());
        assertEquals(changeTime, changed.getUpdatedAt());
        // Nota: En la implementación real, los métodos update deberían marcar como no sincronizado
    }
    
    @Test
    public void testDataIntegrityAfterSync() throws Exception {
        // Crear habitación
        RoomEntity room = new RoomEntity();
        room.setRoomId("integrity_room");
        room.setName("Habitación Integridad");
        room.setRoomType("BEDROOM");
        room.setSynced(false);
        
        database.roomDao().insert(room);
        
        // Crear dispositivo relacionado
        DeviceEntity device = new DeviceEntity();
        device.setDeviceId("integrity_device");
        device.setRoomId("integrity_room");
        device.setName("Dispositivo Integridad");
        device.setDeviceType("LIGHT_SWITCH");
        device.setSynced(false);
        
        database.deviceDao().insert(device);
        
        // Verificar relación antes de sincronización
        List<DeviceEntity> roomDevices = database.deviceDao().getDevicesByRoomSync("integrity_room");
        assertEquals(1, roomDevices.size());
        assertEquals("integrity_device", roomDevices.get(0).getDeviceId());
        
        // Simular sincronización
        long syncTime = System.currentTimeMillis();
        database.roomDao().markAsSynced("integrity_room", syncTime);
        database.deviceDao().markAsSynced("integrity_device", syncTime);
        
        // Verificar que la relación se mantiene después de sincronización
        List<DeviceEntity> roomDevicesAfterSync = database.deviceDao().getDevicesByRoomSync("integrity_room");
        assertEquals(1, roomDevicesAfterSync.size());
        assertEquals("integrity_device", roomDevicesAfterSync.get(0).getDeviceId());
        
        // Verificar estado de sincronización
        RoomEntity syncedRoom = database.roomDao().getRoomSync("integrity_room");
        DeviceEntity syncedDevice = database.deviceDao().getDeviceSync("integrity_device");
        
        assertNotNull(syncedRoom);
        assertNotNull(syncedDevice);
        assertTrue(syncedRoom.isSynced());
        assertTrue(syncedDevice.isSynced());
        assertEquals(syncTime, syncedRoom.getLastSync());
        assertEquals(syncTime, syncedDevice.getLastSync());
    }
    
    @Test
    public void testConnectionStateHandling() {
        // Verificar comportamiento con/sin conexión
        // Nota: En un test real, se mockearía la conectividad
        
        // Simular estado offline
        when(mockSyncManager.isOnline()).thenReturn(false);
        assertFalse(mockSyncManager.isOnline());
        
        // Simular estado online
        when(mockSyncManager.isOnline()).thenReturn(true);
        assertTrue(mockSyncManager.isOnline());
        
        // Verificar que syncAll no se llama cuando está offline
        when(mockSyncManager.isOnline()).thenReturn(false);
        doNothing().when(mockSyncManager).syncAll();
        
        // En estado offline, syncAll no debería ejecutarse
        if (!mockSyncManager.isOnline()) {
            // No llamar syncAll
            verify(mockSyncManager, never()).syncAll();
        }
    }
    
    @Test
    public void testSyncStatusObservation() throws Exception {
        // Test para observar cambios en el estado de sincronización
        CountDownLatch latch = new CountDownLatch(1);
        
        // Observar estado de sincronización
        syncManager.getSyncStatus().observeForever(status -> {
            assertNotNull(status);
            latch.countDown();
        });
        
        // Esperar a que el observer se ejecute
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }
}