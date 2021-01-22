package com.letscooee.retrofit;

import android.util.Log;
import androidx.annotation.NonNull;
import com.letscooee.init.AppController;
import com.letscooee.init.PostLaunchActivity;
import com.letscooee.models.Event;
import com.letscooee.utils.Closure;
import com.letscooee.utils.CooeeSDKConstants;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.Map;

/**
 * HttpCallsHelper will be used to create http calls to the server
 *
 * @author Abhishek Taparia
 */
public final class HttpCallsHelper {

    static ServerAPIService serverAPIService = APIClient.getServerAPIService();

    public static void sendEvent(Event event, Closure closure) {
        //noinspection ResultOfMethodCallIgnored
        PostLaunchActivity.onSDKStateDecided.subscribe((Object ignored) -> {
            event.setSessionID(PostLaunchActivity.currentSessionId);
            sendEventWithoutSDKState(event, closure);
        });
    }

    public static void sendEventWithoutSDKState(Event event, Closure closure) {
        event.setScreenName(AppController.currentScreen);
        event.setSessionNumber(PostLaunchActivity.currentSessionNumber);

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

    public static void sendUserProfile(Map<String, Object> userMap, String msg) {
        //noinspection ResultOfMethodCallIgnored
        PostLaunchActivity.onSDKStateDecided.subscribe((Object ignored) -> {
            userMap.put("sessionID", PostLaunchActivity.currentSessionId);
            serverAPIService.updateProfile(userMap).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    Log.i(CooeeSDKConstants.LOG_PREFIX, msg + " User Profile Response Code : " + response.code());
                }

                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    // TODO Saving the request locally so that it can be sent later
                    Log.e(CooeeSDKConstants.LOG_PREFIX, msg + " User Profile Error Message : " + t.toString());
                }
            });
        });
    }

    public static void sendSessionConcludedEvent(int duration) {
        //noinspection ResultOfMethodCallIgnored
        PostLaunchActivity.onSDKStateDecided.subscribe((Object ignored) -> {
            serverAPIService.concludeSession(PostLaunchActivity.currentSessionId, duration).enqueue(new Callback<ResponseBody>() {
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
