package com.pdm.domohouse;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

/**
 * Actividad principal de la aplicación
 * Actúa como contenedor para el Navigation Component
 */
public class MainActivity extends AppCompatActivity {
    
    // Controlador de navegación
    private NavController navController;
    
    // Configuración de la barra de aplicación
    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Configurar Navigation Component
        setupNavigation();
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
                    R.id.homeFragment
            ).build();
            
            // Por ahora, no configurar ActionBar ya que las pantallas son full screen
            // TODO: Configurar ActionBar cuando sea necesario en futuras sesiones
            // NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
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