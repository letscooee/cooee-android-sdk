package com.letscooee.init;

import android.app.Activity;
import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.letscooee.CooeeSDK;

import com.letscooee.BuildConfig;
import com.letscooee.models.Event;
import com.letscooee.campaign.EngagementTriggerActivity;
import com.letscooee.models.TriggerData;
import com.letscooee.retrofit.APIClient;
import com.letscooee.retrofit.HttpCallsHelper;
import com.letscooee.retrofit.ServerAPIService;
import com.letscooee.utils.Closure;
import com.letscooee.utils.CooeeSDKConstants;
import com.letscooee.utils.LocalStorageHelper;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * AppController Class looks upon the lifecycle of the application, check if app is in foreground or background etc.
 *
 * @author Abhishek Taparia
 */
public class AppController extends Application implements LifecycleObserver, Application.ActivityLifecycleCallbacks {

    public static String currentScreen;
    private String packageName;
    private Date lastEnterForeground;
    private Date lastEnterBackground;

    private Handler handler = new Handler();
    private Runnable runnable;

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onEnterForeground() {
        Log.d(CooeeSDKConstants.LOG_PREFIX, "AppController : Foreground");

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

            new PostLaunchActivity(getApplicationContext());
            Log.d(CooeeSDKConstants.LOG_PREFIX, "After 30 min of App Background " + "Session Concluded");
        } else {
            Map<String, String> sessionProperties = new HashMap<>();
            sessionProperties.put("CE Duration", String.valueOf(backgroundDuration / 1000));

            Event session = new Event("CE App Foreground", sessionProperties);
            HttpCallsHelper.sendEvent(session, new Closure() {
                @Override
                public void call(Map<String, Object> data) {
                    PostLaunchActivity.createTrigger(getApplicationContext(), data);
                }
            });

        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onEnterBackground() {
        Log.d(CooeeSDKConstants.LOG_PREFIX, "AppController : Background");

        //stop sending check message of session alive on app background
        handler.removeCallbacks(runnable);

        if (getApplicationContext() == null) {
            return;
        }

        //Purposefully not added to another method, will be taken cared in separate-http-call merge
        PostLaunchActivity.onSDKStateDecided.subscribe((Object ignored) -> {
            ServerAPIService apiService = APIClient.getServerAPIService();

            lastEnterBackground = new Date();
            String duration = (lastEnterBackground.getTime() - lastEnterForeground.getTime()) / 1000 + "";

            Map<String, String> sessionProperties = new HashMap<>();
            sessionProperties.put("CE Duration", duration);

            Event session = new Event("CE App Background", sessionProperties);
            HttpCallsHelper.sendEvent(session, null);
        });
    }

    private void keepSessionAlive() {
        //send server check message every 5 min that session is still alive
        handler.postDelayed(runnable = new Runnable() {
            public void run() {
                handler.postDelayed(runnable, CooeeSDKConstants.KEEP_ALIVE_TIME_IN_MS);
//                PostLaunchActivity.onSDKStateDecided.subscribe((Object ignored) -> {
//                    apiService.keepAlive(PostLaunchActivity.currentSessionId).enqueue(new Callback<ResponseBody>() {
//                        @Override
//                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                            Log.i(CooeeSDKConstants.LOG_PREFIX, "Session Alive Response Code : " + response.code());
//                        }
//
//                        @Override
//                        public void onFailure(Call<ResponseBody> call, Throwable t) {
//                            Log.e(CooeeSDKConstants.LOG_PREFIX, "Session Alive Response Error Message" + t.toString());
//                        }
//                    });
//                });
                Log.d(CooeeSDKConstants.LOG_PREFIX, "Sent keep alive call");
            }
        }, CooeeSDKConstants.KEEP_ALIVE_TIME_IN_MS);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);

        // Code for bharat-post app migration from 0.0.3: need testing
        int version = LocalStorageHelper.getInt(getApplicationContext(), "sdk_version", 0);
        if (version == 0) {
            migrate();
        }
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
        packageName = activity.getClass().getPackage().getName();
        currentScreen = activity.getLocalClassName();
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        String manualScreenName = CooeeSDK.getDefaultInstance(getApplicationContext()).getCurrentScreenName();
        currentScreen = (manualScreenName != null && !manualScreenName.isEmpty()) ? manualScreenName : activity.getLocalClassName();

        Bundle bundle = activity.getIntent().getExtras();
//        TODO use better hook instead of bundle.getString("id")
        if (bundle != null && bundle.getString("id") != null) {
            Map<String, String> triggerDataMap = new HashMap<>();

            for (String key : bundle.keySet()) {
                triggerDataMap.put(key, String.valueOf(bundle.get(key)));
            }

            Map<String, Object> data = new HashMap<>();
            data.put("triggerData", triggerDataMap);
            PostLaunchActivity.createTrigger(getApplicationContext(), data);
        }
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
    }

    // Code for bharat-post app migration from 0.0.3 to 0.0.4: need testing
    private void migrate() {
        // Getting value from old storage
        boolean appLaunchFromOldVersion = getApplicationContext().getSharedPreferences("is_app_first_time_launch", MODE_PRIVATE).getBoolean("is_app_first_time_launch", true);
        String sdkTokenFromOldVersion = getApplicationContext().getSharedPreferences("com.letscooee.tester", MODE_PRIVATE).getString("com.letscooee.tester", "");

        Log.d(CooeeSDKConstants.LOG_PREFIX, "Old value of is app launch : " + appLaunchFromOldVersion);
        Log.d(CooeeSDKConstants.LOG_PREFIX, "Old value of SDK Token : " + sdkTokenFromOldVersion);

        // Updating value to new storage
        LocalStorageHelper.putBooleanImmediately(getApplicationContext(), CooeeSDKConstants.STORAGE_FIRST_TIME_LAUNCH, appLaunchFromOldVersion);
        LocalStorageHelper.putStringImmediately(getApplicationContext(), CooeeSDKConstants.STORAGE_SDK_TOKEN, sdkTokenFromOldVersion);
        LocalStorageHelper.putIntImmediately(getApplicationContext(), "sdk_version", BuildConfig.VERSION_CODE);

        // Delete the files from the local shared preference folder
        PackageInfo packageInfo;
        try {
            // Clearing data from the files
            getApplicationContext().getSharedPreferences("is_app_first_time_launch", MODE_PRIVATE).edit().clear().commit();
            getApplicationContext().getSharedPreferences("com.letscooee.tester", MODE_PRIVATE).edit().clear().commit();

            packageInfo = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0);
            File dir = new File(getApplicationContext().getFilesDir().getPath() + "/data/" + packageInfo.packageName + "/shared_prefs/");

            // Deleting the files
            boolean isAppLaunchFileDeleted = new File(dir + "/is_app_first_time_launch.xml").delete();
            boolean isSDKFileDeleted = new File(dir + "/com.letscooee.tester.xml").delete();
            Log.d(CooeeSDKConstants.LOG_PREFIX, "App Launch deleted : " + isAppLaunchFileDeleted);
            Log.d(CooeeSDKConstants.LOG_PREFIX, "SDK deleted : " + isSDKFileDeleted);

        } catch (Exception e) {
            Log.e(CooeeSDKConstants.LOG_PREFIX, "Could not delete the file locally");
        }
    }
}
