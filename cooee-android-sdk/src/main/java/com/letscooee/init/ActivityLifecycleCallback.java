package com.letscooee.init;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.letscooee.CooeeFactory;
import com.letscooee.models.Event;
import com.letscooee.models.TriggerData;
import com.letscooee.trigger.CooeeEmptyActivity;
import com.letscooee.trigger.EngagementTriggerHelper;
import com.letscooee.trigger.inapp.InAppTriggerActivity;
import com.letscooee.utils.Constants;
import com.letscooee.utils.LocalStorageHelper;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Register callbacks of different lifecycle of all the activities.
 *
 * @author Ashish Gaikwad
 * @version 0.2.9
 */
public class ActivityLifecycleCallback implements Application.ActivityLifecycleCallbacks {

    private final Context context;

    ActivityLifecycleCallback(Context context) {
        this.context = context;
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        InAppTriggerActivity.captureWindowForBlurryEffect(activity);
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        handleTriggerDataFromActivity(activity);

        if (activity instanceof CooeeEmptyActivity) {
            activity.finish();
        }
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
    }

    /**
     * Handles the creation of triggers
     *
     * @param activity
     */
    private void handleTriggerDataFromActivity(Activity activity) {
        Bundle bundle = activity.getIntent().getBundleExtra(Constants.INTENT_BUNDLE_KEY);

        // Should not go ahead if bundle is null
        if (bundle == null) {
            return;
        }

        TriggerData triggerData = bundle.getParcelable(Constants.INTENT_TRIGGER_DATA_KEY);

        // Should not go ahead if triggerData is null or triggerData's id is null
        if (triggerData == null || triggerData.getId() == null) {
            return;
        }

        new Timer().schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        EngagementTriggerHelper.renderInAppTrigger(context, triggerData);
                        CooeeFactory.getSafeHTTPService().sendEvent(new Event("CE Notification Clicked", new HashMap<>()));
                    }
                }, 6000);
    }
}
