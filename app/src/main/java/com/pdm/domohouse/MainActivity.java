package com.pdm.domohouse;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.imageview.ShapeableImageView;

/**
 * Actividad principal de la aplicación
 * Actúa como contenedor para el Navigation Component y maneja la navegación principal
 */
public class MainActivity extends AppCompatActivity {
    
    // Controlador de navegación
    private NavController navController;
    
    // BottomNavigationView para navegación principal
    private BottomNavigationView bottomNavigationView;
    
    // Elementos del header
    private View appHeader;
    private ImageView appLogo;
    private ShapeableImageView profileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Inicializar componentes del header
        initializeHeaderComponents();
        
        // Inicializar Bottom Navigation
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        
        // Configurar Navigation Component en el próximo ciclo de UI para asegurar que el FragmentContainerView esté listo
        findViewById(R.id.nav_host_fragment).post(() -> {
            setupNavigation();
            setupBottomNavigation();
        });
    }
    
    /**
     * Inicializa los componentes del header
     */
    private void initializeHeaderComponents() {
        appHeader = findViewById(R.id.app_header);
        appLogo = findViewById(R.id.appLogo);
        profileImage = findViewById(R.id.profileImage);
        
        // Configurar click listener para la imagen de perfil
        if (profileImage != null) {
            profileImage.setOnClickListener(v -> {
                // Navegar al fragmento de perfil cuando se toque la imagen
                if (navController != null) {
                    navController.navigate(R.id.profileFragment);
                }
            });
        }
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
            
            // Listener para mostrar/ocultar bottom navigation y header según el destino
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                if (bottomNavigationView != null && appHeader != null) {
                    // Mostrar bottom navigation y header solo en fragmentos principales
                    boolean showMainUI = destination.getId() == R.id.homeFragment ||
                                       destination.getId() == R.id.profileFragment ||
                                       destination.getId() == R.id.devicesFragment ||
                                       destination.getId() == R.id.settingsFragment;
                    
                    bottomNavigationView.setVisibility(showMainUI ? 
                        BottomNavigationView.VISIBLE : BottomNavigationView.GONE);
                    
                    appHeader.setVisibility(showMainUI ? 
                        View.VISIBLE : View.GONE);
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
            return navController.navigateUp() || super.onSupportNavigateUp();
        }
        return super.onSupportNavigateUp();
    }
}