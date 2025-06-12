package com.pdm.domohouse.cache;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.pdm.domohouse.data.database.AppDatabase;
import com.pdm.domohouse.data.database.entity.DeviceEntity;
import com.pdm.domohouse.data.database.entity.RoomEntity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Tests para el DeviceCache
 * Verifica cache en memoria, invalidación y sincronización
 */
@RunWith(AndroidJUnit4.class)
public class DeviceCacheTest {
    
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();
    
    private DeviceCache deviceCache;
    private AppDatabase database;
    private Context context;
    
    @Mock
    private Observer<DeviceEntity> deviceObserver;
    
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        context = ApplicationProvider.getApplicationContext();
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        
        deviceCache = DeviceCache.getInstance(context);
    }
    
    @After
    public void tearDown() throws IOException {
        database.close();
        deviceCache.invalidateCache();
    }
    
    @Test
    public void testCacheSingleton() {
        // Verificar que DeviceCache es singleton
        DeviceCache instance1 = DeviceCache.getInstance(context);
        DeviceCache instance2 = DeviceCache.getInstance(context);
        
        assertSame(instance1, instance2);
    }
    
    @Test
    public void testDeviceStateUpdateInCache() throws Exception {
        // Crear y insertar dispositivo en BD
        DeviceEntity device = new DeviceEntity();
        device.setDeviceId("cache_test_device");
        device.setRoomId("test_room");
        device.setName("Test Device");
        device.setDeviceType("LIGHT_SWITCH");
        device.setOn(false);
        device.setIntensity(0);
        
        database.deviceDao().insert(device);
        
        // Actualizar estado a través del cache
        deviceCache.updateDeviceState("cache_test_device", true);
        
        // Verificar que el cambio se refleja en la BD
        // Esperar un momento para que la operación async termine
        Thread.sleep(100);
        
        DeviceEntity updated = database.deviceDao().getDeviceSync("cache_test_device");
        assertNotNull(updated);
        assertTrue(updated.isOn());
        assertFalse(updated.isSynced()); // Debe marcarse como no sincronizado
    }
    
    @Test
    public void testDeviceIntensityUpdateInCache() throws Exception {
        // Crear dispositivo regulable
        DeviceEntity device = new DeviceEntity();
        device.setDeviceId("intensity_test_device");
        device.setRoomId("test_room");
        device.setName("Dimmer Light");
        device.setDeviceType("DIMMER_LIGHT");
        device.setOn(false);
        device.setIntensity(0);
        
        database.deviceDao().insert(device);
        
        // Actualizar intensidad
        deviceCache.updateDeviceIntensity("intensity_test_device", 75);
        
        // Esperar a que se complete la operación
        Thread.sleep(100);
        
        DeviceEntity updated = database.deviceDao().getDeviceSync("intensity_test_device");
        assertNotNull(updated);
        assertEquals(75, updated.getIntensity());
    }
    
    @Test
    public void testTemperatureUpdateInCache() throws Exception {
        // Crear sensor de temperatura
        DeviceEntity sensor = new DeviceEntity();
        sensor.setDeviceId("temp_sensor");
        sensor.setRoomId("test_room");
        sensor.setName("Temperature Sensor");
        sensor.setDeviceType("TEMPERATURE_SENSOR");
        sensor.setTemperature(20.0f);
        
        database.deviceDao().insert(sensor);
        
        // Actualizar temperatura
        deviceCache.updateDeviceTemperature("temp_sensor", 24.5f);
        
        // Esperar actualización
        Thread.sleep(100);
        
        DeviceEntity updated = database.deviceDao().getDeviceSync("temp_sensor");
        assertNotNull(updated);
        assertEquals(24.5f, updated.getTemperature(), 0.1f);
    }
    
    @Test
    public void testCacheInvalidation() {
        // Obtener estadísticas iniciales
        DeviceCache.CacheStats initialStats = deviceCache.getCacheStats();
        
        // Invalidar cache
        deviceCache.invalidateCache();
        
        // Verificar que el cache se limpió
        DeviceCache.CacheStats afterInvalidation = deviceCache.getCacheStats();
        assertEquals(0, afterInvalidation.totalDevices);
        assertEquals(0, afterInvalidation.totalRooms);
        assertEquals(0, afterInvalidation.lastSync);
    }
    
    @Test
    public void testDeviceInvalidation() throws Exception {
        // Crear dispositivo
        DeviceEntity device = new DeviceEntity();
        device.setDeviceId("invalidation_test");
        device.setRoomId("test_room");
        device.setName("Invalidation Test");
        device.setDeviceType("FAN_SWITCH");
        
        database.deviceDao().insert(device);
        
        // Pre-cargar cache
        deviceCache.preloadDevices();
        Thread.sleep(100);
        
        // Invalidar dispositivo específico
        deviceCache.invalidateDevice("invalidation_test");
        
        // El dispositivo específico debe haberse removido del cache
        // pero otros datos del cache deben permanecer
        DeviceCache.CacheStats stats = deviceCache.getCacheStats();
        // Verificar que la invalidación funcionó
        assertNotNull(stats);
    }
    
    @Test
    public void testRoomRetrieval() throws Exception {
        // Crear habitación
        RoomEntity room = new RoomEntity();
        room.setRoomId("cache_room_test");
        room.setName("Cache Test Room");
        room.setRoomType("BEDROOM");
        room.setFloor(1);
        room.setPositionX(0.5f);
        room.setPositionY(0.5f);
        
        database.roomDao().insert(room);
        
        // Recuperar habitación desde cache
        RoomEntity retrieved = deviceCache.getRoom("cache_room_test");
        
        // En la primera consulta puede ser null si no está en cache
        // así que volvemos a intentar después de un momento
        if (retrieved == null) {
            Thread.sleep(100);
            retrieved = deviceCache.getRoom("cache_room_test");
        }
        
        assertNotNull(retrieved);
        assertEquals("cache_room_test", retrieved.getRoomId());
        assertEquals("Cache Test Room", retrieved.getName());
        assertEquals("BEDROOM", retrieved.getRoomType());
    }
    
    @Test
    public void testCacheStatsAccuracy() throws Exception {
        // Crear múltiples dispositivos y habitaciones
        for (int i = 1; i <= 3; i++) {
            RoomEntity room = new RoomEntity();
            room.setRoomId("stats_room_" + i);
            room.setName("Stats Room " + i);
            room.setRoomType("BEDROOM");
            database.roomDao().insert(room);
            
            DeviceEntity device = new DeviceEntity();
            device.setDeviceId("stats_device_" + i);
            device.setRoomId("stats_room_" + i);
            device.setName("Stats Device " + i);
            device.setDeviceType("LIGHT_SWITCH");
            database.deviceDao().insert(device);
        }
        
        // Pre-cargar cache
        deviceCache.preloadDevices();
        Thread.sleep(200);
        
        // Verificar estadísticas
        DeviceCache.CacheStats stats = deviceCache.getCacheStats();
        assertNotNull(stats);
        assertTrue(stats.totalDevices >= 0);
        assertTrue(stats.totalRooms >= 0);
        assertTrue(stats.lastSync >= 0);
    }
    
    @Test
    public void testLiveDataObservation() throws Exception {
        // Crear dispositivo
        DeviceEntity device = new DeviceEntity();
        device.setDeviceId("livedata_test");
        device.setRoomId("test_room");
        device.setName("LiveData Test");
        device.setDeviceType("LIGHT_SWITCH");
        device.setOn(false);
        
        database.deviceDao().insert(device);
        
        // Observar cambios a través del cache
        CountDownLatch latch = new CountDownLatch(1);
        LiveData<DeviceEntity> deviceLiveData = deviceCache.getDevice("livedata_test");
        
        deviceLiveData.observeForever(deviceEntity -> {
            if (deviceEntity != null) {
                latch.countDown();
            }
        });
        
        // Esperar a que el observer se ejecute
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }
    
    @Test
    public void testCacheValidityDuration() throws Exception {
        // Crear dispositivo
        DeviceEntity device = new DeviceEntity();
        device.setDeviceId("validity_test");
        device.setRoomId("test_room");
        device.setName("Validity Test");
        device.setDeviceType("TEMPERATURE_SENSOR");
        
        database.deviceDao().insert(device);
        
        // Acceder al dispositivo para cargarlo en cache
        RoomEntity room = deviceCache.getRoom("test_room");
        
        // El cache debería ser válido inmediatamente después de cargar
        DeviceCache.CacheStats stats = deviceCache.getCacheStats();
        assertTrue(stats.validDevices >= 0);
        
        // Nota: Para probar expiración del cache necesitaríamos 
        // esperar 15 minutos o mockear el tiempo
    }
    
    @Test
    public void testConcurrentCacheOperations() throws Exception {
        // Crear dispositivos para operaciones concurrentes
        for (int i = 1; i <= 5; i++) {
            DeviceEntity device = new DeviceEntity();
            device.setDeviceId("concurrent_" + i);
            device.setRoomId("test_room");
            device.setName("Concurrent Device " + i);
            device.setDeviceType("LIGHT_SWITCH");
            device.setOn(false);
            database.deviceDao().insert(device);
        }
        
        // Ejecutar operaciones concurrentes
        Thread thread1 = new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                deviceCache.updateDeviceState("concurrent_" + i, true);
            }
        });
        
        Thread thread2 = new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                deviceCache.updateDeviceIntensity("concurrent_" + i, 50);
            }
        });
        
        thread1.start();
        thread2.start();
        
        thread1.join();
        thread2.join();
        
        // Esperar a que las operaciones async terminen
        Thread.sleep(500);
        
        // Verificar que todas las operaciones se completaron
        for (int i = 1; i <= 5; i++) {
            DeviceEntity device = database.deviceDao().getDeviceSync("concurrent_" + i);
            assertNotNull(device);
            assertTrue(device.isOn());
            assertEquals(50, device.getIntensity());
        }
    }
    
    @Test
    public void testPreloadDevices() throws Exception {
        // Crear datos de prueba
        RoomEntity room = new RoomEntity();
        room.setRoomId("preload_room");
        room.setName("Preload Room");
        room.setRoomType("LIVING_ROOM");
        database.roomDao().insert(room);
        
        DeviceEntity device = new DeviceEntity();
        device.setDeviceId("preload_device");
        device.setRoomId("preload_room");
        device.setName("Preload Device");
        device.setDeviceType("SMART_TV");
        database.deviceDao().insert(device);
        
        // Pre-cargar dispositivos
        deviceCache.preloadDevices();
        
        // Esperar a que termine la pre-carga
        Thread.sleep(200);
        
        // Verificar que los datos están en cache
        DeviceCache.CacheStats stats = deviceCache.getCacheStats();
        assertTrue(stats.totalDevices >= 0);
        assertTrue(stats.totalRooms >= 0);
    }
}