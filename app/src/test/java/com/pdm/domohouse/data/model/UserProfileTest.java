package com.pdm.domohouse.data.model;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * Tests unitarios para UserProfile
 */
@RunWith(MockitoJUnitRunner.class)
public class UserProfileTest {
    
    private UserProfile userProfile;
    private final String TEST_USER_ID = "test_user_123";
    private final String TEST_NAME = "Juan Pérez";
    private final String TEST_EMAIL = "juan.perez@example.com";
    
    @Before
    public void setUp() {
        userProfile = new UserProfile(TEST_USER_ID, TEST_NAME, TEST_EMAIL);
    }
    
    @Test
    public void constructor_withValidParams_createsValidProfile() {
        assertNotNull(userProfile);
        assertEquals(TEST_USER_ID, userProfile.getUserId());
        assertEquals(TEST_NAME, userProfile.getFullName());
        assertEquals(TEST_EMAIL, userProfile.getEmail());
        
        // Verificar valores por defecto
        assertNotNull(userProfile.getPreferences());
        assertTrue(userProfile.getRegistrationTimestamp() > 0);
        assertTrue(userProfile.getLastSyncTimestamp() > 0);
    }
    
    @Test
    public void constructor_empty_createsValidProfileWithDefaults() {
        UserProfile emptyProfile = new UserProfile();
        
        assertNotNull(emptyProfile);
        assertNotNull(emptyProfile.getPreferences());
        assertTrue(emptyProfile.getRegistrationTimestamp() > 0);
        assertTrue(emptyProfile.getLastSyncTimestamp() > 0);
    }
    
    @Test
    public void isValid_withValidData_returnsTrue() {
        assertTrue(userProfile.isValid());
    }
    
    @Test
    public void isValid_withNullUserId_returnsFalse() {
        userProfile.setUserId(null);
        assertFalse(userProfile.isValid());
    }
    
    @Test
    public void isValid_withEmptyUserId_returnsFalse() {
        userProfile.setUserId("");
        assertFalse(userProfile.isValid());
    }
    
    @Test
    public void isValid_withEmptyName_returnsFalse() {
        userProfile.setFullName("");
        assertFalse(userProfile.isValid());
    }
    
    @Test
    public void isValid_withNullEmail_returnsFalse() {
        userProfile.setEmail(null);
        assertFalse(userProfile.isValid());
    }
    
    @Test
    public void updateLastLogin_updatesTimestamp() {
        long initialTimestamp = userProfile.getLastLoginTimestamp();
        
        // Esperar un momento para que el timestamp sea diferente
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        userProfile.updateLastLogin();
        
        assertTrue(userProfile.getLastLoginTimestamp() > initialTimestamp);
    }
    
    @Test
    public void updateLastSync_updatesTimestamp() {
        long initialTimestamp = userProfile.getLastSyncTimestamp();
        
        // Esperar un momento para que el timestamp sea diferente
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        userProfile.updateLastSync();
        
        assertTrue(userProfile.getLastSyncTimestamp() > initialTimestamp);
    }
    
    @Test
    public void toMap_createsCorrectMap() {
        userProfile.setPhoneNumber("123456789");
        userProfile.setProfilePhotoUrl("https://example.com/photo.jpg");
        userProfile.setEncryptedPin("encrypted_pin_123");
        
        Map<String, Object> map = userProfile.toMap();
        
        assertNotNull(map);
        assertEquals(TEST_USER_ID, map.get("userId"));
        assertEquals(TEST_NAME, map.get("fullName"));
        assertEquals(TEST_EMAIL, map.get("email"));
        assertEquals("123456789", map.get("phoneNumber"));
        assertEquals("https://example.com/photo.jpg", map.get("profilePhotoUrl"));
        assertEquals("encrypted_pin_123", map.get("encryptedPin"));
        
        // Verificar que incluye preferences
        assertNotNull(map.get("preferences"));
        assertTrue(map.get("preferences") instanceof Map);
    }
    
    @Test
    public void toString_containsKeyInformation() {
        String profileString = userProfile.toString();
        
        assertTrue(profileString.contains(TEST_USER_ID));
        assertTrue(profileString.contains(TEST_NAME));
        assertTrue(profileString.contains(TEST_EMAIL));
    }
    
    @Test
    public void setPhoneNumber_storesCorrectValue() {
        String phoneNumber = "+34 123 456 789";
        userProfile.setPhoneNumber(phoneNumber);
        assertEquals(phoneNumber, userProfile.getPhoneNumber());
    }
    
    @Test
    public void setProfilePhotoUrl_storesCorrectValue() {
        String photoUrl = "https://firebasestorage.googleapis.com/photo123.jpg";
        userProfile.setProfilePhotoUrl(photoUrl);
        assertEquals(photoUrl, userProfile.getProfilePhotoUrl());
    }
    
    @Test
    public void setEncryptedPin_storesCorrectValue() {
        String encryptedPin = "V1_5678";
        userProfile.setEncryptedPin(encryptedPin);
        assertEquals(encryptedPin, userProfile.getEncryptedPin());
    }
    
    @Test
    public void setPreferences_storesCorrectValue() {
        UserPreferences newPreferences = new UserPreferences();
        newPreferences.setLanguage("en");
        newPreferences.setTemperatureUnit("F");
        
        userProfile.setPreferences(newPreferences);
        
        assertEquals(newPreferences, userProfile.getPreferences());
        assertEquals("en", userProfile.getPreferences().getLanguage());
        assertEquals("F", userProfile.getPreferences().getTemperatureUnit());
    }
    
    @Test
    public void setPrimaryDeviceId_storesCorrectValue() {
        String deviceId = "device_123456";
        userProfile.setPrimaryDeviceId(deviceId);
        assertEquals(deviceId, userProfile.getPrimaryDeviceId());
    }
    
    @Test
    public void setFcmToken_storesCorrectValue() {
        String fcmToken = "fcm_token_abc123";
        userProfile.setFcmToken(fcmToken);
        assertEquals(fcmToken, userProfile.getFcmToken());
    }
}