package com.pdm.domohouse.data.model;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * Tests unitarios para UserPreferences
 */
@RunWith(MockitoJUnitRunner.class)
public class UserPreferencesTest {
    
    private UserPreferences userPreferences;
    
    @Before
    public void setUp() {
        userPreferences = new UserPreferences();
    }
    
    @Test
    public void constructor_setsDefaultValues() {
        // Verificar configuraciones de aplicación por defecto
        assertTrue(userPreferences.isNotificationsEnabled());
        assertTrue(userPreferences.isPushNotificationsEnabled());
        assertTrue(userPreferences.isSoundEnabled());
        assertTrue(userPreferences.isVibrationEnabled());
        
        // Verificar configuraciones de seguridad por defecto
        assertFalse(userPreferences.isBiometricEnabled());
        assertTrue(userPreferences.isAutoLockEnabled());
        assertEquals(5, userPreferences.getAutoLockTimeoutMinutes());
        
        // Verificar configuraciones de casa por defecto
        assertEquals("C", userPreferences.getTemperatureUnit());
        assertTrue(userPreferences.isAutoModeEnabled());
        assertEquals(22, userPreferences.getDefaultTemperature());
        
        // Verificar configuraciones de usuario por defecto
        assertEquals("es", userPreferences.getLanguage());
        assertEquals("light", userPreferences.getTheme());
        
        // Verificar configuraciones de dispositivos por defecto
        assertTrue(userPreferences.isOfflineModeEnabled());
        assertEquals(15, userPreferences.getSyncFrequencyMinutes());
        
        // Verificar configuraciones avanzadas por defecto
        assertFalse(userPreferences.isDeveloperModeEnabled());
        assertTrue(userPreferences.isAnalyticsEnabled());
    }
    
    @Test
    public void setNotificationsEnabled_storesCorrectValue() {
        userPreferences.setNotificationsEnabled(false);
        assertFalse(userPreferences.isNotificationsEnabled());
        
        userPreferences.setNotificationsEnabled(true);
        assertTrue(userPreferences.isNotificationsEnabled());
    }
    
    @Test
    public void setPushNotificationsEnabled_storesCorrectValue() {
        userPreferences.setPushNotificationsEnabled(false);
        assertFalse(userPreferences.isPushNotificationsEnabled());
    }
    
    @Test
    public void setSoundEnabled_storesCorrectValue() {
        userPreferences.setSoundEnabled(false);
        assertFalse(userPreferences.isSoundEnabled());
    }
    
    @Test
    public void setVibrationEnabled_storesCorrectValue() {
        userPreferences.setVibrationEnabled(false);
        assertFalse(userPreferences.isVibrationEnabled());
    }
    
    @Test
    public void setBiometricEnabled_storesCorrectValue() {
        userPreferences.setBiometricEnabled(true);
        assertTrue(userPreferences.isBiometricEnabled());
    }
    
    @Test
    public void setAutoLockEnabled_storesCorrectValue() {
        userPreferences.setAutoLockEnabled(false);
        assertFalse(userPreferences.isAutoLockEnabled());
    }
    
    @Test
    public void setAutoLockTimeoutMinutes_storesCorrectValue() {
        userPreferences.setAutoLockTimeoutMinutes(10);
        assertEquals(10, userPreferences.getAutoLockTimeoutMinutes());
    }
    
    @Test
    public void setTemperatureUnit_storesCorrectValue() {
        userPreferences.setTemperatureUnit("F");
        assertEquals("F", userPreferences.getTemperatureUnit());
    }
    
    @Test
    public void setAutoModeEnabled_storesCorrectValue() {
        userPreferences.setAutoModeEnabled(false);
        assertFalse(userPreferences.isAutoModeEnabled());
    }
    
    @Test
    public void setDefaultTemperature_storesCorrectValue() {
        userPreferences.setDefaultTemperature(25);
        assertEquals(25, userPreferences.getDefaultTemperature());
    }
    
    @Test
    public void setLanguage_storesCorrectValue() {
        userPreferences.setLanguage("en");
        assertEquals("en", userPreferences.getLanguage());
    }
    
    @Test
    public void setTheme_storesCorrectValue() {
        userPreferences.setTheme("light");
        assertEquals("light", userPreferences.getTheme());
    }
    
    @Test
    public void setOfflineModeEnabled_storesCorrectValue() {
        userPreferences.setOfflineModeEnabled(false);
        assertFalse(userPreferences.isOfflineModeEnabled());
    }
    
    @Test
    public void setSyncFrequencyMinutes_storesCorrectValue() {
        userPreferences.setSyncFrequencyMinutes(30);
        assertEquals(30, userPreferences.getSyncFrequencyMinutes());
    }
    
    @Test
    public void setDeveloperModeEnabled_storesCorrectValue() {
        userPreferences.setDeveloperModeEnabled(true);
        assertTrue(userPreferences.isDeveloperModeEnabled());
    }
    
    @Test
    public void setAnalyticsEnabled_storesCorrectValue() {
        userPreferences.setAnalyticsEnabled(false);
        assertFalse(userPreferences.isAnalyticsEnabled());
    }
    
    @Test
    public void toMap_createsCorrectMap() {
        // Modificar algunos valores para verificar
        userPreferences.setNotificationsEnabled(false);
        userPreferences.setTemperatureUnit("F");
        userPreferences.setLanguage("en");
        userPreferences.setDefaultTemperature(25);
        
        Map<String, Object> map = userPreferences.toMap();
        
        assertNotNull(map);
        
        // Verificar que todos los campos están presentes
        assertEquals(false, map.get("notificationsEnabled"));
        assertEquals("F", map.get("temperatureUnit"));
        assertEquals("en", map.get("language"));
        assertEquals(25, map.get("defaultTemperature"));
        
        // Verificar algunos valores por defecto
        assertEquals(true, map.get("pushNotificationsEnabled"));
        assertEquals("light", map.get("theme"));
        assertEquals(5, map.get("autoLockTimeoutMinutes"));
    }
    
    @Test
    public void resetToDefaults_restoresDefaultValues() {
        // Modificar algunos valores
        userPreferences.setNotificationsEnabled(false);
        userPreferences.setTemperatureUnit("F");
        userPreferences.setLanguage("en");
        userPreferences.setBiometricEnabled(true);
        
        // Verificar que están modificados
        assertFalse(userPreferences.isNotificationsEnabled());
        assertEquals("F", userPreferences.getTemperatureUnit());
        assertEquals("en", userPreferences.getLanguage());
        assertTrue(userPreferences.isBiometricEnabled());
        
        // Restaurar defaults
        userPreferences.resetToDefaults();
        
        // Verificar que volvieron a los valores por defecto
        assertTrue(userPreferences.isNotificationsEnabled());
        assertEquals("C", userPreferences.getTemperatureUnit());
        assertEquals("es", userPreferences.getLanguage());
        assertFalse(userPreferences.isBiometricEnabled());
    }
    
    @Test
    public void copyFrom_copiesAllValues() {
        // Crear preferencias fuente con valores modificados
        UserPreferences source = new UserPreferences();
        source.setNotificationsEnabled(false);
        source.setTemperatureUnit("F");
        source.setLanguage("en");
        source.setBiometricEnabled(true);
        source.setDefaultTemperature(25);
        source.setAutoLockTimeoutMinutes(10);
        
        // Copiar a nuestras preferencias
        userPreferences.copyFrom(source);
        
        // Verificar que todos los valores se copiaron
        assertEquals(source.isNotificationsEnabled(), userPreferences.isNotificationsEnabled());
        assertEquals(source.getTemperatureUnit(), userPreferences.getTemperatureUnit());
        assertEquals(source.getLanguage(), userPreferences.getLanguage());
        assertEquals(source.isBiometricEnabled(), userPreferences.isBiometricEnabled());
        assertEquals(source.getDefaultTemperature(), userPreferences.getDefaultTemperature());
        assertEquals(source.getAutoLockTimeoutMinutes(), userPreferences.getAutoLockTimeoutMinutes());
    }
}