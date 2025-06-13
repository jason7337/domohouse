package com.pdm.domohouse.data.sync;

import android.util.Log;

import androidx.annotation.NonNull;

import com.pdm.domohouse.data.database.entity.DeviceEntity;
import com.pdm.domohouse.data.database.entity.RoomEntity;
import com.pdm.domohouse.data.database.entity.UserPreferencesEntity;
import com.pdm.domohouse.data.database.entity.UserProfileEntity;
import com.pdm.domohouse.data.model.Device;
import com.pdm.domohouse.data.model.Room;
import com.pdm.domohouse.data.model.UserPreferences;
import com.pdm.domohouse.data.model.UserProfile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sistema avanzado de detección y resolución de conflictos
 * Maneja conflictos entre datos locales y remotos usando múltiples estrategias
 */
public class ConflictResolver {
    
    private static final String TAG = "ConflictResolver";
    
    // Estrategias de resolución por tipo de dato
    private final Map<String, ConflictStrategy> strategyMap = new HashMap<>();
    
    // Listeners para notificar conflictos
    private final List<ConflictListener> conflictListeners = new ArrayList<>();
    
    /**
     * Tipos de conflicto detectables
     */
    public enum ConflictType {
        TIMESTAMP_CONFLICT,     // Diferentes timestamps de modificación
        VALUE_CONFLICT,         // Valores diferentes para el mismo campo
        STRUCTURAL_CONFLICT,    // Cambios estructurales (eliminación vs modificación)
        PERMISSION_CONFLICT,    // Conflictos de permisos o propiedad
        VERSION_CONFLICT        // Versiones incompatibles
    }
    
    /**
     * Estrategias de resolución disponibles
     */
    public enum ConflictStrategy {
        LOCAL_WINS,             // Los datos locales siempre ganan
        REMOTE_WINS,            // Los datos remotos siempre ganan
        LAST_MODIFIED_WINS,     // Gana el que fue modificado más recientemente
        MERGE_INTELLIGENT,      // Fusión inteligente basada en reglas
        USER_DECIDES,           // Presentar al usuario para decidir
        FIELD_BY_FIELD          // Resolver campo por campo
    }
    
    /**
     * Constructor que configura las estrategias por defecto
     */
    public ConflictResolver() {
        configureDefaultStrategies();
    }
    
    /**
     * Configura las estrategias de resolución por defecto para cada tipo de dato
     */
    private void configureDefaultStrategies() {
        // Perfiles de usuario: última modificación gana, excepto para datos críticos
        strategyMap.put("UserProfile", ConflictStrategy.FIELD_BY_FIELD);
        
        // Preferencias: fusión inteligente (mantener preferencias locales críticas)
        strategyMap.put("UserPreferences", ConflictStrategy.MERGE_INTELLIGENT);
        
        // Habitaciones: última modificación gana (cambios estructurales son importantes)
        strategyMap.put("Room", ConflictStrategy.LAST_MODIFIED_WINS);
        
        // Dispositivos: fusión inteligente (estado vs configuración)
        strategyMap.put("Device", ConflictStrategy.MERGE_INTELLIGENT);
        
        // Historial: remoto gana (el servidor es fuente de verdad para historial)
        strategyMap.put("DeviceHistory", ConflictStrategy.REMOTE_WINS);
    }
    
    /**
     * Detecta conflictos entre perfiles de usuario
     */
    public ConflictResult<UserProfile> detectUserProfileConflict(
            @NonNull UserProfileEntity local, 
            @NonNull UserProfile remote) {
        
        List<FieldConflict> conflicts = new ArrayList<>();
        
        // Verificar timestamp general
        if (local.getUpdatedAt() != remote.getLastSyncTimestamp()) {
            conflicts.add(new FieldConflict("timestamp", 
                String.valueOf(local.getUpdatedAt()), 
                String.valueOf(remote.getLastSyncTimestamp()),
                ConflictType.TIMESTAMP_CONFLICT));
        }
        
        // Verificar campos específicos
        if (!safeEquals(local.getName(), remote.getName())) {
            conflicts.add(new FieldConflict("name", local.getName(), remote.getName(), 
                ConflictType.VALUE_CONFLICT));
        }
        
        if (!safeEquals(local.getEmail(), remote.getEmail())) {
            conflicts.add(new FieldConflict("email", local.getEmail(), remote.getEmail(), 
                ConflictType.VALUE_CONFLICT));
        }
        
        if (!safeEquals(local.getPhotoUrl(), remote.getPhotoUrl())) {
            conflicts.add(new FieldConflict("photoUrl", local.getPhotoUrl(), remote.getPhotoUrl(), 
                ConflictType.VALUE_CONFLICT));
        }
        
        // Verificar PIN (crítico para seguridad)
        if (!safeEquals(local.getPinHash(), remote.getEncryptedPin())) {
            conflicts.add(new FieldConflict("pin", "[PROTECTED]", "[PROTECTED]", 
                ConflictType.PERMISSION_CONFLICT));
        }
        
        if (conflicts.isEmpty()) {
            return new ConflictResult<>(false, null, null, conflicts);
        }
        
        UserProfile resolved = resolveUserProfileConflict(local, remote, conflicts);
        return new ConflictResult<>(true, resolved, strategyMap.get("UserProfile"), conflicts);
    }
    
    /**
     * Resuelve conflictos en perfil de usuario usando estrategia campo por campo
     */
    private UserProfile resolveUserProfileConflict(
            UserProfileEntity local, 
            UserProfile remote, 
            List<FieldConflict> conflicts) {
        
        UserProfile resolved = new UserProfile();
        resolved.setUserId(local.getUserId()); // ID nunca cambia
        
        for (FieldConflict conflict : conflicts) {
            switch (conflict.getFieldName()) {
                case "name":
                    // Para nombre: última modificación gana
                    if (local.getUpdatedAt() > remote.getLastSyncTimestamp()) {
                        resolved.setName(local.getName());
                        Log.d(TAG, "Conflicto nombre resuelto: usando valor local");
                    } else {
                        resolved.setName(remote.getName());
                        Log.d(TAG, "Conflicto nombre resuelto: usando valor remoto");
                    }
                    break;
                    
                case "email":
                    // Email: remoto gana (podría haber cambiado en otro dispositivo)
                    resolved.setEmail(remote.getEmail());
                    Log.d(TAG, "Conflicto email resuelto: usando valor remoto");
                    break;
                    
                case "photoUrl":
                    // Foto: usar la más reciente (no nula)
                    if (remote.getPhotoUrl() != null && !remote.getPhotoUrl().isEmpty()) {
                        resolved.setPhotoUrl(remote.getPhotoUrl());
                    } else {
                        resolved.setPhotoUrl(local.getPhotoUrl());
                    }
                    Log.d(TAG, "Conflicto photoUrl resuelto");
                    break;
                    
                case "pin":
                    // PIN: local gana (crítico para seguridad)
                    resolved.setEncryptedPin(local.getPinHash());
                    Log.d(TAG, "Conflicto PIN resuelto: manteniendo valor local por seguridad");
                    break;
                    
                default:
                    Log.w(TAG, "Campo desconocido en conflicto: " + conflict.getFieldName());
            }
        }
        
        // Actualizar timestamps
        resolved.setLastSyncTimestamp(System.currentTimeMillis());
        
        return resolved;
    }
    
    /**
     * Detecta conflictos entre dispositivos
     */
    public ConflictResult<Device> detectDeviceConflict(
            @NonNull DeviceEntity local, 
            @NonNull Device remote) {
        
        List<FieldConflict> conflicts = new ArrayList<>();
        
        // Verificar estado vs configuración
        if (local.isOn() != remote.isOn()) {
            conflicts.add(new FieldConflict("isOn", 
                String.valueOf(local.isOn()), 
                String.valueOf(remote.isOn()),
                ConflictType.VALUE_CONFLICT));
        }
        
        if (local.getIntensity() != remote.getIntensity()) {
            conflicts.add(new FieldConflict("intensity", 
                String.valueOf(local.getIntensity()), 
                String.valueOf(remote.getIntensity()),
                ConflictType.VALUE_CONFLICT));
        }
        
        if (!safeEquals(local.getName(), remote.getName())) {
            conflicts.add(new FieldConflict("name", local.getName(), remote.getName(), 
                ConflictType.VALUE_CONFLICT));
        }
        
        // Verificar timestamps para determinar qué cambio es más reciente
        if (local.getLastStateChange() != remote.getLastStateChange()) {
            conflicts.add(new FieldConflict("lastStateChange", 
                String.valueOf(local.getLastStateChange()), 
                String.valueOf(remote.getLastStateChange()),
                ConflictType.TIMESTAMP_CONFLICT));
        }
        
        if (conflicts.isEmpty()) {
            return new ConflictResult<>(false, null, null, conflicts);
        }
        
        Device resolved = resolveDeviceConflict(local, remote, conflicts);
        return new ConflictResult<>(true, resolved, strategyMap.get("Device"), conflicts);
    }
    
    /**
     * Resuelve conflictos en dispositivos usando fusión inteligente
     */
    private Device resolveDeviceConflict(
            DeviceEntity local, 
            Device remote, 
            List<FieldConflict> conflicts) {
        
        Device resolved = new Device();
        resolved.setDeviceId(local.getDeviceId());
        resolved.setRoomId(local.getRoomId());
        resolved.setDeviceType(remote.getDeviceType()); // Usar enum del modelo
        
        // Para estados del dispositivo: el más reciente gana
        boolean useLocalState = local.getLastStateChange() > remote.getLastStateChange();
        
        for (FieldConflict conflict : conflicts) {
            switch (conflict.getFieldName()) {
                case "isOn":
                case "intensity":
                    if (useLocalState) {
                        resolved.setOn(local.isOn());
                        resolved.setIntensity(local.getIntensity());
                        Log.d(TAG, "Conflicto estado resuelto: usando estado local más reciente");
                    } else {
                        resolved.setOn(remote.isOn());
                        resolved.setIntensity(remote.getIntensity());
                        Log.d(TAG, "Conflicto estado resuelto: usando estado remoto más reciente");
                    }
                    break;
                    
                case "name":
                    // Para nombre: última modificación gana
                    if (local.getUpdatedAt() > remote.getUpdatedAt()) {
                        resolved.setName(local.getName());
                    } else {
                        resolved.setName(remote.getName());
                    }
                    break;
            }
        }
        
        // Actualizar timestamps
        resolved.setLastStateChange(Math.max(local.getLastStateChange(), remote.getLastStateChange()));
        resolved.setUpdatedAt(System.currentTimeMillis());
        
        return resolved;
    }
    
    /**
     * Detecta conflictos entre preferencias
     */
    public ConflictResult<UserPreferences> detectPreferencesConflict(
            @NonNull UserPreferencesEntity local, 
            @NonNull UserPreferences remote) {
        
        List<FieldConflict> conflicts = new ArrayList<>();
        
        // Verificar configuraciones importantes
        if (local.isNotificationsEnabled() != remote.isNotificationsEnabled()) {
            conflicts.add(new FieldConflict("notifications", 
                String.valueOf(local.isNotificationsEnabled()), 
                String.valueOf(remote.isNotificationsEnabled()),
                ConflictType.VALUE_CONFLICT));
        }
        
        if (!safeEquals(local.getLanguage(), remote.getLanguage())) {
            conflicts.add(new FieldConflict("language", local.getLanguage(), remote.getLanguage(), 
                ConflictType.VALUE_CONFLICT));
        }
        
        if (local.isEcoMode() != remote.isEcoMode()) {
            conflicts.add(new FieldConflict("ecoMode", 
                String.valueOf(local.isEcoMode()), 
                String.valueOf(remote.isEcoMode()),
                ConflictType.VALUE_CONFLICT));
        }
        
        if (conflicts.isEmpty()) {
            return new ConflictResult<>(false, null, null, conflicts);
        }
        
        UserPreferences resolved = resolvePreferencesConflict(local, remote, conflicts);
        return new ConflictResult<>(true, resolved, strategyMap.get("UserPreferences"), conflicts);
    }
    
    /**
     * Resuelve conflictos en preferencias usando fusión inteligente
     */
    private UserPreferences resolvePreferencesConflict(
            UserPreferencesEntity local, 
            UserPreferences remote, 
            List<FieldConflict> conflicts) {
        
        UserPreferences resolved = new UserPreferences();
        resolved.setUserId(local.getUserId());
        
        // Para preferencias: mantener configuraciones locales críticas
        // pero sincronizar configuraciones generales
        
        for (FieldConflict conflict : conflicts) {
            switch (conflict.getFieldName()) {
                case "notifications":
                    // Notificaciones: local gana (configuración crítica del dispositivo)
                    resolved.setNotificationsEnabled(local.isNotificationsEnabled());
                    Log.d(TAG, "Conflicto notificaciones resuelto: manteniendo configuración local");
                    break;
                    
                case "language":
                    // Idioma: remoto gana (podría haberse cambiado en otro dispositivo)
                    resolved.setLanguage(remote.getLanguage());
                    Log.d(TAG, "Conflicto idioma resuelto: usando configuración remota");
                    break;
                    
                case "ecoMode":
                    // Modo eco: última modificación gana
                    if (local.getUpdatedAt() > remote.getLastSync()) {
                        resolved.setEcoMode(local.isEcoMode());
                    } else {
                        resolved.setEcoMode(remote.isEcoMode());
                    }
                    Log.d(TAG, "Conflicto modo eco resuelto");
                    break;
            }
        }
        
        return resolved;
    }
    
    /**
     * Registra un listener para notificaciones de conflictos
     */
    public void addConflictListener(ConflictListener listener) {
        if (!conflictListeners.contains(listener)) {
            conflictListeners.add(listener);
        }
    }
    
    /**
     * Remueve un listener de conflictos
     */
    public void removeConflictListener(ConflictListener listener) {
        conflictListeners.remove(listener);
    }
    
    /**
     * Notifica a los listeners sobre un conflicto detectado
     */
    private void notifyConflictDetected(String dataType, List<FieldConflict> conflicts) {
        for (ConflictListener listener : conflictListeners) {
            listener.onConflictDetected(dataType, conflicts);
        }
    }
    
    /**
     * Configura una estrategia específica para un tipo de dato
     */
    public void setStrategy(String dataType, ConflictStrategy strategy) {
        strategyMap.put(dataType, strategy);
        Log.d(TAG, "Estrategia configurada para " + dataType + ": " + strategy);
    }
    
    /**
     * Obtiene la estrategia configurada para un tipo de dato
     */
    public ConflictStrategy getStrategy(String dataType) {
        return strategyMap.getOrDefault(dataType, ConflictStrategy.LAST_MODIFIED_WINS);
    }
    
    /**
     * Método auxiliar para comparación segura de strings
     */
    private boolean safeEquals(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }
    
    // Clases auxiliares
    
    /**
     * Resultado de detección de conflictos
     */
    public static class ConflictResult<T> {
        private final boolean hasConflict;
        private final T resolvedData;
        private final ConflictStrategy usedStrategy;
        private final List<FieldConflict> conflicts;
        
        public ConflictResult(boolean hasConflict, T resolvedData, 
                ConflictStrategy usedStrategy, List<FieldConflict> conflicts) {
            this.hasConflict = hasConflict;
            this.resolvedData = resolvedData;
            this.usedStrategy = usedStrategy;
            this.conflicts = conflicts;
        }
        
        public boolean hasConflict() { return hasConflict; }
        public T getResolvedData() { return resolvedData; }
        public ConflictStrategy getUsedStrategy() { return usedStrategy; }
        public List<FieldConflict> getConflicts() { return conflicts; }
    }
    
    /**
     * Representa un conflicto en un campo específico
     */
    public static class FieldConflict {
        private final String fieldName;
        private final String localValue;
        private final String remoteValue;
        private final ConflictType conflictType;
        
        public FieldConflict(String fieldName, String localValue, 
                String remoteValue, ConflictType conflictType) {
            this.fieldName = fieldName;
            this.localValue = localValue;
            this.remoteValue = remoteValue;
            this.conflictType = conflictType;
        }
        
        public String getFieldName() { return fieldName; }
        public String getLocalValue() { return localValue; }
        public String getRemoteValue() { return remoteValue; }
        public ConflictType getConflictType() { return conflictType; }
    }
    
    /**
     * Interface para listeners de conflictos
     */
    public interface ConflictListener {
        void onConflictDetected(String dataType, List<FieldConflict> conflicts);
        void onConflictResolved(String dataType, ConflictStrategy strategy);
    }
}