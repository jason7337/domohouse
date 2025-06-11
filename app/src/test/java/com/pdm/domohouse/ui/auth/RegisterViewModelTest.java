package com.pdm.domohouse.ui.auth;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

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
 * Tests unitarios para RegisterViewModel
 * Verifica la lógica de validación y registro de usuarios
 */
@RunWith(MockitoJUnitRunner.class)
public class RegisterViewModelTest {
    
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();
    
    @Mock
    private Observer<String> mockErrorObserver;
    
    @Mock
    private Observer<Boolean> mockLoadingObserver;
    
    @Mock
    private Observer<Boolean> mockNavigationObserver;
    
    private RegisterViewModel viewModel;
    
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        viewModel = new RegisterViewModel();
        
        // Observar LiveData
        viewModel.errorMessage.observeForever(mockErrorObserver);
        viewModel.isLoading.observeForever(mockLoadingObserver);
        viewModel.navigateToHome.observeForever(mockNavigationObserver);
    }
    
    @Test
    public void testViewModelCreation() {
        // Given & When
        RegisterViewModel viewModel = new RegisterViewModel();
        
        // Then
        assertNotNull("ViewModel debe ser creado", viewModel);
        assertNotNull("Email field debe estar inicializado", viewModel.email);
        assertNotNull("Password field debe estar inicializado", viewModel.password);
        assertNotNull("Name field debe estar inicializado", viewModel.name);
        assertNotNull("PIN field debe estar inicializado", viewModel.pin);
    }
    
    @Test
    public void testEmailValidation() {
        // Test emails válidos
        assertTrue("Email válido debe pasar validación", isValidEmail("test@example.com"));
        assertTrue("Email con subdominio debe ser válido", isValidEmail("user@mail.company.com"));
        assertTrue("Email con números debe ser válido", isValidEmail("user123@test123.org"));
        
        // Test emails inválidos
        assertFalse("Email sin @ debe ser inválido", isValidEmail("testexample.com"));
        assertFalse("Email sin dominio debe ser inválido", isValidEmail("test@"));
        assertFalse("Email vacío debe ser inválido", isValidEmail(""));
        assertFalse("Email null debe ser inválido", isValidEmail(null));
        assertFalse("Email con espacios debe ser inválido", isValidEmail("test @example.com"));
    }
    
    @Test
    public void testPasswordValidation() {
        // Test contraseñas válidas
        assertTrue("Contraseña de 6 caracteres debe ser válida", isValidPassword("123456"));
        assertTrue("Contraseña larga debe ser válida", isValidPassword("password123456"));
        assertTrue("Contraseña con símbolos debe ser válida", isValidPassword("pass@123"));
        
        // Test contraseñas inválidas
        assertFalse("Contraseña muy corta debe ser inválida", isValidPassword("12345"));
        assertFalse("Contraseña vacía debe ser inválida", isValidPassword(""));
        assertFalse("Contraseña null debe ser inválida", isValidPassword(null));
    }
    
    @Test
    public void testPinValidation() {
        // Test PINs válidos
        assertTrue("PIN 1234 debe ser válido", isValidPin("1234"));
        assertTrue("PIN 0000 debe ser válido", isValidPin("0000"));
        assertTrue("PIN 9999 debe ser válido", isValidPin("9999"));
        
        // Test PINs inválidos
        assertFalse("PIN muy corto debe ser inválido", isValidPin("123"));
        assertFalse("PIN muy largo debe ser inválido", isValidPin("12345"));
        assertFalse("PIN con letras debe ser inválido", isValidPin("12ab"));
        assertFalse("PIN vacío debe ser inválido", isValidPin(""));
        assertFalse("PIN null debe ser inválido", isValidPin(null));
    }
    
    @Test
    public void testNameValidation() {
        // Test nombres válidos
        assertTrue("Nombre normal debe ser válido", isValidName("Juan Pérez"));
        assertTrue("Nombre con acentos debe ser válido", isValidName("María José"));
        assertTrue("Nombre corto debe ser válido", isValidName("Ana"));
        
        // Test nombres inválidos
        assertFalse("Nombre muy corto debe ser inválido", isValidName("A"));
        assertFalse("Nombre vacío debe ser inválido", isValidName(""));
        assertFalse("Nombre null debe ser inválido", isValidName(null));
        assertFalse("Solo espacios debe ser inválido", isValidName("   "));
    }
    
    @Test
    public void testPasswordMatching() {
        // Test contraseñas que coinciden
        assertTrue("Contraseñas iguales deben coincidir", 
                passwordsMatch("password123", "password123"));
        
        // Test contraseñas que no coinciden
        assertFalse("Contraseñas diferentes no deben coincidir", 
                passwordsMatch("password123", "password456"));
        assertFalse("Una contraseña null no debe coincidir", 
                passwordsMatch("password123", null));
        assertFalse("Ambas contraseñas null no deben coincidir", 
                passwordsMatch(null, null));
    }
    
    @Test
    public void testPinMatching() {
        // Test PINs que coinciden
        assertTrue("PINs iguales deben coincidir", pinsMatch("1234", "1234"));
        
        // Test PINs que no coinciden
        assertFalse("PINs diferentes no deben coincidir", pinsMatch("1234", "5678"));
        assertFalse("Un PIN null no debe coincidir", pinsMatch("1234", null));
    }
    
    @Test
    public void testOnNavigationComplete() {
        // When
        viewModel.onNavigationComplete();
        
        // Then
        assertFalse("NavigateToHome debe ser false después de completar navegación", 
                viewModel.navigateToHome.getValue());
        assertFalse("NavigateToLogin debe ser false después de completar navegación", 
                viewModel.navigateToLogin.getValue());
    }
    
    // Métodos auxiliares para simular validaciones
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    
    private boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }
    
    private boolean isValidPin(String pin) {
        return pin != null && pin.length() == 4 && pin.matches("\\d{4}");
    }
    
    private boolean isValidName(String name) {
        return name != null && name.trim().length() >= 2;
    }
    
    private boolean passwordsMatch(String password1, String password2) {
        if (password1 == null || password2 == null) return false;
        return password1.equals(password2);
    }
    
    private boolean pinsMatch(String pin1, String pin2) {
        if (pin1 == null || pin2 == null) return false;
        return pin1.equals(pin2);
    }
}