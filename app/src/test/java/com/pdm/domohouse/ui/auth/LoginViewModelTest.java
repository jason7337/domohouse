package com.pdm.domohouse.ui.auth;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests unitarios básicos para LoginViewModel
 */
public class LoginViewModelTest {

    private LoginViewModel viewModel;

    @Before
    public void setUp() {
        viewModel = new LoginViewModel();
    }

    @Test
    public void testViewModelCreation() {
        // Verificar que el ViewModel se crea correctamente
        assertNotNull(viewModel);
        assertNotNull(viewModel.email);
        assertNotNull(viewModel.password);
    }

    @Test
    public void testEmailFieldInitialization() {
        // Verificar que el campo email se inicializa vacío
        assertEquals("", viewModel.email.getValue());
    }

    @Test
    public void testPasswordFieldInitialization() {
        // Verificar que el campo password se inicializa vacío
        assertEquals("", viewModel.password.getValue());
    }

    @Test
    public void testEmailSetter() {
        // Probar setter del email
        String testEmail = "test@example.com";
        viewModel.email.setValue(testEmail);
        assertEquals(testEmail, viewModel.email.getValue());
    }

    @Test
    public void testPasswordSetter() {
        // Probar setter del password
        String testPassword = "password123";
        viewModel.password.setValue(testPassword);
        assertEquals(testPassword, viewModel.password.getValue());
    }
}