package com.pdm.domohouse.ui.profile;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import com.pdm.domohouse.R;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

/**
 * Tests de UI para ProfileFragment
 * Verifica que los elementos de la interfaz se muestren correctamente
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ProfileFragmentTest {

    @Test
    public void testProfileFragmentLoads() {
        // Lanzar el fragmento
        FragmentScenario<ProfileFragment> scenario = FragmentScenario.launchInContainer(ProfileFragment.class);
        
        // Verificar que los elementos principales estén presentes
        onView(withId(R.id.profileImage)).check(matches(isDisplayed()));
        onView(withId(R.id.nameInputLayout)).check(matches(isDisplayed()));
        onView(withId(R.id.emailInputLayout)).check(matches(isDisplayed()));
        onView(withId(R.id.saveChangesButton)).check(matches(isDisplayed()));
    }

    @Test
    public void testSecuritySectionVisible() {
        // Lanzar el fragmento
        FragmentScenario<ProfileFragment> scenario = FragmentScenario.launchInContainer(ProfileFragment.class);
        
        // Verificar que la sección de seguridad esté visible
        onView(withId(R.id.changePinButton)).check(matches(isDisplayed()));
        onView(withId(R.id.changePasswordButton)).check(matches(isDisplayed()));
    }

    @Test
    public void testPreferencesSectionVisible() {
        // Lanzar el fragmento
        FragmentScenario<ProfileFragment> scenario = FragmentScenario.launchInContainer(ProfileFragment.class);
        
        // Verificar que las preferencias estén visibles
        onView(withId(R.id.notificationsSwitch)).check(matches(isDisplayed()));
        onView(withId(R.id.autoModeSwitch)).check(matches(isDisplayed()));
        onView(withId(R.id.temperatureAlertsSwitch)).check(matches(isDisplayed()));
    }

    @Test
    public void testLogoutButtonVisible() {
        // Lanzar el fragmento
        FragmentScenario<ProfileFragment> scenario = FragmentScenario.launchInContainer(ProfileFragment.class);
        
        // Verificar que el botón de logout esté visible
        onView(withId(R.id.logoutButton)).check(matches(isDisplayed()));
    }

    @Test
    public void testCameraButtonVisible() {
        // Lanzar el fragmento
        FragmentScenario<ProfileFragment> scenario = FragmentScenario.launchInContainer(ProfileFragment.class);
        
        // Verificar que el botón de cámara esté visible
        onView(withId(R.id.cameraButton)).check(matches(isDisplayed()));
    }
}