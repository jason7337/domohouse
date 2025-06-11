package com.pdm.domohouse.network;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Clase para manejar la autenticación con Firebase
 * Proporciona métodos para login, registro y recuperación de contraseña
 */
public class FirebaseAuthManager {
    
    private static FirebaseAuthManager instance;
    private final FirebaseAuth firebaseAuth;
    
    /**
     * Constructor privado para Singleton
     */
    private FirebaseAuthManager() {
        firebaseAuth = FirebaseAuth.getInstance();
    }
    
    /**
     * Obtiene la instancia única del manager
     * @return La instancia del FirebaseAuthManager
     */
    public static synchronized FirebaseAuthManager getInstance() {
        if (instance == null) {
            instance = new FirebaseAuthManager();
        }
        return instance;
    }
    
    /**
     * Inicia sesión con email y contraseña
     * @param email Email del usuario
     * @param password Contraseña del usuario
     * @param callback Callback para manejar el resultado
     */
    public void signInWithEmailAndPassword(String email, String password, AuthCallback callback) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        callback.onSuccess(user);
                    } else {
                        String error = task.getException() != null ? 
                                task.getException().getMessage() : "Error de autenticación";
                        callback.onError(error);
                    }
                });
    }
    
    /**
     * Registra un nuevo usuario con email y contraseña
     * @param email Email del usuario
     * @param password Contraseña del usuario
     * @param callback Callback para manejar el resultado
     */
    public void createUserWithEmailAndPassword(String email, String password, AuthCallback callback) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        callback.onSuccess(user);
                    } else {
                        String error = task.getException() != null ? 
                                task.getException().getMessage() : "Error al crear usuario";
                        callback.onError(error);
                    }
                });
    }
    
    /**
     * Envía un email para restablecer la contraseña
     * @param email Email del usuario
     * @param callback Callback para manejar el resultado
     */
    public void sendPasswordResetEmail(String email, ResetPasswordCallback callback) {
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess();
                    } else {
                        String error = task.getException() != null ? 
                                task.getException().getMessage() : "Error al enviar email de recuperación";
                        callback.onError(error);
                    }
                });
    }
    
    /**
     * Cierra la sesión del usuario actual
     */
    public void signOut() {
        firebaseAuth.signOut();
    }
    
    /**
     * Obtiene el usuario actualmente autenticado
     * @return El FirebaseUser actual o null si no hay usuario autenticado
     */
    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }
    
    /**
     * Verifica si hay un usuario autenticado
     * @return true si hay un usuario autenticado, false en caso contrario
     */
    public boolean isUserSignedIn() {
        return getCurrentUser() != null;
    }
    
    /**
     * Interface para callbacks de autenticación
     */
    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onError(String error);
    }
    
    /**
     * Interface para callbacks de recuperación de contraseña
     */
    public interface ResetPasswordCallback {
        void onSuccess();
        void onError(String error);
    }
}