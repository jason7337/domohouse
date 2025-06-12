# 🏠 DOMO HOUSE - Sistema de Casa Inteligente

![DOMO HOUSE Logo](app/src/main/res/drawable/domo_house_logo.png)

## 📋 Descripción

DOMO HOUSE es un sistema completo de automatización para casa inteligente que combina una aplicación Android, backend Python, Firebase y hardware ESP32 para controlar y monitorear dispositivos del hogar de manera inteligente.

## 🎯 Características Principales

### 📱 Aplicación Android
- **Control Total**: Visualiza y controla el estado de todos los dispositivos
- **Programación Horaria**: Automatiza dispositivos según horarios
- **Modo Offline**: Funciona sin internet usando SQLite local
- **Reportes PDF**: Genera gráficas diarias y semanales
- **Interfaz Accesible**: Cumple con estándares de accesibilidad

### 🏠 Control de Dispositivos
- ✅ Iluminación inteligente en todas las habitaciones
- ✅ Control automático de temperatura con ventiladores
- ✅ Detección de humo y gases
- ✅ Sensores de puertas y ventanas
- ✅ Cerradura inteligente con PIN

### 🔐 Seguridad
- Autenticación dual: Firebase + PIN local
- Historial completo de accesos
- Alertas en tiempo real
- Teclado matricial físico para acceso local

## 🛠️ Stack Tecnológico

### Frontend
- **Android Studio** + Java
- **Min SDK**: 21 (Android 5.0)
- **Target SDK**: 34 (Android 14)
- **Arquitectura**: MVVM

### Backend
- **Python** 3.9+
- **Framework**: Flask/FastAPI
- **Base de datos local**: SQLite
- **Cloud**: Firebase Realtime Database

### Hardware
- **Microcontrolador**: ESP32
- **Comunicación**: WiFi
- **Sensores**: DHT22, MQ-2, magnéticos
- **Actuadores**: Relés, servomotores

## 📁 Estructura del Proyecto

```
DomoHouse/
├── app/                    # Aplicación Android
├── backend/               # API Python (por crear)
├── hardware/              # Código ESP32 (por crear)
├── docs/                  # Documentación
└── tests/                 # Pruebas
```

## 🚀 Instalación

### Requisitos Previos
- Android Studio Arctic Fox o superior
- JDK 11 o superior
- Python 3.9+
- Cuenta Firebase configurada
- Hardware ESP32

### Configuración Android

1. Clona el repositorio:
```bash
git clone https://github.com/yourusername/DomoHouse.git
cd DomoHouse
```

2. Abre el proyecto en Android Studio

3. Sincroniza Gradle

4. Firebase ya está configurado con `google-services.json`

5. Ejecuta la aplicación:
```bash
./gradlew installDebug
```

### Testing

```bash
# Tests unitarios
./gradlew test

# Tests instrumentados
./gradlew connectedAndroidTest

# Verificación de código
./gradlew lint
```

## 🎨 Diseño y Branding

La aplicación utiliza una paleta de colores elegante basada en el logo:

- **Primary**: #D4A574 (Dorado/Bronce)
- **Background**: #FFFFFF (Blanco)
- **Text**: #1A1F2E (Azul oscuro)

## 📊 Estado del Proyecto

### ✅ Completado
- Estructura inicial del proyecto
- Configuración de colores y temas
- Integración Firebase básica

### 🚧 En Desarrollo
- Sistema de autenticación
- Dashboard principal
- Control de dispositivos

### 📅 Próximamente
- Backend Python
- Integración ESP32
- Sistema de reportes

## 🧪 Plan de Testing

Cada componente incluye:
- Tests unitarios (>80% cobertura)
- Tests de integración
- Tests de UI con Espresso
- Validación de accesibilidad

## 📖 Documentación

- [Plan de Desarrollo](CLAUDE.md) - Contexto completo para Claude Code
- [Documentación Técnica](docs/technical/) - Detalles de implementación
- [Manual de Usuario](docs/user/) - Guía de uso

## 🤝 Contribución

Este es un proyecto académico. Para contribuir:
1. Fork el proyecto
2. Crea tu rama (`git checkout -b feature/NuevaCaracteristica`)
3. Commit cambios (`git commit -am 'Agregar característica'`)
4. Push a la rama (`git push origin feature/NuevaCaracteristica`)
5. Abre un Pull Request

## 📄 Licencia

Proyecto académico - Universidad/Instituto

## 👥 Equipo

- Desarrollo Android: Jasson Gomez
- Backend Python: [Por asignar]
- Hardware ESP32: [Por asignar]

---

**Última actualización**: 12/6/2025 - v0.3.0 (Configuración inicial)
