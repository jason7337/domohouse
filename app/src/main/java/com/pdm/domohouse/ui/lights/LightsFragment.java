package com.pdm.domohouse.ui.lights;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.pdm.domohouse.R;
import com.pdm.domohouse.databinding.FragmentLightsBinding;
import com.pdm.domohouse.ui.lights.adapter.RoomLightsAdapter;

/**
 * Fragmento para el control de iluminación
 * Permite controlar luces por habitación con switches e intensidad
 */
public class LightsFragment extends Fragment {

    private FragmentLightsBinding binding;
    private LightsViewModel viewModel;
    private RoomLightsAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Configurar data binding
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_lights, container, false);
        binding.setLifecycleOwner(this);
        
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Inicializar ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(LightsViewModel.class);
        binding.setViewModel(viewModel);
        
        // Configurar RecyclerView
        setupRecyclerView();
        
        // Configurar observadores
        setupObservers();
        
        // Configurar listeners
        setupListeners();
        
        // Cargar datos iniciales
        viewModel.loadRoomsWithLights();
    }

    /**
     * Configura el RecyclerView para mostrar habitaciones con luces
     */
    private void setupRecyclerView() {
        adapter = new RoomLightsAdapter();
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        
        binding.recyclerViewRooms.setLayoutManager(layoutManager);
        binding.recyclerViewRooms.setAdapter(adapter);
        binding.recyclerViewRooms.setHasFixedSize(true);
        
        // Configurar listener para interacciones con los dispositivos
        adapter.setOnRoomLightClickListener(new RoomLightsAdapter.OnRoomLightClickListener() {
            @Override
            public void onRoomToggle(String roomId, boolean turnOn) {
                if (turnOn) {
                    viewModel.turnOnRoomLights(roomId);
                } else {
                    viewModel.turnOffRoomLights(roomId);
                }
            }
            
            @Override
            public void onDeviceToggle(String deviceId) {
                viewModel.toggleLight(deviceId);
            }
            
            @Override
            public void onDeviceIntensityChange(String deviceId, float intensity) {
                viewModel.setLightIntensity(deviceId, intensity);
            }
        });
    }

    /**
     * Configura los observadores del ViewModel
     */
    private void setupObservers() {
        // Observar lista de habitaciones con luces
        viewModel.getRoomsWithLights().observe(getViewLifecycleOwner(), roomsWithLights -> {
            if (roomsWithLights != null) {
                adapter.submitList(roomsWithLights);
            }
        });

        // Observar estado de carga
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (binding != null) {
                binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });

        // Observar errores
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                // TODO: Mostrar mensaje de error (SnackBar o Toast)
            }
        });

        // Observar estadísticas de luces
        viewModel.getLightsStats().observe(getViewLifecycleOwner(), stats -> {
            if (stats != null && binding != null) {
                binding.textTotalLights.setText(String.valueOf(stats.getTotalLights()));
                binding.textLightsOn.setText(String.valueOf(stats.getLightsOn()));
                binding.textLightsOff.setText(String.valueOf(stats.getLightsOff()));
            }
        });
    }

    /**
     * Configura los listeners de la interfaz
     */
    private void setupListeners() {
        // Botón para apagar todas las luces
        binding.buttonAllOff.setOnClickListener(v -> {
            viewModel.turnOffAllLights();
        });

        // Botón para encender todas las luces
        binding.buttonAllOn.setOnClickListener(v -> {
            viewModel.turnOnAllLights();
        });

        // Pull to refresh
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            viewModel.refreshLights();
            binding.swipeRefreshLayout.setRefreshing(false);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}