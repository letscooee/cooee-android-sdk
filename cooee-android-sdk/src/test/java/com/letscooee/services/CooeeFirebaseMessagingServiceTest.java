package com.letscooee.services;

import android.os.Build;
import com.letscooee.BaseTestCase;
import com.letscooee.models.trigger.TriggerData;
import com.letscooee.trigger.EngagementTriggerHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.R)
public class CooeeFirebaseMessagingServiceTest extends BaseTestCase {

    @Mock
    EngagementTriggerHelper engagementTriggerHelper;

    @InjectMocks
    CooeeFirebaseMessagingService cooeeFirebaseMessagingService;

    @Before
    @Override
    public void setUp() {
        super.setUp();
        super.loadPayload();

        MockitoAnnotations.openMocks(this);
        cooeeFirebaseMessagingService.context = context;
        cooeeFirebaseMessagingService.engagementTriggerHelper = engagementTriggerHelper;
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        engagementTriggerHelper = null;
        cooeeFirebaseMessagingService = null;
    }

    private void commonFailForUpdatedPayload(String payload) {
        doNothing().when(engagementTriggerHelper).loadLazyData(any(TriggerData.class));
        cooeeFirebaseMessagingService.handleTriggerData(payload);
        verify(engagementTriggerHelper, never()).loadLazyData(any(TriggerData.class));
    }

    @Test
    public void handle_trigger_data_with_valid_data() {
        doNothing().when(engagementTriggerHelper).loadLazyData(any(TriggerData.class));
        cooeeFirebaseMessagingService.handleTriggerData(samplePayload);
        verify(engagementTriggerHelper, times(1)).loadLazyData(any(TriggerData.class));
    }

    @Test
    public void handle_trigger_data_with_null_data() {
        commonFailForUpdatedPayload(null);
    }

    @Test
    public void handle_trigger_data_with_empty_data() {
        samplePayload = "";

        commonFailForUpdatedPayload(samplePayload);
    }

    @Test
    public void handle_trigger_data_with_invalid_data() {
        samplePayload = "test data";

        commonFailForUpdatedPayload(samplePayload);
    }

    @Test
    public void handle_trigger_data_with_null_trigger_id() {
        payloadMap.put("id", null);
        String updatedPayload = gson.toJson(payloadMap);

        commonFailForUpdatedPayload(updatedPayload);
    }

    @Test
    public void handle_trigger_data_with_null_payload_version() {
        payloadMap.put("v", null);
        String updatedPayload = gson.toJson(payloadMap);

        commonFailForUpdatedPayload(updatedPayload);
    }

    @Test
    public void handle_trigger_data_with_invalid_payload_version() {
        payloadMap.put("v", 1.01);
        String updatedPayload = gson.toJson(payloadMap);

        commonFailForUpdatedPayload(updatedPayload);
        ;
    }
}