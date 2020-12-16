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
import com.letscooee.CooeeSDK;
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

    private String lastScreen;
    private String packageName;
    private Date startTime;
    private long startUp;

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onEnterForeground() {
        Log.d(CooeeSDKConstants.LOG_PREFIX, "AppController : Foreground");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onEnterBackground() {
        Log.d(CooeeSDKConstants.LOG_PREFIX, "AppController : Background");

        if (getApplicationContext() == null) {
            return;
        }

        PostLaunchActivity.onSDKStateDecided.subscribe((Object ignored) -> {
            Map<String, String> userProperties = new HashMap<>();
            userProperties.put("CE Last Screen", lastScreen);
            userProperties.put("CE Package Name", packageName);

            Map<String, Object> userMap = new HashMap<>();
            userMap.put("userProperties", userProperties);

            ServerAPIService apiService = APIClient.getServerAPIService();
            apiService.updateProfile(userMap).enqueue(new Callback<ResponseBody>() {
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

            String stopTime = new Date().toString();
            String duration = (new Date(stopTime).getTime() - new Date(PostLaunchActivity.currentSessionStartTime).getTime()) / 1000 + "s";

            Map<String, String> sessionProperties = new HashMap<>();
            sessionProperties.put("CE Session ID", PostLaunchActivity.currentSessionId);
            sessionProperties.put("CE Session Start", PostLaunchActivity.currentSessionStartTime);
            sessionProperties.put("CE Session Stop", stopTime);
            sessionProperties.put("CE Session Duration", duration);
            sessionProperties.put("CE Session Start Up Time", String.valueOf((this.startUp / 1000)));
            Event session = new Event("CE Session", sessionProperties);
            apiService.sendEvent(session).enqueue(new Callback<Campaign>() {
                @Override
                public void onResponse(Call<Campaign> call, Response<Campaign> response) {
                    Log.i(CooeeSDKConstants.LOG_PREFIX, "Session Event Sent Code : " + response.code());
                }

                @Override
                public void onFailure(Call<Campaign> call, Throwable t) {
                    // TODO Saving the request locally so that it can be sent later
                    Log.e(CooeeSDKConstants.LOG_PREFIX, "Session Event Sent Error Message" + t.toString());
                }
            });
        }, (Throwable error) -> {
            Log.e(CooeeSDKConstants.LOG_PREFIX, "Observable Error : " + error.toString());
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
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        String manualScreenName = CooeeSDK.getDefaultInstance(null).getCurrentScreenName();
        this.lastScreen = (manualScreenName != null && !manualScreenName.isEmpty()) ? manualScreenName : activity.getLocalClassName();
        packageName = activity.getClass().getPackage().getName();
        Log.d(CooeeSDKConstants.LOG_PREFIX + " ActivityStarts", lastScreen);
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        this.startUp = new Date().getTime() - this.startTime.getTime();
        Log.d(CooeeSDKConstants.LOG_PREFIX, "Start up time : " + this.startUp);
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
