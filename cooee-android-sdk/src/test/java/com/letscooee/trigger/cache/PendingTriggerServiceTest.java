package com.letscooee.trigger.cache;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.spy;
import com.letscooee.BaseTestCase;
import com.letscooee.enums.trigger.PendingTriggerAction;
import com.letscooee.exceptions.InvalidTriggerDataException;
import com.letscooee.models.trigger.TriggerData;
import com.letscooee.room.CooeeDatabase;
import com.letscooee.room.trigger.PendingTrigger;
import com.letscooee.trigger.TriggerDataHelper;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.robolectric.util.ReflectionHelpers;
import java.util.List;

public class PendingTriggerServiceTest extends BaseTestCase {

    PendingTriggerService pendingTriggerService;
    CooeeDatabase database;

    @Override
    public void setUp() {
        super.setUp();
        super.loadPayload();
        super.makeActiveTrigger();
        MockitoAnnotations.openMocks(this);
        this.pendingTriggerService = spy(new PendingTriggerService(context));
        database = CooeeDatabase.getInstance(context);
        ReflectionHelpers.setField(this.pendingTriggerService, "cooeeDatabase", database);
    }

    private void commonTriggerLoading(TriggerData triggerData, int times) {
        PendingTrigger pendingTrigger = pendingTriggerService.newTrigger(triggerData);
        if (times == 0) {
            assertThat(pendingTrigger).isNull();
        } else {
            PendingTrigger savedTrigger = pendingTriggerService.peep();
            assertThat(pendingTrigger).isNotNull();
            assertThat(savedTrigger).isNotNull();
            assertThat(savedTrigger.id).isEqualTo(pendingTrigger.id);
            assertThat(savedTrigger.triggerId).isEqualTo(pendingTrigger.triggerId);
        }
        emptyDatabase();
    }

    private void emptyDatabase() {
        database.pendingTriggerDAO().deleteAll();
    }

    @Test
    public void load_ian_with_null_trigger_data() {
        emptyDatabase();
        commonTriggerLoading(null, 0);
    }

    @Test
    public void load_ian_with_empty_trigger_data() {
        try {
            commonTriggerLoading(TriggerDataHelper.parse("{}"), 0);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(InvalidTriggerDataException.class);
        }
    }

    @Test
    public void load_ian_with_valid_trigger_data() {
        commonTriggerLoading(triggerData, 1);
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
        TriggerData triggerData = null;
        try {
            triggerData = TriggerDataHelper.parse(gson.toJson(payloadMap));
        } catch (InvalidTriggerDataException e) {
            assertThat(e).isNull();
        }
        assertThat(triggerData).isNotNull();
        PendingTrigger pendingTrigger = pendingTriggerService.newTrigger(triggerData);

        pendingTriggerService.delete(PendingTriggerAction.DELETE_ID, this.triggerData.getId());
        List<PendingTrigger> pendingTriggers = database.pendingTriggerDAO().getAll();

        assertThat(pendingTriggers.size()).isEqualTo(1);
        assertThat(pendingTriggers.get(0).id).isEqualTo(pendingTrigger.id);
        emptyDatabase();
    }
}