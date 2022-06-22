package com.letscooee.init;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.letscooee.CooeeFactory;
import com.letscooee.device.DebugInfoActivity;
import com.letscooee.gesture.ShakeDetector;
import com.letscooee.screenshot.ScreenshotHelper;
import com.letscooee.screenshot.ScreenshotUtility;
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
        // Activity created
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        // Activity started
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        InAppTriggerActivity.captureWindowForBlurryEffect(activity);
        EngagementTriggerHelper.setCurrentActivity(activity);

        new EngagementTriggerHelper(context).renderInAppFromPushNotification(activity);

        if (activity instanceof CooeeEmptyActivity) {
            activity.finish();
        }

        registerShakeDetector(activity);

        ScreenshotHelper screenshotHelper = ScreenshotUtility.getScreenshotHelper();
        if (screenshotHelper != null && !(activity instanceof PreventBlurActivity)) {
            screenshotHelper.onActivitySwitched(activity);
        }
    }

    /**
     * Register {@link ShakeDetector} to perform some operation when user shakes device
     *
     * @param activity will instance of current active {@link Activity} to which {@link ShakeDetector}
     *                 is going to register.
     */
    private void registerShakeDetector(Activity activity) {
        if (activity instanceof PreventBlurActivity) {
            return;
        }

        shakeDetector = new ShakeDetector(activity, CooeeFactory.getManifestReader().getShakeToDebugCount());
        shakeDetector.onShake((Object object) -> {
            Intent intent = new Intent(activity, DebugInfoActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            activity.startActivity(intent);
        });
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        if (shakeDetector == null) return;
        shakeDetector.unregisterListener();
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        // Activity Stopped
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
        // Activity saved instance state
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        // Activity distroyed
    }
}
