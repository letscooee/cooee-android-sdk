package com.letscooee.trigger;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.room.Room;
import com.letscooee.BaseTestCase;
import com.letscooee.models.trigger.EmbeddedTrigger;
import com.letscooee.models.trigger.TriggerData;
import com.letscooee.room.CooeeDatabase;
import com.letscooee.trigger.adapters.TriggerGsonDeserializer;
import com.letscooee.utils.Constants;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.R)
public class EngagementTriggerHelperTest extends BaseTestCase {

    String samplePayload;
    TriggerData triggerData;
    TriggerData expiredTriggerData;

    EngagementTriggerHelper engagementTriggerHelperMock;
    EngagementTriggerHelper engagementTriggerHelper;

    CooeeEmptyActivity activity;
    CooeeEmptyActivity activityWithNoBundle;
    CooeeEmptyActivity activityWithNoTriggerData;
    CooeeDatabase database;

    @Before
    @Override
    public void setUp() {
        super.setUp();
        context = RuntimeEnvironment.getApplication().getApplicationContext();
        createActivity();
        database = Room.inMemoryDatabaseBuilder(context, CooeeDatabase.class).build();
        engagementTriggerHelper = new EngagementTriggerHelper(context);
        engagementTriggerHelperMock = Mockito.spy(engagementTriggerHelper);

        try {
            InputStream inputStream = context.getAssets().open("payload_2.json");
            samplePayload = new Scanner(inputStream).useDelimiter("\\A").next();
        } catch (IOException e) {
            e.printStackTrace();
        }
        expiredTriggerData = TriggerGsonDeserializer.getGson().fromJson(samplePayload, TriggerData.class);
        makeActiveTrigger();
    }

    @Ignore("Not a test case")
    private void createActivity() {
        Intent intent = new Intent(context, CooeeEmptyActivity.class);
        activityWithNoBundle = Robolectric.buildActivity(CooeeEmptyActivity.class, intent).create().get();

        Bundle bundle = new Bundle();
        intent.putExtra(Constants.INTENT_BUNDLE_KEY, bundle);
        activityWithNoTriggerData = Robolectric.buildActivity(CooeeEmptyActivity.class, intent).create().get();

        bundle.putParcelable(Constants.INTENT_TRIGGER_DATA_KEY, triggerData);
        intent.putExtra(Constants.INTENT_BUNDLE_KEY, bundle);

        activity = Robolectric.buildActivity(CooeeEmptyActivity.class, intent).create().get();
    }

    @Ignore("Not a test case")
    private void makeActiveTrigger() {
        try {
            JSONObject jsonObject = new JSONObject(samplePayload);
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, 5);
            jsonObject.put("expireAt", calendar.getTimeInMillis());
            triggerData = TriggerGsonDeserializer.getGson().fromJson(jsonObject.toString(), TriggerData.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void render_in_app_from_response() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("triggerData", samplePayload);

        assertThat(payload).isNotNull();
        assertThat(payload.get("triggerData")).isNotNull();

        doNothing().when(engagementTriggerHelperMock).renderInAppTriggerFromJSONString(anyString());

        engagementTriggerHelperMock.renderInAppTriggerFromResponse(payload);

        verify(engagementTriggerHelperMock, times(1)).renderInAppTriggerFromJSONString(samplePayload);
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
    public void render_in_app_from_json_string() {
        assertThat(samplePayload).isNotEmpty();

        doNothing().when(engagementTriggerHelperMock).renderInAppTrigger(any(TriggerData.class));

        engagementTriggerHelperMock.renderInAppTriggerFromJSONString(samplePayload);

        verify(engagementTriggerHelperMock, times(1)).renderInAppTrigger(any(TriggerData.class));
    }

    @Test
    public void render_in_app_from_json_string_null_string() {
        samplePayload = null;

        assertThat(samplePayload).isNull();

        doNothing().when(engagementTriggerHelperMock).renderInAppTrigger(any(TriggerData.class));

        engagementTriggerHelperMock.renderInAppTriggerFromJSONString(samplePayload);

        verify(engagementTriggerHelperMock, times(0)).renderInAppTrigger(triggerData);
    }

    @Test
    public void render_in_app_from_json_string_empty_string() {
        samplePayload = "";

        assertThat(samplePayload).isNotNull();

        doNothing().when(engagementTriggerHelperMock).renderInAppTrigger(any(TriggerData.class));

        engagementTriggerHelperMock.renderInAppTriggerFromJSONString(samplePayload);

        verify(engagementTriggerHelperMock, times(0)).renderInAppTrigger(triggerData);
    }

    @Ignore("Failing due to overloaded methods")
    @Test
    public void render_in_app_from_notification_from_activity() {
        assertThat(activity).isNotNull();

        doNothing().when(engagementTriggerHelperMock).renderInAppFromPushNotification(any(TriggerData.class));

        engagementTriggerHelperMock.renderInAppFromPushNotification(activity);

        verify(engagementTriggerHelperMock, times(1)).renderInAppFromPushNotification(any(TriggerData.class));
    }

    @Ignore("Failing due to overloaded methods")
    @Test
    public void render_in_app_from_notification_from_activity_with_no_extra() {
        assertThat(activityWithNoBundle).isNotNull();

        doNothing().when(engagementTriggerHelperMock).renderInAppFromPushNotification(any(TriggerData.class));

        engagementTriggerHelperMock.renderInAppFromPushNotification(activityWithNoBundle);

        verify(engagementTriggerHelperMock, times(0)).renderInAppFromPushNotification(any(TriggerData.class));
    }

    @Ignore("Failing due to overloaded methods")
    @Test
    public void render_in_app_from_notification_from_activity_with_no_trigger_data_in_extra() {
        assertThat(activityWithNoBundle).isNotNull();

        doNothing().when(engagementTriggerHelperMock).renderInAppFromPushNotification(any(TriggerData.class));

        engagementTriggerHelperMock.renderInAppFromPushNotification(activityWithNoBundle);

        verify(engagementTriggerHelperMock, times(0)).renderInAppFromPushNotification(any(TriggerData.class));
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

    @Test
    public void store_active_trigger() {
        assertThat(triggerData).isNotNull();
        assertThat(triggerData.getId()).isNotEmpty();
        assertThat(triggerData.getEngagementID()).isNotEmpty();
        assertThat(triggerData.getExpireAt()).isGreaterThan(0);

        EngagementTriggerHelper.storeActiveTriggerDetails(context, triggerData);

        ArrayList<EmbeddedTrigger> embeddedTriggers = EngagementTriggerHelper.getActiveTriggers(context);

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

        EngagementTriggerHelper.storeActiveTriggerDetails(context, expiredTriggerData);

        ArrayList<EmbeddedTrigger> embeddedTriggers = EngagementTriggerHelper.getActiveTriggers(context);

        assertThat(embeddedTriggers).isEmpty();
        assertThat(embeddedTriggers.size()).isEqualTo(0);
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        reset(engagementTriggerHelperMock);
        database.close();
    }
}