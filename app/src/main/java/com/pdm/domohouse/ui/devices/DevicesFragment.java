package com.pdm.domohouse.ui.devices;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.pdm.domohouse.R;

/**
 * Fragmento para gestionar dispositivos del hogar
 * Sistema de control de 3 habitaciones con Arduino
 */
public class DevicesFragment extends Fragment {
    
    private DevicesViewModel viewModel;
    
    // Vistas del ESP32 y estadísticas
    private TextView esp32StatusText;
    private View esp32StatusIndicator;
    private TextView totalDevicesCount;
    private TextView activeDevicesCount;
    private TextView offlineDevicesCount;
    
    // Tarjetas de habitaciones
    private MaterialCardView room1Card;
    private MaterialCardView room2Card;
    private MaterialCardView room3Card;
    
    // Datos de habitación 1
    private TextView room1Temperature;
    private TextView room1DevicesCount;
    private TextView room1ActiveCount;
    private TextView room1Status;
    
    // Datos de habitación 2
    private TextView room2Temperature;
    private TextView room2DevicesCount;
    private TextView room2ActiveCount;
    private TextView room2Status;
    
    // Datos de habitación 3
    private TextView room3Temperature;
    private TextView room3DevicesCount;
    private TextView room3ActiveCount;
    private TextView room3Status;
    
    // Botones de acción
    private MaterialButton generalControlButton;
    private MaterialButton scheduleButton;
    private ExtendedFloatingActionButton addDeviceFab;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_devices, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Inicializar ViewModel
        viewModel = new ViewModelProvider(this).get(DevicesViewModel.class);
        
        // Inicializar vistas
        initializeViews(view);
        
        // Configurar observadores
        setupObservers();
        
        // Configurar listeners
        setupClickListeners();
    }
    
    /**
     * Inicializa todas las vistas del layout
     */
    private void initializeViews(View view) {
        // Header ESP32
        esp32StatusText = view.findViewById(R.id.esp32StatusText);
        esp32StatusIndicator = view.findViewById(R.id.esp32StatusIndicator);
        
        // Estadísticas de dispositivos
        totalDevicesCount = view.findViewById(R.id.totalDevicesCount);
        activeDevicesCount = view.findViewById(R.id.activeDevicesCount);
        offlineDevicesCount = view.findViewById(R.id.offlineDevicesCount);
        
        // Tarjetas de habitaciones
        room1Card = view.findViewById(R.id.room1Card);
        room2Card = view.findViewById(R.id.room2Card);
        room3Card = view.findViewById(R.id.room3Card);
        
        // Habitación 1
        room1Temperature = view.findViewById(R.id.room1Temperature);
        room1DevicesCount = view.findViewById(R.id.room1DevicesCount);
        room1ActiveCount = view.findViewById(R.id.room1ActiveCount);
        room1Status = view.findViewById(R.id.room1Status);
        
        // Habitación 2
        room2Temperature = view.findViewById(R.id.room2Temperature);
        room2DevicesCount = view.findViewById(R.id.room2DevicesCount);
        room2ActiveCount = view.findViewById(R.id.room2ActiveCount);
        room2Status = view.findViewById(R.id.room2Status);
        
        // Habitación 3
        room3Temperature = view.findViewById(R.id.room3Temperature);
        room3DevicesCount = view.findViewById(R.id.room3DevicesCount);
        room3ActiveCount = view.findViewById(R.id.room3ActiveCount);
        room3Status = view.findViewById(R.id.room3Status);
        
        // Botones de acción
        generalControlButton = view.findViewById(R.id.generalControlButton);
        scheduleButton = view.findViewById(R.id.scheduleButton);
        addDeviceFab = view.findViewById(R.id.addDeviceFab);
    }
    
    /**
     * Configura los observadores del ViewModel
     */
    private void setupObservers() {
        // Estado ESP32
        viewModel.getESP32Status().observe(getViewLifecycleOwner(), status -> {
            if (esp32StatusText != null) {
                esp32StatusText.setText(status);
            }
        });
        
        viewModel.getESP32Connected().observe(getViewLifecycleOwner(), connected -> {
            if (esp32StatusIndicator != null) {
                int color = connected ? 
                    getResources().getColor(R.color.success, null) : 
                    getResources().getColor(R.color.error, null);
                esp32StatusIndicator.getBackground().setTint(color);
            }
        });
        
        // Estadísticas de dispositivos
        viewModel.getTotalDevicesCount().observe(getViewLifecycleOwner(), count -> {
            if (totalDevicesCount != null) {
                totalDevicesCount.setText(String.valueOf(count));
            }
        });
        
        viewModel.getActiveDevicesCount().observe(getViewLifecycleOwner(), count -> {
            if (activeDevicesCount != null) {
                activeDevicesCount.setText(String.valueOf(count));
            }
        });
        
        viewModel.getOfflineDevicesCount().observe(getViewLifecycleOwner(), count -> {
            if (offlineDevicesCount != null) {
                offlineDevicesCount.setText(String.valueOf(count));
            }
        });
        
        // Datos de habitaciones
        viewModel.getRoom1Data().observe(getViewLifecycleOwner(), roomData -> {
            if (roomData != null) {
                updateRoom1UI(roomData);
            }
        });
        
        viewModel.getRoom2Data().observe(getViewLifecycleOwner(), roomData -> {
            if (roomData != null) {
                updateRoom2UI(roomData);
            }
        });
        
        viewModel.getRoom3Data().observe(getViewLifecycleOwner(), roomData -> {
            if (roomData != null) {
                updateRoom3UI(roomData);
            }
        });
        
        // Estado de carga y errores
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // Mostrar/ocultar indicador de carga si es necesario
        });
        
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Configura los listeners de click
     */
    private void setupClickListeners() {
        // Tarjetas de habitaciones
        if (room1Card != null) {
            room1Card.setOnClickListener(v -> 
                viewModel.navigateToRoomDetails("room_arduino_1"));
        }
        
        if (room2Card != null) {
            room2Card.setOnClickListener(v -> 
                viewModel.navigateToRoomDetails("room_arduino_2"));
        }
        
        if (room3Card != null) {
            room3Card.setOnClickListener(v -> 
                viewModel.navigateToRoomDetails("room_arduino_3"));
        }
        
        // Botones de acción
        if (generalControlButton != null) {
            generalControlButton.setOnClickListener(v -> {
                viewModel.openGeneralControl();
                Toast.makeText(getContext(), "Abriendo Control General...", Toast.LENGTH_SHORT).show();
            });
        }
        
        if (scheduleButton != null) {
            scheduleButton.setOnClickListener(v -> {
                viewModel.openScheduleSystem();
                Toast.makeText(getContext(), "Abriendo Sistema de Programación...", Toast.LENGTH_SHORT).show();
            });
        }
        
        if (addDeviceFab != null) {
            addDeviceFab.setOnClickListener(v -> {
                viewModel.addNewDevice();
                Toast.makeText(getContext(), "Función agregar dispositivo próximamente...", Toast.LENGTH_SHORT).show();
            });
        }
    }
    
    /**
     * Actualiza la UI de la habitación 1
     */
    private void updateRoom1UI(DevicesViewModel.RoomData roomData) {
        if (room1Temperature != null) {
            room1Temperature.setText(String.format("%.0f°C", roomData.temperature));
        }
        if (room1DevicesCount != null) {
            room1DevicesCount.setText(String.valueOf(roomData.deviceCount));
        }
        if (room1ActiveCount != null) {
            room1ActiveCount.setText(String.valueOf(roomData.activeCount));
        }
        if (room1Status != null) {
            room1Status.setText(roomData.status);
        }
    }
    
    /**
     * Actualiza la UI de la habitación 2
     */
    private void updateRoom2UI(DevicesViewModel.RoomData roomData) {
        if (room2Temperature != null) {
            room2Temperature.setText(String.format("%.0f°C", roomData.temperature));
        }
        if (room2DevicesCount != null) {
            room2DevicesCount.setText(String.valueOf(roomData.deviceCount));
        }
        if (room2ActiveCount != null) {
            room2ActiveCount.setText(String.valueOf(roomData.activeCount));
        }
        if (room2Status != null) {
            room2Status.setText(roomData.status);
        }
    }
    
    /**
     * Actualiza la UI de la habitación 3
     */
    private void updateRoom3UI(DevicesViewModel.RoomData roomData) {
        if (room3Temperature != null) {
            room3Temperature.setText(String.format("%.0f°C", roomData.temperature));
        }
        if (room3DevicesCount != null) {
            room3DevicesCount.setText(String.valueOf(roomData.deviceCount));
        }
        if (room3ActiveCount != null) {
            room3ActiveCount.setText(String.valueOf(roomData.activeCount));
        }
        if (room3Status != null) {
            room3Status.setText(roomData.status);
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Refrescar datos cuando el fragmento vuelve a ser visible
        if (viewModel != null) {
            viewModel.refreshData();
        }
    }
}