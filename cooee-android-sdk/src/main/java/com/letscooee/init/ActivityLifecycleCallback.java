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
import com.letscooee.CooeeSDK;
import com.letscooee.models.Event;
import com.letscooee.models.TriggerData;
import com.letscooee.retrofit.HttpCallsHelper;
import com.letscooee.trigger.CooeeEmptyActivity;
import com.letscooee.trigger.EngagementTriggerActivity;
import com.letscooee.utils.CooeeSDKConstants;
import com.letscooee.utils.LocalStorageHelper;
import com.letscooee.utils.SentryHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import io.sentry.Sentry;

/**
 * Track the activity lifecycle and perform related operations
 *
 * @author Ashish Gaikwad crated on 27/Apr/2021
 * @version 0.2
 */
public class ActivityLifecycleCallback {
    private static String currentScreen;
    private static boolean isBackground;
    private String packageName;
    private Date lastEnterForeground;
    private Date lastEnterBackground;

    private Handler handler = new Handler();
    private Runnable runnable;

    private Context context;

    /**
     * Used to register activity lifecycle
     *
     * @param application will be instance of application
     */
    public void register(Application application) {

        context = application.getApplicationContext();

        SentryHelper.getInstance(context);
        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
                packageName = activity.getClass().getPackage().getName();
                currentScreen = activity.getLocalClassName();

                // Updating current activity's window for glassmorphism effect
                if (!activity.getLocalClassName().endsWith("EngagementTriggerActivity")) {
                    EngagementTriggerActivity.setWindow(activity.getWindow());
                }

                PostLaunchActivity.setHeatMapRecorder(activity);

                FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String token) {
                        HttpCallsHelper.setFirebaseToken(token);
                    }
                });
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
                String manualScreenName = CooeeSDK.getDefaultInstance(context).getCurrentScreenName();
                currentScreen = (manualScreenName != null && !manualScreenName.isEmpty()) ? manualScreenName : activity.getLocalClassName();
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
                Log.d(CooeeSDKConstants.LOG_PREFIX, "AppController : Foreground");

                isBackground = false;

                keepSessionAlive();

                lastEnterForeground = new Date();

                // return if this method runs when app is just installed/launched
                if (lastEnterBackground == null) {
                    return;
                }

                long backgroundDuration = new Date().getTime() - lastEnterBackground.getTime();

                //Indentation to be solved
                if (backgroundDuration > CooeeSDKConstants.IDLE_TIME_IN_MS) {
                    int duration = (int) (lastEnterBackground.getTime() - PostLaunchActivity.currentSessionStartTime.getTime()) / 1000;
                    Map<String, String> sessionProperties = new HashMap<>();
                    sessionProperties.put("CE Duration", duration + "");

                    HttpCallsHelper.sendSessionConcludedEvent(duration);

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
                Log.d(CooeeSDKConstants.LOG_PREFIX, "AppController : Background");

                isBackground = true;

                //stop sending check message of session alive on app background
                handler.removeCallbacks(runnable);

                if (context == null) {
                    return;
                }

                //Purposefully not added to another method, will be taken cared in separate-http-call merge
                PostLaunchActivity.onSDKStateDecided.subscribe((Object ignored) -> {
                    lastEnterBackground = new Date();
                    long duration = (lastEnterBackground.getTime() - lastEnterForeground.getTime()) / 1000;

                    Map<String, Object> sessionProperties = new HashMap<>();
                    sessionProperties.put("CE Duration", duration);

                    Event session = new Event("CE App Background", sessionProperties);
                    HttpCallsHelper.sendEvent(context, session, null);
                });
                //getApplicationContext().startService(new Intent(getApplicationContext(), GlobalTouchService.class));
                formatAndSendTouchData(context);
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
     * Will process map present in local storage and will send to server
     *
     * @param context application context
     */
    private void formatAndSendTouchData(Context context) {
        Map map = LocalStorageHelper.getTouchMap(context, CooeeSDKConstants.TOUCH_MAP);
        Map processedData = new HashMap<String, Integer>();
        if (map != null) {

            for (Object key : map.keySet()) {
                Map<String, Double> data = (Map<String, Double>) map.get(key);
                String newKey = data.get("x") + "," + data.get("y");
                if (processedData.containsKey(newKey)) {
                    processedData.put(newKey, ((int) processedData.get(newKey)) + 1);
                } else {
                    processedData.put(newKey, 1);
                }

            }
            JSONArray touchData = new JSONArray();
            for (Object key : processedData.keySet()) {
                try {
                    JSONObject singlePoint = new JSONObject();
                    singlePoint.put("x", Double.valueOf(key.toString().split(",")[0]));
                    singlePoint.put("y", Double.valueOf(key.toString().split(",")[1]));
                    singlePoint.put("heatCount", processedData.get(key));
                    touchData.put(singlePoint);
                } catch (Exception e) {
                    Sentry.captureException(e);
                }
            }
            //Log.i(CooeeSDKConstants.LOG_PREFIX, "formatAndSendTouchData: "+touchData.toString());
        }
    }

    /**
     * send server check message every 5 min that session is still alive
     */
    private void keepSessionAlive() {
        //send server check message every 5 min that session is still alive
        handler.postDelayed(runnable = new Runnable() {
            public void run() {
                handler.postDelayed(runnable, CooeeSDKConstants.KEEP_ALIVE_TIME_IN_MS);
                HttpCallsHelper.keepAlive();
                Log.d(CooeeSDKConstants.LOG_PREFIX, "Sent keep alive call");
            }
        }, CooeeSDKConstants.KEEP_ALIVE_TIME_IN_MS);
    }

    public static String getCurrentScreen() {
        return currentScreen;
    }

    public static boolean isIsBackground() {
        return isBackground;
    }


}
