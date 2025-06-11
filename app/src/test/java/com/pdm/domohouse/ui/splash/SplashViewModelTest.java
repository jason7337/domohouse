package com.pdm.domohouse.ui.splash;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * Tests unitarios básicos para SplashViewModel
 */
public class SplashViewModelTest {

    private SplashViewModel viewModel;

    @Before
    public void setUp() {
        viewModel = new SplashViewModel();
    }

    @Test
    public void testViewModelCreation() {
        // Verificar que el ViewModel se crea correctamente
        assertNotNull(viewModel);
        assertNotNull(viewModel.navigationEvent);
    }

    @Test
    public void testNavigationDestinationEnum() {
        // Verificar que el enum NavigationDestination tiene los valores esperados
        SplashViewModel.NavigationDestination[] destinations = SplashViewModel.NavigationDestination.values();
        assertNotNull(destinations);
        
        // Verificar que existe LOGIN
        boolean loginFound = false;
        boolean homeFound = false;
        
        for (SplashViewModel.NavigationDestination dest : destinations) {
            if (dest == SplashViewModel.NavigationDestination.LOGIN) {
                loginFound = true;
            }
            if (dest == SplashViewModel.NavigationDestination.HOME) {
                homeFound = true;
            }
        }
        
        assert(loginFound && homeFound);
    }
}