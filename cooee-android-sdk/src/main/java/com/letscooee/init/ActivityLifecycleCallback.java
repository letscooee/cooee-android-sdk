package com.letscooee.init;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.letscooee.CooeeFactory;
import com.letscooee.gesture.ShakeDetector;
import com.letscooee.trigger.CooeeEmptyActivity;
import com.letscooee.trigger.EngagementTriggerHelper;
import com.letscooee.trigger.inapp.InAppTriggerActivity;
import com.letscooee.trigger.inapp.PreventBlurActivity;

/**
 * Register callbacks of different lifecycle of all the activities.
 *
 * @author Ashish Gaikwad
 * @version 0.2.9
 */
public class ActivityLifecycleCallback implements Application.ActivityLifecycleCallbacks {

    private final Context context;
    private ShakeDetector shakeDetector;

    ActivityLifecycleCallback(Context context) {
        this.context = context;
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        InAppTriggerActivity.captureWindowForBlurryEffect(activity);
        InAppTriggerActivity.captureWindowForBlurryEffect(activity);
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        EngagementTriggerHelper.renderInAppFromPushNotification(context, activity);

        if (activity instanceof CooeeEmptyActivity) {
            activity.finish();
        }

        if (activity instanceof PreventBlurActivity) {
            return;
        }

        shakeDetector = new ShakeDetector(activity);
        shakeDetector.setShakeCount(CooeeFactory.getManifestReader().getShakeToDebugCount());
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        if (shakeDetector == null) return;
        shakeDetector.unregisterListener();
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
}
