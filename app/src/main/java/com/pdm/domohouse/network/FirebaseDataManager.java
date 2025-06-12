package com.pdm.domohouse.network;

import android.net.Uri;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import com.pdm.domohouse.data.model.UserProfile;

import java.util.UUID;

/**
 * Manager para manejar Firebase Realtime Database y Storage
 * Proporciona métodos para operaciones CRUD de perfiles de usuario y subida de archivos
 */
public class FirebaseDataManager {
    
    private static final String TAG = "FirebaseDataManager";
    private static final String USERS_NODE = "users";
    private static final String PROFILE_PHOTOS_PATH = "profile_photos";
    
    private static FirebaseDataManager instance;
    private final DatabaseReference database;
    private final StorageReference storage;
    
    /**
     * Constructor privado para Singleton
     */
    private FirebaseDataManager() {
        database = FirebaseDatabase.getInstance().getReference();
        storage = FirebaseStorage.getInstance().getReference();
    }
    
    /**
     * Obtiene la instancia única del manager
     * @return La instancia del FirebaseDataManager
     */
    public static synchronized FirebaseDataManager getInstance() {
        if (instance == null) {
            instance = new FirebaseDataManager();
        }
        return instance;
    }
    
    /**
     * Guarda el perfil de usuario en Firebase Realtime Database
     * @param userProfile El perfil del usuario a guardar
     * @param callback Callback para manejar el resultado
     */
    public void saveUserProfile(UserProfile userProfile, DatabaseCallback callback) {
        if (userProfile == null || !userProfile.isValid()) {
            callback.onError("Perfil de usuario inválido");
            return;
        }
        
        // Actualizar timestamp de sincronización
        userProfile.updateLastSync();
        
        DatabaseReference userRef = database.child(USERS_NODE).child(userProfile.getUserId());
        
        userRef.setValue(userProfile.toMap())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Perfil guardado exitosamente: " + userProfile.getUserId());
                        callback.onSuccess();
                    } else {
                        String error = task.getException() != null ? 
                                task.getException().getMessage() : "Error al guardar perfil";
                        Log.e(TAG, "Error al guardar perfil: " + error);
                        callback.onError(error);
                    }
                });
    }
    
    /**
     * Obtiene el perfil de usuario desde Firebase Realtime Database
     * @param userId ID del usuario
     * @param callback Callback para manejar el resultado
     */
    public void getUserProfile(String userId, UserProfileCallback callback) {
        if (userId == null || userId.trim().isEmpty()) {
            callback.onError("ID de usuario inválido");
            return;
        }
        
        DatabaseReference userRef = database.child(USERS_NODE).child(userId);
        
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    try {
                        UserProfile userProfile = dataSnapshot.getValue(UserProfile.class);
                        if (userProfile != null) {
                            Log.d(TAG, "Perfil obtenido exitosamente: " + userId);
                            callback.onSuccess(userProfile);
                        } else {
                            Log.w(TAG, "Perfil está vacío para el usuario: " + userId);
                            callback.onError("Perfil de usuario vacío");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error al deserializar perfil: " + e.getMessage());
                        callback.onError("Error al procesar datos del perfil");
                    }
                } else {
                    Log.w(TAG, "Perfil no encontrado para el usuario: " + userId);
                    callback.onError("Perfil de usuario no encontrado");
                }
            }
            
            @Override
            public void onCancelled(DatabaseError databaseError) {
                String error = "Error de base de datos: " + databaseError.getMessage();
                Log.e(TAG, error);
                callback.onError(error);
            }
        });
    }
    
    /**
     * Actualiza campos específicos del perfil de usuario
     * @param userId ID del usuario
     * @param updates Map con los campos a actualizar
     * @param callback Callback para manejar el resultado
     */
    public void updateUserProfile(String userId, java.util.Map<String, Object> updates, DatabaseCallback callback) {
        if (userId == null || userId.trim().isEmpty()) {
            callback.onError("ID de usuario inválido");
            return;
        }
        
        if (updates == null || updates.isEmpty()) {
            callback.onError("No hay actualizaciones para aplicar");
            return;
        }
        
        // Agregar timestamp de sincronización a las actualizaciones
        updates.put("lastSyncTimestamp", System.currentTimeMillis());
        
        DatabaseReference userRef = database.child(USERS_NODE).child(userId);
        
        userRef.updateChildren(updates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Perfil actualizado exitosamente: " + userId);
                        callback.onSuccess();
                    } else {
                        String error = task.getException() != null ? 
                                task.getException().getMessage() : "Error al actualizar perfil";
                        Log.e(TAG, "Error al actualizar perfil: " + error);
                        callback.onError(error);
                    }
                });
    }
    
    /**
     * Elimina el perfil de usuario de Firebase
     * @param userId ID del usuario
     * @param callback Callback para manejar el resultado
     */
    public void deleteUserProfile(String userId, DatabaseCallback callback) {
        if (userId == null || userId.trim().isEmpty()) {
            callback.onError("ID de usuario inválido");
            return;
        }
        
        DatabaseReference userRef = database.child(USERS_NODE).child(userId);
        
        userRef.removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Perfil eliminado exitosamente: " + userId);
                        callback.onSuccess();
                    } else {
                        String error = task.getException() != null ? 
                                task.getException().getMessage() : "Error al eliminar perfil";
                        Log.e(TAG, "Error al eliminar perfil: " + error);
                        callback.onError(error);
                    }
                });
    }
    
    /**
     * Sube una foto de perfil a Firebase Storage
     * @param userId ID del usuario
     * @param imageUri URI de la imagen a subir
     * @param callback Callback para manejar el resultado
     */
    public void uploadProfilePhoto(String userId, Uri imageUri, PhotoUploadCallback callback) {
        if (userId == null || userId.trim().isEmpty()) {
            callback.onError("ID de usuario inválido");
            return;
        }
        
        if (imageUri == null) {
            callback.onError("URI de imagen inválida");
            return;
        }
        
        // Generar nombre único para la imagen
        String filename = userId + "_" + UUID.randomUUID().toString() + ".jpg";
        StorageReference photoRef = storage.child(PROFILE_PHOTOS_PATH).child(filename);
        
        // Comenzar la subida
        UploadTask uploadTask = photoRef.putFile(imageUri);
        
        uploadTask.addOnProgressListener(taskSnapshot -> {
            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
            callback.onProgress((int) progress);
        });
        
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            // Obtener la URL de descarga
            photoRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                String downloadUrl = downloadUri.toString();
                Log.d(TAG, "Foto subida exitosamente: " + downloadUrl);
                callback.onSuccess(downloadUrl);
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Error al obtener URL de descarga: " + e.getMessage());
                callback.onError("Error al obtener URL de descarga");
            });
        });
        
        uploadTask.addOnFailureListener(e -> {
            String error = "Error al subir foto: " + e.getMessage();
            Log.e(TAG, error);
            callback.onError(error);
        });
    }
    
    /**
     * Elimina una foto de perfil de Firebase Storage
     * @param photoUrl URL de la foto a eliminar
     * @param callback Callback para manejar el resultado
     */
    public void deleteProfilePhoto(String photoUrl, DatabaseCallback callback) {
        if (photoUrl == null || photoUrl.trim().isEmpty()) {
            callback.onError("URL de foto inválida");
            return;
        }
        
        StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(photoUrl);
        
        photoRef.delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Foto eliminada exitosamente");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    String error = "Error al eliminar foto: " + e.getMessage();
                    Log.e(TAG, error);
                    callback.onError(error);
                });
    }
    
    /**
     * Escucha cambios en tiempo real del perfil de usuario
     * @param userId ID del usuario
     * @param listener Listener para los cambios
     * @return ValueEventListener para poder removerlo después
     */
    public ValueEventListener listenToUserProfile(String userId, UserProfileListener listener) {
        DatabaseReference userRef = database.child(USERS_NODE).child(userId);
        
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    try {
                        UserProfile userProfile = dataSnapshot.getValue(UserProfile.class);
                        if (userProfile != null) {
                            listener.onUserProfileChanged(userProfile);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error al deserializar perfil en tiempo real: " + e.getMessage());
                        listener.onError("Error al procesar cambios del perfil");
                    }
                } else {
                    listener.onError("Perfil no encontrado");
                }
            }
            
            @Override
            public void onCancelled(DatabaseError databaseError) {
                listener.onError("Error de base de datos: " + databaseError.getMessage());
            }
        };
        
        userRef.addValueEventListener(valueEventListener);
        return valueEventListener;
    }
    
    /**
     * Remueve un listener de cambios en tiempo real
     * @param userId ID del usuario
     * @param listener Listener a remover
     */
    public void removeUserProfileListener(String userId, ValueEventListener listener) {
        database.child(USERS_NODE).child(userId).removeEventListener(listener);
    }
    
    // Interfaces para callbacks
    
    /**
     * Callback para operaciones básicas de base de datos
     */
    public interface DatabaseCallback {
        void onSuccess();
        void onError(String error);
    }
    
    /**
     * Callback para obtener perfiles de usuario
     */
    public interface UserProfileCallback {
        void onSuccess(UserProfile userProfile);
        void onError(String error);
    }
    
    /**
     * Callback para subida de fotos
     */
    public interface PhotoUploadCallback {
        void onSuccess(String downloadUrl);
        void onProgress(int progress);
        void onError(String error);
    }
    
    /**
     * Listener para cambios en tiempo real del perfil
     */
    public interface UserProfileListener {
        void onUserProfileChanged(UserProfile userProfile);
        void onError(String error);
    }
}