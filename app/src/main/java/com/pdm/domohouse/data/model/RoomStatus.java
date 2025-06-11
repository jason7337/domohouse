package com.pdm.domohouse.data.model;

/**
 * Enumeración que define los estados posibles de una habitación
 * Utilizado para mostrar indicadores visuales y determinar colores en la UI
 */
public enum RoomStatus {
    
    // Estado normal - todo funcionando correctamente
    NORMAL("Normal", "#4CAF50"),
    
    // Estado activo - dispositivos en uso
    ACTIVE("Activo", "#D4A574"),
    
    // Estado de advertencia - parámetros fuera del rango ideal
    WARNING("Advertencia", "#FF9800"),
    
    // Estado de alerta - problema detectado
    ALERT("Alerta", "#F44336"),
    
    // Estado desconectado - sin comunicación
    OFFLINE("Desconectado", "#666666"),
    
    // Estado desconocido - error en la lectura
    UNKNOWN("Desconocido", "#9E9E9E");

    private final String displayName;
    private final String colorHex;

    /**
     * Constructor del enum
     * @param displayName Nombre para mostrar al usuario
     * @param colorHex Color hexadecimal asociado al estado
     */
    RoomStatus(String displayName, String colorHex) {
        this.displayName = displayName;
        this.colorHex = colorHex;
    }

    /**
     * Obtiene el nombre para mostrar al usuario
     * @return nombre legible del estado
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Obtiene el color hexadecimal del estado
     * @return color en formato hex (#RRGGBB)
     */
    public String getColorHex() {
        return colorHex;
    }

    /**
     * Convierte el color hex a entero para uso en Android
     * @return color como entero
     */
    public int getColorInt() {
        return android.graphics.Color.parseColor(colorHex);
    }

    /**
     * Determina si el estado requiere atención del usuario
     * @return true si requiere atención
     */
    public boolean requiresAttention() {
        return this == WARNING || this == ALERT || this == OFFLINE;
    }

    /**
     * Determina si el estado indica un problema crítico
     * @return true si es crítico
     */
    public boolean isCritical() {
        return this == ALERT || this == OFFLINE;
    }

    /**
     * Obtiene la prioridad del estado para ordenamiento
     * @return prioridad (menor número = mayor prioridad)
     */
    public int getPriority() {
        switch (this) {
            case ALERT:
                return 1;
            case WARNING:
                return 2;
            case OFFLINE:
                return 3;
            case ACTIVE:
                return 4;
            case NORMAL:
                return 5;
            case UNKNOWN:
                return 6;
            default:
                return 7;
        }
    }

    /**
     * Obtiene un icono emoji representativo del estado
     * @return emoji del estado
     */
    public String getIcon() {
        switch (this) {
            case NORMAL:
                return "✅";
            case ACTIVE:
                return "🟡";
            case WARNING:
                return "⚠️";
            case ALERT:
                return "🚨";
            case OFFLINE:
                return "❌";
            case UNKNOWN:
                return "❓";
            default:
                return "⚪";
        }
    }

    /**
     * Obtiene una descripción del estado
     * @return descripción del estado
     */
    public String getDescription() {
        switch (this) {
            case NORMAL:
                return "Funcionando correctamente";
            case ACTIVE:
                return "Dispositivos en uso";
            case WARNING:
                return "Parámetros fuera del rango ideal";
            case ALERT:
                return "Problema detectado - requiere atención";
            case OFFLINE:
                return "Sin comunicación con dispositivos";
            case UNKNOWN:
                return "Estado no disponible";
            default:
                return "Estado desconocido";
        }
    }
}