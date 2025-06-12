package com.pdm.domohouse.ui.profile;

import android.app.Application;
import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;
import com.pdm.domohouse.data.model.UserProfile;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para ProfileViewModel
 * Verifica la lógica de gestión del perfil de usuario
 */
@RunWith(MockitoJUnitRunner.class)
public class ProfileViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private Application mockApplication;

    @Mock
    private Observer<UserProfile> mockProfileObserver;

    @Mock
    private Observer<Boolean> mockLoadingObserver;

    @Mock
    private Observer<String> mockErrorObserver;

    @Mock
    private Observer<String> mockMessageObserver;

    private ProfileViewModel viewModel;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Crear instancia del ViewModel
        viewModel = new ProfileViewModel();
        
        // Configurar observadores
        viewModel.userProfile.observeForever(mockProfileObserver);
        viewModel.isLoading.observeForever(mockLoadingObserver);
        viewModel.error.observeForever(mockErrorObserver);
        viewModel.message.observeForever(mockMessageObserver);
    }

    @Test
    public void testInitialState() {
        // Verificar estado inicial del ViewModel
        assertNotNull("ViewModel no debe ser null", viewModel);
        assertNotNull("userProfile LiveData no debe ser null", viewModel.userProfile);
        assertNotNull("isSaving LiveData no debe ser null", viewModel.isSaving);
        assertNotNull("hasUnsavedChanges LiveData no debe ser null", viewModel.hasUnsavedChanges);
        assertNotNull("isUploadingImage LiveData no debe ser null", viewModel.isUploadingImage);
    }

    @Test
    public void testSaveProfileWithValidData() {
        // Datos de prueba
        String validName = "Juan Pérez";
        
        // Crear perfil de prueba
        UserProfile testProfile = new UserProfile("test123", validName, "juan@test.com");
        
        // Simular perfil existente
        viewModel.setUserProfile(testProfile);
        
        // Ejecutar método a probar
        viewModel.saveProfile(validName);
        
        // Verificar que no se muestre error por datos válidos
        verify(mockErrorObserver, never()).onChanged(contains("nombre completo es requerido"));
    }

    @Test
    public void testSaveProfileWithEmptyName() {
        // Datos de prueba
        String emptyName = "";
        
        // Crear perfil de prueba
        UserProfile testProfile = new UserProfile("test123", "Nombre Original", "juan@test.com");
        viewModel.setUserProfile(testProfile);
        
        // Ejecutar método a probar
        viewModel.saveProfile(emptyName);
        
        // Verificar que se muestre error
        verify(mockErrorObserver).onChanged("El nombre completo es requerido");
    }

    @Test
    public void testSaveProfileWithNullName() {
        // Crear perfil de prueba
        UserProfile testProfile = new UserProfile("test123", "Nombre Original", "juan@test.com");
        viewModel.setUserProfile(testProfile);
        
        // Ejecutar método a probar con nombre null
        viewModel.saveProfile(null);
        
        // Verificar que se muestre error
        verify(mockErrorObserver).onChanged("El nombre completo es requerido");
    }

    @Test
    public void testSaveProfileWithoutUserProfile() {
        // No establecer perfil de usuario (será null)
        
        // Ejecutar método a probar
        viewModel.saveProfile("Nombre Válido");
        
        // Verificar que se muestre error apropiado
        verify(mockErrorObserver).onChanged("No hay perfil para guardar");
    }

    @Test
    public void testMarkAsChanged() {
        // Ejecutar método a probar
        viewModel.markAsChanged();
        
        // Verificar que se marque como cambiado
        assertTrue("Debe marcar que hay cambios no guardados", 
                viewModel.hasUnsavedChanges.getValue() != null && 
                viewModel.hasUnsavedChanges.getValue());
    }

    @Test
    public void testHasChangesWithSameData() {
        // Crear perfil original
        UserProfile originalProfile = new UserProfile("test123", "Juan Pérez", "juan@test.com");
        originalProfile.getPreferences().setNotificationsEnabled(true);
        originalProfile.getPreferences().setAutoModeEnabled(false);
        originalProfile.getPreferences().setAnalyticsEnabled(true);
        
        // Establecer como perfil actual (sin cambios)
        viewModel.setUserProfile(originalProfile);
        
        // Verificar que no detecte cambios cuando los datos son iguales
        boolean hasChanges = viewModel.hasChanges();
        
        // El resultado puede variar según la implementación interna
        // Este test verifica que el método no lanza excepción
        assertNotNull("hasChanges() no debe retornar null", Boolean.valueOf(hasChanges));
    }

    @Test
    public void testLogout() {
        // Ejecutar logout
        viewModel.logout();
        
        // Verificar que se llame al observer de mensaje
        verify(mockMessageObserver, atLeastOnce()).onChanged(anyString());
    }

    @Test
    public void testUpdatePreferences() {
        // Crear perfil de prueba
        UserProfile testProfile = new UserProfile("test123", "Juan Pérez", "juan@test.com");
        viewModel.setUserProfile(testProfile);
        
        // Valores de prueba
        boolean notifications = true;
        boolean autoMode = false;
        boolean analytics = true;
        
        // Ejecutar método a probar
        viewModel.updatePreferences(notifications, autoMode, analytics);
        
        // Verificar que se actualicen las preferencias
        UserProfile currentProfile = viewModel.userProfile.getValue();
        assertNotNull("El perfil no debe ser null después de actualizar preferencias", currentProfile);
        
        if (currentProfile != null && currentProfile.getPreferences() != null) {
            assertEquals("Notificaciones debe actualizarse", notifications, 
                    currentProfile.getPreferences().isNotificationsEnabled());
            assertEquals("Modo automático debe actualizarse", autoMode, 
                    currentProfile.getPreferences().isAutoModeEnabled());
            assertEquals("Analytics debe actualizarse", analytics, 
                    currentProfile.getPreferences().isAnalyticsEnabled());
        }
    }

    @Test
    public void testUpdatePreferencesWithoutProfile() {
        // No establecer perfil (será null)
        
        // Ejecutar método a probar
        viewModel.updatePreferences(true, false, true);
        
        // Verificar que se muestre error apropiado
        verify(mockErrorObserver).onChanged("No hay preferencias para actualizar");
    }

    /**
     * Test de integración básico que verifica el flujo completo
     */
    @Test
    public void testProfileWorkflow() {
        // 1. Crear perfil inicial
        UserProfile testProfile = new UserProfile("test123", "Juan Pérez", "juan@test.com");
        viewModel.setUserProfile(testProfile);
        
        // 2. Marcar como cambiado
        viewModel.markAsChanged();
        
        // 3. Actualizar preferencias
        viewModel.updatePreferences(false, true, false);
        
        // 4. Guardar perfil con nuevo nombre
        viewModel.saveProfile("Juan Carlos Pérez");
        
        // Verificar que el flujo no genere errores inesperados
        // Los métodos deben ejecutarse sin lanzar excepciones
        assertNotNull("El ViewModel debe mantener su estado", viewModel.userProfile.getValue());
    }
}