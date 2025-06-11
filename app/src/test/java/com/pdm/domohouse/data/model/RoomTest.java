package com.pdm.domohouse.data.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests unitarios para la clase Room
 * Prueba la lógica de los modelos de habitaciones
 */
public class RoomTest {

    private Room room;

    @Before
    public void setUp() {
        room = new Room("test_room", "Habitación de Prueba", RoomType.LIVING_ROOM, 
                       0.1f, 0.2f, 0.3f, 0.4f);
    }

    @Test
    public void testRoomCreation() {
        assertNotNull("Room no debe ser null", room);
        assertEquals("ID debe coincidir", "test_room", room.getId());
        assertEquals("Nombre debe coincidir", "Habitación de Prueba", room.getName());
        assertEquals("Tipo debe coincidir", RoomType.LIVING_ROOM, room.getType());
        assertEquals("Posición X debe coincidir", 0.1f, room.getPositionX(), 0.01f);
        assertEquals("Posición Y debe coincidir", 0.2f, room.getPositionY(), 0.01f);
        assertEquals("Ancho debe coincidir", 0.3f, room.getWidth(), 0.01f);
        assertEquals("Alto debe coincidir", 0.4f, room.getHeight(), 0.01f);
    }

    @Test
    public void testDefaultValues() {
        // Verificar valores por defecto
        assertEquals("Temperatura por defecto", 22.0f, room.getTemperature(), 0.1f);
        assertEquals("Humedad por defecto", 45.0f, room.getHumidity(), 0.1f);
        assertEquals("Nivel de luz por defecto", 0, room.getLightLevel());
        assertEquals("Velocidad de ventilador por defecto", 0, room.getFanSpeed());
        assertEquals("Dispositivos activos por defecto", 0, room.getActiveDevices());
        assertFalse("Puerta debe estar cerrada por defecto", room.isDoorOpen());
        assertFalse("Ventana debe estar cerrada por defecto", room.isWindowOpen());
    }

    @Test
    public void testSimpleConstructor() {
        Room simpleRoom = new Room("simple", "Simple", RoomType.BEDROOM);
        
        assertNotNull("Room simple no debe ser null", simpleRoom);
        assertEquals("ID debe coincidir", "simple", simpleRoom.getId());
        assertEquals("Posición X debe ser 0", 0f, simpleRoom.getPositionX(), 0.01f);
        assertEquals("Posición Y debe ser 0", 0f, simpleRoom.getPositionY(), 0.01f);
        assertEquals("Ancho debe ser 1", 1f, simpleRoom.getWidth(), 0.01f);
        assertEquals("Alto debe ser 1", 1f, simpleRoom.getHeight(), 0.01f);
    }

    @Test
    public void testLightLevelBounds() {
        // Probar límites del nivel de luz
        room.setLightLevel(150); // Excede máximo
        assertEquals("Nivel de luz debe limitarse a 100", 100, room.getLightLevel());
        
        room.setLightLevel(-10); // Por debajo del mínimo
        assertEquals("Nivel de luz debe limitarse a 0", 0, room.getLightLevel());
        
        room.setLightLevel(50); // Valor válido
        assertEquals("Nivel de luz debe ser 50", 50, room.getLightLevel());
    }

    @Test
    public void testFanSpeedBounds() {
        // Probar límites de velocidad del ventilador
        room.setFanSpeed(150); // Excede máximo
        assertEquals("Velocidad debe limitarse a 100", 100, room.getFanSpeed());
        
        room.setFanSpeed(-10); // Por debajo del mínimo
        assertEquals("Velocidad debe limitarse a 0", 0, room.getFanSpeed());
        
        room.setFanSpeed(75); // Valor válido
        assertEquals("Velocidad debe ser 75", 75, room.getFanSpeed());
    }

    @Test
    public void testHasActiveDevices() {
        // Sin dispositivos activos
        assertFalse("No debe tener dispositivos activos", room.hasActiveDevices());
        
        // Con luz encendida
        room.setLightLevel(50);
        assertTrue("Debe tener dispositivos activos con luz", room.hasActiveDevices());
        
        // Resetear y probar con ventilador
        room.setLightLevel(0);
        room.setFanSpeed(30);
        assertTrue("Debe tener dispositivos activos con ventilador", room.hasActiveDevices());
        
        // Resetear y probar con contador de dispositivos
        room.setFanSpeed(0);
        room.setActiveDevices(2);
        assertTrue("Debe tener dispositivos activos por contador", room.hasActiveDevices());
    }

    @Test
    public void testOverallStatusNormal() {
        // Condiciones normales
        room.setTemperature(22.0f);
        room.setHumidity(45.0f);
        room.setLightLevel(0);
        room.setFanSpeed(0);
        room.setActiveDevices(0);
        
        assertEquals("Estado debe ser NORMAL", RoomStatus.NORMAL, room.getOverallStatus());
    }

    @Test
    public void testOverallStatusActive() {
        // Con dispositivos activos
        room.setTemperature(22.0f);
        room.setHumidity(45.0f);
        room.setLightLevel(50);
        
        assertEquals("Estado debe ser ACTIVE", RoomStatus.ACTIVE, room.getOverallStatus());
    }

    @Test
    public void testOverallStatusWarningTemperature() {
        // Temperatura alta
        room.setTemperature(30.0f);
        room.setHumidity(45.0f);
        
        assertEquals("Estado debe ser WARNING por temperatura", 
                    RoomStatus.WARNING, room.getOverallStatus());
    }

    @Test
    public void testOverallStatusWarningHumidity() {
        // Humedad alta
        room.setTemperature(22.0f);
        room.setHumidity(75.0f);
        
        assertEquals("Estado debe ser WARNING por humedad", 
                    RoomStatus.WARNING, room.getOverallStatus());
    }

    @Test
    public void testTemperatureAndHumiditySetters() {
        room.setTemperature(25.5f);
        assertEquals("Temperatura debe ser 25.5", 25.5f, room.getTemperature(), 0.01f);
        
        room.setHumidity(60.0f);
        assertEquals("Humedad debe ser 60.0", 60.0f, room.getHumidity(), 0.01f);
    }

    @Test
    public void testDoorAndWindowStates() {
        // Probar estados de puerta
        room.setDoorOpen(true);
        assertTrue("Puerta debe estar abierta", room.isDoorOpen());
        
        room.setDoorOpen(false);
        assertFalse("Puerta debe estar cerrada", room.isDoorOpen());
        
        // Probar estados de ventana
        room.setWindowOpen(true);
        assertTrue("Ventana debe estar abierta", room.isWindowOpen());
        
        room.setWindowOpen(false);
        assertFalse("Ventana debe estar cerrada", room.isWindowOpen());
    }

    @Test
    public void testPositionSetters() {
        room.setPositionX(0.5f);
        assertEquals("Posición X debe ser 0.5", 0.5f, room.getPositionX(), 0.01f);
        
        room.setPositionY(0.7f);
        assertEquals("Posición Y debe ser 0.7", 0.7f, room.getPositionY(), 0.01f);
        
        room.setWidth(0.8f);
        assertEquals("Ancho debe ser 0.8", 0.8f, room.getWidth(), 0.01f);
        
        room.setHeight(0.6f);
        assertEquals("Alto debe ser 0.6", 0.6f, room.getHeight(), 0.01f);
    }

    @Test
    public void testToString() {
        String toString = room.toString();
        
        assertNotNull("toString no debe ser null", toString);
        assertTrue("toString debe contener ID", toString.contains("test_room"));
        assertTrue("toString debe contener nombre", toString.contains("Habitación de Prueba"));
        assertTrue("toString debe contener tipo", toString.contains("LIVING_ROOM"));
    }

    @Test
    public void testActiveDevicesCounter() {
        room.setActiveDevices(5);
        assertEquals("Contador de dispositivos debe ser 5", 5, room.getActiveDevices());
        
        // Valores negativos no deberían ser válidos, pero no hay validación explícita
        room.setActiveDevices(-1);
        assertEquals("Contador puede ser negativo", -1, room.getActiveDevices());
    }
}