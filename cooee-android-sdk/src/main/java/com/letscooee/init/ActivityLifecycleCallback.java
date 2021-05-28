package com.letscooee.init;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.letscooee.brodcast.CooeeJobSchedulerBroadcast;
import com.letscooee.models.Event;
import com.letscooee.models.TriggerData;
import com.letscooee.retrofit.HttpCallsHelper;
import com.letscooee.schedular.jobschedular.CooeeScheduleJob;
import com.letscooee.trigger.CooeeEmptyActivity;
import com.letscooee.trigger.EngagementTriggerActivity;
import com.letscooee.utils.*;
import io.sentry.Sentry;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Register operations on different lifecycle callbacks of all Activities.
 *
 * @author Ashish Gaikwad
 * @version 0.2.9
 */
public class ActivityLifecycleCallback {

    private Handler handler = new Handler();
    private Runnable runnable;

    private final Context context;
    private final Application application;
    private final SessionManager sessionManager;

    ActivityLifecycleCallback(Application application) {
        this.application = application;
        this.context = application.getApplicationContext();
        this.sessionManager = SessionManager.getInstance(context);
    }

    /**
     * Used to register activity lifecycle
     */
    public void register() {
        RuntimeData runtimeData = RuntimeData.getInstance(this.context);

        SentryHelper.getInstance(context);
        checkAndStartJob(application.getApplicationContext());

        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
                EngagementTriggerActivity.captureWindowForBlurryEffect(activity);

                FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String token) {
                        HttpCallsHelper.setFirebaseToken(token, application.getApplicationContext());
                    }
                });
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

                handleGlassmorphismAfterLaunch(activity);
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
        });

        ProcessLifecycleOwner.get().getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_START)
            public void onEnterForeground() {
                runtimeData.setInForeground();

                keepSessionAlive(context);

                if (runtimeData.isFirstForeground()) {
                    return;
                }

                long backgroundDuration = runtimeData.getTimeInBackgroundInSeconds();

                if (backgroundDuration > CooeeSDKConstants.IDLE_TIME_IN_SECONDS) {
                    long duration = sessionManager.getTotalSessionDurationInSeconds();

                    HttpCallsHelper.sendSessionConcludedEvent(duration, application.getApplicationContext());

                    new PostLaunchActivity(context);
                    Log.d(CooeeSDKConstants.LOG_PREFIX, "After 30 min of App Background " + "Session Concluded");
                } else {
                    Map<String, Object> sessionProperties = new HashMap<>();
                    sessionProperties.put("CE Duration", backgroundDuration / 1000);

                    Event session = new Event("CE App Foreground", sessionProperties);
                    HttpCallsHelper.sendEvent(context, session, data -> PostLaunchActivity.createTrigger(application.getApplicationContext(), data));
                }
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
            public void onEnterBackground() {
                runtimeData.setInBackground();

                //stop sending check message of session alive on app background
                handler.removeCallbacks(runnable);

                if (context == null) {
                    return;
                }

                long duration = runtimeData.getTimeInForegroundInSeconds();

                Map<String, Object> sessionProperties = new HashMap<>();
                sessionProperties.put("CE Duration", duration);

                Event session = new Event("CE App Background", sessionProperties);
                HttpCallsHelper.sendEvent(context, session, null);
            }
        });
    }

    /**
     * Handles the creation of triggers
     *
     * @param activity
     */
    private void handleTriggerDataFromActivity(Activity activity) {
        Bundle bundle = activity.getIntent().getBundleExtra(CooeeSDKConstants.INTENT_BUNDLE_KEY);

        // Should not go ahead if bundle is null
        if (bundle == null) {
            return;
        }

        TriggerData triggerData = bundle.getParcelable(CooeeSDKConstants.INTENT_TRIGGER_DATA_KEY);

        // Should not go ahead if triggerData is null or triggerData's id is null
        if (triggerData == null || triggerData.getId() == null) {
            return;
        }

        new Timer().schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        PostLaunchActivity.createTrigger(context, triggerData);
                        HttpCallsHelper.sendEvent(context, new Event("CE Notification Clicked", new HashMap<>()), null);
                    }
                }, 4000);
    }

    /**
     * This block handle the glassmorphism effect for the triggers
     *
     * @param activity
     */
    private void handleGlassmorphismAfterLaunch(Activity activity) {
        // Do not entertain if activity is instance of EngagementTriggerActivity
        if (activity instanceof EngagementTriggerActivity) {
            return;
        }

        // Do not entertain if onInAppPopListener in not initialized
        if (EngagementTriggerActivity.onInAppPopListener == null) {
            return;
        }

        // Do not entertain if EngagementTriggerActivity's isManualClose set true
        if (EngagementTriggerActivity.isManualClose) {
            return;
        }

        String triggerString = LocalStorageHelper.getString(activity, "trigger", null);
        if (!TextUtils.isEmpty(triggerString)) {
            PostLaunchActivity.createTrigger(activity, new Gson().fromJson(triggerString, TriggerData.class));
        }
    }

    /**
     * This method will check if job is currently present or not with system
     * If job is not present it will add job in a queue
     *
     * @param context will be application context
     */
    private void checkAndStartJob(Context context) {
        if (!CooeeJobSchedulerBroadcast.isJobServiceOn(context)) {
            CooeeScheduleJob.scheduleJob(context);
        }
    }

    /**
     * send server check message every 5 min that session is still alive
     *
     * @param applicationContext
     */
    private void keepSessionAlive(Context applicationContext) {
        //send server check message every 5 min that session is still alive
        handler.postDelayed(runnable = new Runnable() {
            public void run() {
                handler.postDelayed(runnable, CooeeSDKConstants.KEEP_ALIVE_TIME_IN_MS);
                HttpCallsHelper.keepAlive(applicationContext);
                Log.d(CooeeSDKConstants.LOG_PREFIX, "Sent keep alive call");
            }
        }, CooeeSDKConstants.KEEP_ALIVE_TIME_IN_MS);
    }
}
