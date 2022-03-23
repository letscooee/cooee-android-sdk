package com.letscooee;

import android.os.Build;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.R)
public class CooeeSDKTest extends BaseTestCase {

    @Mock
    CooeeSDK cooeeSDK;

    @Override
    public void setUp() {
        super.setUp();
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void validEvent(){
        Mockito.doNothing().when(cooeeSDK).sendEvent(Mockito.anyString());
        cooeeSDK.sendEvent("Test Event");
        Mockito.verify(cooeeSDK, Mockito.times(1)).sendEvent("Test Event");
    }

}