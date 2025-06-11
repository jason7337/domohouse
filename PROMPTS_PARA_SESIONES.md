# 🚀 PROMPTS PARA CADA SESIÓN - DOMO HOUSE

## 📋 CÓMO USAR ESTE DOCUMENTO

Este documento contiene los prompts exactos que debes usar al iniciar cada sesión con Claude Code. Simplemente copia y pega el prompt correspondiente.

---

## 🎯 PROMPT MAESTRO (USAR SIEMPRE AL INICIO)

```
Continúa el desarrollo de DOMO HOUSE. Lee CLAUDE.md para contexto completo y revisa el último informe de sesión en /docs/sessions/. Actualiza el plan si es necesario basándote en el progreso real. TODO el código debe estar comentado en ESPAÑOL. Ejecuta tests al finalizar y genera informe de sesión.
```

---

## 📱 PROMPTS POR SESIÓN

### SESIÓN 2 - Arquitectura y Navegación
```
Implementa la SESIÓN 2 según CLAUDE.md: Arquitectura MVVM y Navigation Component. Crea Splash Screen con el logo, configura navegación básica y pantalla Login (preparar para Firebase+PIN). Comenta TODO en español. Ejecuta tests y genera informe SESSION_02_REPORT.md
```

### SESIÓN 3 - Autenticación
```
Implementa la SESIÓN 3: Sistema de autenticación completo. Firebase Auth + PIN local con SharedPreferences cifradas. Incluye registro y recuperación de contraseña. Código comentado en español. Tests de autenticación y genera informe.
```

### SESIÓN 4 - Dashboard Principal
```
Implementa la SESIÓN 4: Dashboard con vista de maqueta de casa. Usa Custom View para mostrar habitaciones y estados de dispositivos. Aplica la paleta de colores elegante. Comentarios en español. Tests y genera informe.
```

### SESIÓN 5 - Control de Iluminación
```
Implementa la SESIÓN 5: Sistema de control de luces por habitación. Switches personalizados, control intensidad, mock del backend. Mantén diseño elegante. Código en español. Tests UI y genera informe.
```

### SESIÓN 6 - Sistema de Temperatura
```
Implementa la SESIÓN 6: Control de temperatura y ventiladores. Visualización por habitación, umbrales automáticos, gráficos con MPAndroidChart. Comentarios en español. Tests de automatización y genera informe.
```

### SESIÓN 7 - Seguridad
```
Implementa la SESIÓN 7: Pantalla de seguridad completa. Estados puertas/ventanas, alertas humo/gases, control cerradura. Diseño intuitivo. Código en español. Tests del sistema de alertas y genera informe.
```

### SESIÓN 8 - Programación Horaria
```
Implementa la SESIÓN 8: Sistema de programación horaria. Calendario, selector dispositivos, reglas de automatización. Interfaz elegante. Comentarios en español. Tests del scheduler y genera informe.
```

### SESIÓN 9 - Base de Datos Local
```
Implementa la SESIÓN 9: SQLite con Room. Esquema completo, DAOs, Repositories, sincronización offline/online. Arquitectura limpia. Código en español. Tests de persistencia y genera informe.
```

### SESIÓN 10 - Historial y Eventos
```
Implementa la SESIÓN 10: Sistema de historial completo. Filtros, búsqueda, exportación, sincronización Firebase. UI elegante. Comentarios en español. Tests de queries y genera informe.
```

### SESIÓN 11 - Reportes y Analytics
```
Implementa la SESIÓN 11: Sistema de reportes con gráficas y PDFs. Estadísticas de uso, compartir reportes. Diseño profesional. Código en español. Tests de generación y genera informe.
```

### SESIÓN 12 - Backend Python Base
```
Inicia backend Python SESIÓN 12: Setup Flask/FastAPI, modelos, endpoints CRUD, Firebase Admin SDK. Código comentado en español. Tests de API y genera informe.
```

### SESIÓN 13 - Backend Hardware
```
Implementa SESIÓN 13: Backend para hardware. Comunicación ESP32, procesamiento sensores, automatización, colas de comandos. Comentarios en español. Tests de comunicación y genera informe.
```

### SESIÓN 14 - ESP32 Sensores
```
Implementa SESIÓN 14: Código ESP32 para sensores. DHT22 temperatura, MQ-2 humo, magnéticos puertas. Documentación en español. Tests de precisión y genera informe.
```

### SESIÓN 15 - ESP32 Actuadores
```
Implementa SESIÓN 15: Control actuadores ESP32. Relés luces, PWM ventiladores, servo cerradura, teclado matricial. Código en español. Tests de control y genera informe.
```

### SESIÓN 16 - Integración Completa
```
Ejecuta SESIÓN 16: Integración Android+Backend+ESP32. Pruebas end-to-end, optimización, corrección bugs. Verifica que todo funcione. Tests completos y genera informe.
```

### SESIÓN 17 - Accesibilidad
```
Implementa SESIÓN 17: Accesibilidad completa. TalkBack, fuentes ajustables, alto contraste, navegación teclado. Cumple WCAG. Código en español. Tests accesibilidad y genera informe.
```

### SESIÓN 18 - Testing Final
```
Ejecuta SESIÓN 18: Suite completa de tests. Unitarios, integración, UI con Espresso, carga. Alcanza >80% cobertura. Documenta en español. Genera informe completo de testing.
```

### SESIÓN 19 - Documentación
```
Completa SESIÓN 19: Documentación final. Manual usuario, API docs, guía hardware, videos. Todo en español. Actualiza README y CLAUDE.md. Genera informe final.
```

### SESIÓN 20 - Despliegue
```
Finaliza SESIÓN 20: Build release APK, deploy backend, configuración Firebase producción. Prepara entrega final. Verifica todo funcione. Genera informe de cierre.
```

---

## 🔄 PROMPTS DE CONTINUACIÓN

### Si una sesión no se completó:
```
Continúa con la sesión anterior que quedó incompleta. Revisa el último informe y completa las tareas pendientes. Mantén código comentado en español. Ejecuta tests y actualiza el informe.
```

### Para revisar progreso general:
```
Genera un reporte de progreso general del proyecto DOMO HOUSE. Revisa todos los informes de sesión, actualiza CLAUDE.md con el estado actual y sugiere ajustes al plan si es necesario.
```

### Para corregir bugs entre sesiones:
```
Revisa y corrige los bugs reportados en el último informe de sesión. Mantén la arquitectura y diseño elegante. Código en español. Ejecuta tests de regresión y documenta las correcciones.
```

---

## 💡 TIPS IMPORTANTES

1. **SIEMPRE** menciona que el código debe estar en español
2. **SIEMPRE** pide que ejecute tests al final
3. **SIEMPRE** pide que genere informe de sesión
4. Claude Code leerá automáticamente CLAUDE.md al inicio
5. Los informes anteriores le dan contexto del progreso
6. El plan se puede ajustar según avance real

---

## 🎯 EJEMPLO DE USO

```
# Al iniciar Sesión 2:
"Continúa el desarrollo de DOMO HOUSE. Lee CLAUDE.md para contexto completo y revisa el último informe de sesión en /docs/sessions/. Implementa la SESIÓN 2 según CLAUDE.md: Arquitectura MVVM y Navigation Component. Crea Splash Screen con el logo, configura navegación básica y pantalla Login (preparar para Firebase+PIN). Comenta TODO en español. Ejecuta tests y genera informe SESSION_02_REPORT.md"
```

---

**IMPORTANTE**: Este documento es tu guía maestra. Claude Code está preparado para seguir estas instrucciones y mantener consistencia entre sesiones.