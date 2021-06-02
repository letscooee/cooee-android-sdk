package com.letscooee.retrofit;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.letscooee.BuildConfig;
import com.letscooee.init.ActivityLifecycleCallback;
import com.letscooee.init.PostLaunchActivity;
import com.letscooee.models.Event;
import com.letscooee.utils.Closure;
import com.letscooee.utils.CooeeSDKConstants;
import com.letscooee.utils.LocalStorageHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.sentry.Sentry;
import okhttp3.*;

/**
 * HttpCallsHelper will be used to create http calls to the server
 *
 * @author Abhishek Taparia
 */
public final class HttpCallsHelper {

    static OkHttpClient client = new OkHttpClient();
    private static String apiToken;
    private static String deviceName = "";
    private static String userId = "";

    public static void sendEvent(Context context, Event event, Closure closure) {
        //noinspection ResultOfMethodCallIgnored
        PostLaunchActivity.onSDKStateDecided.subscribe((Object ignored) -> {
            event.setSessionID(PostLaunchActivity.currentSessionId);
            sendEventWithoutSDKState(context, event, closure);
        });
    }

    public static void sendEventWithoutSDKState(Context context, Event event, Closure closure) {
        event.setScreenName(ActivityLifecycleCallback.getCurrentScreen());
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

        Request request = createRequest(event, CooeeSDKConstants.EVENT_PATH, CooeeSDKConstants.POST_METHOD, null);

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(CooeeSDKConstants.LOG_PREFIX, event.getName() + " Event Sent Error Message: " + e.toString());
                Sentry.captureException(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Log.i(CooeeSDKConstants.LOG_PREFIX, event.getName() + " Event Sent Code: " + response.code());

                if (closure != null) {
                    Map<String, Object> map = new Gson().fromJson(
                            response.body().string(), new TypeToken<HashMap<String, Object>>() {
                            }.getType()
                    );
                    closure.call(map);
                }
            }
        });
    }

    public static void sendUserProfile(Map<String, Object> userMap, String msg, Closure closure) {
        //noinspection ResultOfMethodCallIgnored
        PostLaunchActivity.onSDKStateDecided.subscribe((Object ignored) -> {
            userMap.put("sessionID", PostLaunchActivity.currentSessionId);

            Request request = createRequest(userMap,
                    CooeeSDKConstants.USER_PROFILE_PATH,
                    CooeeSDKConstants.PUT_METHOD,
                    null);
            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e(CooeeSDKConstants.LOG_PREFIX, msg + " User Profile Error Message : " + e.toString());
                    Sentry.captureException(e);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    Log.i(CooeeSDKConstants.LOG_PREFIX, msg + " User Profile Response Code : " + response.code());

                    if (closure == null) {          // space change
                        return;
                    }

                    if (response.body() != null) {          // space change
                        Map<String, Object> map = new Gson().fromJson(
                                response.body().string(), new TypeToken<HashMap<String, Object>>() {
                                }.getType()
                        );
                        closure.call(map);
                    }
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

            Request request = createRequest(sessionConcludedRequest,
                    CooeeSDKConstants.SESSION_CONCLUDED_PATH,
                    CooeeSDKConstants.POST_METHOD,
                    null);

            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e(CooeeSDKConstants.LOG_PREFIX, "Session Concluded Event Sent Error Message" + e.toString());
                    Sentry.captureException(e);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    Log.i(CooeeSDKConstants.LOG_PREFIX, "Session Concluded Event Sent Code : " + response.code());
                }
            });
        });
    }

    public static void keepAlive() {
        //noinspection ResultOfMethodCallIgnored
        PostLaunchActivity.onSDKStateDecided.subscribe((Object ignored) -> {
            Map<String, String> keepAliveRequest = new HashMap<>();
            keepAliveRequest.put("sessionID", PostLaunchActivity.currentSessionId);

            Request request = createRequest(keepAliveRequest,
                    CooeeSDKConstants.KEEP_ALIVE_PATH,
                    CooeeSDKConstants.POST_METHOD,
                    null);

            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e(CooeeSDKConstants.LOG_PREFIX, "Session Alive Response Error Message" + e.toString());
                    Sentry.captureException(e);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    Log.i(CooeeSDKConstants.LOG_PREFIX, "Session Alive Response Code : " + response.code());
                }
            });
        });
    }

    public static void setFirebaseToken(String firebaseToken) {
        PostLaunchActivity.onSDKStateDecided.subscribe((Object ignored) -> {
            Map<String, String> tokenRequest = new HashMap<>();
            tokenRequest.put("sessionID", PostLaunchActivity.currentSessionId);
            tokenRequest.put("firebaseToken", firebaseToken);

            Request request = createRequest(tokenRequest,
                    CooeeSDKConstants.FIREBASE_TOKEN_PATH,
                    CooeeSDKConstants.POST_METHOD,
                    null);

            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e(CooeeSDKConstants.LOG_PREFIX, "Firebase Token Response Error Message" + e.toString());
                    Sentry.captureException(e);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    Log.i(CooeeSDKConstants.LOG_PREFIX, "Firebase Token Response Code : " + response.code());
                }
            });
        });
    }

    public static Request createRequest(Object object, String urlPath, String methodType, Headers headers) {
        Gson gson = new Gson();
        String jsonObject = gson.toJson(object);
        final MediaType jsonMediaType = MediaType.parse("application/json;charset=utf-8");

        Headers.Builder headersBuilder = new Headers.Builder()
                .add("device-name", deviceName)
                .add("user-id", userId);

        // Not sending sdk token header on user creation api
        boolean isPublicAPI = urlPath.equals(CooeeSDKConstants.SAVE_USER_PATH);
        if (!isPublicAPI && apiToken != null) {
            headersBuilder.add("x-sdk-token", apiToken);
        }

        // Adding specific headers for individual request, if any
        if (headers != null) {
            headersBuilder.addAll(headers);
        }
        Headers appendedHeaders = headersBuilder.build();

        RequestBody body = RequestBody.create(jsonObject, jsonMediaType);

        Request.Builder requestBuilder = new Request.Builder()
                .url(BuildConfig.SERVER_URL + urlPath)
                .headers(appendedHeaders);

        switch (methodType) {
            case CooeeSDKConstants.POST_METHOD: {
                requestBuilder.post(body);
                break;
            }
            case CooeeSDKConstants.PUT_METHOD: {
                requestBuilder.put(body);
                break;
            }
        }

        Request request = requestBuilder.build();
        Log.d(CooeeSDKConstants.LOG_PREFIX, "Request : " + request.toString());

        return request;
    }

    public static void setAPIToken(String token) {
        apiToken = token;
    }

    public static void setDeviceName(String name) {
        deviceName = name;
    }

    public static void setUserId(String id) {
        userId = id;
    }
}
