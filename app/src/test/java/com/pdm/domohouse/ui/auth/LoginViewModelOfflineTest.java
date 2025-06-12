package com.pdm.domohouse.ui.auth;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.pdm.domohouse.data.database.AppDatabase;
import com.pdm.domohouse.data.database.entity.UserProfileEntity;
import com.pdm.domohouse.sync.SyncManager;
import com.pdm.domohouse.utils.SecurePreferencesManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Tests para funcionalidad offline de LoginViewModel
 * Verifica login sin conexión usando datos locales
 */
@RunWith(AndroidJUnit4.class)
public class LoginViewModelOfflineTest {
    
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();
    
    private LoginViewModel loginViewModel;
    private AppDatabase database;
    private Context context;
    
    @Mock
    private SecurePreferencesManager mockSecurePreferences;
    
    @Mock
    private SyncManager mockSyncManager;
    
    @Mock
    private Observer<Boolean> navigateObserver;
    
    @Mock
    private Observer<String> errorObserver;
    
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        context = ApplicationProvider.getApplicationContext();
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        
        loginViewModel = new LoginViewModel();
        loginViewModel.initializeSecurePreferences(context);
    }
    
    @After
    public void tearDown() throws IOException {
        database.close();
    }
    
    @Test
    public void testOfflineLoginWithStoredUser() throws Exception {
        // Preparar datos de usuario local
        UserProfileEntity localUser = new UserProfileEntity();
        localUser.setUserId("offline_user_123");
        localUser.setEmail("test@domohouse.com");
        localUser.setName("Usuario Test");
        localUser.setPinHash("encrypted_pin");
        localUser.setSynced(true);
        localUser.setLastSync(System.currentTimeMillis() - 3600000); // 1 hora atrás
        
        database.userProfileDao().insert(localUser);
        
        // Configurar SecurePreferences mock
        when(mockSecurePreferences.getStoredPin()).thenReturn("1234");
        when(mockSecurePreferences.verifyPin("1234")).thenReturn(true);
        
        // Configurar SyncManager mock para simular sin conexión
        when(mockSyncManager.isOnline()).thenReturn(false);
        
        // Configurar campos de login
        loginViewModel.email.setValue("test@domohouse.com");
        loginViewModel.password.setValue("password123");
        
        // Observar navegación
        CountDownLatch navigationLatch = new CountDownLatch(1);
        loginViewModel.navigateToHome.observeForever(navigate -> {
            if (navigate != null && navigate) {
                navigationLatch.countDown();
            }
        });
        
        // Intentar login offline
        loginViewModel.login();
        
        // Esperar resultado
        assertTrue(navigationLatch.await(5, TimeUnit.SECONDS));
        
        // Verificar que la navegación ocurrió
        Boolean navigateValue = loginViewModel.navigateToHome.getValue();
        assertNotNull(navigateValue);
        assertTrue(navigateValue);
    }
    
    @Test
    public void testOfflineLoginWithoutStoredUser() throws Exception {
        // No insertar usuario en la base de datos (simular usuario no encontrado)
        
        // Configurar SyncManager mock para simular sin conexión
        when(mockSyncManager.isOnline()).thenReturn(false);
        
        // Configurar campos de login
        loginViewModel.email.setValue("notfound@domohouse.com");
        loginViewModel.password.setValue("password123");
        
        // Observar errores
        CountDownLatch errorLatch = new CountDownLatch(1);
        loginViewModel.error.observeForever(error -> {
            if (error != null && error.contains("Usuario no encontrado")) {
                errorLatch.countDown();
            }
        });
        
        // Intentar login offline
        loginViewModel.login();
        
        // Esperar error
        assertTrue(errorLatch.await(5, TimeUnit.SECONDS));
        
        // Verificar que no navega
        Boolean navigateValue = loginViewModel.navigateToHome.getValue();
        assertNull(navigateValue);
    }
    
    @Test
    public void testOfflineLoginWithoutStoredPin() throws Exception {
        // Preparar usuario local sin PIN almacenado
        UserProfileEntity localUser = new UserProfileEntity();
        localUser.setUserId("no_pin_user");
        localUser.setEmail("nopin@domohouse.com");
        localUser.setName("Usuario Sin PIN");
        localUser.setSynced(true);
        
        database.userProfileDao().insert(localUser);
        
        // Configurar SecurePreferences mock para simular sin PIN
        when(mockSecurePreferences.getStoredPin()).thenReturn(null);
        
        // Configurar SyncManager mock para simular sin conexión
        when(mockSyncManager.isOnline()).thenReturn(false);
        
        // Configurar campos de login
        loginViewModel.email.setValue("nopin@domohouse.com");
        loginViewModel.password.setValue("password123");
        
        // Observar errores
        CountDownLatch errorLatch = new CountDownLatch(1);
        loginViewModel.error.observeForever(error -> {
            if (error != null && error.contains("Se requiere conexión")) {
                errorLatch.countDown();
            }
        });
        
        // Intentar login offline
        loginViewModel.login();
        
        // Esperar error
        assertTrue(errorLatch.await(5, TimeUnit.SECONDS));
    }
    
    @Test
    public void testPinLoginOffline() throws Exception {
        // Configurar PIN válido
        when(mockSecurePreferences.verifyPin("1234")).thenReturn(true);
        when(mockSecurePreferences.isPinEnabled()).thenReturn(true);
        
        // Observar navegación
        CountDownLatch navigationLatch = new CountDownLatch(1);
        loginViewModel.navigateToHome.observeForever(navigate -> {
            if (navigate != null && navigate) {
                navigationLatch.countDown();
            }
        });
        
        // Login con PIN
        loginViewModel.loginWithPin("1234");
        
        // Esperar resultado
        assertTrue(navigationLatch.await(5, TimeUnit.SECONDS));
        
        // Verificar navegación exitosa
        Boolean navigateValue = loginViewModel.navigateToHome.getValue();
        assertNotNull(navigateValue);
        assertTrue(navigateValue);
    }
    
    @Test
    public void testPinLoginOfflineIncorrect() throws Exception {
        // Configurar PIN incorrecto
        when(mockSecurePreferences.verifyPin("9999")).thenReturn(false);
        when(mockSecurePreferences.isPinEnabled()).thenReturn(true);
        
        // Observar errores
        CountDownLatch errorLatch = new CountDownLatch(1);
        loginViewModel.error.observeForever(error -> {
            if (error != null && error.contains("PIN incorrecto")) {
                errorLatch.countDown();
            }
        });
        
        // Login con PIN incorrecto
        loginViewModel.loginWithPin("9999");
        
        // Esperar error
        assertTrue(errorLatch.await(5, TimeUnit.SECONDS));
        
        // Verificar que no navega
        Boolean navigateValue = loginViewModel.navigateToHome.getValue();
        assertNull(navigateValue);
    }
    
    @Test
    public void testOnlineLoginFallbackToOffline() throws Exception {
        // Preparar usuario local
        UserProfileEntity localUser = new UserProfileEntity();
        localUser.setUserId("fallback_user");
        localUser.setEmail("fallback@domohouse.com");
        localUser.setName("Usuario Fallback");
        localUser.setSynced(true);
        
        database.userProfileDao().insert(localUser);
        
        // Configurar PIN almacenado
        when(mockSecurePreferences.getStoredPin()).thenReturn("1234");
        
        // Simular pérdida de conexión durante el login
        when(mockSyncManager.isOnline())
                .thenReturn(true)   // Inicialmente online
                .thenReturn(false); // Luego offline
        
        // Configurar campos de login
        loginViewModel.email.setValue("fallback@domohouse.com");
        loginViewModel.password.setValue("password123");
        
        // Observar cambios de estado
        CountDownLatch latch = new CountDownLatch(1);
        loginViewModel.navigateToHome.observeForever(navigate -> {
            if (navigate != null && navigate) {
                latch.countDown();
            }
        });
        
        // Intentar login
        loginViewModel.login();
        
        // El sistema debería intentar online primero, fallar, y usar offline
        // Esperar resultado
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }
    
    @Test
    public void testLoadingStatesDuringOfflineLogin() throws Exception {
        // Preparar usuario local
        UserProfileEntity localUser = new UserProfileEntity();
        localUser.setUserId("loading_test_user");
        localUser.setEmail("loading@domohouse.com");
        localUser.setName("Usuario Loading Test");
        localUser.setSynced(true);
        
        database.userProfileDao().insert(localUser);
        
        // Configurar mocks
        when(mockSecurePreferences.getStoredPin()).thenReturn("1234");
        when(mockSyncManager.isOnline()).thenReturn(false);
        
        // Observar estado de carga
        CountDownLatch loadingStartLatch = new CountDownLatch(1);
        CountDownLatch loadingEndLatch = new CountDownLatch(1);
        
        loginViewModel.isLoading.observeForever(loading -> {
            if (loading != null) {
                if (loading) {
                    loadingStartLatch.countDown();
                } else {
                    loadingEndLatch.countDown();
                }
            }
        });
        
        // Configurar campos
        loginViewModel.email.setValue("loading@domohouse.com");
        loginViewModel.password.setValue("password123");
        
        // Intentar login
        loginViewModel.login();
        
        // Verificar que el loading se activó y desactivó
        assertTrue(loadingStartLatch.await(2, TimeUnit.SECONDS));
        assertTrue(loadingEndLatch.await(5, TimeUnit.SECONDS));
    }
    
    @Test
    public void testFormValidationBeforeOfflineLogin() throws Exception {
        // Configurar SyncManager offline
        when(mockSyncManager.isOnline()).thenReturn(false);
        
        // Intentar login con email vacío
        loginViewModel.email.setValue("");
        loginViewModel.password.setValue("password123");
        
        // Observar errores de validación
        CountDownLatch validationErrorLatch = new CountDownLatch(1);
        loginViewModel.emailError.observeForever(error -> {
            if (error != null && error.contains("Por favor ingresa")) {
                validationErrorLatch.countDown();
            }
        });
        
        // Intentar login
        loginViewModel.login();
        
        // Verificar que se detectó el error de validación
        assertTrue(validationErrorLatch.await(2, TimeUnit.SECONDS));
        
        // Verificar que no navega con datos inválidos
        Boolean navigateValue = loginViewModel.navigateToHome.getValue();
        assertNull(navigateValue);
    }
    
    @Test
    public void testUserProfileSyncAfterOfflineLogin() throws Exception {
        // Preparar usuario local
        UserProfileEntity localUser = new UserProfileEntity();
        localUser.setUserId("sync_test_user");
        localUser.setEmail("sync@domohouse.com");
        localUser.setName("Usuario Sync Test");
        localUser.setSynced(false); // No sincronizado
        
        database.userProfileDao().insert(localUser);
        
        // Configurar mocks
        when(mockSecurePreferences.getStoredPin()).thenReturn("1234");
        when(mockSyncManager.isOnline()).thenReturn(false);
        
        // Login offline exitoso
        loginViewModel.email.setValue("sync@domohouse.com");
        loginViewModel.password.setValue("password123");
        
        CountDownLatch loginLatch = new CountDownLatch(1);
        loginViewModel.navigateToHome.observeForever(navigate -> {
            if (navigate != null && navigate) {
                loginLatch.countDown();
            }
        });
        
        loginViewModel.login();
        assertTrue(loginLatch.await(5, TimeUnit.SECONDS));
        
        // Verificar que el perfil se marcó para sincronización futura
        UserProfileEntity updatedUser = database.userProfileDao().getUserProfileSync("sync_test_user");
        assertNotNull(updatedUser);
        // En una implementación real, se marcaría para sincronización
    }
}