package com.pdm.domohouse.data.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests unitarios para la clase Device
 * Prueba la lógica de los modelos de dispositivos IoT
 */
public class DeviceTest {

    private Device device;

    @Before
    public void setUp() {
        device = new Device("test_device", "Dispositivo de Prueba", 
                           DeviceType.LIGHT_SWITCH, "test_room");
    }

    @Test
    public void testDeviceCreation() {
        assertNotNull("Device no debe ser null", device);
        assertEquals("ID debe coincidir", "test_device", device.getId());
        assertEquals("Nombre debe coincidir", "Dispositivo de Prueba", device.getName());
        assertEquals("Tipo debe coincidir", DeviceType.LIGHT_SWITCH, device.getType());
        assertEquals("Room ID debe coincidir", "test_room", device.getRoomId());
    }

    @Test
    public void testDefaultValues() {
        // Verificar valores por defecto
        assertFalse("Debe estar deshabilitado por defecto", device.isEnabled());
        assertFalse("Debe estar desconectado por defecto", device.isConnected());
        assertEquals("Valor actual por defecto", 0f, device.getCurrentValue(), 0.01f);
        assertEquals("Nivel de batería por defecto", 100, device.getBatteryLevel());
        assertFalse("Modo automático deshabilitado por defecto", device.isAutoMode());
        assertEquals("Señal al máximo por defecto", 100, device.getSignalStrength());
        assertTrue("Debe tener timestamp de creación", device.getLastUpdated() > 0);
    }

    @Test
    public void testMinMaxValues() {
        // Verificar que los valores min/max se configuran correctamente del tipo
        assertEquals("Valor mínimo debe coincidir con el tipo", 
                    device.getType().getDefaultMinValue(), device.getMinValue(), 0.01f);
        assertEquals("Valor máximo debe coincidir con el tipo", 
                    device.getType().getDefaultMaxValue(), device.getMaxValue(), 0.01f);
        assertEquals("Unidad debe coincidir con el tipo", 
                    device.getType().getDefaultUnit(), device.getUnit());
    }

    @Test
    public void testCurrentValueBounds() {
        // Configurar dispositivo con rango
        Device rangeDevice = new Device("range_test", "Test Range", 
                                       DeviceType.DIMMER_LIGHT, "test_room");
        
        // Probar valor dentro del rango
        rangeDevice.setCurrentValue(50f);
        assertEquals("Valor dentro del rango", 50f, rangeDevice.getCurrentValue(), 0.01f);
        
        // Probar valor por encima del máximo
        rangeDevice.setCurrentValue(150f);
        assertEquals("Valor debe limitarse al máximo", 
                    rangeDevice.getMaxValue(), rangeDevice.getCurrentValue(), 0.01f);
        
        // Probar valor por debajo del mínimo
        rangeDevice.setCurrentValue(-10f);
        assertEquals("Valor debe limitarse al mínimo", 
                    rangeDevice.getMinValue(), rangeDevice.getCurrentValue(), 0.01f);
    }

    @Test
    public void testBatteryLevelBounds() {
        // Probar límites del nivel de batería
        device.setBatteryLevel(150); // Excede máximo
        assertEquals("Nivel de batería debe limitarse a 100", 100, device.getBatteryLevel());
        
        device.setBatteryLevel(-10); // Por debajo del mínimo
        assertEquals("Nivel de batería debe limitarse a 0", 0, device.getBatteryLevel());
        
        device.setBatteryLevel(50); // Valor válido
        assertEquals("Nivel de batería debe ser 50", 50, device.getBatteryLevel());
    }

    @Test
    public void testSignalStrengthBounds() {
        // Probar límites de señal
        device.setSignalStrength(150); // Excede máximo
        assertEquals("Señal debe limitarse a 100", 100, device.getSignalStrength());
        
        device.setSignalStrength(-10); // Por debajo del mínimo
        assertEquals("Señal debe limitarse a 0", 0, device.getSignalStrength());
        
        device.setSignalStrength(75); // Valor válido
        assertEquals("Señal debe ser 75", 75, device.getSignalStrength());
    }

    @Test
    public void testDeviceStatusOffline() {
        device.setConnected(false);
        assertEquals("Estado debe ser OFFLINE cuando está desconectado", 
                    RoomStatus.OFFLINE, device.getDeviceStatus());
    }

    @Test
    public void testDeviceStatusWarningLowBattery() {
        device.setConnected(true);
        device.setBatteryLevel(15); // Batería baja
        assertEquals("Estado debe ser WARNING con batería baja", 
                    RoomStatus.WARNING, device.getDeviceStatus());
    }

    @Test
    public void testDeviceStatusActive() {
        device.setConnected(true);
        device.setBatteryLevel(80);
        device.setEnabled(true);
        device.setCurrentValue(1f); // Para switch, cualquier valor > 0
        
        assertEquals("Estado debe ser ACTIVE cuando está encendido", 
                    RoomStatus.ACTIVE, device.getDeviceStatus());
    }

    @Test
    public void testDeviceStatusNormal() {
        device.setConnected(true);
        device.setBatteryLevel(80);
        device.setEnabled(false);
        device.setCurrentValue(0f);
        
        assertEquals("Estado debe ser NORMAL", 
                    RoomStatus.NORMAL, device.getDeviceStatus());
    }

    @Test
    public void testNeedsAttention() {
        // No necesita atención por defecto
        device.setConnected(true);
        device.setBatteryLevel(80);
        device.setSignalStrength(50);
        assertFalse("No debe necesitar atención", device.needsAttention());
        
        // Necesita atención por desconexión
        device.setConnected(false);
        assertTrue("Debe necesitar atención por desconexión", device.needsAttention());
        
        // Necesita atención por batería baja
        device.setConnected(true);
        device.setBatteryLevel(15);
        assertTrue("Debe necesitar atención por batería baja", device.needsAttention());
        
        // Necesita atención por señal baja
        device.setBatteryLevel(80);
        device.setSignalStrength(25);
        assertTrue("Debe necesitar atención por señal baja", device.needsAttention());
    }

    @Test
    public void testFormattedValueSwitch() {
        // Para dispositivos switch
        device.setEnabled(true);
        assertEquals("Switch encendido debe mostrar 'Encendido'", 
                    "Encendido", device.getFormattedValue());
        
        device.setEnabled(false);
        assertEquals("Switch apagado debe mostrar 'Apagado'", 
                    "Apagado", device.getFormattedValue());
    }

    @Test
    public void testFormattedValueRange() {
        // Para dispositivos con rango
        Device tempSensor = new Device("temp", "Temperatura", 
                                      DeviceType.TEMPERATURE_SENSOR, "room");
        tempSensor.setCurrentValue(23.5f);
        
        String formatted = tempSensor.getFormattedValue();
        assertTrue("Valor formateado debe incluir unidad", formatted.contains("°C"));
        assertTrue("Valor formateado debe incluir valor", formatted.contains("23.5"));
    }

    @Test
    public void testValuePercentage() {
        Device dimmer = new Device("dimmer", "Dimmer", 
                                  DeviceType.DIMMER_LIGHT, "room");
        
        // Valor al 50%
        dimmer.setCurrentValue(50f);
        assertEquals("Porcentaje debe ser 50", 50, dimmer.getValuePercentage());
        
        // Valor al máximo
        dimmer.setCurrentValue(100f);
        assertEquals("Porcentaje debe ser 100", 100, dimmer.getValuePercentage());
        
        // Valor al mínimo
        dimmer.setCurrentValue(0f);
        assertEquals("Porcentaje debe ser 0", 0, dimmer.getValuePercentage());
    }

    @Test
    public void testValuePercentageEdgeCase() {
        // Caso donde min == max
        device.setMinValue(50f);
        device.setMaxValue(50f);
        device.setCurrentValue(50f);
        assertEquals("Porcentaje debe ser 0 cuando min == max", 
                    0, device.getValuePercentage());
    }

    @Test
    public void testIsSensor() {
        Device sensor = new Device("sensor", "Sensor", 
                                  DeviceType.TEMPERATURE_SENSOR, "room");
        assertTrue("Sensor de temperatura debe ser sensor", sensor.isSensor());
        
        Device actuator = new Device("light", "Luz", 
                                    DeviceType.LIGHT_SWITCH, "room");
        assertFalse("Switch de luz no debe ser sensor", actuator.isSensor());
    }

    @Test
    public void testIsActuator() {
        Device sensor = new Device("sensor", "Sensor", 
                                  DeviceType.TEMPERATURE_SENSOR, "room");
        assertFalse("Sensor de temperatura no debe ser actuador", sensor.isActuator());
        
        Device actuator = new Device("light", "Luz", 
                                    DeviceType.LIGHT_SWITCH, "room");
        assertTrue("Switch de luz debe ser actuador", actuator.isActuator());
    }

    @Test
    public void testLastUpdatedTimestamp() {
        long initialTime = device.getLastUpdated();
        
        // Esperar un poco
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            // Ignorar
        }
        
        // Cambiar estado para actualizar timestamp
        device.setEnabled(true);
        long updatedTime = device.getLastUpdated();
        
        assertTrue("Timestamp debe actualizarse", updatedTime > initialTime);
    }

    @Test
    public void testMinutesSinceLastUpdate() {
        // Simular timestamp antiguo (5 minutos atrás)
        long fiveMinutesAgo = System.currentTimeMillis() - (5 * 60 * 1000);
        device.setLastUpdated(fiveMinutesAgo);
        
        long minutes = device.getMinutesSinceLastUpdate();
        assertTrue("Debe ser aproximadamente 5 minutos", minutes >= 4 && minutes <= 6);
    }

    @Test
    public void testToString() {
        String toString = device.toString();
        
        assertNotNull("toString no debe ser null", toString);
        assertTrue("toString debe contener ID", toString.contains("test_device"));
        assertTrue("toString debe contener nombre", toString.contains("Dispositivo de Prueba"));
        assertTrue("toString debe contener tipo", toString.contains("LIGHT_SWITCH"));
    }

    @Test
    public void testSettersUpdateTimestamp() {
        long initialTime = device.getLastUpdated();
        
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            // Ignorar
        }
        
        // Probar que los setters principales actualizan el timestamp
        device.setConnected(true);
        assertTrue("setConnected debe actualizar timestamp", 
                  device.getLastUpdated() > initialTime);
        
        long afterConnected = device.getLastUpdated();
        
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            // Ignorar
        }
        
        device.setCurrentValue(1f);
        assertTrue("setCurrentValue debe actualizar timestamp", 
                  device.getLastUpdated() > afterConnected);
    }
}