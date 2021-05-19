package com.letscooee.retrofit;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.letscooee.init.ActivityLifecycleCallback;
import com.letscooee.init.PostLaunchActivity;
import com.letscooee.models.Event;
import com.letscooee.room.CooeeDatabase;
import com.letscooee.room.postoperations.entity.PendingTask;
import com.letscooee.room.postoperations.enums.EventType;
import com.letscooee.utils.Closure;
import com.letscooee.utils.CooeeSDKConstants;
import com.letscooee.utils.LocalStorageHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
    static Gson gson = new Gson();


    public static void sendEvent(Context context, Event event, Closure closure) {
        CooeeDatabase db = CooeeDatabase.getInstance(context);
        //noinspection ResultOfMethodCallIgnored
        PostLaunchActivity.onSDKStateDecided.subscribe((Object ignored) -> {
            event.setSessionID(PostLaunchActivity.currentSessionId);
            sendEventWithoutSDKState(context, event, db, closure);
        });
    }

    public static void sendEventWithoutSDKState(Context context, Event event, CooeeDatabase db, Closure closure) {
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

        Date currentDate = new Date();
        event.setOccurred(currentDate);
        LocalStorageHelper.putListImmediately(context, CooeeSDKConstants.STORAGE_ACTIVE_TRIGGERS, activeTriggerList);

        if (TextUtils.isEmpty(event.getSessionID()) && (!event.getName().equalsIgnoreCase("CE Notification Received") || !event.getName().equalsIgnoreCase("CE Notification Viewed"))) {
            pushEvent(event, closure, null, null);
        } else {


            PendingTask task = new PendingTask();
            task.attempts = 0;
            task.data = gson.toJson(event);
            task.type = EventType.EVENT;
            task.dateCreated = currentDate.getTime();
            db.pendingTaskDAO().insertAll(task);
        }

    }

    public static void pushEvent(Event event, Closure closure, CooeeDatabase appDatabase, PendingTask task) {
        Date currentTime = new Date();
        serverAPIService.sendEvent(event).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                Log.i(CooeeSDKConstants.LOG_PREFIX, event.getName() + " Event Sent Code: " + response.code());

                if (closure != null) {
                    closure.call(response.body());
                }

                if (appDatabase != null) {
                    appDatabase.pendingTaskDAO().delete(task);
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.e(CooeeSDKConstants.LOG_PREFIX, event.getName() + " Event Sent Error Message: " + t.toString());

                if (task != null) {
                    task.lastAttempted = currentTime.getTime();
                    task.attempts = task.attempts + 1;
                    appDatabase.pendingTaskDAO().update(task);
                }
                //Sentry.captureException(t);
            }
        });
    }

    public static void sendUserProfile(Context context, Map<String, Object> userMap, String msg, Closure closure) {
        Date currentTime = new Date();
        CooeeDatabase db = CooeeDatabase.getInstance(context);
        userMap.put("occurred", currentTime);
        PostLaunchActivity.onSDKStateDecided.subscribe((Object ignored) -> {
            userMap.put("sessionID", PostLaunchActivity.currentSessionId);

            PendingTask task = new PendingTask();
            task.attempts = 0;
            task.data = gson.toJson(userMap);
            task.type = EventType.PROFILE;
            task.dateCreated = currentTime.getTime();
            db.pendingTaskDAO().insertAll(task);
        });
    }

    public static void pushUserProfile(Map<String, Object> userMap, String msg, Closure closure, CooeeDatabase appDatabase, PendingTask task) {
        Date currentTime = new Date();
        serverAPIService.updateProfile(userMap).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call, @NonNull Response<Map<String, Object>> response) {
                Log.i(CooeeSDKConstants.LOG_PREFIX, msg + " User Profile Response Code : " + response.code());
                appDatabase.pendingTaskDAO().delete(task);
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
                task.lastAttempted = currentTime.getTime();
                task.attempts = task.attempts + 1;
                appDatabase.pendingTaskDAO().update(task);
                //Sentry.captureException(t);
            }
        });
    }

    public static void sendSessionConcludedEvent(int duration, Context context) {
        Log.d("COOEE", "********* session concluded");
        Date currentTime = new Date();
        CooeeDatabase db = CooeeDatabase.getInstance(context);
        //noinspection ResultOfMethodCallIgnored
        PostLaunchActivity.onSDKStateDecided.subscribe((Object ignored) -> {
            Map<String, Object> sessionConcludedRequest = new HashMap<>();
            sessionConcludedRequest.put("sessionID", PostLaunchActivity.currentSessionId);
            sessionConcludedRequest.put("duration", duration);

            PendingTask task = new PendingTask();
            task.attempts = 0;
            task.data = gson.toJson(sessionConcludedRequest);
            task.type = EventType.SESSION_CONCLUDED;
            task.dateCreated = currentTime.getTime();
            db.pendingTaskDAO().insertAll(task);
        });
    }

    public static void pushSessionConcluded(Map<String, Object> sessionConcludedRequest, CooeeDatabase appDatabase, PendingTask task) {
        Date currentTime = new Date();
        serverAPIService.concludeSession(sessionConcludedRequest).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                Log.i(CooeeSDKConstants.LOG_PREFIX, "Session Concluded Event Sent Code : " + response.code());
                appDatabase.pendingTaskDAO().delete(task);
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(CooeeSDKConstants.LOG_PREFIX, "Session Concluded Event Sent Error Message" + t.toString());
                task.attempts = task.attempts + 1;
                task.lastAttempted = currentTime.getTime();
                appDatabase.pendingTaskDAO().update(task);
                //Sentry.captureException(t);
            }
        });
    }

    public static void keepAlive(Context context) {
        Date currentTime = new Date();
        CooeeDatabase db = CooeeDatabase.getInstance(context);
        //noinspection ResultOfMethodCallIgnored
        PostLaunchActivity.onSDKStateDecided.subscribe((Object ignored) -> {
            Map<String, String> keepAliveRequest = new HashMap<>();
            keepAliveRequest.put("sessionID", PostLaunchActivity.currentSessionId);

            PendingTask task = new PendingTask();
            task.attempts = 0;
            task.data = gson.toJson(keepAliveRequest);
            task.type = EventType.KEEP_ALIVE;
            task.dateCreated = currentTime.getTime();
            db.pendingTaskDAO().insertAll(task);
        });
    }

    public static void pushKeepAlive(Map<String, String> keepAliveRequest, CooeeDatabase appDatabase, PendingTask task) {
        Date currentTime = new Date();
        serverAPIService.keepAlive(keepAliveRequest).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                Log.i(CooeeSDKConstants.LOG_PREFIX, "Session Alive Response Code : " + response.code());
                appDatabase.pendingTaskDAO().delete(task);
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(CooeeSDKConstants.LOG_PREFIX, "Session Alive Response Error Message" + t.toString());
                //Sentry.captureException(t);
                task.lastAttempted = currentTime.getTime();
                task.attempts = task.attempts + 1;
                appDatabase.pendingTaskDAO().update(task);
            }
        });
    }

    public static void setFirebaseToken(String firebaseToken, Context context) {
        Date currentTime = new Date();
        CooeeDatabase db = CooeeDatabase.getInstance(context);
        PostLaunchActivity.onSDKStateDecided.subscribe((Object ignored) -> {
            Map<String, String> tokenRequest = new HashMap<>();
            tokenRequest.put("sessionID", PostLaunchActivity.currentSessionId);
            tokenRequest.put("firebaseToken", firebaseToken);


            PendingTask task = new PendingTask();
            task.attempts = 0;
            task.data = gson.toJson(tokenRequest);
            task.type = EventType.FB_TOKEN;
            task.dateCreated = currentTime.getTime();
            db.pendingTaskDAO().insertAll(task);

        });
    }

    public static void pushFirebaseToken(Map<String, String> tokenRequest, CooeeDatabase appDatabase, PendingTask task) {
        Date currentTime = new Date();
        serverAPIService.setFirebaseToken(tokenRequest).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                Log.i(CooeeSDKConstants.LOG_PREFIX, "Firebase Token Response Code : " + response.code());
                appDatabase.pendingTaskDAO().delete(task);
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(CooeeSDKConstants.LOG_PREFIX, "Firebase Token Response Error Message" + t.toString());
                //Sentry.captureException(t);
                task.lastAttempted = currentTime.getTime();
                task.attempts = task.attempts + 1;
                appDatabase.pendingTaskDAO().update(task);
            }
        });
    }
}
