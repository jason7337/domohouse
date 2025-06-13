package com.pdm.domohouse.data.model;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Tests unitarios para los modelos de datos relacionados con temperatura
 * Verifica la lógica de evaluación y cálculos de los modelos
 */
public class TemperatureModelsTest {

    private TemperatureThreshold temperatureThreshold;
    private RoomTemperature roomTemperature;
    private FanControl fanControl;

    @Before
    public void setUp() {
        // Configurar umbral de temperatura de prueba
        temperatureThreshold = new TemperatureThreshold(
            "test_threshold",
            "room_001", 
            "Sala de Prueba",
            18f,  // min temp
            28f,  // max temp
            13f,  // critical low
            33f,  // critical high
            true, // auto control enabled
            true, // enable cooling
            true, // enable heating
            "fan_001", // cooling device
            "heater_001", // heating device
            System.currentTimeMillis(),
            "user_001"
        );

        // Configurar temperatura de habitación de prueba
        roomTemperature = new RoomTemperature(
            "room_001",
            "Sala de Prueba",
            RoomType.LIVING_ROOM,
            24.5f, // current temp
            65f,   // humidity
            true,  // has humidity
            2,     // sensor count
            RoomTemperature.TemperatureStatus.NORMAL,
            System.currentTimeMillis(),
            true   // is online
        );

        // Configurar control de ventilador de prueba
        fanControl = new FanControl(
            "fan_001",
            "Ventilador Sala",
            "Sala de Prueba",
            "room_001",
            DeviceType.FAN_SPEED,
            true,  // is on
            60f,   // speed percentage
            true,  // is connected
            35f,   // power consumption
            System.currentTimeMillis(),
            false, // automatic mode
            24f    // target temperature
        );
    }

    // Tests para TemperatureThreshold

    @Test
    public void testTemperatureThreshold_EvaluateNormalTemperature() {
        float normalTemp = 22f;
        RoomTemperature.TemperatureStatus status = temperatureThreshold.evaluateTemperature(normalTemp);
        
        assertEquals("Normal temperature should be evaluated as NORMAL", 
            RoomTemperature.TemperatureStatus.NORMAL, status);
    }

    @Test
    public void testTemperatureThreshold_EvaluateHighTemperature() {
        float highTemp = 30f;
        RoomTemperature.TemperatureStatus status = temperatureThreshold.evaluateTemperature(highTemp);
        
        assertEquals("High temperature should be evaluated as HIGH", 
            RoomTemperature.TemperatureStatus.HIGH, status);
    }

    @Test
    public void testTemperatureThreshold_EvaluateLowTemperature() {
        float lowTemp = 16f;
        RoomTemperature.TemperatureStatus status = temperatureThreshold.evaluateTemperature(lowTemp);
        
        assertEquals("Low temperature should be evaluated as LOW", 
            RoomTemperature.TemperatureStatus.LOW, status);
    }

    @Test
    public void testTemperatureThreshold_EvaluateCriticalHighTemperature() {
        float criticalHighTemp = 35f;
        RoomTemperature.TemperatureStatus status = temperatureThreshold.evaluateTemperature(criticalHighTemp);
        
        assertEquals("Critical high temperature should be evaluated as CRITICAL_HIGH", 
            RoomTemperature.TemperatureStatus.CRITICAL_HIGH, status);
    }

    @Test
    public void testTemperatureThreshold_EvaluateCriticalLowTemperature() {
        float criticalLowTemp = 10f;
        RoomTemperature.TemperatureStatus status = temperatureThreshold.evaluateTemperature(criticalLowTemp);
        
        assertEquals("Critical low temperature should be evaluated as CRITICAL_LOW", 
            RoomTemperature.TemperatureStatus.CRITICAL_LOW, status);
    }

    @Test
    public void testTemperatureThreshold_IsCriticalTemperature() {
        assertTrue("35°C should be critical", temperatureThreshold.isCriticalTemperature(35f));
        assertTrue("10°C should be critical", temperatureThreshold.isCriticalTemperature(10f));
        assertFalse("22°C should not be critical", temperatureThreshold.isCriticalTemperature(22f));
    }

    @Test
    public void testTemperatureThreshold_NeedsCooling() {
        assertTrue("30°C should need cooling", temperatureThreshold.needsCooling(30f));
        assertFalse("22°C should not need cooling", temperatureThreshold.needsCooling(22f));
        assertFalse("16°C should not need cooling", temperatureThreshold.needsCooling(16f));
    }

    @Test
    public void testTemperatureThreshold_NeedsHeating() {
        assertTrue("16°C should need heating", temperatureThreshold.needsHeating(16f));
        assertFalse("22°C should not need heating", temperatureThreshold.needsHeating(22f));
        assertFalse("30°C should not need heating", temperatureThreshold.needsHeating(30f));
    }

    @Test
    public void testTemperatureThreshold_CalculateIntensity() {
        // Test cooling intensity
        float coolingIntensity = temperatureThreshold.calculateIntensity(30f);
        assertTrue("Cooling intensity should be positive", coolingIntensity > 0);
        assertTrue("Cooling intensity should not exceed 100%", coolingIntensity <= 100f);

        // Test heating intensity
        float heatingIntensity = temperatureThreshold.calculateIntensity(16f);
        assertTrue("Heating intensity should be positive", heatingIntensity > 0);
        assertTrue("Heating intensity should not exceed 100%", heatingIntensity <= 100f);

        // Test no action needed
        float normalIntensity = temperatureThreshold.calculateIntensity(22f);
        assertEquals("Normal temperature should need 0% intensity", 0f, normalIntensity, 0.1f);
    }

    @Test
    public void testTemperatureThreshold_IsValid() {
        assertTrue("Valid threshold should return true", temperatureThreshold.isValid());
        
        // Test invalid threshold
        TemperatureThreshold invalidThreshold = new TemperatureThreshold();
        invalidThreshold.setMinTemperature(30f);
        invalidThreshold.setMaxTemperature(20f); // max < min
        assertFalse("Invalid threshold should return false", invalidThreshold.isValid());
    }

    @Test
    public void testTemperatureThreshold_GetTemperatureRange() {
        String expectedRange = "18.0°C - 28.0°C";
        assertEquals("Temperature range should be formatted correctly", 
            expectedRange, temperatureThreshold.getTemperatureRange());
    }

    // Tests para RoomTemperature

    @Test
    public void testRoomTemperature_GetTemperatureColor() {
        int color = roomTemperature.getTemperatureColor();
        assertNotEquals("Temperature color should not be 0", 0, color);
    }

    @Test
    public void testRoomTemperature_GetStatusColor() {
        int statusColor = roomTemperature.getStatusColor();
        assertNotEquals("Status color should not be 0", 0, statusColor);
        
        // Test offline device
        roomTemperature.setIsOnline(false);
        int offlineColor = roomTemperature.getStatusColor();
        assertNotEquals("Offline color should not be 0", 0, offlineColor);
    }

    @Test
    public void testRoomTemperature_IsTemperatureNormal() {
        assertTrue("Room temperature should be normal", roomTemperature.isTemperatureNormal());
        
        roomTemperature.setStatus(RoomTemperature.TemperatureStatus.HIGH);
        assertFalse("High temperature should not be normal", roomTemperature.isTemperatureNormal());
    }

    @Test
    public void testRoomTemperature_NeedsAttention() {
        assertFalse("Normal online room should not need attention", roomTemperature.needsAttention());
        
        // Test offline room
        roomTemperature.setIsOnline(false);
        assertTrue("Offline room should need attention", roomTemperature.needsAttention());
        
        // Test critical temperature
        roomTemperature.setIsOnline(true);
        roomTemperature.setStatus(RoomTemperature.TemperatureStatus.CRITICAL_HIGH);
        assertTrue("Critical temperature should need attention", roomTemperature.needsAttention());
    }

    @Test
    public void testRoomTemperature_GetStatusText() {
        assertEquals("Normal status text should be correct", "Normal", roomTemperature.getStatusText());
        
        roomTemperature.setIsOnline(false);
        assertEquals("Offline status text should be correct", "Sin conexión", roomTemperature.getStatusText());
        
        roomTemperature.setIsOnline(true);
        roomTemperature.setStatus(RoomTemperature.TemperatureStatus.HIGH);
        assertEquals("High status text should be correct", "Alta", roomTemperature.getStatusText());
    }

    // Tests para FanControl

    @Test
    public void testFanControl_GetSpeedLevel() {
        assertEquals("60% speed should be level 3", 3, fanControl.getSpeedLevel());
        
        fanControl.setSpeedPercentage(90f);
        assertEquals("90% speed should be level 5", 5, fanControl.getSpeedLevel());
        
        fanControl.setIsOn(false);
        assertEquals("Off fan should be level 0", 0, fanControl.getSpeedLevel());
    }

    @Test
    public void testFanControl_GetSpeedDescription() {
        assertEquals("60% speed should be 'Media'", "Media", fanControl.getSpeedDescription());
        
        fanControl.setSpeedPercentage(15f);
        assertEquals("15% speed should be 'Muy baja'", "Muy baja", fanControl.getSpeedDescription());
        
        fanControl.setIsOn(false);
        assertEquals("Off fan should be 'Apagado'", "Apagado", fanControl.getSpeedDescription());
    }

    @Test
    public void testFanControl_IsEfficient() {
        assertTrue("Connected, on fan with power consumption should be efficient", 
            fanControl.isEfficient());
        
        fanControl.setIsConnected(false);
        assertFalse("Disconnected fan should not be efficient", fanControl.isEfficient());
        
        fanControl.setIsConnected(true);
        fanControl.setIsOn(false);
        assertFalse("Off fan should not be efficient", fanControl.isEfficient());
    }

    @Test
    public void testFanControl_GetHourlyPowerConsumption() {
        assertEquals("Hourly consumption should equal power consumption when on", 
            35f, fanControl.getHourlyPowerConsumption(), 0.1f);
        
        fanControl.setIsOn(false);
        assertEquals("Off fan should have 0 consumption", 
            0f, fanControl.getHourlyPowerConsumption(), 0.1f);
    }

    @Test
    public void testFanControl_GetStatusText() {
        assertEquals("Connected on fan should show speed", "Media", fanControl.getStatusText());
        
        fanControl.setIsConnected(false);
        assertEquals("Disconnected fan should show 'Desconectado'", "Desconectado", fanControl.getStatusText());
        
        fanControl.setIsConnected(true);
        fanControl.setIsOn(false);
        assertEquals("Off fan should show 'Apagado'", "Apagado", fanControl.getStatusText());
        
        fanControl.setIsOn(true);
        fanControl.setIsAutomaticMode(true);
        assertTrue("Automatic mode should be mentioned in status", 
            fanControl.getStatusText().contains("Automático"));
    }

    @Test
    public void testFanControl_NeedsAttention() {
        assertFalse("Normal fan should not need attention", fanControl.needsAttention());
        
        fanControl.setIsConnected(false);
        assertTrue("Disconnected fan should need attention", fanControl.needsAttention());
        
        fanControl.setIsConnected(true);
        fanControl.setPowerConsumption(0f);
        assertTrue("Fan with no power consumption when on should need attention", 
            fanControl.needsAttention());
    }
}