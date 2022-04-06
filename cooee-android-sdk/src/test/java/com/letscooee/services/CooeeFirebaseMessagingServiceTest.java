package com.letscooee.services;

import android.os.Build;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.letscooee.BaseTestCase;
import com.letscooee.models.trigger.TriggerData;
import com.letscooee.trigger.EngagementTriggerHelper;
import com.letscooee.utils.Constants;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.R)
public class CooeeFirebaseMessagingServiceTest extends BaseTestCase {

    String samplePayload;
    Map<String, Object> payloadMap;
    Gson gson = new Gson();

    @Mock
    EngagementTriggerHelper engagementTriggerHelper;

    @InjectMocks
    CooeeFirebaseMessagingService cooeeFirebaseMessagingService;

    @Before
    @Override
    public void setUp() {
        super.setUp();
        MockitoAnnotations.openMocks(this);

        try {
            InputStream inputStream = context.getAssets().open("payload_2.json");
            samplePayload = new Scanner(inputStream).useDelimiter("\\A").next();
        } catch (IOException e) {
            Log.e(Constants.TAG, "Error: ", e);
        }

        payloadMap = gson.fromJson(samplePayload, new TypeToken<HashMap<String, Object>>() {
        }.getType());
    }

    @Test
    public void handle_trigger_data_with_valid_data() {
        cooeeFirebaseMessagingService.context = context;
        cooeeFirebaseMessagingService.engagementTriggerHelper = engagementTriggerHelper;

        doNothing().when(engagementTriggerHelper).loadLazyData(any(TriggerData.class));
        cooeeFirebaseMessagingService.handleTriggerData(samplePayload);
        verify(engagementTriggerHelper, times(1)).loadLazyData(any(TriggerData.class));
    }

    @Test
    public void handle_trigger_data_with_null_data() {
        samplePayload = null;

        cooeeFirebaseMessagingService.context = context;
        cooeeFirebaseMessagingService.engagementTriggerHelper = engagementTriggerHelper;

        doNothing().when(engagementTriggerHelper).loadLazyData(any(TriggerData.class));
        cooeeFirebaseMessagingService.handleTriggerData(samplePayload);
        verify(engagementTriggerHelper, never()).loadLazyData(any(TriggerData.class));
    }

    @Test
    public void handle_trigger_data_with_empty_data() {
        samplePayload = "";

        cooeeFirebaseMessagingService.context = context;
        cooeeFirebaseMessagingService.engagementTriggerHelper = engagementTriggerHelper;

        doNothing().when(engagementTriggerHelper).loadLazyData(any(TriggerData.class));
        cooeeFirebaseMessagingService.handleTriggerData(samplePayload);
        verify(engagementTriggerHelper, never()).loadLazyData(any(TriggerData.class));
    }

    @Test
    public void handle_trigger_data_with_invalid_data() {
        samplePayload = "test data";

        cooeeFirebaseMessagingService.context = context;
        cooeeFirebaseMessagingService.engagementTriggerHelper = engagementTriggerHelper;

        doNothing().when(engagementTriggerHelper).loadLazyData(any(TriggerData.class));
        cooeeFirebaseMessagingService.handleTriggerData(samplePayload);
        verify(engagementTriggerHelper, never()).loadLazyData(any(TriggerData.class));
    }

    @Test
    public void handle_trigger_data_with_null_trigger_id() {
        payloadMap.put("id", null);
        String updatedPayload = gson.toJson(payloadMap);

        cooeeFirebaseMessagingService.context = context;
        cooeeFirebaseMessagingService.engagementTriggerHelper = engagementTriggerHelper;

        doNothing().when(engagementTriggerHelper).loadLazyData(any(TriggerData.class));
        cooeeFirebaseMessagingService.handleTriggerData(updatedPayload);
        verify(engagementTriggerHelper, never()).loadLazyData(any(TriggerData.class));
    }

    @Test
    public void handle_trigger_data_with_null_payload_version() {
        payloadMap.put("v", null);
        String updatedPayload = gson.toJson(payloadMap);

        cooeeFirebaseMessagingService.context = context;
        cooeeFirebaseMessagingService.engagementTriggerHelper = engagementTriggerHelper;

        doNothing().when(engagementTriggerHelper).loadLazyData(any(TriggerData.class));
        cooeeFirebaseMessagingService.handleTriggerData(updatedPayload);
        verify(engagementTriggerHelper, never()).loadLazyData(any(TriggerData.class));
    }

    @Test
    public void handle_trigger_data_with_invalid_payload_version() {
        payloadMap.put("v", 1.01);
        String updatedPayload = gson.toJson(payloadMap);

        cooeeFirebaseMessagingService.context = context;
        cooeeFirebaseMessagingService.engagementTriggerHelper = engagementTriggerHelper;

        doNothing().when(engagementTriggerHelper).loadLazyData(any(TriggerData.class));
        cooeeFirebaseMessagingService.handleTriggerData(updatedPayload);
        verify(engagementTriggerHelper, never()).loadLazyData(any(TriggerData.class));
    }
}