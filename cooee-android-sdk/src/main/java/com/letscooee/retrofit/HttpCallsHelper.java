package com.letscooee.retrofit;

import android.util.Log;

import androidx.annotation.NonNull;

import com.letscooee.init.AppController;
import com.letscooee.init.PostLaunchActivity;
import com.letscooee.models.Event;
import com.letscooee.utils.Closure;
import com.letscooee.utils.CooeeSDKConstants;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * HttpCallsHelper will be used to create http calls to the server
 *
 * @author Abhishek Taparia
 */
public final class HttpCallsHelper {
    static ServerAPIService serverAPIService = APIClient.getServerAPIService();

    public static void sendEvent(Event event, String msg) {
        PostLaunchActivity.onSDKStateDecided.subscribe((Object ignored) -> {
            sendEventWithoutSDKState(event, msg, null);

        }, (Throwable error) -> {
            Log.e(CooeeSDKConstants.LOG_PREFIX, "Observable Error : " + error.toString());
        });
    }

    public static void sendEventWithoutSDKState(Event event, String msg, Closure closure) {
        event.setSessionID(PostLaunchActivity.currentSessionId);
        event.setScreenName(AppController.currentScreen);
        event.setSessionNumber(PostLaunchActivity.currentSessionNumber);

        serverAPIService.sendEvent(event).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                Log.i(CooeeSDKConstants.LOG_PREFIX, msg + " Event Sent Code : " + response.code());

                if (closure != null) {
                    closure.call(response.body());
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.e(CooeeSDKConstants.LOG_PREFIX, msg + " Event Sent Error Message" + t.toString());
            }
        });
    }

    public static void sendUserProfile(Map<String, Object> userMap, String msg) {
        PostLaunchActivity.onSDKStateDecided.subscribe((Object ignored) -> {
            Call<ResponseBody> call = serverAPIService.updateProfile(userMap);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    Log.i(CooeeSDKConstants.LOG_PREFIX, msg + " User Profile Response Code : " + response.code());
                }

                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    // TODO Saving the request locally so that it can be sent later
                    Log.e(CooeeSDKConstants.LOG_PREFIX, msg + " User Profile Error Message : " + t.toString());
                }
            });
        }, (Throwable error) -> {
            Log.e(CooeeSDKConstants.LOG_PREFIX, "Observable Error : " + error.toString());
        });
    }

    public static void sendSessionConcludedEvent(String sessionID, int duration) {
        //TODO remove session from parameter and also update sendSessionConcludedEvent method in ServerAPIService
        PostLaunchActivity.onSDKStateDecided.subscribe((Object ignored) -> {
            serverAPIService.concludeSession(sessionID, duration).enqueue(new Callback<ResponseBody>() {
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
}
