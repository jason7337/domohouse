package com.pdm.domohouse.ui.home;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.pdm.domohouse.data.model.Device;
import com.pdm.domohouse.data.model.Room;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para HomeViewModel
 * Prueba la lógica de negocio del dashboard principal
 */
@RunWith(MockitoJUnitRunner.class)
public class HomeViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private Observer<List<Room>> roomsObserver;

    @Mock
    private Observer<List<Device>> devicesObserver;

    @Mock
    private Observer<HomeViewModel.DashboardStats> statsObserver;

    @Mock
    private Observer<Room> selectedRoomObserver;

    @Mock
    private Observer<Boolean> loadingObserver;

    private HomeViewModel viewModel;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        viewModel = new HomeViewModel();
    }

    @Test
    public void testViewModelInitialization() {
        // Verificar que el ViewModel se inicializa correctamente
        assertNotNull("ViewModel no debe ser null", viewModel);
        assertNotNull("LiveData de habitaciones no debe ser null", viewModel.rooms);
        assertNotNull("LiveData de dispositivos no debe ser null", viewModel.devices);
        assertNotNull("LiveData de estadísticas no debe ser null", viewModel.dashboardStats);
        assertNotNull("LiveData de habitación seleccionada no debe ser null", viewModel.selectedRoom);
    }

    @Test
    public void testRoomsDataGeneration() {
        // Configurar observador
        viewModel.rooms.observeForever(roomsObserver);

        // Capturar argumentos
        ArgumentCaptor<List<Room>> roomsCaptor = ArgumentCaptor.forClass(List.class);
        verify(roomsObserver, atLeastOnce()).onChanged(roomsCaptor.capture());

        List<Room> rooms = roomsCaptor.getValue();
        
        // Verificar que se generaron habitaciones
        assertNotNull("Lista de habitaciones no debe ser null", rooms);
        assertTrue("Debe haber al menos una habitación", rooms.size() > 0);
        
        // Verificar que las habitaciones tienen datos válidos
        for (Room room : rooms) {
            assertNotNull("ID de habitación no debe ser null", room.getId());
            assertNotNull("Nombre de habitación no debe ser null", room.getName());
            assertNotNull("Tipo de habitación no debe ser null", room.getType());
            assertTrue("Temperatura debe ser realista", room.getTemperature() >= 0 && room.getTemperature() <= 50);
            assertTrue("Humedad debe estar en rango válido", room.getHumidity() >= 0 && room.getHumidity() <= 100);
        }

        // Limpiar
        viewModel.rooms.removeObserver(roomsObserver);
    }

    @Test
    public void testDevicesDataGeneration() {
        // Configurar observador
        viewModel.devices.observeForever(devicesObserver);

        // Capturar argumentos
        ArgumentCaptor<List<Device>> devicesCaptor = ArgumentCaptor.forClass(List.class);
        verify(devicesObserver, atLeastOnce()).onChanged(devicesCaptor.capture());

        List<Device> devices = devicesCaptor.getValue();
        
        // Verificar que se generaron dispositivos
        assertNotNull("Lista de dispositivos no debe ser null", devices);
        assertTrue("Debe haber al menos un dispositivo", devices.size() > 0);
        
        // Verificar que los dispositivos tienen datos válidos
        for (Device device : devices) {
            assertNotNull("ID de dispositivo no debe ser null", device.getId());
            assertNotNull("Nombre de dispositivo no debe ser null", device.getName());
            assertNotNull("Tipo de dispositivo no debe ser null", device.getType());
            assertNotNull("ID de habitación no debe ser null", device.getRoomId());
            
            // Verificar que los valores están en rango
            assertTrue("Valor actual debe estar en rango", 
                      device.getCurrentValue() >= device.getMinValue() && 
                      device.getCurrentValue() <= device.getMaxValue());
        }

        // Limpiar
        viewModel.devices.removeObserver(devicesObserver);
    }

    @Test
    public void testDashboardStatsGeneration() {
        // Configurar observador
        viewModel.dashboardStats.observeForever(statsObserver);

        // Capturar argumentos
        ArgumentCaptor<HomeViewModel.DashboardStats> statsCaptor = 
            ArgumentCaptor.forClass(HomeViewModel.DashboardStats.class);
        verify(statsObserver, atLeastOnce()).onChanged(statsCaptor.capture());

        HomeViewModel.DashboardStats stats = statsCaptor.getValue();
        
        // Verificar estadísticas
        assertNotNull("Estadísticas no deben ser null", stats);
        assertTrue("Total de habitaciones debe ser positivo", stats.totalRooms > 0);
        assertTrue("Habitaciones activas debe ser >= 0", stats.activeRooms >= 0);
        assertTrue("Total de dispositivos debe ser positivo", stats.totalDevices > 0);
        assertTrue("Dispositivos conectados debe ser >= 0", stats.connectedDevices >= 0);
        assertTrue("Dispositivos activos debe ser >= 0", stats.activeDevices >= 0);
        assertTrue("Alertas debe ser >= 0", stats.alertsCount >= 0);
        assertTrue("Temperatura promedio debe ser realista", 
                  stats.avgTemperature >= 0 && stats.avgTemperature <= 50);

        // Verificar lógica de porcentajes
        assertTrue("Porcentaje de habitaciones activas debe estar en rango 0-100",
                  stats.getActiveRoomsPercentage() >= 0 && stats.getActiveRoomsPercentage() <= 100);
        assertTrue("Porcentaje de dispositivos conectados debe estar en rango 0-100",
                  stats.getConnectedDevicesPercentage() >= 0 && stats.getConnectedDevicesPercentage() <= 100);

        // Limpiar
        viewModel.dashboardStats.removeObserver(statsObserver);
    }

    @Test
    public void testRoomSelection() {
        // Configurar observadores
        viewModel.rooms.observeForever(roomsObserver);
        viewModel.selectedRoom.observeForever(selectedRoomObserver);

        // Obtener una habitación para seleccionar
        ArgumentCaptor<List<Room>> roomsCaptor = ArgumentCaptor.forClass(List.class);
        verify(roomsObserver, atLeastOnce()).onChanged(roomsCaptor.capture());
        List<Room> rooms = roomsCaptor.getValue();
        
        assertNotNull("Lista de habitaciones no debe ser null", rooms);
        assertTrue("Debe haber al menos una habitación", rooms.size() > 0);

        Room roomToSelect = rooms.get(0);

        // Seleccionar habitación
        viewModel.selectRoom(roomToSelect);

        // Verificar que se seleccionó
        ArgumentCaptor<Room> selectedCaptor = ArgumentCaptor.forClass(Room.class);
        verify(selectedRoomObserver, atLeastOnce()).onChanged(selectedCaptor.capture());
        
        Room selectedRoom = selectedCaptor.getValue();
        assertNotNull("Habitación seleccionada no debe ser null", selectedRoom);
        assertEquals("La habitación seleccionada debe coincidir", 
                    roomToSelect.getId(), selectedRoom.getId());

        // Limpiar
        viewModel.rooms.removeObserver(roomsObserver);
        viewModel.selectedRoom.removeObserver(selectedRoomObserver);
    }

    @Test
    public void testGetDevicesForRoom() {
        // Obtener habitaciones y dispositivos
        viewModel.rooms.observeForever(roomsObserver);
        viewModel.devices.observeForever(devicesObserver);

        ArgumentCaptor<List<Room>> roomsCaptor = ArgumentCaptor.forClass(List.class);
        verify(roomsObserver, atLeastOnce()).onChanged(roomsCaptor.capture());
        List<Room> rooms = roomsCaptor.getValue();

        if (rooms != null && !rooms.isEmpty()) {
            Room testRoom = rooms.get(0);
            List<Device> roomDevices = viewModel.getDevicesForRoom(testRoom.getId());
            
            assertNotNull("Lista de dispositivos de habitación no debe ser null", roomDevices);
            
            // Verificar que todos los dispositivos pertenecen a la habitación
            for (Device device : roomDevices) {
                assertEquals("Dispositivo debe pertenecer a la habitación correcta",
                           testRoom.getId(), device.getRoomId());
            }
        }

        // Limpiar
        viewModel.rooms.removeObserver(roomsObserver);
        viewModel.devices.removeObserver(devicesObserver);
    }

    @Test
    public void testUpdateDeviceState() {
        // Configurar observador
        viewModel.devices.observeForever(devicesObserver);

        // Obtener dispositivos
        ArgumentCaptor<List<Device>> devicesCaptor = ArgumentCaptor.forClass(List.class);
        verify(devicesObserver, atLeastOnce()).onChanged(devicesCaptor.capture());
        List<Device> devices = devicesCaptor.getValue();

        if (devices != null && !devices.isEmpty()) {
            Device testDevice = devices.get(0);
            String deviceId = testDevice.getId();
            boolean newState = !testDevice.isEnabled();

            // Actualizar estado
            viewModel.updateDeviceState(deviceId, newState);

            // Verificar que se actualizó
            verify(devicesObserver, atLeast(2)).onChanged(any());
            
            // Encontrar el dispositivo actualizado
            List<Device> updatedDevices = viewModel.devices.getValue();
            assertNotNull("Lista actualizada no debe ser null", updatedDevices);
            
            Device updatedDevice = null;
            for (Device device : updatedDevices) {
                if (device.getId().equals(deviceId)) {
                    updatedDevice = device;
                    break;
                }
            }
            
            assertNotNull("Dispositivo actualizado debe existir", updatedDevice);
            assertEquals("Estado del dispositivo debe haber cambiado", 
                        newState, updatedDevice.isEnabled());
        }

        // Limpiar
        viewModel.devices.removeObserver(devicesObserver);
    }

    @Test
    public void testRefreshSensorData() {
        // Configurar observador
        viewModel.isLoading.observeForever(loadingObserver);

        // Ejecutar refresh
        viewModel.refreshSensorData();

        // Verificar que se muestra loading
        ArgumentCaptor<Boolean> loadingCaptor = ArgumentCaptor.forClass(Boolean.class);
        verify(loadingObserver, atLeastOnce()).onChanged(loadingCaptor.capture());
        
        List<Boolean> loadingStates = loadingCaptor.getAllValues();
        
        // Debe haber al menos un estado de loading true y luego false
        assertTrue("Debe haber mostrado estado de carga", loadingStates.size() >= 2);

        // Limpiar
        viewModel.isLoading.removeObserver(loadingObserver);
    }

    @Test
    public void testDashboardStatsCalculations() {
        // Crear estadísticas de prueba
        HomeViewModel.DashboardStats stats = new HomeViewModel.DashboardStats(
            8, 5, 24, 22, 15, 2, 23.5f
        );

        // Verificar cálculos
        assertEquals("Porcentaje de habitaciones activas incorrecto", 
                    62, stats.getActiveRoomsPercentage()); // 5/8 * 100 = 62.5 -> 62
        assertEquals("Porcentaje de dispositivos conectados incorrecto", 
                    91, stats.getConnectedDevicesPercentage()); // 22/24 * 100 = 91.6 -> 91
        assertTrue("Debe tener alertas", stats.hasAlerts());
    }

    @Test
    public void testDashboardStatsEdgeCases() {
        // Caso con ceros
        HomeViewModel.DashboardStats emptyStats = new HomeViewModel.DashboardStats(
            0, 0, 0, 0, 0, 0, 0f
        );

        assertEquals("Porcentaje con cero habitaciones debe ser 0", 
                    0, emptyStats.getActiveRoomsPercentage());
        assertEquals("Porcentaje con cero dispositivos debe ser 0", 
                    0, emptyStats.getConnectedDevicesPercentage());
        assertFalse("No debe tener alertas", emptyStats.hasAlerts());
    }
}