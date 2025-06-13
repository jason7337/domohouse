package com.pdm.domohouse.ui.home;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.pdm.domohouse.R;
import com.pdm.domohouse.data.model.Room;
import com.pdm.domohouse.data.model.RoomStatus;
import com.pdm.domohouse.databinding.FragmentHomeBinding;

/**
 * Fragmento Home - Dashboard principal con vista de maqueta de casa
 * Muestra estadísticas generales, maqueta interactiva y detalles de habitaciones
 */
public class HomeFragment extends Fragment implements HouseMapView.OnRoomClickListener {

    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Configurar Data Binding
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false);
        binding.setLifecycleOwner(this);
        
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Inicializar ViewModel
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        binding.setViewModel(viewModel);
        
        // Configurar vista de maqueta
        setupHouseMapView();
        
        // Configurar observadores
        setupObservers();
        
        // Configurar listeners
        setupListeners();
    }

    /**
     * Configura la vista de maqueta de casa
     */
    private void setupHouseMapView() {
        binding.houseMapView.setOnRoomClickListener(this);
    }

    /**
     * Configura los observadores de LiveData
     */
    private void setupObservers() {
        // Observar habitaciones
        viewModel.rooms.observe(getViewLifecycleOwner(), rooms -> {
            if (rooms != null) {
                binding.houseMapView.setRooms(rooms);
            }
        });

        // Observar estadísticas del dashboard
        viewModel.dashboardStats.observe(getViewLifecycleOwner(), stats -> {
            if (stats != null) {
                updateDashboardStats(stats);
            }
        });

        // Observar habitación seleccionada
        viewModel.selectedRoom.observe(getViewLifecycleOwner(), room -> {
            if (room != null) {
                showRoomDetails(room);
                binding.houseMapView.selectRoom(room);
            } else {
                hideRoomDetails();
            }
        });

        // Observar estado de carga
        viewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            binding.loadingIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.refreshButton.setEnabled(!isLoading);
        });

        // Observar mensajes de error
        viewModel.error.observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Configura los listeners de eventos
     */
    private void setupListeners() {
        // Botón de actualizar datos
        binding.refreshButton.setOnClickListener(v -> {
            // Animar botón
            ObjectAnimator rotation = ObjectAnimator.ofFloat(v, "rotation", 0f, 360f);
            rotation.setDuration(500);
            rotation.start();
            
            // Actualizar datos
            viewModel.refreshSensorData();
            
            Toast.makeText(getContext(), getString(R.string.refresh_data), Toast.LENGTH_SHORT).show();
        });
        
        // Botón de menú contextual
        binding.menuButton.setOnClickListener(v -> showPopupMenu(v));
        
        // Click en estadística de temperatura para navegar a pantalla de temperatura
        binding.avgTemperatureValue.setOnClickListener(v -> {
            Navigation.findNavController(binding.getRoot())
                .navigate(R.id.action_home_to_temperature);
        });
        
        // También permitir navegación desde toda la tarjeta de estadísticas (click largo)
        binding.statsCard.setOnLongClickListener(v -> {
            // Mostrar opciones de navegación rápida
            showQuickNavigationMenu(v);
            return true;
        });
    }
    
    /**
     * Muestra el menú contextual
     */
    private void showPopupMenu(View anchor) {
        PopupMenu popup = new PopupMenu(getContext(), anchor);
        popup.getMenuInflater().inflate(R.menu.home_popup_menu, popup.getMenu());
        
        popup.setOnMenuItemClickListener(this::onMenuItemClick);
        popup.show();
    }
    
    /**
     * Maneja la selección de elementos del menú
     */
    private boolean onMenuItemClick(MenuItem item) {
        int itemId = item.getItemId();
        
        if (itemId == R.id.menu_profile) {
            // Navegar a perfil
            Navigation.findNavController(binding.getRoot())
                .navigate(R.id.action_home_to_profile);
            return true;
        } else if (itemId == R.id.menu_settings) {
            // Navegar a configuración
            // Navigation.findNavController(binding.getRoot())
            //     .navigate(R.id.action_home_to_settings); // TODO: Fix navigation
            return true;
        } else if (itemId == R.id.menu_logout) {
            // Mostrar confirmación de cierre de sesión
            showLogoutConfirmation();
            return true;
        }
        
        return false;
    }
    
    /**
     * Muestra confirmación para cerrar sesión
     */
    private void showLogoutConfirmation() {
        new AlertDialog.Builder(getContext())
            .setTitle("Cerrar Sesión")
            .setMessage("¿Estás seguro de que quieres cerrar sesión?")
            .setPositiveButton("Sí", (dialog, which) -> {
                // Realizar logout
                viewModel.logout();
                // Navegar a login
                Navigation.findNavController(binding.getRoot())
                    .navigate(R.id.action_home_to_login);
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }
    
    /**
     * Muestra el menú de navegación rápida
     */
    private void showQuickNavigationMenu(View anchor) {
        PopupMenu popup = new PopupMenu(getContext(), anchor);
        
        // Agregar opciones de navegación manualmente
        popup.getMenu().add(0, 1, 0, "🌡️ Control de Temperatura");
        popup.getMenu().add(0, 2, 0, "💡 Control de Luces");
        popup.getMenu().add(0, 3, 0, "🔒 Seguridad");
        popup.getMenu().add(0, 4, 0, "📱 Gestión de Dispositivos");
        
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1: // Temperatura
                    Navigation.findNavController(binding.getRoot())
                        .navigate(R.id.action_home_to_temperature);
                    return true;
                case 2: // Luces
                    Navigation.findNavController(binding.getRoot())
                        .navigate(R.id.action_home_to_lights);
                    return true;
                case 3: // Seguridad
                    Navigation.findNavController(binding.getRoot())
                        .navigate(R.id.action_home_to_security);
                    return true;
                case 4: // Dispositivos
                    Navigation.findNavController(binding.getRoot())
                        .navigate(R.id.action_home_to_devices);
                    return true;
                default:
                    return false;
            }
        });
        
        popup.show();
    }

    /**
     * Actualiza las estadísticas del dashboard
     */
    private void updateDashboardStats(HomeViewModel.DashboardStats stats) {
        // Habitaciones activas
        binding.activeRoomsValue.setText(String.valueOf(stats.activeRooms));
        
        // Dispositivos conectados
        String devicesText = stats.connectedDevices + "/" + stats.totalDevices;
        binding.connectedDevicesValue.setText(devicesText);
        
        // Temperatura promedio
        String tempText = getString(R.string.temperature_format, stats.avgTemperature);
        binding.avgTemperatureValue.setText(tempText);
        
        // Alertas
        binding.alertsValue.setText(String.valueOf(stats.alertsCount));
        
        // Cambiar color de alertas si hay problemas
        if (stats.hasAlerts()) {
            binding.alertsValue.setTextColor(getResources().getColor(R.color.error, null));
        } else {
            binding.alertsValue.setTextColor(getResources().getColor(R.color.success, null));
        }
    }

    /**
     * Muestra los detalles de una habitación seleccionada
     */
    private void showRoomDetails(Room room) {
        // Configurar información de la habitación
        binding.roomIcon.setText(room.getType().getIcon());
        binding.roomName.setText(room.getName());
        
        // Configurar estado de la habitación
        RoomStatus status = room.getOverallStatus();
        binding.roomStatus.setText(status.getDisplayName());
        binding.roomStatus.setBackgroundTintList(
            getResources().getColorStateList(getStatusColor(status), null)
        );
        
        // Configurar datos de sensores
        String tempText = getString(R.string.temperature_format, room.getTemperature());
        binding.roomTemperature.setText(tempText);
        
        String humidityText = String.format("%.0f%%", room.getHumidity());
        binding.roomHumidity.setText(humidityText);
        
        binding.roomDevices.setText(String.valueOf(room.getActiveDevices()));
        
        // Mostrar tarjeta con animación
        if (binding.roomDetailsCard.getVisibility() == View.GONE) {
            binding.roomDetailsCard.setVisibility(View.VISIBLE);
            binding.roomDetailsCard.setAlpha(0f);
            binding.roomDetailsCard.animate()
                .alpha(1f)
                .setDuration(300)
                .start();
        }
    }

    /**
     * Oculta los detalles de habitación
     */
    private void hideRoomDetails() {
        if (binding.roomDetailsCard.getVisibility() == View.VISIBLE) {
            binding.roomDetailsCard.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction(() -> binding.roomDetailsCard.setVisibility(View.GONE))
                .start();
        }
    }

    /**
     * Obtiene el color correspondiente a un estado de habitación
     */
    private int getStatusColor(RoomStatus status) {
        switch (status) {
            case ACTIVE:
                return R.color.primary;
            case WARNING:
                return R.color.warning;
            case ALERT:
                return R.color.error;
            case OFFLINE:
                return R.color.text_secondary;
            default:
                return R.color.success;
        }
    }

    /**
     * Callback cuando se hace click en una habitación de la maqueta
     */
    @Override
    public void onRoomClick(Room room) {
        viewModel.selectRoom(room);
        
        // Mostrar mensaje informativo
        String message = "Habitación: " + room.getName() + " - " + 
                        room.getOverallStatus().getDisplayName();
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}