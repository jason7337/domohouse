package com.pdm.domohouse.ui.temperature;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.slider.Slider;

import com.pdm.domohouse.R;
import com.pdm.domohouse.databinding.FragmentTemperatureBinding;
import com.pdm.domohouse.ui.temperature.adapter.RoomTemperatureAdapter;
import com.pdm.domohouse.ui.temperature.adapter.FanControlAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Fragment para el control y monitoreo de temperatura
 * Muestra dashboard completo con gráficos, controles de ventiladores y configuración de umbrales
 */
public class TemperatureFragment extends Fragment implements FanControlListener {
    
    private static final String TAG = "TemperatureFragment";
    
    private FragmentTemperatureBinding binding;
    private TemperatureViewModel viewModel;
    
    // Adaptadores para RecyclerViews
    private RoomTemperatureAdapter roomTemperatureAdapter;
    private FanControlAdapter fanControlAdapter;
    
    // Referencias a componentes UI
    private LineChart temperatureChart;
    private MaterialButtonToggleGroup periodToggleGroup;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        binding = FragmentTemperatureBinding.inflate(inflater, container, false);
        binding.setLifecycleOwner(this);
        
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupViewModel();
        setupUI();
        setupChart();
        setupRecyclerViews();
        setupObservers();
        setupClickListeners();
    }
    
    /**
     * Configura el ViewModel y conecta con el Data Binding
     */
    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(TemperatureViewModel.class);
        binding.setViewModel(viewModel);
    }
    
    /**
     * Configura los componentes de la UI
     */
    private void setupUI() {
        // Configurar toolbar
        binding.toolbar.setNavigationOnClickListener(v -> 
            Navigation.findNavController(v).navigateUp());
        
        // Referencias a componentes
        temperatureChart = binding.chartTemperature;
        periodToggleGroup = binding.togglePeriod;
        
        // Configurar período inicial del gráfico
        binding.btn24h.setChecked(true);
    }
    
    /**
     * Configura el gráfico de temperatura histórica usando MPAndroidChart
     */
    private void setupChart() {
        // Configuración general del gráfico
        temperatureChart.setTouchEnabled(true);
        temperatureChart.setDragEnabled(true);
        temperatureChart.setScaleEnabled(true);
        temperatureChart.setPinchZoom(true);
        temperatureChart.setDrawGridBackground(false);
        temperatureChart.getAxisRight().setEnabled(false);
        
        // Configurar descripción
        Description description = new Description();
        description.setText("Temperatura a lo largo del tiempo");
        description.setTextColor(getResources().getColor(R.color.text_secondary, null));
        description.setTextSize(12f);
        temperatureChart.setDescription(description);
        
        // Configurar eje X (tiempo)
        XAxis xAxis = temperatureChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(getResources().getColor(R.color.text_secondary, null));
        xAxis.setValueFormatter(new ValueFormatter() {
            private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            
            @Override
            public String getFormattedValue(float value) {
                return dateFormat.format(new Date((long) value));
            }
        });
        
        // Configurar eje Y (temperatura)
        YAxis leftAxis = temperatureChart.getAxisLeft();
        leftAxis.setTextColor(getResources().getColor(R.color.text_secondary, null));
        leftAxis.setAxisMinimum(15f);
        leftAxis.setAxisMaximum(35f);
        leftAxis.setGranularity(1f);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format(Locale.getDefault(), "%.1f°C", value);
            }
        });
        
        // Cargar datos iniciales del gráfico
        loadChartData(TemperatureViewModel.ChartPeriod.DAY_24H);
    }
    
    /**
     * Configura los RecyclerViews para temperaturas y controles de ventiladores
     */
    private void setupRecyclerViews() {
        // RecyclerView para temperaturas por habitación
        roomTemperatureAdapter = new RoomTemperatureAdapter();
        binding.rvRoomTemperatures.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvRoomTemperatures.setAdapter(roomTemperatureAdapter);
        
        // RecyclerView para controles de ventiladores
        fanControlAdapter = new FanControlAdapter(this);
        binding.rvFanControls.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvFanControls.setAdapter(fanControlAdapter);
    }
    
    /**
     * Configura los observadores del ViewModel
     */
    private void setupObservers() {
        // Observar temperaturas por habitación
        viewModel.getRoomTemperatures().observe(getViewLifecycleOwner(), temperatures -> {
            if (temperatures != null) {
                roomTemperatureAdapter.submitList(temperatures);
            }
        });
        
        // Observar controles de ventiladores
        viewModel.getFanControls().observe(getViewLifecycleOwner(), fanControls -> {
            if (fanControls != null) {
                fanControlAdapter.submitList(fanControls);
            }
        });
        
        // Observar mensajes de error
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                viewModel.clearErrorMessage();
            }
        });
        
        // Observar mensajes de éxito
        viewModel.getSuccessMessage().observe(getViewLifecycleOwner(), successMessage -> {
            if (successMessage != null && !successMessage.isEmpty()) {
                Toast.makeText(getContext(), successMessage, Toast.LENGTH_SHORT).show();
            }
        });
        
        // Observar cambios en el período del gráfico
        viewModel.getSelectedPeriod().observe(getViewLifecycleOwner(), period -> {
            if (period != null) {
                loadChartData(period);
            }
        });
    }
    
    /**
     * Configura los listeners de clicks
     */
    private void setupClickListeners() {
        // Toggle de período del gráfico
        periodToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                TemperatureViewModel.ChartPeriod period;
                if (checkedId == R.id.btn_24h) {
                    period = TemperatureViewModel.ChartPeriod.DAY_24H;
                } else if (checkedId == R.id.btn_7d) {
                    period = TemperatureViewModel.ChartPeriod.WEEK_7D;
                } else {
                    period = TemperatureViewModel.ChartPeriod.MONTH_30D;
                }
                viewModel.setChartPeriod(period);
            }
        });
        
        // Botón para guardar umbrales
        binding.btnSaveThresholds.setOnClickListener(v -> saveThresholds());
        
        // FAB para configuraciones adicionales
        binding.fabTemperatureSettings.setOnClickListener(v -> {
            // Aquí se podría abrir un dialog de configuraciones adicionales
            Toast.makeText(getContext(), "Configuraciones adicionales próximamente", Toast.LENGTH_SHORT).show();
        });
        
        // Listener para el switch de control automático
        binding.switchAutoControl.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Toast.makeText(getContext(), "Control automático activado", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Carga datos del gráfico según el período seleccionado
     */
    private void loadChartData(TemperatureViewModel.ChartPeriod period) {
        List<Entry> entries = new ArrayList<>();
        Random random = new Random();
        
        // Simular datos históricos de temperatura
        long currentTime = System.currentTimeMillis();
        int dataPoints = period == TemperatureViewModel.ChartPeriod.DAY_24H ? 24 : 
                        period == TemperatureViewModel.ChartPeriod.WEEK_7D ? 168 : 720;
        
        long timeInterval = period.getHours() * 60 * 60 * 1000L / dataPoints; // Intervalo en milisegundos
        
        for (int i = 0; i < dataPoints; i++) {
            long time = currentTime - (dataPoints - i) * timeInterval;
            float temperature = 22f + random.nextFloat() * 8f - 4f; // 18-26°C con variación
            entries.add(new Entry(time, temperature));
        }
        
        // Crear dataset
        LineDataSet dataSet = new LineDataSet(entries, "Temperatura Promedio");
        dataSet.setColor(getResources().getColor(R.color.primary, null));
        dataSet.setCircleColor(getResources().getColor(R.color.primary, null));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(3f);
        dataSet.setDrawCircleHole(false);
        dataSet.setValueTextSize(9f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(getResources().getColor(R.color.primary_light, null));
        dataSet.setFillAlpha(50);
        
        // Configurar formato de valores
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format(Locale.getDefault(), "%.1f°", value);
            }
        });
        
        // Aplicar datos al gráfico
        LineData lineData = new LineData(dataSet);
        temperatureChart.setData(lineData);
        temperatureChart.invalidate(); // Refrescar gráfico
    }
    
    /**
     * Guarda la configuración de umbrales de temperatura
     */
    private void saveThresholds() {
        try {
            String minTempStr = binding.etMinThreshold.getText().toString().trim();
            String maxTempStr = binding.etMaxThreshold.getText().toString().trim();
            
            if (minTempStr.isEmpty() || maxTempStr.isEmpty()) {
                Toast.makeText(getContext(), "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }
            
            float minTemp = Float.parseFloat(minTempStr);
            float maxTemp = Float.parseFloat(maxTempStr);
            boolean autoControl = binding.switchAutoControl.isChecked();
            
            viewModel.saveTemperatureThresholds(minTemp, maxTemp, autoControl);
            
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Por favor ingresa valores numéricos válidos", Toast.LENGTH_SHORT).show();
        }
    }
    
    // Implementación de FanControlListener
    
    @Override
    public void onFanPowerChanged(String deviceId, boolean isOn) {
        viewModel.toggleFanPower(deviceId, isOn);
    }
    
    @Override
    public void onFanSpeedChanged(String deviceId, float speedPercentage) {
        viewModel.setFanSpeed(deviceId, speedPercentage);
    }
    
    @Override
    public void onFanClicked(String deviceId) {
        Toast.makeText(getContext(), "Detalles del ventilador: " + deviceId, Toast.LENGTH_SHORT).show();
        // Aquí se podría navegar a una pantalla de detalles del ventilador
    }
    
    @Override
    public void onFanConfigClicked(String deviceId) {
        Toast.makeText(getContext(), "Configurar ventilador: " + deviceId, Toast.LENGTH_SHORT).show();
        // Aquí se podría abrir un dialog de configuración del ventilador
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}