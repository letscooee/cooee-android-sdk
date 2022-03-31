package com.letscooee.trigger.inapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.room.Room;
import com.letscooee.BaseTestCase;
import com.letscooee.cooee.TestCaseActivity;
import com.letscooee.models.trigger.TriggerData;
import com.letscooee.room.CooeeDatabase;
import com.letscooee.trigger.adapters.TriggerGsonDeserializer;
import com.letscooee.utils.Constants;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Scanner;

import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.R)
public class InAppTriggerActivityTest extends BaseTestCase {

    enum PayloadProperty {
        BG, BR, SPC, CLC, SHD, TRF, GVT, ANIM, ELEMS, CONT, IAN
    }

    String samplePayload;
    TriggerData triggerData;

    Intent validIntent;
    Intent intentWithNoBundle;
    Intent intentWithNoTriggerData;

    TestCaseActivity dummyActivity;

    @Before
    @Override
    public void setUp() {
        super.setUp();

        context = RuntimeEnvironment.getApplication().getApplicationContext();
        try {
            InputStream inputStream = context.getAssets().open("payload_2.json");
            samplePayload = new Scanner(inputStream).useDelimiter("\\A").next();
        } catch (IOException e) {
            e.printStackTrace();
        }

        makeActiveTrigger();
        createActivity();
        Room.inMemoryDatabaseBuilder(context, CooeeDatabase.class).build();

        dummyActivity = Robolectric.buildActivity(TestCaseActivity.class).create().get();//.setupActivity(InAppTriggerActivity.class);
        InAppTriggerActivity.captureWindowForBlurryEffect(dummyActivity);
    }

    @Ignore("Not a test case")
    private void createActivity() {
        intentWithNoBundle = new Intent(context, InAppTriggerActivity.class);

        Bundle bundle = new Bundle();
        intentWithNoTriggerData = new Intent(context, InAppTriggerActivity.class);
        intentWithNoTriggerData.putExtra("bundle", bundle);

        Bundle bundle1 = new Bundle();
        validIntent = new Intent(context, InAppTriggerActivity.class);
        bundle1.putParcelable(Constants.INTENT_TRIGGER_DATA_KEY, triggerData);
        validIntent.putExtra("bundle", bundle1);
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

    @Ignore("Not a test case")
    public static <T extends Activity> T getSpy(ActivityController<T> activityController) {
        T spy = spy(activityController.get());
        ReflectionHelpers.setField(activityController, "component", spy);
        return spy;
    }

    @Ignore("Not a test case")
    public void updatePayloadAndIntent(@NonNull PayloadProperty property) {
        String updatedPayload = samplePayload.replace(property.name().toLowerCase(), property.name().toLowerCase() + "x");

        triggerData = TriggerGsonDeserializer.getGson().fromJson(updatedPayload, TriggerData.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.INTENT_TRIGGER_DATA_KEY, triggerData);
        validIntent.putExtra("bundle", bundle);
    }

    @Test
    public void on_create_with_valid_intent() {
        ActivityController<InAppTriggerActivity> controller = Robolectric.buildActivity(InAppTriggerActivity.class, validIntent);
        InAppTriggerActivity spy = getSpy(controller);

        doNothing().when(spy).sendTriggerDisplayedEvent();
        controller.create();//.postCreate(null).start().resume().pause().destroy();
        verify(spy, times(1)).sendTriggerDisplayedEvent();
    }

    @Test
    public void on_create_with_no_trigger_data() {
        ActivityController<InAppTriggerActivity> controller = Robolectric.buildActivity(InAppTriggerActivity.class, intentWithNoTriggerData);
        InAppTriggerActivity spy = getSpy(controller);

        doNothing().when(spy).sendTriggerDisplayedEvent();
        controller.create();
        verify(spy, never()).sendTriggerDisplayedEvent();
    }

    @Test
    public void on_create_with_no_bundle_data() {
        ActivityController<InAppTriggerActivity> controller = Robolectric.buildActivity(InAppTriggerActivity.class, intentWithNoBundle);
        InAppTriggerActivity spy = getSpy(controller);

        doNothing().when(spy).sendTriggerDisplayedEvent();
        controller.create();
        verify(spy, never()).sendTriggerDisplayedEvent();
    }

    @Test
    public void on_create_with_valid_intent_no_background() {
        updatePayloadAndIntent(PayloadProperty.BG);
        ActivityController<InAppTriggerActivity> controller = Robolectric.buildActivity(InAppTriggerActivity.class, validIntent);
        InAppTriggerActivity spy = getSpy(controller);

        doNothing().when(spy).sendTriggerDisplayedEvent();
        controller.create();
        verify(spy, times(1)).sendTriggerDisplayedEvent();
    }

    @Test
    public void on_create_with_valid_intent_no_border() {
        updatePayloadAndIntent(PayloadProperty.BR);
        ActivityController<InAppTriggerActivity> controller = Robolectric.buildActivity(InAppTriggerActivity.class, validIntent);
        InAppTriggerActivity spy = getSpy(controller);

        doNothing().when(spy).sendTriggerDisplayedEvent();
        controller.create();
        verify(spy, times(1)).sendTriggerDisplayedEvent();
    }

    @Test
    public void on_create_with_valid_intent_no_spacing() {
        updatePayloadAndIntent(PayloadProperty.SPC);
        ActivityController<InAppTriggerActivity> controller = Robolectric.buildActivity(InAppTriggerActivity.class, validIntent);
        InAppTriggerActivity spy = getSpy(controller);

        doNothing().when(spy).sendTriggerDisplayedEvent();
        controller.create();
        verify(spy, times(1)).sendTriggerDisplayedEvent();
    }

    @Test
    public void on_create_with_valid_intent_no_click_action() {
        updatePayloadAndIntent(PayloadProperty.CLC);
        ActivityController<InAppTriggerActivity> controller = Robolectric.buildActivity(InAppTriggerActivity.class, validIntent);
        InAppTriggerActivity spy = getSpy(controller);

        doNothing().when(spy).sendTriggerDisplayedEvent();
        controller.create();
        verify(spy, times(1)).sendTriggerDisplayedEvent();
    }

    @Test
    public void on_create_with_valid_intent_no_shadow() {
        updatePayloadAndIntent(PayloadProperty.SHD);
        ActivityController<InAppTriggerActivity> controller = Robolectric.buildActivity(InAppTriggerActivity.class, validIntent);
        InAppTriggerActivity spy = getSpy(controller);

        doNothing().when(spy).sendTriggerDisplayedEvent();
        controller.create();
        verify(spy, times(1)).sendTriggerDisplayedEvent();
    }

    @Test
    public void on_create_with_valid_intent_no_transform() {
        updatePayloadAndIntent(PayloadProperty.TRF);
        ActivityController<InAppTriggerActivity> controller = Robolectric.buildActivity(InAppTriggerActivity.class, validIntent);
        InAppTriggerActivity spy = getSpy(controller);

        doNothing().when(spy).sendTriggerDisplayedEvent();
        controller.create();
        verify(spy, times(1)).sendTriggerDisplayedEvent();
    }

    @Test
    public void on_create_with_valid_intent_no_in_app_gravity() {
        updatePayloadAndIntent(PayloadProperty.GVT);
        ActivityController<InAppTriggerActivity> controller = Robolectric.buildActivity(InAppTriggerActivity.class, validIntent);
        InAppTriggerActivity spy = getSpy(controller);

        doNothing().when(spy).sendTriggerDisplayedEvent();
        controller.create();
        verify(spy, times(1)).sendTriggerDisplayedEvent();
    }

    @Test
    public void on_create_with_valid_intent_no_in_app_animation() {
        updatePayloadAndIntent(PayloadProperty.ANIM);
        ActivityController<InAppTriggerActivity> controller = Robolectric.buildActivity(InAppTriggerActivity.class, validIntent);
        InAppTriggerActivity spy = getSpy(controller);

        doNothing().when(spy).sendTriggerDisplayedEvent();
        controller.create();
        verify(spy, times(1)).sendTriggerDisplayedEvent();
    }

    @Test
    public void on_create_with_valid_intent_no_in_app_elements() {
        updatePayloadAndIntent(PayloadProperty.ELEMS);
        ActivityController<InAppTriggerActivity> controller = Robolectric.buildActivity(InAppTriggerActivity.class, validIntent);
        InAppTriggerActivity spy = getSpy(controller);

        doNothing().when(spy).sendTriggerDisplayedEvent();
        controller.create();
        verify(spy, never()).sendTriggerDisplayedEvent();
    }

    @Test
    public void on_create_with_valid_intent_no_in_app_container() {
        updatePayloadAndIntent(PayloadProperty.CONT);
        ActivityController<InAppTriggerActivity> controller = Robolectric.buildActivity(InAppTriggerActivity.class, validIntent);
        InAppTriggerActivity spy = getSpy(controller);

        doNothing().when(spy).sendTriggerDisplayedEvent();
        controller.create();
        verify(spy, never()).sendTriggerDisplayedEvent();
    }

    @Test
    public void on_create_with_valid_intent_no_in_app() {
        updatePayloadAndIntent(PayloadProperty.IAN);
        ActivityController<InAppTriggerActivity> controller = Robolectric.buildActivity(InAppTriggerActivity.class, validIntent);
        InAppTriggerActivity spy = getSpy(controller);

        doNothing().when(spy).sendTriggerDisplayedEvent();
        controller.create();
        verify(spy, never()).sendTriggerDisplayedEvent();
    }
}