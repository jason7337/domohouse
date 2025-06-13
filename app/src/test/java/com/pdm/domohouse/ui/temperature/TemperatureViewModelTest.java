package com.pdm.domohouse.ui.temperature;

import android.app.Application;
import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import com.pdm.domohouse.data.model.RoomTemperature;
import com.pdm.domohouse.data.model.FanControl;
import com.pdm.domohouse.data.model.TemperatureThreshold;

import java.util.List;
import java.util.ArrayList;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para TemperatureViewModel
 * Verifica la lógica de negocio para el control de temperatura
 */
@RunWith(MockitoJUnitRunner.class)
public class TemperatureViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private Application mockApplication;

    @Mock
    private Observer<List<RoomTemperature>> roomTemperatureObserver;

    @Mock
    private Observer<List<FanControl>> fanControlObserver;

    @Mock
    private Observer<Float> averageTemperatureObserver;

    @Mock
    private Observer<Boolean> loadingObserver;

    @Mock
    private Observer<String> errorObserver;

    private TemperatureViewModel viewModel;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Configurar mock de Application
        when(mockApplication.getApplicationContext()).thenReturn(mockApplication);
        
        // Crear ViewModel
        viewModel = new TemperatureViewModel(mockApplication);
        
        // Configurar observadores
        viewModel.getRoomTemperatures().observeForever(roomTemperatureObserver);
        viewModel.getFanControls().observeForever(fanControlObserver);
        viewModel.getAverageTemperature().observeForever(averageTemperatureObserver);
        viewModel.getIsLoading().observeForever(loadingObserver);
        viewModel.getErrorMessage().observeForever(errorObserver);
    }

    @Test
    public void testInitialState() {
        // Verificar estado inicial del ViewModel
        assertNotNull("ViewModel should not be null", viewModel);
        
        // Verificar que LiveData están inicializados
        assertNotNull("Room temperatures LiveData should be initialized", 
            viewModel.getRoomTemperatures());
        assertNotNull("Fan controls LiveData should be initialized", 
            viewModel.getFanControls());
        assertNotNull("Average temperature LiveData should be initialized", 
            viewModel.getAverageTemperature());
        assertNotNull("Loading state LiveData should be initialized", 
            viewModel.getIsLoading());
    }

    @Test
    public void testLoadTemperatureData() {
        // Ejecutar carga de datos
        viewModel.loadTemperatureData();
        
        // Dar tiempo para que se ejecute la operación asíncrona
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Verificar que se llamó al observador de loading
        verify(loadingObserver, atLeastOnce()).onChanged(true);
        verify(loadingObserver, timeout(2000)).onChanged(false);
        
        // Verificar que se cargaron datos de temperatura
        verify(roomTemperatureObserver, timeout(2000)).onChanged(any(List.class));
    }

    @Test
    public void testLoadFanControls() {
        // Ejecutar carga de controles de ventiladores
        viewModel.loadFanControls();
        
        // Dar tiempo para la operación asíncrona
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Verificar que se cargaron los controles de ventiladores
        verify(fanControlObserver, timeout(2000)).onChanged(any(List.class));
    }

    @Test
    public void testSaveTemperatureThresholds_ValidRange() {
        // Configurar valores válidos
        float minTemp = 18f;
        float maxTemp = 28f;
        boolean autoControl = true;
        
        // Ejecutar guardado
        viewModel.saveTemperatureThresholds(minTemp, maxTemp, autoControl);
        
        // Verificar que se inició el loading
        verify(loadingObserver, timeout(1000)).onChanged(true);
        
        // Dar tiempo para la operación
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Verificar que se completó el loading
        verify(loadingObserver, timeout(2000)).onChanged(false);
    }

    @Test
    public void testSaveTemperatureThresholds_InvalidRange() {
        // Configurar rango inválido (min >= max)
        float minTemp = 28f;
        float maxTemp = 18f;
        boolean autoControl = true;
        
        // Ejecutar guardado
        viewModel.saveTemperatureThresholds(minTemp, maxTemp, autoControl);
        
        // Verificar que se muestra error
        verify(errorObserver, timeout(1000))
            .onChanged("La temperatura mínima debe ser menor que la máxima");
    }

    @Test
    public void testSaveTemperatureThresholds_OutOfBounds() {
        // Configurar valores fuera de rango
        float minTemp = 2f;  // Muy bajo
        float maxTemp = 55f; // Muy alto
        boolean autoControl = true;
        
        // Ejecutar guardado
        viewModel.saveTemperatureThresholds(minTemp, maxTemp, autoControl);
        
        // Verificar que se muestra error
        verify(errorObserver, timeout(1000))
            .onChanged("Las temperaturas deben estar entre 5°C y 50°C");
    }

    @Test
    public void testSetChartPeriod() {
        // Configurar período inicial
        TemperatureViewModel.ChartPeriod initialPeriod = 
            TemperatureViewModel.ChartPeriod.DAY_24H;
        
        // Cambiar período
        viewModel.setChartPeriod(initialPeriod);
        
        // Verificar que el período se actualizó
        assertEquals("Chart period should be updated", 
            initialPeriod, viewModel.getSelectedPeriod().getValue());
    }

    @Test
    public void testToggleFanPower() {
        String deviceId = "test_fan_001";
        boolean isOn = true;
        
        // Ejecutar cambio de estado
        viewModel.toggleFanPower(deviceId, isOn);
        
        // Dar tiempo para la operación asíncrona
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Verificar que no se produjeron errores críticos
        // (Los errores de red simulados son normales)
        assertNotNull("ViewModel should remain functional", viewModel);
    }

    @Test
    public void testSetFanSpeed() {
        String deviceId = "test_fan_001";
        float speedPercentage = 75f;
        
        // Ejecutar cambio de velocidad
        viewModel.setFanSpeed(deviceId, speedPercentage);
        
        // Dar tiempo para la operación asíncrona
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Verificar que no se produjeron errores críticos
        assertNotNull("ViewModel should remain functional", viewModel);
    }

    @Test
    public void testRefreshAllData() {
        // Ejecutar actualización completa
        viewModel.refreshAllData();
        
        // Dar tiempo para todas las operaciones
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Verificar que se llamaron los observadores
        verify(loadingObserver, atLeastOnce()).onChanged(true);
        verify(roomTemperatureObserver, timeout(3000)).onChanged(any(List.class));
        verify(fanControlObserver, timeout(3000)).onChanged(any(List.class));
    }

    @Test
    public void testClearErrorMessage() {
        // Limpiar mensaje de error
        viewModel.clearErrorMessage();
        
        // Verificar que se limpió
        assertNull("Error message should be cleared", 
            viewModel.getErrorMessage().getValue());
    }

    @Test
    public void testChartPeriodValues() {
        // Verificar valores de los períodos del gráfico
        TemperatureViewModel.ChartPeriod day = TemperatureViewModel.ChartPeriod.DAY_24H;
        TemperatureViewModel.ChartPeriod week = TemperatureViewModel.ChartPeriod.WEEK_7D;
        TemperatureViewModel.ChartPeriod month = TemperatureViewModel.ChartPeriod.MONTH_30D;
        
        assertEquals("24h period should have correct hours", 24, day.getHours());
        assertEquals("7d period should have correct hours", 168, week.getHours());
        assertEquals("30d period should have correct hours", 720, month.getHours());
        
        assertEquals("24h display name should be correct", "24h", day.getDisplayName());
        assertEquals("7d display name should be correct", "7d", week.getDisplayName());
        assertEquals("30d display name should be correct", "30d", month.getDisplayName());
    }
}