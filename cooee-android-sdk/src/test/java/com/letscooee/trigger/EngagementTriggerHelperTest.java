package com.letscooee.trigger;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import androidx.room.Room;
import com.google.gson.JsonSyntaxException;
import com.letscooee.BaseTestCase;
import com.letscooee.models.trigger.EmbeddedTrigger;
import com.letscooee.models.trigger.TriggerData;
import com.letscooee.room.CooeeDatabase;
import com.letscooee.trigger.adapters.TriggerGsonDeserializer;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.*;

public class EngagementTriggerHelperTest extends BaseTestCase {

    EngagementTriggerHelper engagementTriggerHelperMock;
    EngagementTriggerHelper engagementTriggerHelper;

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
        expiredTriggerData = TriggerGsonDeserializer.getGson().fromJson(samplePayload, TriggerData.class);
        makeActiveTrigger();
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

    private void commonRenderInAppTriggerFromJSONString(String payload, int times) {
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
        assertThat(samplePayload).isNotEmpty();

        commonRenderInAppTriggerFromJSONString(samplePayload, 1);
    }

    @Test
    public void render_in_app_from_json_string_null_string() {
        commonRenderInAppTriggerFromJSONString(null, 0);
    }

    @Test
    public void render_in_app_from_json_string_empty_string() {
        samplePayload = "";

        assertThat(samplePayload).isNotNull();

        commonRenderInAppTriggerFromJSONString(samplePayload, 0);
    }

    /**
     * This test reproduces bug and check that bug is handled correctly
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
    public void render_in_app_from_json_string_invalid_json_string_pass_two() {
        samplePayload = "invalid json string";
        assertThat(samplePayload).isNotNull();

        try {
            commonRenderInAppTriggerFromJSONString(samplePayload, 0);
        } catch (Exception e) {
            assertThat(e).isNotNull();
            assertThat(e).isInstanceOf(JsonSyntaxException.class);
        }
    }

    @Test
    public void render_in_app_from_json_string_invalid_json_string_pass_three() {
        try {
            commonRenderInAppTriggerFromJSONString(new HashMap<String, Object>().toString(), 1);
        } catch (Exception e) {
            assertThat(e).isNotNull();
            assertThat(e).isInstanceOf(JsonSyntaxException.class);
        }
    }

    private void commonRenderInAppFromPushNotification(Activity activity, int times) {
        doNothing().when(engagementTriggerHelperMock).renderInAppFromPushNotification(any(TriggerData.class));
        engagementTriggerHelperMock.renderInAppFromPushNotification(activity);
        verify(engagementTriggerHelperMock, times(times)).renderInAppFromPushNotification(any(TriggerData.class));
    }

    @Ignore("Failing due to overloaded methods")
    @Test
    public void render_in_app_from_notification_from_activity() {
        assertThat(activity).isNotNull();

        commonRenderInAppFromPushNotification(activity, 1);
    }

    @Ignore("Failing due to overloaded methods")
    @Test
    public void render_in_app_from_notification_from_activity_with_no_extra() {
        assertThat(activityWithNoBundle).isNotNull();

        commonRenderInAppFromPushNotification(activityWithNoBundle, 0);
    }

    @Ignore("Failing due to overloaded methods")
    @Test
    public void render_in_app_from_notification_from_activity_with_no_trigger_data_in_extra() {
        assertThat(activityWithNoBundle).isNotNull();

        commonRenderInAppFromPushNotification(activityWithNoBundle, 0);
    }

    @Ignore("Failing due pending task")
    @Test
    public void render_in_app_from_notification_from_trigger_data() {
        assertThat(triggerData).isNotNull();
        assertThat(triggerData.getId()).isNotEmpty();
        assertThat(triggerData.getEngagementID()).isNotEmpty();
        assertThat(triggerData.getExpireAt()).isGreaterThan(0);

        doNothing().when(engagementTriggerHelperMock).loadLazyData(any(TriggerData.class));
        engagementTriggerHelperMock.renderInAppFromPushNotification(triggerData);
        verify(engagementTriggerHelperMock, times(1)).loadLazyData(any(TriggerData.class));
    }

    private ArrayList<EmbeddedTrigger> saveAndGetActiveTriggers(Context context, TriggerData triggerData) {
        EngagementTriggerHelper.storeActiveTriggerDetails(context, triggerData);
        return EngagementTriggerHelper.getActiveTriggers(context);
    }

    @Test
    public void store_active_trigger() {
        assertThat(triggerData).isNotNull();
        assertThat(triggerData.getId()).isNotEmpty();
        assertThat(triggerData.getEngagementID()).isNotEmpty();
        assertThat(triggerData.getExpireAt()).isGreaterThan(0);

        ArrayList<EmbeddedTrigger> embeddedTriggers = saveAndGetActiveTriggers(context, triggerData);

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

        ArrayList<EmbeddedTrigger> embeddedTriggers = saveAndGetActiveTriggers(context, expiredTriggerData);

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