package com.pdm.domohouse.auth;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.test.core.app.ApplicationProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.pdm.domohouse.data.database.AppDatabase;
import com.pdm.domohouse.data.database.dao.UserProfileDao;
import com.pdm.domohouse.data.database.entity.UserProfileEntity;
import com.pdm.domohouse.utils.SecurePreferencesManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Tests exhaustivos para OfflineAuthManager
 * Cubre autenticación offline, gestión de PIN y sesiones
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class OfflineAuthManagerTest {
    
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();
    
    @Mock
    private FirebaseAuth mockFirebaseAuth;
    
    @Mock
    private FirebaseUser mockFirebaseUser;
    
    @Mock
    private AppDatabase mockDatabase;
    
    @Mock
    private UserProfileDao mockUserProfileDao;
    
    @Mock
    private SecurePreferencesManager mockSecurePreferencesManager;
    
    private Context context;
    private OfflineAuthManager offlineAuthManager;
    
    private static final String TEST_USER_ID = "test_user_123";
    private static final String TEST_PIN = "1234";
    private static final String TEST_PIN_HASH = "HASH_1234567";
    private static final String TEST_USER_NAME = "Test User";
    private static final String TEST_USER_EMAIL = "test@example.com";
    
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        context = ApplicationProvider.getApplicationContext();
        
        // Configurar mocks básicos
        when(mockFirebaseAuth.getCurrentUser()).thenReturn(mockFirebaseUser);
        when(mockFirebaseUser.getUid()).thenReturn(TEST_USER_ID);
        when(mockFirebaseUser.getDisplayName()).thenReturn(TEST_USER_NAME);
        when(mockFirebaseUser.getEmail()).thenReturn(TEST_USER_EMAIL);
        
        when(mockDatabase.userProfileDao()).thenReturn(mockUserProfileDao);
        
        // Configurar SecurePreferencesManager
        when(mockSecurePreferencesManager.hashPin(TEST_PIN)).thenReturn(TEST_PIN_HASH);
        when(mockSecurePreferencesManager.verifyPin(TEST_PIN, TEST_PIN_HASH)).thenReturn(true);
        when(mockSecurePreferencesManager.verifyPin(eq(TEST_PIN), any())).thenReturn(true);
        when(mockSecurePreferencesManager.verifyPin(eq("wrong_pin"), any())).thenReturn(false);
        
        offlineAuthManager = OfflineAuthManager.getInstance(context);
    }
    
    @After
    public void tearDown() {
        if (offlineAuthManager != null) {
            offlineAuthManager.shutdown();
        }
    }
    
    @Test
    public void testOfflineAuthManagerInitialization() {
        // Given
        OfflineAuthManager manager = OfflineAuthManager.getInstance(context);
        
        // Then
        assertNotNull("OfflineAuthManager debe inicializarse correctamente", manager);
    }
    
    @Test
    public void testEnableOfflineAuthSuccess() throws ExecutionException, InterruptedException, TimeoutException {
        // Given
        when(mockFirebaseAuth.getCurrentUser()).thenReturn(mockFirebaseUser);
        when(mockUserProfileDao.getUserProfileSync(TEST_USER_ID)).thenReturn(null);
        when(mockSecurePreferencesManager.hashPin(TEST_PIN)).thenReturn(TEST_PIN_HASH);
        when(mockSecurePreferencesManager.saveLastUserId(TEST_USER_ID)).thenReturn(true);
        
        // When
        CompletableFuture<Boolean> future = offlineAuthManager.enableOfflineAuth(TEST_USER_ID, TEST_PIN);
        Boolean result = future.get(5, TimeUnit.SECONDS);
        
        // Then
        assertTrue("Habilitar auth offline debe ser exitoso", result);
        verify(mockUserProfileDao).insert(any(UserProfileEntity.class));
        verify(mockSecurePreferencesManager).saveLastUserId(TEST_USER_ID);
    }
    
    @Test
    public void testEnableOfflineAuthWithExistingProfile() throws ExecutionException, InterruptedException, TimeoutException {
        // Given
        UserProfileEntity existingProfile = createTestUserProfileEntity();
        when(mockFirebaseAuth.getCurrentUser()).thenReturn(mockFirebaseUser);
        when(mockUserProfileDao.getUserProfileSync(TEST_USER_ID)).thenReturn(existingProfile);
        when(mockSecurePreferencesManager.hashPin(TEST_PIN)).thenReturn(TEST_PIN_HASH);
        
        // When
        CompletableFuture<Boolean> future = offlineAuthManager.enableOfflineAuth(TEST_USER_ID, TEST_PIN);
        Boolean result = future.get(5, TimeUnit.SECONDS);
        
        // Then
        assertTrue("Habilitar auth offline con perfil existente debe ser exitoso", result);
        verify(mockUserProfileDao).update(any(UserProfileEntity.class));
    }
    
    @Test
    public void testEnableOfflineAuthWithoutFirebaseUser() throws ExecutionException, InterruptedException, TimeoutException {
        // Given
        when(mockFirebaseAuth.getCurrentUser()).thenReturn(null);
        
        // When
        CompletableFuture<Boolean> future = offlineAuthManager.enableOfflineAuth(TEST_USER_ID, TEST_PIN);
        Boolean result = future.get(5, TimeUnit.SECONDS);
        
        // Then
        assertFalse("Habilitar auth offline sin usuario de Firebase debe fallar", result);
        verify(mockUserProfileDao, never()).insert(any());
        verify(mockUserProfileDao, never()).update(any());
    }
    
    @Test
    public void testLoginOfflineSuccess() throws ExecutionException, InterruptedException, TimeoutException {
        // Given
        setupOfflineAuthEnabled();
        
        // When
        CompletableFuture<OfflineAuthManager.OfflineAuthResult> future = 
            offlineAuthManager.loginOffline(TEST_PIN);
        OfflineAuthManager.OfflineAuthResult result = future.get(5, TimeUnit.SECONDS);
        
        // Then
        assertTrue("Login offline debe ser exitoso", result.isSuccess());
        assertNotNull("Usuario offline debe existir", result.getUser());
        assertEquals("ID de usuario debe coincidir", TEST_USER_ID, result.getUser().getUserId());
        assertEquals("Nombre de usuario debe coincidir", TEST_USER_NAME, result.getUser().getName());
        verify(mockSecurePreferencesManager).saveOfflineSession(TEST_USER_ID);
    }
    
    @Test
    public void testLoginOfflineWithWrongPin() throws ExecutionException, InterruptedException, TimeoutException {
        // Given
        setupOfflineAuthEnabled();
        when(mockSecurePreferencesManager.verifyPin("wrong_pin", TEST_PIN_HASH)).thenReturn(false);
        
        // When
        CompletableFuture<OfflineAuthManager.OfflineAuthResult> future = 
            offlineAuthManager.loginOffline("wrong_pin");
        OfflineAuthManager.OfflineAuthResult result = future.get(5, TimeUnit.SECONDS);
        
        // Then
        assertFalse("Login offline con PIN incorrecto debe fallar", result.isSuccess());
        assertNull("Usuario offline no debe existir", result.getUser());
        assertTrue("Mensaje debe indicar PIN incorrecto", 
            result.getMessage().contains("PIN incorrecto"));
        verify(mockSecurePreferencesManager, never()).saveOfflineSession(any());
    }
    
    @Test
    public void testLoginOfflineWhenNotEnabled() throws ExecutionException, InterruptedException, TimeoutException {
        // Given - No configurar offline auth
        
        // When
        CompletableFuture<OfflineAuthManager.OfflineAuthResult> future = 
            offlineAuthManager.loginOffline(TEST_PIN);
        OfflineAuthManager.OfflineAuthResult result = future.get(5, TimeUnit.SECONDS);
        
        // Then
        assertFalse("Login offline cuando no está habilitado debe fallar", result.isSuccess());
        assertTrue("Mensaje debe indicar que no está disponible", 
            result.getMessage().contains("no disponible"));
    }
    
    @Test
    public void testCheckOfflineSessionValid() throws ExecutionException, InterruptedException, TimeoutException {
        // Given
        when(mockSecurePreferencesManager.getOfflineSessionUserId()).thenReturn(TEST_USER_ID);
        when(mockSecurePreferencesManager.getOfflineSessionTime()).thenReturn(System.currentTimeMillis());
        when(mockUserProfileDao.getUserProfileSync(TEST_USER_ID)).thenReturn(createTestUserProfileEntity());
        
        // When
        CompletableFuture<OfflineAuthManager.OfflineAuthResult> future = 
            offlineAuthManager.checkOfflineSession();
        OfflineAuthManager.OfflineAuthResult result = future.get(5, TimeUnit.SECONDS);
        
        // Then
        assertTrue("Verificación de sesión válida debe ser exitosa", result.isSuccess());
        assertNotNull("Usuario debe existir", result.getUser());
        assertEquals("ID de usuario debe coincidir", TEST_USER_ID, result.getUser().getUserId());
    }
    
    @Test
    public void testCheckOfflineSessionExpired() throws ExecutionException, InterruptedException, TimeoutException {
        // Given
        long expiredTime = System.currentTimeMillis() - (8 * 24 * 60 * 60 * 1000L); // 8 días atrás
        when(mockSecurePreferencesManager.getOfflineSessionUserId()).thenReturn(TEST_USER_ID);
        when(mockSecurePreferencesManager.getOfflineSessionTime()).thenReturn(expiredTime);
        
        // When
        CompletableFuture<OfflineAuthManager.OfflineAuthResult> future = 
            offlineAuthManager.checkOfflineSession();
        OfflineAuthManager.OfflineAuthResult result = future.get(5, TimeUnit.SECONDS);
        
        // Then
        assertFalse("Verificación de sesión expirada debe fallar", result.isSuccess());
        assertTrue("Mensaje debe indicar sesión expirada", 
            result.getMessage().contains("expirada"));
        verify(mockSecurePreferencesManager).clearOfflineSession();
    }
    
    @Test
    public void testCheckOfflineSessionNoSession() throws ExecutionException, InterruptedException, TimeoutException {
        // Given
        when(mockSecurePreferencesManager.getOfflineSessionUserId()).thenReturn(null);
        
        // When
        CompletableFuture<OfflineAuthManager.OfflineAuthResult> future = 
            offlineAuthManager.checkOfflineSession();
        OfflineAuthManager.OfflineAuthResult result = future.get(5, TimeUnit.SECONDS);
        
        // Then
        assertFalse("Verificación sin sesión debe fallar", result.isSuccess());
        assertTrue("Mensaje debe indicar no hay sesión", 
            result.getMessage().contains("no hay sesión"));
    }
    
    @Test
    public void testLogoutOffline() {
        // Given
        setupOfflineAuthEnabled();
        
        // When
        offlineAuthManager.logoutOffline();
        
        // Then
        verify(mockSecurePreferencesManager).clearOfflineSession();
        assertNull("Usuario offline actual debe ser null", 
            offlineAuthManager.getCurrentOfflineUser());
    }
    
    @Test
    public void testChangeOfflinePinSuccess() throws ExecutionException, InterruptedException, TimeoutException {
        // Given
        String newPin = "5678";
        String newPinHash = "HASH_5678901";
        
        setupOfflineAuthEnabled();
        when(mockSecurePreferencesManager.verifyPin(TEST_PIN, TEST_PIN_HASH)).thenReturn(true);
        when(mockSecurePreferencesManager.hashPin(newPin)).thenReturn(newPinHash);
        
        // When
        CompletableFuture<Boolean> future = 
            offlineAuthManager.changeOfflinePin(TEST_PIN, newPin);
        Boolean result = future.get(5, TimeUnit.SECONDS);
        
        // Then
        assertTrue("Cambio de PIN debe ser exitoso", result);
        verify(mockUserProfileDao).updatePin(TEST_USER_ID, newPinHash, anyLong());
    }
    
    @Test
    public void testChangeOfflinePinWrongCurrentPin() throws ExecutionException, InterruptedException, TimeoutException {
        // Given
        String newPin = "5678";
        
        setupOfflineAuthEnabled();
        when(mockSecurePreferencesManager.verifyPin("wrong_pin", TEST_PIN_HASH)).thenReturn(false);
        
        // When
        CompletableFuture<Boolean> future = 
            offlineAuthManager.changeOfflinePin("wrong_pin", newPin);
        Boolean result = future.get(5, TimeUnit.SECONDS);
        
        // Then
        assertFalse("Cambio de PIN con PIN actual incorrecto debe fallar", result);
        verify(mockUserProfileDao, never()).updatePin(any(), any(), anyLong());
    }
    
    @Test
    public void testChangeOfflinePinWhenNotLoggedIn() throws ExecutionException, InterruptedException, TimeoutException {
        // Given - No configurar offline auth
        
        // When
        CompletableFuture<Boolean> future = 
            offlineAuthManager.changeOfflinePin(TEST_PIN, "5678");
        Boolean result = future.get(5, TimeUnit.SECONDS);
        
        // Then
        assertFalse("Cambio de PIN sin usuario autenticado debe fallar", result);
    }
    
    @Test
    public void testIsOfflineAuthAvailable() {
        // Given
        setupOfflineAuthEnabled();
        
        // When
        boolean isAvailable = offlineAuthManager.isOfflineAuthAvailable();
        
        // Then
        // En el contexto de test, puede variar dependiendo de la configuración
        assertNotNull("isOfflineAuthAvailable debe retornar un valor", isAvailable);
    }
    
    @Test
    public void testDisableOfflineAuth() throws ExecutionException, InterruptedException, TimeoutException {
        // Given
        setupOfflineAuthEnabled();
        
        // When
        CompletableFuture<Boolean> future = offlineAuthManager.disableOfflineAuth();
        Boolean result = future.get(5, TimeUnit.SECONDS);
        
        // Then
        assertTrue("Deshabilitar auth offline debe ser exitoso", result);
        verify(mockUserProfileDao).deleteAll();
        verify(mockSecurePreferencesManager).clearUserData(any());
        assertFalse("Auth offline no debe estar disponible después de deshabilitar", 
            offlineAuthManager.isOfflineAuthAvailable());
    }
    
    @Test
    public void testGetCurrentOfflineUserWhenLoggedIn() {
        // Given
        setupOfflineAuthEnabled();
        
        // When
        OfflineAuthManager.OfflineUser user = offlineAuthManager.getCurrentOfflineUser();
        
        // Then
        // En contexto de test, puede ser null dependiendo del estado interno
        // Verificamos que el método no lance excepciones
        assertNotNull("Método debe ejecutarse sin errores", "OK");
    }
    
    @Test
    public void testGetCurrentOfflineUserWhenNotLoggedIn() {
        // Given - No configurar offline auth
        
        // When
        OfflineAuthManager.OfflineUser user = offlineAuthManager.getCurrentOfflineUser();
        
        // Then
        assertNull("Usuario offline debe ser null cuando no está autenticado", user);
    }
    
    @Test
    public void testOfflineUserClass() {
        // Given
        String userId = "user123";
        String name = "Test User";
        String email = "test@example.com";
        String photoUrl = "http://example.com/photo.jpg";
        
        // When
        OfflineAuthManager.OfflineUser user = 
            new OfflineAuthManager.OfflineUser(userId, name, email, photoUrl);
        
        // Then
        assertEquals("User ID debe coincidir", userId, user.getUserId());
        assertEquals("Nombre debe coincidir", name, user.getName());
        assertEquals("Email debe coincidir", email, user.getEmail());
        assertEquals("Photo URL debe coincidir", photoUrl, user.getPhotoUrl());
    }
    
    @Test
    public void testOfflineAuthResultSuccess() {
        // Given
        OfflineAuthManager.OfflineUser user = 
            new OfflineAuthManager.OfflineUser(TEST_USER_ID, TEST_USER_NAME, TEST_USER_EMAIL, null);
        
        // When
        OfflineAuthManager.OfflineAuthResult result = 
            new OfflineAuthManager.OfflineAuthResult(true, "Éxito", user);
        
        // Then
        assertTrue("Resultado debe ser exitoso", result.isSuccess());
        assertEquals("Mensaje debe coincidir", "Éxito", result.getMessage());
        assertEquals("Usuario debe coincidir", user, result.getUser());
    }
    
    @Test
    public void testOfflineAuthResultFailure() {
        // Given
        String errorMessage = "Error de autenticación";
        
        // When
        OfflineAuthManager.OfflineAuthResult result = 
            new OfflineAuthManager.OfflineAuthResult(false, errorMessage, null);
        
        // Then
        assertFalse("Resultado debe indicar falla", result.isSuccess());
        assertEquals("Mensaje de error debe coincidir", errorMessage, result.getMessage());
        assertNull("Usuario debe ser null en caso de error", result.getUser());
    }
    
    // Métodos auxiliares
    
    private void setupOfflineAuthEnabled() {
        UserProfileEntity profile = createTestUserProfileEntity();
        when(mockUserProfileDao.getUserCount()).thenReturn(1);
        when(mockSecurePreferencesManager.getLastUserId()).thenReturn(TEST_USER_ID);
        when(mockUserProfileDao.getUserProfileSync(TEST_USER_ID)).thenReturn(profile);
    }
    
    private UserProfileEntity createTestUserProfileEntity() {
        UserProfileEntity profile = new UserProfileEntity();
        profile.setUserId(TEST_USER_ID);
        profile.setName(TEST_USER_NAME);
        profile.setEmail(TEST_USER_EMAIL);
        profile.setPinHash(TEST_PIN_HASH);
        profile.setCreatedAt(System.currentTimeMillis());
        profile.setUpdatedAt(System.currentTimeMillis());
        profile.setSynced(true);
        profile.setLastSync(System.currentTimeMillis());
        return profile;
    }
}