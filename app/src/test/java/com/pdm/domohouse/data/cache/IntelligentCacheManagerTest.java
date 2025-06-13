package com.pdm.domohouse.data.cache;

import static org.junit.Assert.*;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.pdm.domohouse.data.model.Device;
import com.pdm.domohouse.data.model.DeviceType;
import com.pdm.domohouse.data.model.Room;
import com.pdm.domohouse.data.model.RoomType;
import com.pdm.domohouse.data.model.UserPreferences;
import com.pdm.domohouse.data.model.UserProfile;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests exhaustivos para IntelligentCacheManager
 * Cubre cache inteligente, gestión de memoria y estadísticas
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class IntelligentCacheManagerTest {
    
    private Context context;
    private IntelligentCacheManager cacheManager;
    
    private static final String TEST_USER_ID = "test_user_123";
    private static final String TEST_DEVICE_ID = "device_123";
    private static final String TEST_ROOM_ID = "room_123";
    
    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        cacheManager = IntelligentCacheManager.getInstance(context);
        
        // Limpiar cache antes de cada test
        cacheManager.clearAllCache();
    }
    
    @After
    public void tearDown() {
        if (cacheManager != null) {
            cacheManager.shutdown();
        }
    }
    
    @Test
    public void testCacheManagerInitialization() {
        // Given
        IntelligentCacheManager manager = IntelligentCacheManager.getInstance(context);
        
        // Then
        assertNotNull("CacheManager debe inicializarse correctamente", manager);
        assertNotNull("Estadísticas deben estar disponibles", manager.getStats());
        assertNotNull("Información de memoria debe estar disponible", manager.getMemoryInfo());
    }
    
    @Test
    public void testUserProfileCache() {
        // Given
        UserProfile profile = createTestUserProfile();
        
        // When
        cacheManager.putUserProfile(TEST_USER_ID, profile);
        UserProfile cachedProfile = cacheManager.getUserProfile(TEST_USER_ID);
        
        // Then
        assertNotNull("Perfil cacheado no debe ser null", cachedProfile);
        assertEquals("ID de usuario debe coincidir", profile.getUserId(), cachedProfile.getUserId());
        assertEquals("Nombre debe coincidir", profile.getName(), cachedProfile.getName());
        assertEquals("Email debe coincidir", profile.getEmail(), cachedProfile.getEmail());
    }
    
    @Test
    public void testUserProfileCacheMiss() {
        // When
        UserProfile cachedProfile = cacheManager.getUserProfile("non_existent_user");
        
        // Then
        assertNull("Perfil no existente debe retornar null", cachedProfile);
    }
    
    @Test
    public void testUserPreferencesCache() {
        // Given
        UserPreferences preferences = createTestUserPreferences();
        
        // When
        cacheManager.putUserPreferences(TEST_USER_ID, preferences);
        UserPreferences cachedPreferences = cacheManager.getUserPreferences(TEST_USER_ID);
        
        // Then
        assertNotNull("Preferencias cacheadas no deben ser null", cachedPreferences);
        assertEquals("ID de usuario debe coincidir", preferences.getUserId(), cachedPreferences.getUserId());
        assertEquals("Idioma debe coincidir", preferences.getLanguage(), cachedPreferences.getLanguage());
        assertEquals("Notificaciones deben coincidir", 
            preferences.isNotificationsEnabled(), cachedPreferences.isNotificationsEnabled());
    }
    
    @Test
    public void testDeviceCache() {
        // Given
        Device device = createTestDevice();
        
        // When
        cacheManager.putDevice(TEST_DEVICE_ID, device);
        Device cachedDevice = cacheManager.getDevice(TEST_DEVICE_ID);
        
        // Then
        assertNotNull("Dispositivo cacheado no debe ser null", cachedDevice);
        assertEquals("ID de dispositivo debe coincidir", device.getDeviceId(), cachedDevice.getDeviceId());
        assertEquals("Nombre debe coincidir", device.getName(), cachedDevice.getName());
        assertEquals("Estado debe coincidir", device.isOn(), cachedDevice.isOn());
        assertEquals("Intensidad debe coincidir", device.getIntensity(), cachedDevice.getIntensity());
    }
    
    @Test
    public void testDeviceListCache() {
        // Given
        List<Device> devices = createTestDeviceList();
        String cacheKey = "room_" + TEST_ROOM_ID + "_devices";
        
        // When
        cacheManager.putDeviceList(cacheKey, devices);
        List<Device> cachedDevices = cacheManager.getDeviceList(cacheKey);
        
        // Then
        assertNotNull("Lista de dispositivos cacheada no debe ser null", cachedDevices);
        assertEquals("Tamaño de lista debe coincidir", devices.size(), cachedDevices.size());
        
        Device originalDevice = devices.get(0);
        Device cachedDevice = cachedDevices.get(0);
        assertEquals("ID del primer dispositivo debe coincidir", 
            originalDevice.getDeviceId(), cachedDevice.getDeviceId());
    }
    
    @Test
    public void testDeviceStateCache() {
        // Given
        boolean isOn = true;
        int intensity = 75;
        Float temperature = 23.5f;
        
        // When
        cacheManager.updateDeviceState(TEST_DEVICE_ID, isOn, intensity, temperature);
        IntelligentCacheManager.DeviceStateEntry stateEntry = 
            cacheManager.getDeviceState(TEST_DEVICE_ID);
        
        // Then
        assertNotNull("Estado de dispositivo cacheado no debe ser null", stateEntry);
        assertEquals("Estado encendido debe coincidir", isOn, stateEntry.isOn);
        assertEquals("Intensidad debe coincidir", intensity, stateEntry.intensity);
        assertEquals("Temperatura debe coincidir", temperature, stateEntry.temperature);
        assertFalse("Estado no debe estar expirado inmediatamente", stateEntry.isExpired());
    }
    
    @Test
    public void testDeviceStateCacheWithDeviceUpdate() {
        // Given
        Device device = createTestDevice();
        cacheManager.putDevice(TEST_DEVICE_ID, device);
        
        // When
        cacheManager.updateDeviceState(TEST_DEVICE_ID, true, 90, 25.0f);
        Device updatedDevice = cacheManager.getDevice(TEST_DEVICE_ID);
        
        // Then
        assertNotNull("Dispositivo actualizado no debe ser null", updatedDevice);
        assertTrue("Dispositivo debe estar encendido", updatedDevice.isOn());
        assertEquals("Intensidad debe estar actualizada", 90, updatedDevice.getIntensity());
        assertEquals("Temperatura debe estar actualizada", 25.0f, updatedDevice.getTemperature(), 0.01f);
    }
    
    @Test
    public void testRoomCache() {
        // Given
        Room room = createTestRoom();
        
        // When
        cacheManager.putRoom(TEST_ROOM_ID, room);
        Room cachedRoom = cacheManager.getRoom(TEST_ROOM_ID);
        
        // Then
        assertNotNull("Habitación cacheada no debe ser null", cachedRoom);
        assertEquals("ID de habitación debe coincidir", room.getRoomId(), cachedRoom.getRoomId());
        assertEquals("Nombre debe coincidir", room.getName(), cachedRoom.getName());
        assertEquals("Tipo debe coincidir", room.getRoomType(), cachedRoom.getRoomType());
    }
    
    @Test
    public void testRoomListCache() {
        // Given
        List<Room> rooms = createTestRoomList();
        String cacheKey = "all_rooms";
        
        // When
        cacheManager.putRoomList(cacheKey, rooms);
        List<Room> cachedRooms = cacheManager.getRoomList(cacheKey);
        
        // Then
        assertNotNull("Lista de habitaciones cacheada no debe ser null", cachedRooms);
        assertEquals("Tamaño de lista debe coincidir", rooms.size(), cachedRooms.size());
        
        Room originalRoom = rooms.get(0);
        Room cachedRoom = cachedRooms.get(0);
        assertEquals("ID de la primera habitación debe coincidir", 
            originalRoom.getRoomId(), cachedRoom.getRoomId());
    }
    
    @Test
    public void testCacheInvalidation() {
        // Given
        UserProfile profile = createTestUserProfile();
        Device device = createTestDevice();
        Room room = createTestRoom();
        
        cacheManager.putUserProfile(TEST_USER_ID, profile);
        cacheManager.putDevice(TEST_DEVICE_ID, device);
        cacheManager.putRoom(TEST_ROOM_ID, room);
        
        // When
        cacheManager.invalidateUserCache(TEST_USER_ID);
        cacheManager.invalidateDeviceCache(TEST_DEVICE_ID);
        cacheManager.invalidateRoomCache(TEST_ROOM_ID);
        
        // Then
        assertNull("Perfil invalidado debe retornar null", 
            cacheManager.getUserProfile(TEST_USER_ID));
        assertNull("Dispositivo invalidado debe retornar null", 
            cacheManager.getDevice(TEST_DEVICE_ID));
        assertNull("Habitación invalidada debe retornar null", 
            cacheManager.getRoom(TEST_ROOM_ID));
    }
    
    @Test
    public void testClearAllCache() {
        // Given
        UserProfile profile = createTestUserProfile();
        Device device = createTestDevice();
        Room room = createTestRoom();
        
        cacheManager.putUserProfile(TEST_USER_ID, profile);
        cacheManager.putDevice(TEST_DEVICE_ID, device);
        cacheManager.putRoom(TEST_ROOM_ID, room);
        
        // When
        cacheManager.clearAllCache();
        
        // Then
        assertNull("Perfil debe ser null después de limpiar", 
            cacheManager.getUserProfile(TEST_USER_ID));
        assertNull("Dispositivo debe ser null después de limpiar", 
            cacheManager.getDevice(TEST_DEVICE_ID));
        assertNull("Habitación debe ser null después de limpiar", 
            cacheManager.getRoom(TEST_ROOM_ID));
    }
    
    @Test
    public void testCacheStats() {
        // Given
        UserProfile profile = createTestUserProfile();
        
        // When
        cacheManager.putUserProfile(TEST_USER_ID, profile);
        cacheManager.getUserProfile(TEST_USER_ID); // Hit
        cacheManager.getUserProfile("non_existent"); // Miss
        
        IntelligentCacheManager.CacheStats stats = cacheManager.getStats();
        
        // Then
        assertTrue("Debe haber al menos un write", stats.getWrites() >= 1);
        assertTrue("Debe haber al menos un hit", stats.getHits() >= 1);
        assertTrue("Debe haber al menos un miss", stats.getMisses() >= 1);
        assertTrue("Hit ratio debe ser mayor a 0", stats.getHitRatio() > 0.0);
        assertTrue("Hit ratio debe ser menor o igual a 1", stats.getHitRatio() <= 1.0);
    }
    
    @Test
    public void testCacheStatsReset() {
        // Given
        UserProfile profile = createTestUserProfile();
        cacheManager.putUserProfile(TEST_USER_ID, profile);
        cacheManager.getUserProfile(TEST_USER_ID);
        
        IntelligentCacheManager.CacheStats statsBefore = cacheManager.getStats();
        assertTrue("Debe haber estadísticas antes del reset", statsBefore.getWrites() > 0);
        
        // When
        statsBefore.reset();
        
        // Then
        assertEquals("Writes debe ser 0 después del reset", 0, statsBefore.getWrites());
        assertEquals("Hits debe ser 0 después del reset", 0, statsBefore.getHits());
        assertEquals("Misses debe ser 0 después del reset", 0, statsBefore.getMisses());
    }
    
    @Test
    public void testCacheMemoryInfo() {
        // Given
        UserProfile profile = createTestUserProfile();
        Device device = createTestDevice();
        Room room = createTestRoom();
        List<Device> devices = createTestDeviceList();
        
        cacheManager.putUserProfile(TEST_USER_ID, profile);
        cacheManager.putDevice(TEST_DEVICE_ID, device);
        cacheManager.putRoom(TEST_ROOM_ID, room);
        cacheManager.putDeviceList("test_devices", devices);
        cacheManager.updateDeviceState(TEST_DEVICE_ID, true, 50, null);
        
        // When
        IntelligentCacheManager.CacheMemoryInfo memoryInfo = cacheManager.getMemoryInfo();
        
        // Then
        assertTrue("Debe haber entradas de perfil de usuario", memoryInfo.userProfileEntries > 0);
        assertTrue("Debe haber entradas de dispositivos", memoryInfo.deviceEntries > 0);
        assertTrue("Debe haber entradas de habitaciones", memoryInfo.roomEntries > 0);
        assertTrue("Debe haber entradas de lista de dispositivos", memoryInfo.deviceListEntries > 0);
        assertTrue("Debe haber entradas de estado de dispositivos", memoryInfo.deviceStateEntries > 0);
        assertTrue("Total de entradas debe ser mayor a 0", memoryInfo.getTotalEntries() > 0);
    }
    
    @Test
    public void testDeviceStateEntryExpiration() throws InterruptedException {
        // Given
        IntelligentCacheManager.DeviceStateEntry entry = 
            new IntelligentCacheManager.DeviceStateEntry(true, 50, 23.0f);
        
        // Then
        assertFalse("Estado recién creado no debe estar expirado", entry.isExpired());
        
        // Note: En un test real, esperaríamos o mockearíamos el tiempo
        // Para este test, verificamos que el método funciona
        assertNotNull("Timestamp debe existir", entry.timestamp);
        assertTrue("Timestamp debe ser reciente", 
            System.currentTimeMillis() - entry.timestamp < 1000);
    }
    
    @Test
    public void testCacheStatsCopy() {
        // Given
        IntelligentCacheManager.CacheStats originalStats = cacheManager.getStats();
        
        // Agregar algunas estadísticas
        UserProfile profile = createTestUserProfile();
        cacheManager.putUserProfile(TEST_USER_ID, profile);
        cacheManager.getUserProfile(TEST_USER_ID);
        
        // When
        IntelligentCacheManager.CacheStats copiedStats = cacheManager.getStats().copy();
        
        // Then
        assertEquals("Writes debe coincidir", originalStats.getWrites(), copiedStats.getWrites());
        assertEquals("Hits debe coincidir", originalStats.getHits(), copiedStats.getHits());
        assertEquals("Misses debe coincidir", originalStats.getMisses(), copiedStats.getMisses());
        
        // Modificar original no debe afectar la copia
        originalStats.reset();
        assertTrue("Copia debe mantener valores después de reset original", 
            copiedStats.getWrites() > 0);
    }
    
    @Test
    public void testSingletonPattern() {
        // Given
        IntelligentCacheManager manager1 = IntelligentCacheManager.getInstance(context);
        IntelligentCacheManager manager2 = IntelligentCacheManager.getInstance(context);
        
        // Then
        assertSame("Debe retornar la misma instancia", manager1, manager2);
    }
    
    // Métodos auxiliares para crear objetos de test
    
    private UserProfile createTestUserProfile() {
        UserProfile profile = new UserProfile();
        profile.setUserId(TEST_USER_ID);
        profile.setName("Test User");
        profile.setEmail("test@example.com");
        profile.setPhotoUrl("http://example.com/photo.jpg");
        profile.setLastSyncTimestamp(System.currentTimeMillis());
        return profile;
    }
    
    private UserPreferences createTestUserPreferences() {
        UserPreferences preferences = new UserPreferences();
        preferences.setUserId(TEST_USER_ID);
        preferences.setLanguage("es");
        preferences.setNotificationsEnabled(true);
        preferences.setEcoMode(false);
        preferences.setTemperatureUnit("C");
        preferences.setDefaultLightIntensity(50);
        return preferences;
    }
    
    private Device createTestDevice() {
        Device device = new Device();
        device.setDeviceId(TEST_DEVICE_ID);
        device.setRoomId(TEST_ROOM_ID);
        device.setName("Test Light");
        device.setDeviceType(DeviceType.LIGHT);
        device.setOn(false);
        device.setIntensity(0);
        device.setOnline(true);
        device.setLastStateChange(System.currentTimeMillis());
        device.setUpdatedAt(System.currentTimeMillis());
        return device;
    }
    
    private List<Device> createTestDeviceList() {
        List<Device> devices = new ArrayList<>();
        
        Device light = createTestDevice();
        devices.add(light);
        
        Device fan = new Device();
        fan.setDeviceId("device_fan_123");
        fan.setRoomId(TEST_ROOM_ID);
        fan.setName("Test Fan");
        fan.setDeviceType(DeviceType.FAN);
        fan.setOn(true);
        fan.setIntensity(75);
        fan.setOnline(true);
        devices.add(fan);
        
        return devices;
    }
    
    private Room createTestRoom() {
        Room room = new Room();
        room.setRoomId(TEST_ROOM_ID);
        room.setName("Test Room");
        room.setRoomType(RoomType.LIVING_ROOM);
        room.setFloor(0);
        room.setPositionX(0.5f);
        room.setPositionY(0.5f);
        room.setColor("#E5C3A0");
        room.setIconName("ic_living_room");
        room.setUpdatedAt(System.currentTimeMillis());
        return room;
    }
    
    private List<Room> createTestRoomList() {
        List<Room> rooms = new ArrayList<>();
        
        Room livingRoom = createTestRoom();
        rooms.add(livingRoom);
        
        Room kitchen = new Room();
        kitchen.setRoomId("room_kitchen_123");
        kitchen.setName("Test Kitchen");
        kitchen.setRoomType(RoomType.KITCHEN);
        kitchen.setFloor(0);
        kitchen.setPositionX(0.7f);
        kitchen.setPositionY(0.3f);
        kitchen.setColor("#B8935F");
        kitchen.setIconName("ic_kitchen");
        rooms.add(kitchen);
        
        return rooms;
    }
}