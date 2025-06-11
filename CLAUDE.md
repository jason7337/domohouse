# CLAUDE CODE - CONTEXTO DE DESARROLLO

## 🏠 PROYECTO: DOMO HOUSE - Sistema de Casa Inteligente

### 📋 INFORMACIÓN DEL PROYECTO
- **Tipo**: Aplicación Android para automatización de casa inteligente
- **Stack**: Android (Java) + Python (Backend) + Firebase + ESP32
- **Estado**: En desarrollo inicial
- **Última actualización**: 10/6/2025

### 🎨 DISEÑO Y BRANDING
**Paleta de Colores (basada en logo):**
- **Primary**: #D4A574 (Dorado/Bronce - del logo)
- **Primary Dark**: #B8935F (Dorado oscuro)
- **Primary Light**: #E5C3A0 (Dorado claro)
- **Background**: #FFFFFF (Blanco)
- **Surface**: #F5F5F5 (Gris muy claro)
- **Text Primary**: #1A1F2E (Azul oscuro del logo)
- **Text Secondary**: #666666 (Gris)
- **Success**: #4CAF50 (Verde)
- **Warning**: #FF9800 (Naranja)
- **Error**: #F44336 (Rojo)

**Nota**: Tema oscuro ELIMINADO por solicitud del cliente

### 📁 ESTRUCTURA DEL PROYECTO

```
DomoHouse/
├── app/                           # Aplicación Android
│   ├── src/main/java/            # Código fuente Java
│   │   └── com/pdm/domohouse/    
│   │       ├── ui/               # Actividades y Fragmentos
│   │       ├── data/             # Modelos y SQLite
│   │       ├── network/          # Cliente API y Firebase
│   │       ├── utils/            # Utilidades
│   │       └── hardware/         # Comunicación con ESP32
│   └── src/main/res/             # Recursos Android
├── backend/                       # Backend Python (A CREAR)
│   ├── api/                      # Endpoints REST
│   ├── models/                   # Modelos de datos
│   ├── services/                 # Lógica de negocio
│   ├── hardware/                 # Comunicación ESP32
│   └── reports/                  # Generación de PDFs
├── hardware/                      # Código ESP32 (A CREAR)
│   ├── sensors/                  # Lecturas de sensores
│   ├── actuators/                # Control de dispositivos
│   └── communication/            # WiFi y protocolos
├── docs/                         # Documentación
│   ├── technical/                # Documentación técnica
│   ├── user/                     # Manual de usuario
│   └── testing/                  # Planes de pruebas
└── tests/                        # Tests unitarios e integración
```

### 🚀 PLAN DE DESARROLLO POR SESIONES

#### SESIÓN 1: Configuración Base ✅
- [x] Crear estructura de proyecto
- [x] Eliminar tema oscuro
- [x] Aplicar paleta de colores
- [x] Crear documentación inicial
- [ ] Configurar Firebase (google-services.json ya existe)

#### SESIÓN 2: Arquitectura y Navegación ✅
- [x] Implementar arquitectura MVVM
- [x] Crear sistema de navegación (Navigation Component)
- [x] Diseñar pantalla Splash con logo
- [x] Crear pantalla de Login (Firebase + PIN)
- [x] Tests: Navegación básica

#### SESIÓN 3: Autenticación
- [ ] Implementar Firebase Authentication
- [ ] Sistema PIN local con SharedPreferences cifradas
- [ ] Pantalla de registro
- [ ] Recuperación de contraseña
- [ ] Tests: Flujos de autenticación

#### SESIÓN 4: Dashboard Principal
- [ ] Diseñar layout del dashboard
- [ ] Vista de maqueta de casa (Custom View)
- [ ] Estados de dispositivos en tiempo real
- [ ] Animaciones de transición
- [ ] Tests: Renderizado y estados

#### SESIÓN 5: Control de Iluminación
- [ ] Pantalla de control por habitaciones
- [ ] Switches personalizados
- [ ] Control de intensidad (seekbars)
- [ ] Integración con backend (mock inicial)
- [ ] Tests: Controles UI

#### SESIÓN 6: Sistema de Temperatura
- [ ] Visualización de temperaturas por habitación
- [ ] Controles de ventiladores
- [ ] Configuración de umbrales automáticos
- [ ] Gráficos de temperatura (MPAndroidChart)
- [ ] Tests: Lógica de automatización

#### SESIÓN 7: Seguridad
- [ ] Pantalla de seguridad
- [ ] Estados de puertas/ventanas
- [ ] Alertas de humo/gases
- [ ] Control de cerradura inteligente
- [ ] Tests: Sistema de alertas

#### SESIÓN 8: Programación Horaria
- [ ] Calendario de programación
- [ ] Selector de dispositivos
- [ ] Configuración de reglas
- [ ] Vista de programaciones activas
- [ ] Tests: Scheduler

#### SESIÓN 9: Base de Datos Local
- [ ] Implementar SQLite con Room
- [ ] Esquema de base de datos
- [ ] DAOs y Repositories
- [ ] Sincronización offline/online
- [ ] Tests: Persistencia de datos

#### SESIÓN 10: Historial y Eventos
- [ ] Pantalla de historial
- [ ] Filtros y búsqueda
- [ ] Exportación de datos
- [ ] Sincronización con Firebase
- [ ] Tests: Queries y filtros

#### SESIÓN 11: Reportes y Analytics
- [ ] Integración de gráficas (días/semanas)
- [ ] Generación de PDFs
- [ ] Estadísticas de uso
- [ ] Compartir reportes
- [ ] Tests: Generación de reportes

#### SESIÓN 12: Backend Python - Base
- [ ] Setup Flask/FastAPI
- [ ] Modelos de datos
- [ ] Endpoints básicos CRUD
- [ ] Integración Firebase Admin SDK
- [ ] Tests: API endpoints

#### SESIÓN 13: Backend Python - Hardware
- [ ] Comunicación con ESP32
- [ ] Procesamiento de sensores
- [ ] Lógica de automatización
- [ ] Sistema de colas de comandos
- [ ] Tests: Comunicación hardware

#### SESIÓN 14: ESP32 - Sensores
- [ ] Código base ESP32
- [ ] Lectura sensores temperatura
- [ ] Sensor humo/gases
- [ ] Sensores magnéticos
- [ ] Tests: Lecturas precisas

#### SESIÓN 15: ESP32 - Actuadores
- [ ] Control de relés (luces)
- [ ] PWM para ventiladores
- [ ] Servomotor cerradura
- [ ] Teclado matricial
- [ ] Tests: Control de dispositivos

#### SESIÓN 16: Integración Completa
- [ ] Conectar Android + Backend + ESP32
- [ ] Pruebas end-to-end
- [ ] Optimización de rendimiento
- [ ] Corrección de bugs
- [ ] Tests: Sistema completo

#### SESIÓN 17: Accesibilidad
- [ ] Implementar TalkBack support
- [ ] Tamaños de fuente ajustables
- [ ] Contraste alto
- [ ] Navegación por teclado
- [ ] Tests: Criterios WCAG

#### SESIÓN 18: Testing Final
- [ ] Tests unitarios completos
- [ ] Tests de integración
- [ ] Tests de UI (Espresso)
- [ ] Tests de carga
- [ ] Documentación de tests

#### SESIÓN 19: Documentación
- [ ] Manual de usuario completo
- [ ] Documentación técnica API
- [ ] Guía de instalación hardware
- [ ] Videos tutoriales
- [ ] README actualizado

#### SESIÓN 20: Despliegue
- [ ] Build release APK
- [ ] Deploy backend
- [ ] Configuración Firebase producción
- [ ] Entrega final
- [ ] Presentación

### 📊 MÉTRICAS DE CALIDAD

**Cada sesión debe cumplir:**
1. ✅ Código compilable sin errores
2. ✅ Tests unitarios pasando (>80% cobertura)
3. ✅ Sin warnings críticos
4. ✅ Documentación actualizada
5. ✅ Commit con mensaje descriptivo

### 🧪 COMANDOS DE TESTING

```bash
# Android Tests
./gradlew test                    # Unit tests
./gradlew connectedAndroidTest    # Instrumented tests
./gradlew lint                    # Lint check

# Python Tests (cuando esté creado)
pytest backend/tests/             # Backend tests
flake8 backend/                   # Linting Python
```

### 📝 NOTAS IMPORTANTES

1. **Firebase**: El archivo `google-services.json` ya está configurado
2. **Package**: `com.pdm.domohouse`
3. **Min SDK**: Verificar en build.gradle
4. **Dependencias**: Agregar según necesidad (Room, Retrofit, etc.)
5. **Idioma del código**: TODOS los comentarios en ESPAÑOL
6. **Prompts**: Ver archivo `PROMPTS_PARA_SESIONES.md` para iniciar cada sesión

### 🔄 ESTADO ACTUAL

**Sesión 1 - Completada**:
- ✅ Estructura de proyecto creada
- ✅ Documentación inicial (CLAUDE.md, README.md)
- ✅ Paleta de colores definida
- ✅ Eliminación tema oscuro completada
- ✅ Aplicación de nuevos colores

**Sesión 2 - Completada**:
- ✅ Arquitectura MVVM implementada (BaseViewModel, BaseActivity)
- ✅ Navigation Component configurado con nav_graph.xml
- ✅ SplashFragment con animaciones elegantes
- ✅ LoginFragment con validaciones y PIN
- ✅ Data Binding funcional
- ✅ Tests básicos estructurados

**Sesión 3 - Completada**:
- ✅ Firebase Authentication completo
- ✅ Sistema PIN local con SharedPreferences cifradas
- ✅ Pantalla de registro completamente funcional
- ✅ Recuperación de contraseña implementada
- ✅ Tests de autenticación y seguridad
- ✅ Error crítico de threading resuelto

**Sesión 4 - Completada**:
- ✅ Dashboard principal con vista de maqueta de casa
- ✅ HouseMapView (Custom View) interactiva y elegante
- ✅ Modelos de datos completos (Room, Device, RoomType, etc.)
- ✅ HomeViewModel con lógica de dashboard completa
- ✅ Estadísticas en tiempo real y actualización de sensores
- ✅ Animaciones de transición suaves y profesionales
- ✅ +60 tests unitarios adicionales implementados
- ✅ Layout responsive con paleta de colores elegante

**Próxima sesión**: Control de Iluminación

### 🎯 OBJETIVO DE CADA SESIÓN

Cada sesión debe ser autocontenida y dejar el proyecto en un estado funcional. Al final de cada sesión:
1. Ejecutar tests
2. Actualizar este documento
3. Crear informe de sesión en `/docs/sessions/`
4. Commit con todos los cambios

### 📝 REGLAS DE DESARROLLO

1. **TODO el código debe estar comentado en ESPAÑOL**
2. **Cada clase, método y lógica compleja debe tener comentarios claros**
3. **Los nombres de variables y métodos pueden estar en inglés, pero los comentarios en español**
4. **Actualizar el plan según el progreso real (agregar/modificar sesiones si es necesario)**
5. **Cada sesión debe generar un informe detallado con:**
   - Tareas completadas
   - Problemas encontrados
   - Tests ejecutados
   - Métricas de calidad
   - Recomendaciones para la siguiente sesión

### 🔧 ADAPTABILIDAD DEL PLAN

Este plan es **FLEXIBLE** y debe adaptarse según:
- Complejidad real de las tareas
- Problemas técnicos encontrados
- Nuevos requerimientos descubiertos
- Sugerencias de mejora

Claude Code tiene autorización para:
- Dividir sesiones muy complejas en múltiples partes
- Agregar sesiones intermedias si es necesario
- Reorganizar tareas para optimizar el desarrollo
- Sugerir mejoras arquitectónicas

---

**Última actualización**: 11/6/2025 - Sesión 4
**Próxima sesión recomendada**: Sesión 5 - Control de Iluminación