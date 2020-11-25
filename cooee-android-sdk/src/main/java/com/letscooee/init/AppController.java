package com.letscooee.init;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;
import com.letscooee.cooeesdk.CooeeSDK;
import com.letscooee.models.Campaign;
import com.letscooee.models.Event;
import com.letscooee.retrofit.APIClient;
import com.letscooee.retrofit.ServerAPIService;
import com.letscooee.utils.CooeeSDKConstants;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * AppController Class looks upon the lifecycle of the application, check if app is in foreground or background etc.
 *
 * @author Abhishek Taparia
 */
public class AppController extends Application implements LifecycleObserver, Application.ActivityLifecycleCallbacks {

    static String CURRENT_SCREEN;
    private String packageName;
    private Date startTime;
    private Date stopTime;
    private long startUp;

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onEnterForeground() {
        Log.d(CooeeSDKConstants.LOG_PREFIX, "AppController : Foreground");

        if (stopTime == null) {
            return;
        }

        long backgroundDuration = new Date().getTime() - stopTime.getTime();
        ServerAPIService apiService = APIClient.getServerAPIService();

        PostLaunchActivity.observable.subscribe((String sdkToken) -> {
            if (backgroundDuration > CooeeSDKConstants.IDLE_TIME) {
                String duration = (stopTime.getTime() - new Date(PostLaunchActivity.SESSION_START_TIME).getTime()) / 1000 + "s";
                Map<String, String> sessionProperties = new HashMap<>();
                sessionProperties.put("CE Duration", duration);

                Event session = new Event("CE Session Concluded", sessionProperties);
                apiService.sendEvent(sdkToken, session).enqueue(new Callback<Campaign>() {
                    @Override
                    public void onResponse(@NonNull Call<Campaign> call, @NonNull Response<Campaign> response) {
                        Log.i(CooeeSDKConstants.LOG_PREFIX, "Session Concluded Event Sent Code : " + response.code());
                    }

                    @Override
                    public void onFailure(@NonNull Call<Campaign> call, @NonNull Throwable t) {
                        Log.e(CooeeSDKConstants.LOG_PREFIX, "Session Concluded Event Sent Error Message" + t.toString());
                    }
                });

                new PostLaunchActivity(getApplicationContext()).createSession();
                Log.d(CooeeSDKConstants.LOG_PREFIX, "After 30 min of App Background " + "Session Concluded");
            } else {
                Map<String, String> sessionProperties = new HashMap<>();
                sessionProperties.put("CE Duration", String.valueOf(backgroundDuration / 1000));

                Event session = new Event("CE App Foreground", sessionProperties);
                apiService.sendEvent(sdkToken, session).enqueue(new Callback<Campaign>() {
                    @Override
                    public void onResponse(@NonNull Call<Campaign> call, @NonNull Response<Campaign> response) {
                        Log.i(CooeeSDKConstants.LOG_PREFIX, "App Foreground Event Sent Code : " + response.code());
                    }

                    @Override
                    public void onFailure(@NonNull Call<Campaign> call, @NonNull Throwable t) {
                        Log.e(CooeeSDKConstants.LOG_PREFIX, "App Foreground Event Sent Error Message" + t.toString());
                    }
                });
            }
        });
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onEnterBackground() {
        Log.d(CooeeSDKConstants.LOG_PREFIX, "AppController : " + "Background");

        if (getApplicationContext() == null) {
            return;
        }

        PostLaunchActivity.onSDKStateDecided.subscribe((Object ignored) -> {
            Map<String, String> userProperties = new HashMap<>();
            userProperties.put("CE Last Screen", CURRENT_SCREEN);
            userProperties.put("CE Package Name", packageName);

            Map<String, Object> userMap = new HashMap<>();
            userMap.put("userProperties", userProperties);

            ServerAPIService apiService = APIClient.getServerAPIService();
            apiService.updateProfile(sdkToken, userMap).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    Log.i(CooeeSDKConstants.LOG_PREFIX, "User Properties Response Code : " + response.code());
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    // TODO Saving the request locally so that it can be sent later
                    Log.e(CooeeSDKConstants.LOG_PREFIX, "User Properties Error Message : " + t.toString());
                }
            });

            stopTime = new Date();
            String duration = (stopTime.getTime() - new Date(PostLaunchActivity.SESSION_START_TIME).getTime()) / 1000 + "s";

            Map<String, String> sessionProperties = new HashMap<>();
            sessionProperties.put("CE Session ID", PostLaunchActivity.CURRENT_SESSION_ID);
            sessionProperties.put("CE Session Number", PostLaunchActivity.CURRENT_SESSION_NUMBER);
            sessionProperties.put("CE Session Start", PostLaunchActivity.SESSION_START_TIME);
            sessionProperties.put("CE Session Stop", stopTime.toString());
            sessionProperties.put("CE Duration", duration);
            Event session = new Event("CE App Background", sessionProperties);
            apiService.sendEvent(sdkToken, session).enqueue(new Callback<Campaign>() {
                @Override
                public void onResponse(Call<Campaign> call, Response<Campaign> response) {
                    Log.i(CooeeSDKConstants.LOG_PREFIX, "App Background Event Sent Code : " + response.code());
                }

                @Override
                public void onFailure(Call<Campaign> call, Throwable t) {
                    // TODO Saving the request locally so that it can be sent later
                    Log.e(CooeeSDKConstants.LOG_PREFIX, "Session Event Sent Error Message" + t.toString());
                }
            });
        });
    }

    @Override
    public void onCreate() {
        this.startTime = new Date();
        super.onCreate();
        registerActivityLifecycleCallbacks(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
        packageName = activity.getClass().getPackage().getName();
        CURRENT_SCREEN = activity.getLocalClassName();
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        String manualScreenName = CooeeSDK.getDefaultInstance(null).getCurrentScreenName();
        CURRENT_SCREEN = (manualScreenName != null && !manualScreenName.isEmpty()) ? manualScreenName : activity.getLocalClassName();
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        this.startUp = new Date().getTime() - this.startTime.getTime();
        Log.d(CooeeSDKConstants.LOG_PREFIX, "Start Up Time : " + this.startUp);
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
}
