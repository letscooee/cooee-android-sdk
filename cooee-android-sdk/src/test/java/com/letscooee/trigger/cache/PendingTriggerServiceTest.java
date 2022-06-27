package com.letscooee.trigger.cache;

import com.letscooee.BaseTestCase;
import com.letscooee.enums.trigger.PendingTriggerAction;
import com.letscooee.models.trigger.TriggerData;
import com.letscooee.room.CooeeDatabase;
import com.letscooee.room.trigger.PendingTrigger;
import com.letscooee.trigger.InAppTriggerHelper;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.util.ReflectionHelpers;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class PendingTriggerServiceTest extends BaseTestCase {

    PendingTriggerService pendingTriggerService;
    CooeeDatabase database;
    @Mock
    InAppTriggerHelper inAppTriggerHelper;

    @Override
    public void setUp() {
        super.setUp();
        super.loadPayload();
        super.makeActiveTrigger();
        MockitoAnnotations.openMocks(this);
        this.pendingTriggerService = spy(new PendingTriggerService(context));
        database = CooeeDatabase.getInstance(context);
        ReflectionHelpers.setField(this.pendingTriggerService, "inAppTriggerHelper", inAppTriggerHelper);
        ReflectionHelpers.setField(this.pendingTriggerService, "cooeeDatabase", database);
    }

    private void commonTriggerLoading(TriggerData triggerData, int times) {
        /*PendingTrigger pendingTrigger = pendingTriggerService.newTrigger(triggerData);
        doNothing().when(inAppTriggerHelper).loadLazyData(any(TriggerData.class), any());
        pendingTriggerService.loadAndSaveTriggerData(pendingTrigger, triggerData);
        if (times == 0) {
            verify(inAppTriggerHelper, never()).loadLazyData(any(TriggerData.class), any());
        } else {
            verify(inAppTriggerHelper, times(times)).loadLazyData(any(TriggerData.class), any());
        }
        emptyDatabase();*/
    }

    private void emptyDatabase() {
        database.pendingTriggerDAO().deleteAll();
    }

    @Test
    public void load_ian_with_null_trigger_data() {
        commonTriggerLoading(null, 0);
    }

    @Test
    public void load_ian_with_empty_trigger_data() {
        commonTriggerLoading(TriggerData.fromJson("{}"), 0);
    }

    @Test
    public void load_ian_with_valid_trigger_data() {
        commonTriggerLoading(triggerData, 1);
    }

    private void commonContentDownloadDownload(TriggerData triggerData, int times) {
        /*doNothing().when(pendingTriggerService).loadImage(anyString(), anyInt());
        pendingTriggerService.loadAndCacheInAppContent(triggerData);
        if (times == 0) {
            verify(pendingTriggerService, never()).loadImage(anyString(), anyInt());
        } else {
            verify(pendingTriggerService, atLeast(times)).loadImage(anyString(), anyInt());
        }*/
    }

    @Test
    public void load_ian_content_with_null_trigger_content() {
        commonContentDownloadDownload(null, 0);
    }

    @Test
    public void load_ian_content_with_empty_trigger_content() {
        commonContentDownloadDownload(TriggerData.fromJson("{}"), 0);
    }

    @Test
    public void load_ian_content_with_valid_trigger_content() {
        commonContentDownloadDownload(triggerData, 1);
    }

    private void addMultipleTriggers() {
        pendingTriggerService.newTrigger(triggerData);
        pendingTriggerService.newTrigger(triggerData);
        pendingTriggerService.newTrigger(triggerData);
        pendingTriggerService.newTrigger(triggerData);
        pendingTriggerService.newTrigger(triggerData);
    }

    @Test
    public void store_new_trigger() {
        addMultipleTriggers();
        PendingTrigger pendingTrigger = pendingTriggerService.newTrigger(triggerData);
        PendingTrigger pendingTrigger1 = pendingTriggerService.peep();
        assertEquals(pendingTrigger.triggerId, pendingTrigger1.triggerId);
        emptyDatabase();
    }

    @Test
    public void update_pending_trigger_action_delete_all() {
        addMultipleTriggers();
        PendingTrigger pendingTrigger = pendingTriggerService.newTrigger(triggerData);

        List<PendingTrigger> pendingTriggers = database.pendingTriggerDAO().getAll();

        assertThat(pendingTriggers.size()).isEqualTo(6);
        assertThat(pendingTrigger.id).isEqualTo(pendingTriggers.get(0).id);

        pendingTriggerService.delete(PendingTriggerAction.DELETE_ALL, null);
        pendingTriggers = database.pendingTriggerDAO().getAll();

        assertThat(pendingTriggers).isEmpty();
        assertThat(pendingTriggers.size()).isEqualTo(0);
        emptyDatabase();
    }

    @Test
    public void update_pending_trigger_action_delete_with_valid_id() {
        pendingTriggerService.newTrigger(triggerData);
        addMultipleTriggers();
        payloadMap.put("id", "1243");
        TriggerData triggerData = TriggerData.fromJson(gson.toJson(payloadMap));
        PendingTrigger pendingTrigger = pendingTriggerService.newTrigger(triggerData);

        pendingTriggerService.delete(PendingTriggerAction.DELETE_ID, this.triggerData.getId());
        List<PendingTrigger> pendingTriggers = database.pendingTriggerDAO().getAll();

        assertThat(pendingTriggers.size()).isEqualTo(1);
        assertThat(pendingTriggers.get(0).id).isEqualTo(pendingTrigger.id);
        emptyDatabase();
    }
}