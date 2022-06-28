package com.letscooee.trigger.inapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.room.Room;
import com.letscooee.BaseTestCase;
import com.letscooee.TestCaseActivity;
import com.letscooee.exceptions.InvalidTriggerDataException;
import com.letscooee.room.CooeeDatabase;
import com.letscooee.trigger.TriggerDataHelper;
import com.letscooee.utils.Constants;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.util.ReflectionHelpers;

import static org.mockito.Mockito.*;

public class InAppTriggerActivityTest extends BaseTestCase {

    enum PayloadProperty {
        BG, BR, SPC, CLC, SHD, TRF, GVT, ANIM, ELEMS, CONT, IAN
    }

    Intent validIntent;
    Intent intentWithNoBundle;
    Intent intentWithNoTriggerData;

    TestCaseActivity dummyActivity;

    @Before
    @Override
    public void setUp() {
        super.setUp();

        context = RuntimeEnvironment.getApplication().getApplicationContext();

        loadPayload();

        makeActiveTrigger();
        createIntent();
        Room.inMemoryDatabaseBuilder(context, CooeeDatabase.class).build();

        dummyActivity = Robolectric.buildActivity(TestCaseActivity.class).create().get();
        runtimeData.setCurrentActivity(dummyActivity);
    }

    private void createIntent() {
        intentWithNoBundle = new Intent(context, InAppTriggerActivity.class);

        Bundle bundle = new Bundle();
        intentWithNoTriggerData = new Intent(context, InAppTriggerActivity.class);
        intentWithNoTriggerData.putExtra("bundle", bundle);

        Bundle bundle1 = new Bundle();
        validIntent = new Intent(context, InAppTriggerActivity.class);
        bundle1.putParcelable(Constants.INTENT_TRIGGER_DATA_KEY, triggerData);
        validIntent.putExtra("bundle", bundle1);
    }

    private static <T extends Activity> T getSpy(ActivityController<T> activityController) {
        T spy = spy(activityController.get());
        ReflectionHelpers.setField(activityController, "component", spy);
        return spy;
    }

    private void updatePayloadAndIntent(@NonNull PayloadProperty property) {
        String updatedPayload = samplePayload.replace(property.name().toLowerCase(), property.name().toLowerCase() + "x");

        try {
            triggerData = TriggerDataHelper.parse(updatedPayload);
        } catch (InvalidTriggerDataException e) {
            e.printStackTrace();
        }

        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.INTENT_TRIGGER_DATA_KEY, triggerData);
        validIntent.putExtra("bundle", bundle);
    }

    private void commonTestCheck(ActivityController<InAppTriggerActivity> controller, InAppTriggerActivity spy, boolean isValid) {
        doNothing().when(spy).sendTriggerDisplayedEvent();
        controller.create();

        if (isValid) {
            verify(spy, times(1)).sendTriggerDisplayedEvent();
        } else {
            verify(spy, times(0)).sendTriggerDisplayedEvent();
        }
    }

    @Test
    public void on_create_with_valid_intent() {
        ActivityController<InAppTriggerActivity> controller = Robolectric.buildActivity(InAppTriggerActivity.class, validIntent);
        InAppTriggerActivity spy = getSpy(controller);

        commonTestCheck(controller, spy, true);
    }

    @Test
    public void on_create_with_no_trigger_data() {
        ActivityController<InAppTriggerActivity> controller = Robolectric.buildActivity(InAppTriggerActivity.class, intentWithNoTriggerData);
        InAppTriggerActivity spy = getSpy(controller);

        commonTestCheck(controller, spy, false);
    }

    @Test
    public void on_create_with_no_bundle_data() {
        ActivityController<InAppTriggerActivity> controller = Robolectric.buildActivity(InAppTriggerActivity.class, intentWithNoBundle);
        InAppTriggerActivity spy = getSpy(controller);

        commonTestCheck(controller, spy, false);
    }

    @Test
    public void on_create_with_valid_intent_no_background() {
        updatePayloadAndIntent(PayloadProperty.BG);
        ActivityController<InAppTriggerActivity> controller = Robolectric.buildActivity(InAppTriggerActivity.class, validIntent);
        InAppTriggerActivity spy = getSpy(controller);

        commonTestCheck(controller, spy, true);
    }

    @Test
    public void on_create_with_valid_intent_no_border() {
        updatePayloadAndIntent(PayloadProperty.BR);
        ActivityController<InAppTriggerActivity> controller = Robolectric.buildActivity(InAppTriggerActivity.class, validIntent);
        InAppTriggerActivity spy = getSpy(controller);

        commonTestCheck(controller, spy, true);
    }

    @Test
    public void on_create_with_valid_intent_no_spacing() {
        updatePayloadAndIntent(PayloadProperty.SPC);
        ActivityController<InAppTriggerActivity> controller = Robolectric.buildActivity(InAppTriggerActivity.class, validIntent);
        InAppTriggerActivity spy = getSpy(controller);

        commonTestCheck(controller, spy, true);
    }

    @Test
    public void on_create_with_valid_intent_no_click_action() {
        updatePayloadAndIntent(PayloadProperty.CLC);
        ActivityController<InAppTriggerActivity> controller = Robolectric.buildActivity(InAppTriggerActivity.class, validIntent);
        InAppTriggerActivity spy = getSpy(controller);

        commonTestCheck(controller, spy, true);
    }

    @Test
    public void on_create_with_valid_intent_no_shadow() {
        updatePayloadAndIntent(PayloadProperty.SHD);
        ActivityController<InAppTriggerActivity> controller = Robolectric.buildActivity(InAppTriggerActivity.class, validIntent);
        InAppTriggerActivity spy = getSpy(controller);

        commonTestCheck(controller, spy, true);
    }

    @Test
    public void on_create_with_valid_intent_no_transform() {
        updatePayloadAndIntent(PayloadProperty.TRF);
        ActivityController<InAppTriggerActivity> controller = Robolectric.buildActivity(InAppTriggerActivity.class, validIntent);
        InAppTriggerActivity spy = getSpy(controller);

        commonTestCheck(controller, spy, true);
    }

    @Test
    public void on_create_with_valid_intent_no_in_app_gravity() {
        updatePayloadAndIntent(PayloadProperty.GVT);
        ActivityController<InAppTriggerActivity> controller = Robolectric.buildActivity(InAppTriggerActivity.class, validIntent);
        InAppTriggerActivity spy = getSpy(controller);

        commonTestCheck(controller, spy, true);
    }

    @Test
    public void on_create_with_valid_intent_no_in_app_animation() {
        updatePayloadAndIntent(PayloadProperty.ANIM);
        ActivityController<InAppTriggerActivity> controller = Robolectric.buildActivity(InAppTriggerActivity.class, validIntent);
        InAppTriggerActivity spy = getSpy(controller);

        commonTestCheck(controller, spy, true);
    }

    @Test
    public void on_create_with_valid_intent_no_in_app_elements() {
        updatePayloadAndIntent(PayloadProperty.ELEMS);
        ActivityController<InAppTriggerActivity> controller = Robolectric.buildActivity(InAppTriggerActivity.class, validIntent);
        InAppTriggerActivity spy = getSpy(controller);

        commonTestCheck(controller, spy, false);
    }

    @Test
    public void on_create_with_valid_intent_no_in_app_container() {
        updatePayloadAndIntent(PayloadProperty.CONT);
        ActivityController<InAppTriggerActivity> controller = Robolectric.buildActivity(InAppTriggerActivity.class, validIntent);
        InAppTriggerActivity spy = getSpy(controller);

        commonTestCheck(controller, spy, false);
    }

    @Test
    public void on_create_with_valid_intent_no_in_app() {
        updatePayloadAndIntent(PayloadProperty.IAN);
        ActivityController<InAppTriggerActivity> controller = Robolectric.buildActivity(InAppTriggerActivity.class, validIntent);
        InAppTriggerActivity spy = getSpy(controller);

        commonTestCheck(controller, spy, false);
    }

}