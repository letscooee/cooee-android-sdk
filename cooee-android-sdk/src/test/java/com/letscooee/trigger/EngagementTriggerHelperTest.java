package com.letscooee.trigger;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import android.app.Activity;
import android.content.Context;
import androidx.room.Room;
import com.google.gson.JsonSyntaxException;
import com.letscooee.BaseTestCase;
import com.letscooee.exceptions.InvalidTriggerDataException;
import com.letscooee.models.trigger.EmbeddedTrigger;
import com.letscooee.models.trigger.TriggerData;
import com.letscooee.room.CooeeDatabase;
import com.letscooee.trigger.cache.PendingTriggerService;
import com.letscooee.utils.Constants;
import com.letscooee.utils.LocalStorageHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.ReflectionHelpers;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EngagementTriggerHelperTest extends BaseTestCase {

    EngagementTriggerHelper engagementTriggerHelperMock;
    EngagementTriggerHelper engagementTriggerHelper;
    PendingTriggerService pendingTriggerService;

    @Before
    @Override
    public void setUp() {
        super.setUp();
        context = RuntimeEnvironment.getApplication().getApplicationContext();
        loadPayload();
        createActivity();
        database = Room.inMemoryDatabaseBuilder(context, CooeeDatabase.class).build();
        engagementTriggerHelper = new EngagementTriggerHelper(context);
        engagementTriggerHelperMock = Mockito.spy(engagementTriggerHelper);
        pendingTriggerService = Mockito.mock(PendingTriggerService.class);
        try {
            expiredTriggerData = TriggerDataHelper.parse(samplePayload);
        } catch (InvalidTriggerDataException e) {
            e.printStackTrace();
        }
        makeActiveTrigger();
        ReflectionHelpers.setField(engagementTriggerHelperMock, "pendingTriggerService", pendingTriggerService);
    }

    @Test
    public void render_in_app_from_response() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("triggerData", samplePayload);

        assertThat(payload).isNotNull();
        assertThat(payload.get("triggerData")).isNotNull();

        doNothing().when(engagementTriggerHelperMock).renderInAppTriggerFromJSONString(anyString());
        engagementTriggerHelperMock.renderInAppTriggerFromResponse(payload);
        verify(engagementTriggerHelperMock, times(1)).renderInAppTriggerFromJSONString(anyString());
    }

    @Test
    public void render_in_app_from_response_invalid() {
        Map<String, Object> payload = new HashMap<>();

        assertThat(payload).isNotNull();
        assertThat(payload.get("triggerData")).isNull();

        doNothing().when(engagementTriggerHelperMock).renderInAppTriggerFromJSONString(anyString());
        engagementTriggerHelperMock.renderInAppTriggerFromResponse(payload);
        verify(engagementTriggerHelperMock, times(0)).renderInAppTriggerFromJSONString(samplePayload);
    }

    @Test
    public void render_in_app_from_response_null_response() {
        doNothing().when(engagementTriggerHelperMock).renderInAppTriggerFromJSONString(anyString());
        engagementTriggerHelperMock.renderInAppTriggerFromResponse(null);
        verify(engagementTriggerHelperMock, never()).renderInAppTriggerFromJSONString(samplePayload);
    }

    private void commonRenderInAppTriggerFromJSONString(String payload, @SuppressWarnings("SameParameterValue") int times) {
        doNothing().when(engagementTriggerHelperMock).renderInAppTrigger(any(TriggerData.class));
        engagementTriggerHelperMock.renderInAppTriggerFromJSONString(payload);

        if (times > 0) {
            verify(engagementTriggerHelperMock, times(times)).renderInAppTrigger(any(TriggerData.class));
        } else {
            verify(engagementTriggerHelperMock, never()).renderInAppTrigger(any(TriggerData.class));
        }
    }

    @Test
    public void render_in_app_from_json_string() {
        assertThat(triggerData).isNotNull();
        LocalStorageHelper.remove(context, Constants.STORAGE_ACTIVATED_TRIGGERS);
        engagementTriggerHelperMock.renderInAppTriggerFromJSONString(TriggerDataHelper.stringify(triggerData));
        List<EmbeddedTrigger> list = EngagementTriggerHelper.getActiveTriggers(context);
        assertThat(triggerData.getId()).isEqualTo(list.get(0).getTriggerID());
    }

    @Test
    public void render_in_app_from_json_string_null_string() {
        commonRenderInAppTriggerFromJSONString(null, 0);
    }

    @Test
    public void render_in_app_from_json_string_empty_string() {
        String emptyPayload = "";
        commonRenderInAppTriggerFromJSONString(emptyPayload, 0);
    }

    /**
     * This test reproduces bug (COOEE-713) and check that bug is handled correctly.
     */
    @Test
    public void render_in_app_from_json_string_invalid_json_string() {
        assertThat(payloadMap).isNotNull();

        try {
            commonRenderInAppTriggerFromJSONString(payloadMap.toString(), 0);
        } catch (Exception e) {
            assertThat(e).isNotNull();
            assertThat(e).isInstanceOf(JsonSyntaxException.class);
        }
    }

    @Test
    public void render_in_app_from_json_string_invalid_json_string_scenario_2() {
        String invalidPayload = "invalid json string";
        commonRenderInAppTriggerFromJSONString(invalidPayload, 0);
    }

    @Test
    public void render_in_app_from_json_string_invalid_json_string_scenario_3() {
        commonRenderInAppTriggerFromJSONString("{}", 0);
    }

    private void commonRenderInAppFromPushNotification(Activity activity, int times) {
        doNothing().when(engagementTriggerHelperMock).renderInAppTrigger(any(TriggerData.class));
        engagementTriggerHelperMock.renderInAppFromPushNotification(activity);
        verify(engagementTriggerHelperMock, timeout(5000).times(times)).renderInAppTrigger(any(TriggerData.class));
    }

    @Ignore("Failing due to overloaded methods")
    @Test
    public void render_in_app_from_notification_from_activity() {
        assertThat(activity).isNotNull();

        commonRenderInAppFromPushNotification(activity, 1);
    }

    @Test
    public void render_in_app_from_notification_from_activity_with_no_extra() {
        assertThat(activityWithNoBundle).isNotNull();

        commonRenderInAppFromPushNotification(activityWithNoBundle, 0);
    }

    @Test
    public void render_in_app_from_notification_from_activity_with_no_trigger_data_in_extra() {
        assertThat(activityWithNoBundle).isNotNull();

        commonRenderInAppFromPushNotification(activityWithNoBundle, 0);
    }

    @Ignore("This test is not working properly. Need to fix it.")
    @Test
    public void render_in_app_from_notification_from_trigger_data() {
        assertThat(triggerData).isNotNull();
        assertThat(triggerData.getId()).isNotEmpty();
        assertThat(triggerData.getEngagementID()).isNotEmpty();
        assertThat(triggerData.getExpireAt()).isGreaterThan(0);

        doNothing().when(engagementTriggerHelperMock).renderInAppTrigger(any(TriggerData.class));
        engagementTriggerHelperMock.renderInAppFromPushNotification(triggerData, 1);
        verify(engagementTriggerHelperMock, timeout(5000).times(1)).renderInAppTrigger(any(TriggerData.class));
    }

    private List<EmbeddedTrigger> saveAndGetActiveTriggers(Context context, TriggerData triggerData) {
        EngagementTriggerHelper.storeActiveTriggerDetails(context, triggerData);
        return EngagementTriggerHelper.getActiveTriggers(context);
    }

    @Test
    public void store_active_trigger() {
        assertThat(triggerData).isNotNull();
        assertThat(triggerData.getId()).isNotEmpty();
        assertThat(triggerData.getEngagementID()).isNotEmpty();
        assertThat(triggerData.getExpireAt()).isGreaterThan(0);

        List<EmbeddedTrigger> embeddedTriggers = saveAndGetActiveTriggers(context, triggerData);

        assertThat(embeddedTriggers).isNotEmpty();
        assertThat(embeddedTriggers.size()).isEqualTo(1);
        assertThat(embeddedTriggers.get(0).getTriggerID()).isEqualTo(triggerData.getId());
    }

    @Test
    public void store_expired_trigger() {
        assertThat(expiredTriggerData).isNotNull();
        assertThat(expiredTriggerData.getId()).isNotEmpty();
        assertThat(expiredTriggerData.getEngagementID()).isNotEmpty();
        assertThat(expiredTriggerData.getExpireAt()).isGreaterThan(0);

        List<EmbeddedTrigger> embeddedTriggers = saveAndGetActiveTriggers(context, expiredTriggerData);

        assertThat(embeddedTriggers).isEmpty();
        assertThat(embeddedTriggers.size()).isEqualTo(0);
    }

    @After
    @Override
    public void tearDown() throws Exception {
        reset(engagementTriggerHelperMock);
        database.close();
        super.tearDown();
    }
}