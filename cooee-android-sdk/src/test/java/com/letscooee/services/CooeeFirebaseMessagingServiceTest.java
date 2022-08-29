package com.letscooee.services;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import com.letscooee.BaseTestCase;
import com.letscooee.models.trigger.TriggerData;
import com.letscooee.room.CooeeDatabase;
import com.letscooee.room.trigger.PendingTrigger;
import com.letscooee.trigger.cache.PendingTriggerService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.robolectric.util.ReflectionHelpers;
import java.util.List;

public class CooeeFirebaseMessagingServiceTest extends BaseTestCase {

    PendingTriggerService pendingTriggerService;

    CooeeFirebaseMessagingService cooeeFirebaseMessagingService;

    private CooeeDatabase cooeeDatabase;

    @Before
    @Override
    public void setUp() {
        super.setUp();
        super.loadPayload();

        MockitoAnnotations.openMocks(this);
        pendingTriggerService = spy(new PendingTriggerService(context));
        cooeeFirebaseMessagingService = spy(new CooeeFirebaseMessagingService(context));
        ReflectionHelpers.setField(cooeeFirebaseMessagingService, "pendingTriggerService", pendingTriggerService);
        ReflectionHelpers.setField(cooeeFirebaseMessagingService, "context", context);
        cooeeDatabase = CooeeDatabase.getInstance(context);
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        cooeeFirebaseMessagingService = null;
    }

    private void commonFailForUpdatedPayload(String payload) {
        doReturn(new PendingTrigger()).when(pendingTriggerService).newTrigger(any(TriggerData.class));
        cooeeFirebaseMessagingService.handleTriggerData(payload);
        verify(pendingTriggerService, never()).newTrigger(any(TriggerData.class));
    }

    @Test
    public void handle_trigger_data_with_valid_data() {
        payloadMap.remove("pn");
        doReturn(new PendingTrigger()).when(pendingTriggerService).newTrigger(any(TriggerData.class));
        cooeeFirebaseMessagingService.handleTriggerData(gson.toJson(payloadMap));

        verify(pendingTriggerService, never()).newTrigger(any(TriggerData.class));
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
    }

    @Test
    public void store_trigger() {
        payloadMap.put("ian", null);
        String updatedPayload = gson.toJson(payloadMap);

        cooeeFirebaseMessagingService.handleTriggerData(updatedPayload);
        List<PendingTrigger> pendingTriggers = cooeeDatabase.pendingTriggerDAO().getAll();
        assertThat(pendingTriggers.size()).isGreaterThan(0);
        assertEquals(pendingTriggers.get(0).triggerId, payloadMap.get("id"));
    }

    @Test
    public void store_trigger_with_invalid_trigger_id() {
        payloadMap.put("ian", null);
        payloadMap.put("id", "1234");
        String updatedPayload = gson.toJson(payloadMap);

        doReturn(new PendingTrigger()).when(pendingTriggerService).newTrigger(any(TriggerData.class));
        cooeeFirebaseMessagingService.handleTriggerData(updatedPayload);
        verify(pendingTriggerService, times(1)).newTrigger(any(TriggerData.class));
        List<PendingTrigger> pendingTriggers = cooeeDatabase.pendingTriggerDAO().getAll();
        assertThat(pendingTriggers.size()).isGreaterThan(0);
    }

    @Test
    public void store_multiple_triggers() {
        payloadMap.put("ian", null);
        String updatedPayload = gson.toJson(payloadMap);

        doReturn(new PendingTrigger()).when(pendingTriggerService).newTrigger(any(TriggerData.class));
        cooeeFirebaseMessagingService.handleTriggerData(updatedPayload);
        cooeeFirebaseMessagingService.handleTriggerData(updatedPayload);
        verify(pendingTriggerService, times(2)).newTrigger(any(TriggerData.class));
        List<PendingTrigger> pendingTriggers = cooeeDatabase.pendingTriggerDAO().getAll();
        assertThat(pendingTriggers.size()).isGreaterThan(0);
        assertEquals(pendingTriggers.get(0).triggerId, payloadMap.get("id"));
    }
}