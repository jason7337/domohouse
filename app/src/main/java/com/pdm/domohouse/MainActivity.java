package com.pdm.domohouse;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Actividad principal de la aplicación
 * Actúa como contenedor para el Navigation Component y maneja la navegación principal
 */
public class MainActivity extends AppCompatActivity {
    
    // Controlador de navegación
    private NavController navController;
    
    // Configuración de la barra de aplicación
    private AppBarConfiguration appBarConfiguration;
    
    // BottomNavigationView para navegación principal
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Inicializar Bottom Navigation primero
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        
        // Configurar Navigation Component
        setupNavigation();
        
        // Configurar Bottom Navigation
        setupBottomNavigation();
    }
    
    /**
     * Configura el Navigation Component
     */
    private void setupNavigation() {
        // Obtener el NavHostFragment del FragmentManager
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        
        if (navHostFragment != null) {
            // Obtener el NavController del NavHostFragment
            navController = navHostFragment.getNavController();
            
            // Configurar la barra de aplicación con el NavController
            // Los fragmentos de nivel superior no mostrarán el botón de retroceso
            appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.splashFragment,
                    R.id.loginFragment,
                    R.id.homeFragment,
                    R.id.profileFragment,
                    R.id.devicesFragment,
                    R.id.settingsFragment
            ).build();
            
            // Listener para mostrar/ocultar bottom navigation según el destino
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                if (bottomNavigationView != null) {
                    // Mostrar bottom navigation solo en fragmentos principales
                    boolean showBottomNav = destination.getId() == R.id.homeFragment ||
                                          destination.getId() == R.id.profileFragment ||
                                          destination.getId() == R.id.devicesFragment ||
                                          destination.getId() == R.id.settingsFragment;
                    
                    bottomNavigationView.setVisibility(showBottomNav ? 
                        BottomNavigationView.VISIBLE : BottomNavigationView.GONE);
                }
            });
        }
    }
    
    /**
     * Configura el BottomNavigationView
     */
    private void setupBottomNavigation() {
        if (bottomNavigationView != null && navController != null) {
            // Conectar el BottomNavigationView con el NavController
            NavigationUI.setupWithNavController(bottomNavigationView, navController);
        }
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        // Manejar el botón de navegación hacia arriba
        if (navController != null) {
            return NavigationUI.navigateUp(navController, appBarConfiguration)
                    || super.onSupportNavigateUp();
        }
        return super.onSupportNavigateUp();
    }
}