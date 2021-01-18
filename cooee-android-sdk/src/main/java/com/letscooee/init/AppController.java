package com.letscooee.init;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
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

    public static String currentScreen;
    private String packageName;
    private Date startTime;
    private Date stopTime;
    private long startUp;

    private Handler handler = new Handler();
    private Runnable runnable;

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onEnterForeground() {
        Log.d(CooeeSDKConstants.LOG_PREFIX, "AppController : Foreground");

        ServerAPIService apiService = APIClient.getServerAPIService();

        //send server check message every 5 min that session is still alive
        handler.postDelayed(runnable = new Runnable() {
            public void run() {
                handler.postDelayed(runnable, 5 * 60 * 1000);
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
        }, 5 * 60 * 1000);

        if (stopTime == null) {
            return;
        }

        long backgroundDuration = new Date().getTime() - stopTime.getTime();

        PostLaunchActivity.onSDKStateDecided.subscribe((Object ignored) -> {
            if (backgroundDuration > CooeeSDKConstants.IDLE_TIME) {
                int duration = (int) (stopTime.getTime() - new Date(PostLaunchActivity.currentSessionStartTime).getTime()) / 1000;

                apiService.concludeSession(PostLaunchActivity.currentSessionId, duration).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                        Log.i(CooeeSDKConstants.LOG_PREFIX, "Session Concluded Event Sent Code : " + response.code());
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                        Log.e(CooeeSDKConstants.LOG_PREFIX, "Session Concluded Event Sent Error Message" + t.toString());
                    }
                });

                new PostLaunchActivity(getApplicationContext());
                Log.d(CooeeSDKConstants.LOG_PREFIX, "After 30 min of App Background " + "Session Concluded");
            } else {
                Map<String, String> sessionProperties = new HashMap<>();
                sessionProperties.put("CE Duration", String.valueOf(backgroundDuration / 1000));

                Event session = new Event("CE App Foreground", sessionProperties, PostLaunchActivity.currentSessionId, PostLaunchActivity.currentSessionNumber, currentScreen);
                apiService.sendEvent(session).enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(@NonNull Call<Map<String, Object>> call, @NonNull Response<Map<String, Object>> response) {
                        Log.i(CooeeSDKConstants.LOG_PREFIX, "App Foreground Event Sent Code : " + response.code());
                    }

                    @Override
                    public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                        Log.e(CooeeSDKConstants.LOG_PREFIX, "App Foreground Event Sent Error Message" + t.toString());
                    }
                });
            }
        });
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onEnterBackground() {
        Log.d(CooeeSDKConstants.LOG_PREFIX, "AppController : Background");

        //stop sending check message of session alive on app background
        handler.removeCallbacks(runnable);

        if (getApplicationContext() == null) {
            return;
        }

        PostLaunchActivity.onSDKStateDecided.subscribe((Object ignored) -> {
            Map<String, String> userProperties = new HashMap<>();
            userProperties.put("CE Last Screen", currentScreen);
            userProperties.put("CE Package Name", packageName);

            Map<String, Object> userMap = new HashMap<>();
            userMap.put("userProperties", userProperties);
            userMap.put("sessionID", PostLaunchActivity.currentSessionId);
            userMap.put("userData", new HashMap<>());

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

            stopTime = new Date();
            String duration = (stopTime.getTime() - new Date(PostLaunchActivity.currentSessionStartTime).getTime()) / 1000 + "";

            Map<String, String> sessionProperties = new HashMap<>();
            sessionProperties.put("CE Duration", duration);
            Event session = new Event("CE App Background", sessionProperties, PostLaunchActivity.currentSessionId, PostLaunchActivity.currentSessionNumber, currentScreen);
            apiService.sendEvent(session).enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    Log.i(CooeeSDKConstants.LOG_PREFIX, "App Background Event Sent Code : " + response.code());
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    // TODO Saving the request locally so that it can be sent later
                    Log.e(CooeeSDKConstants.LOG_PREFIX, "App Background Event Sent Error Message" + t.toString());
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
        currentScreen = activity.getLocalClassName();
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        String manualScreenName = CooeeSDK.getDefaultInstance(getApplicationContext()).getCurrentScreenName();
        currentScreen = (manualScreenName != null && !manualScreenName.isEmpty()) ? manualScreenName : activity.getLocalClassName();
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
