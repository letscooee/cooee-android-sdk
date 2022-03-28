package com.letscooee.services;

import android.os.Build;
import com.letscooee.BaseTestCase;
import com.letscooee.models.trigger.TriggerData;
import com.letscooee.trigger.adapters.TriggerGsonDeserializer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.R)
public class CooeeFirebaseMessagingServiceTest extends BaseTestCase {

    String samplePayload;

    @Before
    @Override
    public void setUp() {
        super.setUp();
        try {
            InputStream inputStream = context.getAssets().open("payload_2.json");
            samplePayload = new Scanner(inputStream).useDelimiter("\\A").next();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void handle_trigger_data_with_valid_data() {

//        TriggerData triggerData = TriggerGsonDeserializer.getGson().fromJson(samplePayload, TriggerData.class);

        CooeeFirebaseMessagingService cooeeFirebaseMessagingService = Mockito.spy(CooeeFirebaseMessagingService.class);
//        mockStatic(EngagementTriggerHelper.class)

        /*try (MockedStatic<InAppTriggerHelper> inAppTriggerHelperMockedStatic = Mockito.mockStatic(InAppTriggerHelper.class)) {
            inAppTriggerHelperMockedStatic.when(() -> InAppTriggerHelper.loadLazyData(triggerData, data -> {
            })).thenAnswer((Answer<Void>) invocation -> null);

            inAppTriggerHelperMockedStatic.verify(() -> InAppTriggerHelper.loadLazyData(triggerData, data -> {
            }), times(1));
        }*/


        doNothing().when(cooeeFirebaseMessagingService).handleTriggerData(anyString());

        /*cooeeFirebaseMessagingService.handleTriggerData(samplePayload);
        verify(cooeeFirebaseMessagingService, times(1)).handleTriggerData(samplePayload);

        try (MockedStatic<EngagementTriggerHelper> engagementTriggerHelperMockedStatic = Mockito.mockStatic(EngagementTriggerHelper.class)) {
            engagementTriggerHelperMockedStatic.when(() -> EngagementTriggerHelper.loadLazyData(any(Context.class), any(TriggerData.class)))
                    .thenAnswer((Answer<Void>) invocation -> null);

            engagementTriggerHelperMockedStatic.verify(() -> EngagementTriggerHelper.loadLazyData(any(Context.class), any(TriggerData.class)));

        }*/

    }
}