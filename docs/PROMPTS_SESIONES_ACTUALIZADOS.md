# 🎭 PROMPTS PARA PRÓXIMAS SESIONES - DOMOHOUSE
## Solo Sesiones Pendientes (NO Completadas)

### 🚨 **PRÓXIMAS 4 SESIONES CRÍTICAS** (Ejecutar en este orden)

#### **SESIÓN 5A - Firebase Realtime Database + Perfil** ← **EMPEZAR AQUÍ**
```
Hola Claude, vamos a trabajar en la Sesión 5A de DomoHouse. Esta es una sesión CRÍTICA que debe implementar:

1. **Firebase Realtime Database**: Configurar y conectar Firebase Realtime Database al proyecto (actualmente solo usa Authentication)

2. **Modelo UserProfile completo**: Crear un modelo de usuario que incluya:
   - Nombre completo
   - Email
   - URL de foto de perfil
   - PIN cifrado (respaldo en la nube)
   - Fecha de último login
   - Configuraciones personales

3. **Modificar registro**: Actualizar RegisterFragment para:
   - Solicitar foto de perfil (galería/cámara)
   - Guardar toda la información en Firebase Realtime Database
   - Subir foto a Firebase Storage
   - Mantener respaldo cifrado del PIN

4. **Sincronización**: Implementar sincronización bidireccional entre local y Firebase

5. **Tests**: Crear tests unitarios para toda la funcionalidad de Firebase

Revisar el archivo CLAUDE.md para contexto completo del proyecto. Esta sesión es fundamental para el resto del proyecto ya que establece la base de datos y perfil de usuario que todo lo demás necesita.
```

#### **SESIÓN 5B - Pantalla de Perfil + Navegación**
```
Hola Claude, continuamos con la Sesión 5B de DomoHouse. Necesitamos implementar:

1. **ProfileFragment**: Nueva pantalla completa de perfil que permita:
   - Ver información personal
   - Editar nombre y foto
   - Cambiar PIN de acceso
   - Configurar preferencias
   - Cerrar sesión

2. **BottomNavigationView**: Implementar navegación inferior con:
   - Home (Dashboard)
   - Dispositivos
   - Perfil
   - Configuración

3. **Menú contextual**: Agregar menú en HomeFragment con acceso directo a:
   - Perfil
   - Configuración
   - Cerrar sesión

4. **Navegación mejorada**: Actualizar nav_graph.xml y MainActivity para soportar la nueva navegación

5. **UX mejorada**: Animaciones y transiciones elegantes entre pantallas

La navegación debe ser intuitiva y seguir las mejores prácticas de Material Design.
```

#### **SESIÓN 5C - Sistema Offline/Online Completo**
```
Hola Claude, vamos con la Sesión 5C de DomoHouse - la más técnica. Implementar:

1. **Room Database completa**: Crear todas las entidades necesarias:
   - UserProfile
   - Device
   - Room
   - DeviceHistory
   - UserPreferences

2. **SyncManager**: Clase central para manejar:
   - Sincronización automática cuando hay internet
   - Detección de conflictos
   - Resolución de conflictos
   - Sincronización manual

3. **Login offline**: Hacer funcional el login con PIN sin internet (después de primera sincronización)

4. **Cache inteligente**: Implementar cache local para:
   - Estados de dispositivos
   - Configuraciones
   - Historial reciente

5. **Tests exhaustivos**: Probar todos los escenarios offline/online

Esta sesión garantiza que la app funcione sin internet después del primer uso.
```

#### **SESIÓN 5D - Gestión de Dispositivos**
```
Hola Claude, finalizamos las sesiones críticas con 5D - Gestión de Dispositivos:

1. **DevicesFragment**: Nueva pantalla para gestionar dispositivos:
   - Lista de dispositivos por habitación
   - Agregar nuevos dispositivos
   - Configurar dispositivos existentes
   - Eliminar/editar dispositivos

2. **Wizard de configuración**: Flujo completo para agregar dispositivos:
   - Seleccionar tipo de dispositivo
   - Configurar nombre y habitación
   - Establecer configuraciones específicas
   - Prueba de conexión

3. **Sincronización bidireccional**: Los cambios en dispositivos deben:
   - Guardarse localmente inmediatamente
   - Sincronizarse con Firebase cuando hay internet
   - Resolverse conflictos automáticamente

4. **Integración con dashboard**: Los nuevos dispositivos deben aparecer automáticamente en:
   - HouseMapView
   - Estadísticas del dashboard
   - Controles específicos

Con esta sesión tendremos una base sólida para todas las funcionalidades IoT.
```

---

### 📝 **DESPUÉS DE LAS 4 CRÍTICAS, CONTINUAR CON:**

#### **SESIÓN 6 - Control de Iluminación**
```
Hola Claude, ahora sí vamos con la Sesión 6 - Control de Iluminación:

1. **LightsFragment**: Pantalla principal de control de luces
2. **Control por habitaciones**: Vista organizada por habitaciones
3. **Switches personalizados**: Controles elegantes para encender/apagar
4. **Control de intensidad**: Seekbars para dimmer
5. **Integración con dispositivos**: Conectar con los dispositivos reales configurados en 5D

Ya tenemos la base sólida de Firebase + Perfil + Offline + Dispositivos, ahora podemos enfocarnos en la funcionalidad específica.
```

#### **SESIÓN 7 - Sistema de Temperatura**
```
Hola Claude, continuamos con la Sesión 7 - Temperatura:

1. **TemperatureFragment**: Dashboard de temperaturas por habitación
2. **Gráficos**: Implementar MPAndroidChart para visualizar historiales
3. **Control de ventiladores**: Interfaces para controlar ventilación
4. **Umbrales automáticos**: Configuración de temperaturas automáticas
5. **Alertas**: Sistema de notificaciones por temperatura

Usar los dispositivos ya configurados en la sesión 5D.
```

#### **SESIÓN 8 - Seguridad**
```
Hola Claude, vamos con la Sesión 8 - Sistema de Seguridad:

1. **SecurityFragment**: Panel de control de seguridad
2. **Estados de accesos**: Visualización de puertas/ventanas
3. **Sensores de humo/gas**: Alertas y estados
4. **Cerradura inteligente**: Control de acceso principal
5. **Historial de eventos**: Log de todos los eventos de seguridad

Integrar con el sistema de dispositivos y notificaciones.
```

#### **SESIÓN 9 - Programación Horaria**
```
Hola Claude, vamos con la Sesión 9 - Programación Horaria:

1. **Calendario de programación**: Interface para programar dispositivos
2. **Selector de dispositivos**: Elegir qué dispositivos programar
3. **Configuración de reglas**: Horarios y condiciones
4. **Vista de programaciones activas**: Dashboard de automaciones
5. **Tests**: Verificar sistema de scheduler

Crear un sistema completo de automatización por horarios.
```

---

### 🎯 **INSTRUCCIONES DE USO**

1. **Copia exactamente** el prompt de la sesión que vas a hacer
2. **Pégalo en tu nueva conversación** con Claude
3. **Ejecuta la sesión completa**
4. **Al terminar, borra ese prompt de esta lista**
5. **Continúa con el siguiente prompt**

### ✅ **ORDEN OBLIGATORIO**
**5A → 5B → 5C → 5D** (Las 4 críticas DEBEN hacerse primero)
Después: 6 → 7 → 8 → 9 → etc.

---

**Última actualización**: 12/6/2025  
**Próximo prompt a usar**: SESIÓN 5A (el primero de la lista)