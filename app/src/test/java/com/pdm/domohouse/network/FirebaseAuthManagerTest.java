package com.pdm.domohouse.network;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para FirebaseAuthManager
 * Verifica el comportamiento de autenticación con Firebase
 */
@RunWith(MockitoJUnitRunner.class)
public class FirebaseAuthManagerTest {
    
    @Mock
    private FirebaseAuth mockFirebaseAuth;
    
    @Mock
    private FirebaseUser mockFirebaseUser;
    
    private FirebaseAuthManager authManager;
    
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Mock FirebaseAuth.getInstance()
        try (MockedStatic<FirebaseAuth> firebaseAuthStatic = mockStatic(FirebaseAuth.class)) {
            firebaseAuthStatic.when(FirebaseAuth::getInstance).thenReturn(mockFirebaseAuth);
            authManager = FirebaseAuthManager.getInstance();
        }
    }
    
    @Test
    public void testGetInstance_returnsSameInstance() {
        // Given
        FirebaseAuthManager instance1 = FirebaseAuthManager.getInstance();
        FirebaseAuthManager instance2 = FirebaseAuthManager.getInstance();
        
        // Then
        assertSame("getInstance debe devolver la misma instancia", instance1, instance2);
    }
    
    @Test
    public void testIsUserSignedIn_withUser_returnsTrue() {
        // Given
        when(mockFirebaseAuth.getCurrentUser()).thenReturn(mockFirebaseUser);
        
        // When
        boolean isSignedIn = authManager.isUserSignedIn();
        
        // Then
        assertTrue("Debe devolver true cuando hay un usuario autenticado", isSignedIn);
    }
    
    @Test
    public void testIsUserSignedIn_withoutUser_returnsFalse() {
        // Given
        when(mockFirebaseAuth.getCurrentUser()).thenReturn(null);
        
        // When
        boolean isSignedIn = authManager.isUserSignedIn();
        
        // Then
        assertFalse("Debe devolver false cuando no hay usuario autenticado", isSignedIn);
    }
    
    @Test
    public void testGetCurrentUser_returnsCurrentUser() {
        // Given
        when(mockFirebaseAuth.getCurrentUser()).thenReturn(mockFirebaseUser);
        
        // When
        FirebaseUser currentUser = authManager.getCurrentUser();
        
        // Then
        assertEquals("Debe devolver el usuario actual", mockFirebaseUser, currentUser);
    }
    
    @Test
    public void testSignOut_callsFirebaseSignOut() {
        // When
        authManager.signOut();
        
        // Then
        verify(mockFirebaseAuth).signOut();
    }
}