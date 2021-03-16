package com.letscooee.retrofit;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.letscooee.init.AppController;
import com.letscooee.init.PostLaunchActivity;
import com.letscooee.models.Event;
import com.letscooee.utils.Closure;
import com.letscooee.utils.CooeeSDKConstants;
import com.letscooee.utils.LocalStorageHelper;
import com.letscooee.utils.Utility;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * HttpCallsHelper will be used to create http calls to the server
 *
 * @author Abhishek Taparia
 */
public final class HttpCallsHelper {

    static ServerAPIService serverAPIService = APIClient.getServerAPIService();

    public static void sendEvent(Context context, Event event, Closure closure) {
        //noinspection ResultOfMethodCallIgnored
        PostLaunchActivity.onSDKStateDecided.subscribe((Object ignored) -> {
            event.setSessionID(PostLaunchActivity.currentSessionId);
            sendEventWithoutSDKState(context, event, closure);
        });
    }

    public static void sendEventWithoutSDKState(Context context, Event event, Closure closure) {
        event.setScreenName(AppController.currentScreen);
        event.setSessionNumber(PostLaunchActivity.currentSessionNumber);

        ArrayList<HashMap<String, String>> allTriggers = LocalStorageHelper.getList(context, CooeeSDKConstants.STORAGE_ACTIVE_TRIGGERS);

        ArrayList<HashMap<String, String>> activeTriggerList = new ArrayList<>();

        for (HashMap<String, String> map : allTriggers) {
            long time = Long.parseLong(map.get("duration"));
            long currentTime = new Date().getTime();
            if (time > currentTime) {
                activeTriggerList.add(map);
            }
        }

        event.setActiveTriggers(activeTriggerList);

        LocalStorageHelper.putListImmediately(context, CooeeSDKConstants.STORAGE_ACTIVE_TRIGGERS, activeTriggerList);

        serverAPIService.sendEvent(event).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                Log.i(CooeeSDKConstants.LOG_PREFIX, event.getName() + " Event Sent Code: " + response.code());

                if (closure != null) {
                    closure.call(response.body());
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.e(CooeeSDKConstants.LOG_PREFIX, event.getName() + " Event Sent Error Message: " + t.toString());
            }
        });
    }

    public static void sendUserProfile(Map<String, Object> userMap, String msg, Closure closure) {
        //noinspection ResultOfMethodCallIgnored
        PostLaunchActivity.onSDKStateDecided.subscribe((Object ignored) -> {
            userMap.put("sessionID", PostLaunchActivity.currentSessionId);
            serverAPIService.updateProfile(userMap).enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(@NonNull Call<Map<String, Object>> call, @NonNull Response<Map<String, Object>> response) {
                    Log.i(CooeeSDKConstants.LOG_PREFIX, msg + " User Profile Response Code : " + response.code());

                    if (closure == null) {          // space change
                        return;
                    }

                    if (response.body() != null) {          // space change
                        closure.call(response.body());
                    }
                }

                public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                    // TODO Saving the request locally so that it can be sent later
                    Log.e(CooeeSDKConstants.LOG_PREFIX, msg + " User Profile Error Message : " + t.toString());
                }
            });
        });
    }

    public static void sendSessionConcludedEvent(int duration) {
        //noinspection ResultOfMethodCallIgnored
        PostLaunchActivity.onSDKStateDecided.subscribe((Object ignored) -> {
            Map<String, Object> sessionConcludedRequest = new HashMap<>();
            sessionConcludedRequest.put("sessionID", PostLaunchActivity.currentSessionId);
            sessionConcludedRequest.put("duration", duration);

            serverAPIService.concludeSession(sessionConcludedRequest).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    Log.i(CooeeSDKConstants.LOG_PREFIX, "Session Concluded Event Sent Code : " + response.code());
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    Log.e(CooeeSDKConstants.LOG_PREFIX, "Session Concluded Event Sent Error Message" + t.toString());
                }
            });
        });
    }

    public static void keepAlive() {
        //noinspection ResultOfMethodCallIgnored
        PostLaunchActivity.onSDKStateDecided.subscribe((Object ignored) -> {
            Map<String, String> keepAliveRequest = new HashMap<>();
            keepAliveRequest.put("sessionID", PostLaunchActivity.currentSessionId);

            serverAPIService.keepAlive(keepAliveRequest).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Log.i(CooeeSDKConstants.LOG_PREFIX, "Session Alive Response Code : " + response.code());
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e(CooeeSDKConstants.LOG_PREFIX, "Session Alive Response Error Message" + t.toString());
                }
            });
        });
    }

    public static void setFirebaseToken(String firebaseToken) {
        PostLaunchActivity.onSDKStateDecided.subscribe((Object ignored) -> {
            Map<String, String> tokenRequest = new HashMap<>();
            tokenRequest.put("sessionID", PostLaunchActivity.currentSessionId);
            tokenRequest.put("firebaseToken", firebaseToken);

            serverAPIService.setFirebaseToken(tokenRequest).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Log.i(CooeeSDKConstants.LOG_PREFIX, "Firebase Token Response Code : " + response.code());
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e(CooeeSDKConstants.LOG_PREFIX, "Firebase Token Response Error Message" + t.toString());
                }
            });
        });
    }
}
