package com.letscooee.trigger;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.RestrictTo;
import com.google.gson.Gson;
import com.letscooee.BuildConfig;
import com.letscooee.models.TriggerData;
import com.letscooee.trigger.inapp.InAppTriggerActivity;
import com.letscooee.utils.CooeeSDKConstants;
import com.letscooee.utils.LocalStorageHelper;
import com.letscooee.utils.RuntimeData;
import io.sentry.Sentry;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * A small helper class for any kind of engagement trigger like caching or retriving from local storage.
 *
 * @author Shashank Agrawal
 * @version 0.3.0
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class EngagementTriggerHelper {

    /**
     * Store the current active trigger details in local storage for "late engagement tracking".
     *
     * @param context The application context.
     * @param id      Unique id of this engagement trigger.
     * @param ttl     The valid time-to-live duration (in seconds) of the this trigger.
     */
    public static void storeActiveTriggerDetails(Context context, String id, long ttl) {
        ArrayList<HashMap<String, String>> activeTriggers = LocalStorageHelper.getList(context, CooeeSDKConstants.STORAGE_ACTIVE_TRIGGERS);

        HashMap<String, String> newActiveTrigger = new HashMap<>();
        newActiveTrigger.put("triggerID", id);
        newActiveTrigger.put("duration", String.valueOf(new Date().getTime() + (ttl * 1000)));

        activeTriggers.add(newActiveTrigger);
        if (BuildConfig.DEBUG) {
            Log.d(CooeeSDKConstants.LOG_PREFIX, "Current active triggers: " + activeTriggers.toString());
        }

        LocalStorageHelper.putListImmediately(context, CooeeSDKConstants.STORAGE_ACTIVE_TRIGGERS, activeTriggers);
    }

    /**
     * Start rendering the in-app trigger from the the raw response received from the backend API.
     *
     * @param context context of the application
     * @param data    Data received from the backend
     */
    public static void renderInAppTriggerFromResponse(Context context, Map<String, Object> data) {
        if (data == null) {
            return;
        }

        Object triggerData = data.get("triggerData");
        if (triggerData == null) {
            return;
        }

        renderInAppTriggerFromJSONString(context, triggerData.toString());
    }

    public static void renderInAppTriggerFromJSONString(Context context, String rawTriggerData) {
        if (TextUtils.isEmpty(rawTriggerData)) {
            Log.i(CooeeSDKConstants.LOG_PREFIX, "Empty/null trigger data received");
            return;
        }

        Gson gson = new Gson();
        TriggerData triggerData = gson.fromJson(rawTriggerData, TriggerData.class);

        storeActiveTriggerDetails(context, triggerData.getId(), triggerData.getDuration());
        renderInAppTrigger(context, triggerData);
    }

    /**
     * Start rendering the in-app trigger.
     *
     * @param context     context of the application.
     * @param triggerData received and parsed trigger data.
     */
    public static void renderInAppTrigger(Context context, TriggerData triggerData) {
        RuntimeData runtimeData = RuntimeData.getInstance(context);
        if (runtimeData.isInBackground()) {
            return;
        }

        try {
            Intent intent = new Intent(context, InAppTriggerActivity.class);
            Bundle sendBundle = new Bundle();
            sendBundle.putParcelable("triggerData", triggerData);
            intent.putExtra("bundle", sendBundle);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception ex) {
            Log.d(CooeeSDKConstants.LOG_PREFIX, "Couldn't show Engagement Trigger " + ex.toString());
            Sentry.captureException(ex);
        }
    }
}
