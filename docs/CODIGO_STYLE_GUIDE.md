# 📖 GUÍA DE ESTILO DE CÓDIGO - DOMO HOUSE

## 🎯 PRINCIPIO FUNDAMENTAL

**TODO el código debe estar comentado en ESPAÑOL** para facilitar el entendimiento y mantenimiento.

## 📝 REGLAS DE COMENTARIOS

### 1. Comentarios de Clase
```java
/**
 * Actividad principal que muestra el dashboard de la casa inteligente.
 * Gestiona el estado de todos los dispositivos y muestra la vista
 * general de la casa con sus habitaciones.
 * 
 * @author DOMO HOUSE Team
 * @version 1.0
 * @since 2025-01-10
 */
public class DashboardActivity extends AppCompatActivity {
```

### 2. Comentarios de Métodos
```java
/**
 * Actualiza el estado de una luz específica en la habitación.
 * 
 * @param habitacionId ID único de la habitación
 * @param luzId ID de la luz dentro de la habitación
 * @param encendida true para encender, false para apagar
 * @return true si la actualización fue exitosa
 */
public boolean actualizarEstadoLuz(String habitacionId, String luzId, boolean encendida) {
    // Validar parámetros de entrada
    if (habitacionId == null || luzId == null) {
        return false;
    }
    
    // Actualizar estado en la base de datos local
    // ...
}
```

### 3. Comentarios de Variables Importantes
```java
// Tiempo máximo de espera para respuesta del servidor (en milisegundos)
private static final int TIMEOUT_SERVIDOR = 5000;

// Lista de dispositivos actualmente conectados
private List<Dispositivo> dispositivosConectados;

// Estado actual del modo offline
private boolean modoOffline = false;
```

### 4. Comentarios de Lógica Compleja
```java
// Algoritmo para calcular la temperatura promedio de la casa
// considerando solo las habitaciones con sensores activos
double temperaturaPromedio = habitaciones.stream()
    .filter(h -> h.tieneSensorActivo()) // Filtrar solo habitaciones con sensor
    .mapToDouble(Habitacion::getTemperatura) // Obtener temperatura
    .average() // Calcular promedio
    .orElse(TEMPERATURA_DEFAULT); // Valor por defecto si no hay sensores
```

## 🏗️ ESTRUCTURA DE ARCHIVOS

### Ejemplo de Estructura de Clase Completa
```java
package com.pdm.domohouse.ui.dashboard;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Actividad del Dashboard principal de DOMO HOUSE.
 * Muestra el estado general de la casa y permite control rápido
 * de los dispositivos más importantes.
 */
public class DashboardActivity extends AppCompatActivity {
    
    // ===== CONSTANTES =====
    // Intervalo de actualización automática (5 segundos)
    private static final long INTERVALO_ACTUALIZACION = 5000L;
    
    // ===== VARIABLES DE INSTANCIA =====
    // ViewModel que gestiona los datos del dashboard
    private DashboardViewModel viewModel;
    
    // Binding para acceder a las vistas
    private ActivityDashboardBinding binding;
    
    // ===== CICLO DE VIDA =====
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Inicializar binding
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Configurar ViewModel
        configurarViewModel();
        
        // Inicializar interfaz de usuario
        inicializarUI();
        
        // Observar cambios en los datos
        observarCambios();
    }
    
    // ===== MÉTODOS PRIVADOS =====
    /**
     * Configura el ViewModel y sus dependencias
     */
    private void configurarViewModel() {
        // Implementación...
    }
    
    /**
     * Inicializa todos los componentes de la interfaz
     */
    private void inicializarUI() {
        // Configurar toolbar
        setSupportActionBar(binding.toolbar);
        
        // Configurar RecyclerView de dispositivos
        configurarListaDispositivos();
        
        // Configurar vista de maqueta de casa
        configurarVistaCasa();
    }
}
```

## 🎨 CONVENCIONES DE NOMBRES

### Variables y Métodos (camelCase en inglés, comentarios en español)
```java
// Temperatura actual de la habitación principal
private float currentTemperature;

// Obtiene el estado actual de todos los sensores
public List<SensorStatus> getSensorStatuses() {
    // Implementación...
}
```

### Constantes (UPPER_SNAKE_CASE)
```java
// Código PIN por defecto para acceso local
private static final String DEFAULT_PIN = "1234";

// Número máximo de intentos de login
private static final int MAX_LOGIN_ATTEMPTS = 3;
```

### Clases e Interfaces (PascalCase)
```java
public class TemperatureSensor { }
public interface DeviceController { }
```

## 📦 ORGANIZACIÓN DE PAQUETES

```
com.pdm.domohouse/
├── ui/                 # Interfaz de usuario
│   ├── auth/          # Autenticación (login, registro)
│   ├── dashboard/     # Dashboard principal
│   ├── devices/       # Control de dispositivos
│   ├── schedule/      # Programación horaria
│   └── settings/      # Configuración
├── data/              # Capa de datos
│   ├── local/        # Base de datos local (Room)
│   ├── remote/       # API remota
│   └── repository/   # Repositorios
├── network/           # Comunicación de red
│   ├── api/         # Definiciones de API
│   └── firebase/    # Integración Firebase
├── hardware/          # Comunicación con hardware
│   └── esp32/       # Protocolo ESP32
└── utils/             # Utilidades generales
```

## 🧪 COMENTARIOS PARA TESTS

```java
/**
 * Test para verificar el cálculo correcto de temperatura promedio
 * cuando hay múltiples sensores activos en diferentes habitaciones.
 */
@Test
public void testCalculoTemperaturaPromedio_ConMultiplesSensores() {
    // Preparar: Crear habitaciones con diferentes temperaturas
    List<Habitacion> habitaciones = Arrays.asList(
        new Habitacion("Sala", 22.5f, true),
        new Habitacion("Dormitorio", 20.0f, true),
        new Habitacion("Cocina", 24.0f, false) // Sensor inactivo
    );
    
    // Ejecutar: Calcular temperatura promedio
    double promedio = calculadoraTemp.calcularPromedio(habitaciones);
    
    // Verificar: Solo debe considerar Sala y Dormitorio (22.5 + 20.0) / 2
    assertEquals(21.25, promedio, 0.01);
}
```

## 📋 CHECKLIST DE REVISIÓN

Antes de hacer commit, verifica:

- [ ] ¿Todas las clases tienen comentario de documentación?
- [ ] ¿Todos los métodos públicos están documentados?
- [ ] ¿La lógica compleja tiene comentarios explicativos?
- [ ] ¿Las constantes tienen comentarios descriptivos?
- [ ] ¿Los comentarios están en ESPAÑOL?
- [ ] ¿Los comentarios son claros y útiles?
- [ ] ¿Se eliminaron los comentarios obsoletos?

## 🚀 BENEFICIOS

1. **Mantenibilidad**: Código fácil de entender y modificar
2. **Colaboración**: Cualquier desarrollador puede entender el código
3. **Debugging**: Más fácil encontrar y corregir errores
4. **Documentación**: El código se auto-documenta
5. **Aprendizaje**: Ideal para proyectos académicos

---

**RECUERDA**: Un buen comentario explica el "por qué", no solo el "qué".

```java
// MAL: Incrementa el contador
contador++;

// BIEN: Incrementa el contador de intentos fallidos para 
// bloquear el acceso después de 3 intentos
contador++;
```