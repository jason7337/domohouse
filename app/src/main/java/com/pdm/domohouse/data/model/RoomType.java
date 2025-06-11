package com.pdm.domohouse.data.model;

/**
 * Enumeración que define los tipos de habitaciones disponibles en la casa inteligente
 * Cada tipo tiene características específicas y iconos asociados
 */
public enum RoomType {
    
    // Área de estar principal
    LIVING_ROOM("Sala", "🛋️"),
    
    // Cocina y comedor
    KITCHEN("Cocina", "🍳"),
    DINING_ROOM("Comedor", "🍽️"),
    
    // Dormitorios
    MASTER_BEDROOM("Dormitorio Principal", "🛏️"),
    BEDROOM("Dormitorio", "🛌"),
    GUEST_ROOM("Cuarto de Huéspedes", "🏠"),
    
    // Baños
    BATHROOM("Baño", "🚿"),
    POWDER_ROOM("Medio Baño", "🚽"),
    
    // Áreas de trabajo y estudio
    OFFICE("Oficina", "💼"),
    STUDY("Estudio", "📚"),
    
    // Entretenimiento
    ENTERTAINMENT("Entretenimiento", "📺"),
    GAME_ROOM("Sala de Juegos", "🎮"),
    
    // Utilidades
    GARAGE("Garaje", "🚗"),
    LAUNDRY("Lavandería", "🧺"),
    STORAGE("Bodega", "📦"),
    
    // Exteriores
    PATIO("Patio", "🌿"),
    GARDEN("Jardín", "🌱"),
    BALCONY("Balcón", "🪴"),
    
    // Pasillos y escaleras
    HALLWAY("Pasillo", "🚪"),
    STAIRWAY("Escalera", "🪜"),
    
    // Otros
    OTHER("Otro", "🏢");

    private final String displayName;
    private final String icon;

    /**
     * Constructor del enum
     * @param displayName Nombre para mostrar al usuario
     * @param icon Icono emoji representativo
     */
    RoomType(String displayName, String icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    /**
     * Obtiene el nombre para mostrar al usuario
     * @return nombre legible del tipo de habitación
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Obtiene el icono emoji del tipo de habitación
     * @return emoji representativo
     */
    public String getIcon() {
        return icon;
    }

    /**
     * Obtiene el nombre completo con icono
     * @return string con icono y nombre
     */
    public String getDisplayNameWithIcon() {
        return icon + " " + displayName;
    }

    /**
     * Determina si el tipo de habitación normalmente tiene control de temperatura
     * @return true si típicamente tiene control de temperatura
     */
    public boolean hasTemperatureControl() {
        switch (this) {
            case LIVING_ROOM:
            case KITCHEN:
            case DINING_ROOM:
            case MASTER_BEDROOM:
            case BEDROOM:
            case GUEST_ROOM:
            case OFFICE:
            case STUDY:
            case ENTERTAINMENT:
            case GAME_ROOM:
                return true;
            default:
                return false;
        }
    }

    /**
     * Determina si el tipo de habitación normalmente tiene control de iluminación avanzado
     * @return true si típicamente tiene control de iluminación avanzado
     */
    public boolean hasAdvancedLighting() {
        switch (this) {
            case LIVING_ROOM:
            case DINING_ROOM:
            case MASTER_BEDROOM:
            case ENTERTAINMENT:
            case KITCHEN:
                return true;
            default:
                return false;
        }
    }

    /**
     * Determina si el tipo de habitación normalmente tiene sensores de seguridad
     * @return true si típicamente tiene sensores de seguridad
     */
    public boolean hasSecuritySensors() {
        switch (this) {
            case LIVING_ROOM:
            case MASTER_BEDROOM:
            case OFFICE:
            case GARAGE:
            case PATIO:
            case GARDEN:
                return true;
            default:
                return false;
        }
    }

    /**
     * Obtiene la temperatura recomendada por defecto para el tipo de habitación
     * @return temperatura en grados Celsius
     */
    public float getRecommendedTemperature() {
        switch (this) {
            case BEDROOM:
            case MASTER_BEDROOM:
            case GUEST_ROOM:
                return 20.0f; // Más fresco para dormir
            case BATHROOM:
            case POWDER_ROOM:
                return 24.0f; // Más cálido para comodidad
            case KITCHEN:
                return 21.0f; // Temperatura media para cocinar
            case LIVING_ROOM:
            case DINING_ROOM:
            case ENTERTAINMENT:
                return 22.0f; // Temperatura confortable para estar
            case OFFICE:
            case STUDY:
                return 21.5f; // Temperatura para concentración
            default:
                return 22.0f; // Temperatura estándar
        }
    }
}