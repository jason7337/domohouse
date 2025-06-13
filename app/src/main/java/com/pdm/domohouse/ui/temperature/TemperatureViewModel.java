package com.pdm.domohouse.ui.temperature;

import android.app.Application;
import com.pdm.domohouse.ui.base.BaseAndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.annotation.NonNull;

import com.pdm.domohouse.data.model.*;
import com.pdm.domohouse.data.repository.DeviceRepository;
import com.pdm.domohouse.data.repository.TemperatureRepository;
import com.pdm.domohouse.utils.SingleLiveEvent;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ViewModel para el control y monitoreo de temperatura
 * Maneja la lógica de negocio para sensores, ventiladores y umbrales automáticos
 */
public class TemperatureViewModel extends BaseAndroidViewModel {
    
    private static final String TAG = "TemperatureViewModel";
    
    // Repositorios
    private final TemperatureRepository temperatureRepository;
    private final DeviceRepository deviceRepository;
    
    // Executor para operaciones asíncronas
    private final ExecutorService executorService;
    
    // LiveData para UI
    private final MutableLiveData<List<RoomTemperature>> roomTemperatures = new MutableLiveData<>();
    private final MutableLiveData<List<FanControl>> fanControls = new MutableLiveData<>();
    private final MutableLiveData<TemperatureThreshold> temperatureThresholds = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final SingleLiveEvent<String> successMessage = new SingleLiveEvent<>();
    
    // Estadísticas calculadas
    private final MutableLiveData<Float> averageTemperature = new MutableLiveData<>(0f);
    private final MutableLiveData<Float> minTemperature = new MutableLiveData<>(0f);
    private final MutableLiveData<Float> maxTemperature = new MutableLiveData<>(0f);
    
    // Configuración de umbrales
    private final MutableLiveData<Float> minThreshold = new MutableLiveData<>(18f);
    private final MutableLiveData<Float> maxThreshold = new MutableLiveData<>(28f);
    private final MutableLiveData<Boolean> isAutomaticControlEnabled = new MutableLiveData<>(false);
    
    // Período de gráfico seleccionado
    private final MutableLiveData<ChartPeriod> selectedPeriod = new MutableLiveData<>(ChartPeriod.DAY_24H);
    
    public TemperatureViewModel(@NonNull Application application) {
        super(application);
        
        this.temperatureRepository = new TemperatureRepository();
        this.deviceRepository = new DeviceRepository(getApplication());
        this.executorService = Executors.newFixedThreadPool(3);
        
        // Inicializar datos
        loadTemperatureData();
        loadFanControls();
        loadTemperatureThresholds();
    }
    
    // Getters para LiveData
    public LiveData<List<RoomTemperature>> getRoomTemperatures() { return roomTemperatures; }
    public LiveData<List<FanControl>> getFanControls() { return fanControls; }
    public LiveData<TemperatureThreshold> getTemperatureThresholds() { return temperatureThresholds; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<String> getSuccessMessage() { return successMessage; }
    
    public LiveData<Float> getAverageTemperature() { return averageTemperature; }
    public LiveData<Float> getMinTemperature() { return minTemperature; }
    public LiveData<Float> getMaxTemperature() { return maxTemperature; }
    
    public LiveData<Float> getMinThreshold() { return minThreshold; }
    public LiveData<Float> getMaxThreshold() { return maxThreshold; }
    public LiveData<Boolean> getIsAutomaticControlEnabled() { return isAutomaticControlEnabled; }
    
    public LiveData<ChartPeriod> getSelectedPeriod() { return selectedPeriod; }
    
    /**
     * Carga los datos de temperatura de todas las habitaciones
     */
    public void loadTemperatureData() {
        isLoading.setValue(true);
        
        executorService.execute(() -> {
            try {
                List<RoomTemperature> temperatures = temperatureRepository.getAllRoomTemperatures();
                
                // Calcular estadísticas
                calculateTemperatureStatistics(temperatures);
                
                // Actualizar UI en el hilo principal
                roomTemperatures.postValue(temperatures);
                
            } catch (Exception e) {
                errorMessage.postValue("Error al cargar datos de temperatura: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }
    
    /**
     * Carga los controles de ventiladores disponibles
     */
    public void loadFanControls() {
        executorService.execute(() -> {
            try {
                List<FanControl> fans = temperatureRepository.getAllFanControls();
                fanControls.postValue(fans);
                
            } catch (Exception e) {
                errorMessage.postValue("Error al cargar controles de ventiladores: " + e.getMessage());
            }
        });
    }
    
    /**
     * Carga la configuración de umbrales de temperatura
     */
    public void loadTemperatureThresholds() {
        executorService.execute(() -> {
            try {
                TemperatureThreshold threshold = temperatureRepository.getTemperatureThresholds();
                if (threshold != null) {
                    temperatureThresholds.postValue(threshold);
                    minThreshold.postValue(threshold.getMinTemperature());
                    maxThreshold.postValue(threshold.getMaxTemperature());
                    isAutomaticControlEnabled.postValue(threshold.getIsAutomaticControlEnabled());
                }
                
            } catch (Exception e) {
                errorMessage.postValue("Error al cargar configuración de umbrales: " + e.getMessage());
            }
        });
    }
    
    /**
     * Cambia el estado de encendido/apagado de un ventilador
     */
    public void toggleFanPower(String deviceId, boolean isOn) {
        executorService.execute(() -> {
            try {
                boolean success = temperatureRepository.setFanPower(deviceId, isOn);
                if (success) {
                    successMessage.postValue("Ventilador " + (isOn ? "encendido" : "apagado"));
                    loadFanControls(); // Recargar datos
                } else {
                    errorMessage.postValue("Error al cambiar estado del ventilador");
                }
                
            } catch (Exception e) {
                errorMessage.postValue("Error de conexión al cambiar ventilador: " + e.getMessage());
            }
        });
    }
    
    /**
     * Cambia la velocidad de un ventilador
     */
    public void setFanSpeed(String deviceId, float speedPercentage) {
        executorService.execute(() -> {
            try {
                boolean success = temperatureRepository.setFanSpeed(deviceId, speedPercentage);
                if (success) {
                    successMessage.postValue("Velocidad del ventilador ajustada");
                    loadFanControls(); // Recargar datos
                } else {
                    errorMessage.postValue("Error al ajustar velocidad del ventilador");
                }
                
            } catch (Exception e) {
                errorMessage.postValue("Error de conexión al ajustar velocidad: " + e.getMessage());
            }
        });
    }
    
    /**
     * Guarda la configuración de umbrales de temperatura
     */
    public void saveTemperatureThresholds(float minTemp, float maxTemp, boolean autoControl) {
        if (minTemp >= maxTemp) {
            errorMessage.setValue("La temperatura mínima debe ser menor que la máxima");
            return;
        }
        
        if (minTemp < 5 || maxTemp > 50) {
            errorMessage.setValue("Las temperaturas deben estar entre 5°C y 50°C");
            return;
        }
        
        isLoading.setValue(true);
        
        executorService.execute(() -> {
            try {
                TemperatureThreshold threshold = temperatureThresholds.getValue();
                if (threshold == null) {
                    threshold = new TemperatureThreshold();
                    threshold.setId("main_threshold");
                    threshold.setRoomId("all");
                    threshold.setRoomName("Todas las habitaciones");
                }
                
                threshold.setMinTemperature(minTemp);
                threshold.setMaxTemperature(maxTemp);
                threshold.setIsAutomaticControlEnabled(autoControl);
                threshold.setLastUpdated(System.currentTimeMillis());
                
                // Configurar límites críticos (±5°C de los umbrales normales)
                threshold.setCriticalLow(minTemp - 5);
                threshold.setCriticalHigh(maxTemp + 5);
                
                boolean success = temperatureRepository.saveTemperatureThreshold(threshold);
                if (success) {
                    successMessage.postValue("Configuración guardada exitosamente");
                    
                    // Actualizar valores locales
                    minThreshold.postValue(minTemp);
                    maxThreshold.postValue(maxTemp);
                    isAutomaticControlEnabled.postValue(autoControl);
                    temperatureThresholds.postValue(threshold);
                    
                } else {
                    errorMessage.postValue("Error al guardar configuración");
                }
                
            } catch (Exception e) {
                errorMessage.postValue("Error al guardar configuración: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }
    
    /**
     * Cambia el período del gráfico de temperatura
     */
    public void setChartPeriod(ChartPeriod period) {
        selectedPeriod.setValue(period);
        // Aquí se podría recargar los datos del gráfico según el período
    }
    
    /**
     * Actualiza todos los datos de temperatura
     */
    public void refreshAllData() {
        loadTemperatureData();
        loadFanControls();
        loadTemperatureThresholds();
    }
    
    /**
     * Calcula estadísticas de temperatura a partir de las habitaciones
     */
    private void calculateTemperatureStatistics(List<RoomTemperature> temperatures) {
        if (temperatures == null || temperatures.isEmpty()) {
            averageTemperature.postValue(0f);
            minTemperature.postValue(0f);
            maxTemperature.postValue(0f);
            return;
        }
        
        float total = 0f;
        float min = Float.MAX_VALUE;
        float max = Float.MIN_VALUE;
        int validCount = 0;
        
        for (RoomTemperature room : temperatures) {
            if (room.getIsOnline()) {
                float temp = room.getCurrentTemperature();
                total += temp;
                validCount++;
                
                if (temp < min) min = temp;
                if (temp > max) max = temp;
            }
        }
        
        if (validCount > 0) {
            averageTemperature.postValue(total / validCount);
            minTemperature.postValue(min);
            maxTemperature.postValue(max);
        } else {
            averageTemperature.postValue(0f);
            minTemperature.postValue(0f);
            maxTemperature.postValue(0f);
        }
    }
    
    /**
     * Obtiene las habitaciones que necesitan atención
     */
    public LiveData<List<RoomTemperature>> getRoomsNeedingAttention() {
        return Transformations.map(roomTemperatures, temperatures -> {
            List<RoomTemperature> needingAttention = new ArrayList<>();
            if (temperatures != null) {
                for (RoomTemperature room : temperatures) {
                    if (room.needsAttention()) {
                        needingAttention.add(room);
                    }
                }
            }
            return needingAttention;
        });
    }
    
    /**
     * Obtiene los ventiladores que necesitan atención
     */
    public LiveData<List<FanControl>> getFansNeedingAttention() {
        return Transformations.map(fanControls, fans -> {
            List<FanControl> needingAttention = new ArrayList<>();
            if (fans != null) {
                for (FanControl fan : fans) {
                    if (fan.needsAttention()) {
                        needingAttention.add(fan);
                    }
                }
            }
            return needingAttention;
        });
    }
    
    /**
     * Limpia mensajes de error
     */
    public void clearErrorMessage() {
        errorMessage.setValue(null);
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
    
    /**
     * Enumeración para los períodos del gráfico
     */
    public enum ChartPeriod {
        DAY_24H("24h", 24),
        WEEK_7D("7d", 168),
        MONTH_30D("30d", 720);
        
        private final String displayName;
        private final int hours;
        
        ChartPeriod(String displayName, int hours) {
            this.displayName = displayName;
            this.hours = hours;
        }
        
        public String getDisplayName() { return displayName; }
        public int getHours() { return hours; }
    }
}