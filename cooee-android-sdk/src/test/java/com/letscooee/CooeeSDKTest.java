package com.letscooee;

import android.os.Build;
import com.letscooee.utils.CooeeCTAListener;
import com.letscooee.utils.PropertyNameException;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.R)
public class CooeeSDKTest extends BaseTestCase {

    @Mock
    CooeeSDK cooeeSDK;

    Map<String, Object> validProperty;
    Map<String, Object> inValidProperty;
    String eventName = "Test Event";

    @Override
    public void setUp() {
        super.setUp();
        MockitoAnnotations.openMocks(this);
        prepareTestData();
    }

    @Ignore("This method just prepare data for test cases")
    private void prepareTestData() {
        validProperty = new HashMap<>();
        validProperty.put("foo", "bar");
        validProperty.put("number", 123);

        inValidProperty = new HashMap<>();
        inValidProperty.put("CE foo", "bar");
        inValidProperty.put("CE number", 123);
    }

    // region sendEvent Test Cases
    @Test
    public void event_With_Valid_Name() {
        doNothing().when(cooeeSDK).sendEvent(anyString());
        cooeeSDK.sendEvent(eventName);
        verify(cooeeSDK, times(1)).sendEvent(eventName);
    }

    @Test(expected = NullPointerException.class)
    public void event_With_Null_Name() {
        doThrow(new NullPointerException()).when(cooeeSDK).sendEvent(isNull());
        cooeeSDK.sendEvent(null);
    }

    @Test
    public void event_With_Valid_Name_And_Valid_EventProperties() {
        doNothing().when(cooeeSDK).sendEvent(anyString(), anyMap());
        cooeeSDK.sendEvent(eventName, validProperty);
        verify(cooeeSDK, times(1)).sendEvent(eventName, validProperty);
    }

    @Test(expected = NullPointerException.class)
    public void event_With_Null_Name_And_Valid_EventProperties() {
        doThrow(new NullPointerException()).when(cooeeSDK).sendEvent(isNull(), anyMap());
        cooeeSDK.sendEvent(null, validProperty);
    }

    @Test(expected = PropertyNameException.class)
    public void event_With_Valid_Name_And_InValid_EventProperties() {
        doThrow(new PropertyNameException()).when(cooeeSDK).sendEvent(anyString(), anyMap());
        cooeeSDK.sendEvent(eventName, inValidProperty);
    }

    @Test
    public void event_With_Valid_Name_And_Null_EventProperties() {
        doNothing().when(cooeeSDK).sendEvent(anyString(), isNull());
        cooeeSDK.sendEvent(eventName, null);
        verify(cooeeSDK, times(1)).sendEvent(eventName, null);
    }

    // endregion

    // region updateUserProfile

    @Test
    public void profile_with_valid_data() {
        doNothing().when(cooeeSDK).updateUserProfile(anyMap());
        cooeeSDK.updateUserProfile(validProperty);
        verify(cooeeSDK, times(1)).updateUserProfile(validProperty);
    }

    @Test(expected = NullPointerException.class)
    public void profile_with_null_data() {
        doThrow(new NullPointerException()).when(cooeeSDK).updateUserProfile(isNull());
        cooeeSDK.updateUserProfile(null);
    }

    @Test(expected = PropertyNameException.class)
    public void profile_with_invalid_data() {
        doThrow(new PropertyNameException()).when(cooeeSDK).updateUserProfile(anyMap());
        cooeeSDK.updateUserProfile(inValidProperty);
    }

    // endregion

    // region Screen Name
    @Test
    public void valid_screen_name() {
        doNothing().when(cooeeSDK).setCurrentScreen(anyString());
        cooeeSDK.setCurrentScreen("Main");
        verify(cooeeSDK, times(1)).setCurrentScreen("Main");
    }

    @Test
    public void invalid_screen_name() {
        doNothing().when(cooeeSDK).setCurrentScreen(isNull());
        cooeeSDK.setCurrentScreen(null);
        verify(cooeeSDK, times(1)).setCurrentScreen(null);
    }
    // endregion

    // region CTA Listener
    @Test
    public void cta_valid_cta_listener() {
        doNothing().when(cooeeSDK).setCTAListener(any(CooeeCTAListener.class));

        CooeeCTAListener cooeeCTAListener = payload -> {
            // Body not required as it is just a mock test to verify.
        };
        cooeeSDK.setCTAListener(cooeeCTAListener);

        verify(cooeeSDK, times(1)).setCTAListener(cooeeCTAListener);
    }

    public void cta_invalid_cta_listener() {
        doNothing().when(cooeeSDK).setCTAListener(isNull());
        cooeeSDK.setCTAListener(null);

        verify(cooeeSDK, times(1)).setCTAListener(null);
    }
    // endregion

    public void check_debug_info_call(){
        doNothing().when(cooeeSDK).showDebugInfo();
        cooeeSDK.showDebugInfo();
        verify(cooeeSDK,times(1)).showDebugInfo();
    }
}