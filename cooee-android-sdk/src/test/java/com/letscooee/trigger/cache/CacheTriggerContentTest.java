package com.letscooee.trigger.cache;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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

public class CacheTriggerContentTest extends BaseTestCase {

    CacheTriggerContent cacheTriggerContent;
    CooeeDatabase database;
    @Mock
    InAppTriggerHelper inAppTriggerHelper;

    @Override
    public void setUp() {
        super.setUp();
        super.loadPayload();
        super.makeActiveTrigger();
        MockitoAnnotations.openMocks(this);
        this.cacheTriggerContent = spy(new CacheTriggerContent(context));
        database = CooeeDatabase.getInstance(context);
        ReflectionHelpers.setField(this.cacheTriggerContent, "inAppTriggerHelper", inAppTriggerHelper);
        ReflectionHelpers.setField(this.cacheTriggerContent, "cooeeDatabase", database);
    }

    private void commonTriggerLoading(TriggerData triggerData, int times) {
        PendingTrigger pendingTrigger = cacheTriggerContent.newTrigger(triggerData);
        doNothing().when(inAppTriggerHelper).loadLazyData(any(TriggerData.class), any());
        cacheTriggerContent.loadAndSaveTriggerData(pendingTrigger, triggerData);
        if (times == 0) {
            verify(inAppTriggerHelper, never()).loadLazyData(any(TriggerData.class), any());
        } else {
            verify(inAppTriggerHelper, times(times)).loadLazyData(any(TriggerData.class), any());
        }
        emptyDatabase();
    }

    private void emptyDatabase() {
        database.pendingTriggerDAO().deleteAllPendingTriggers();
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
        doNothing().when(cacheTriggerContent).loadImage(anyString(), anyInt());
        cacheTriggerContent.loadAndCacheInAppContent(triggerData);
        if (times == 0) {
            verify(cacheTriggerContent, never()).loadImage(anyString(), anyInt());
        } else {
            verify(cacheTriggerContent, atLeast(times)).loadImage(anyString(), anyInt());
        }
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
        cacheTriggerContent.newTrigger(triggerData);
        cacheTriggerContent.newTrigger(triggerData);
        cacheTriggerContent.newTrigger(triggerData);
        cacheTriggerContent.newTrigger(triggerData);
        cacheTriggerContent.newTrigger(triggerData);
    }

    @Test
    public void store_new_trigger() {
        addMultipleTriggers();
        PendingTrigger pendingTrigger = cacheTriggerContent.newTrigger(triggerData);
        PendingTrigger pendingTrigger1 = cacheTriggerContent.getPendingTrigger();
        assertEquals(pendingTrigger.triggerId, pendingTrigger1.triggerId);
        emptyDatabase();
    }

    @Test
    public void update_pending_trigger_action_delete_all() {
        addMultipleTriggers();
        PendingTrigger pendingTrigger = cacheTriggerContent.newTrigger(triggerData);

        List<PendingTrigger> pendingTriggers = database.pendingTriggerDAO().getAllPendingTriggers();

        assertThat(pendingTriggers.size()).isEqualTo(6);
        assertThat(pendingTrigger.id).isEqualTo(pendingTriggers.get(0).id);

        cacheTriggerContent.updatePendingTriggerAction(PendingTriggerAction.DELETE_ALL, null);
        pendingTriggers = database.pendingTriggerDAO().getAllPendingTriggers();

        assertThat(pendingTriggers).isEmpty();
        assertThat(pendingTriggers.size()).isEqualTo(0);
        emptyDatabase();
    }

    @Test
    public void update_pending_trigger_action_delete_first() {
        PendingTrigger pendingTrigger = cacheTriggerContent.newTrigger(triggerData);
        cacheTriggerContent.newTrigger(triggerData);

        cacheTriggerContent.updatePendingTriggerAction(PendingTriggerAction.DELETE_FIRST, null);
        List<PendingTrigger> pendingTriggers = database.pendingTriggerDAO().getAllPendingTriggers();

        assertThat(pendingTrigger.id).isEqualTo(pendingTriggers.get(0).id);
        emptyDatabase();
    }

    @Test
    public void update_pending_trigger_action_delete_last() {
        cacheTriggerContent.newTrigger(triggerData);
        PendingTrigger pendingTrigger = cacheTriggerContent.newTrigger(triggerData);
        addMultipleTriggers();

        cacheTriggerContent.updatePendingTriggerAction(PendingTriggerAction.DELETE_LAST, null);
        List<PendingTrigger> pendingTriggers = database.pendingTriggerDAO().getAllPendingTriggers();

        assertThat(pendingTrigger.id).isEqualTo(pendingTriggers.get(pendingTriggers.size() - 1).id);
        emptyDatabase();
    }

    @Test
    public void update_pending_trigger_action_delete_all_except_last() {
        PendingTrigger pendingTrigger = cacheTriggerContent.newTrigger(triggerData);
        cacheTriggerContent.newTrigger(triggerData);
        addMultipleTriggers();

        cacheTriggerContent.updatePendingTriggerAction(PendingTriggerAction.DELETE_ALL_EXCEPT_LAST, null);
        PendingTrigger last = cacheTriggerContent.getPendingTrigger();

        assertThat(pendingTrigger.id).isEqualTo(last.id);
        emptyDatabase();
    }

    @Test
    public void update_pending_trigger_action_delete_all_except_first() {
        cacheTriggerContent.newTrigger(triggerData);
        addMultipleTriggers();
        PendingTrigger pendingTrigger = cacheTriggerContent.newTrigger(triggerData);

        cacheTriggerContent.updatePendingTriggerAction(PendingTriggerAction.DELETE_ALL_EXCEPT_FIRST, null);
        PendingTrigger last = cacheTriggerContent.getPendingTrigger();

        assertThat(pendingTrigger.id).isEqualTo(last.id);
        emptyDatabase();
    }

    @Test
    public void update_pending_trigger_action_delete_with_valid_id() {
        cacheTriggerContent.newTrigger(triggerData);
        addMultipleTriggers();
        payloadMap.put("id", "1243");
        TriggerData triggerData = TriggerData.fromJson(gson.toJson(payloadMap));
        PendingTrigger pendingTrigger = cacheTriggerContent.newTrigger(triggerData);

        cacheTriggerContent.updatePendingTriggerAction(PendingTriggerAction.DELETE_ID, this.triggerData.getId());
        List<PendingTrigger> pendingTriggers = database.pendingTriggerDAO().getAllPendingTriggers();

        assertThat(pendingTriggers.size()).isEqualTo(1);
        assertThat(pendingTriggers.get(0).id).isEqualTo(pendingTrigger.id);
        emptyDatabase();
    }
}