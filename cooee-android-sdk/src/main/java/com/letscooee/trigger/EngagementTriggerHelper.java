package com.letscooee.trigger;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.letscooee.BuildConfig;
import com.letscooee.CooeeFactory;
import com.letscooee.models.Event;
import com.letscooee.models.trigger.EmbeddedTrigger;
import com.letscooee.models.trigger.TriggerData;
import com.letscooee.models.trigger.elements.BaseElement;
import com.letscooee.models.trigger.inapp.InAppTrigger;
import com.letscooee.trigger.adapters.ChildElementDeserializer;
import com.letscooee.trigger.inapp.InAppTriggerActivity;
import com.letscooee.utils.Constants;
import com.letscooee.utils.LocalStorageHelper;
import com.letscooee.utils.RuntimeData;
import com.letscooee.utils.Timer;

import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A small helper class for any kind of engagement trigger like caching or retriving from local storage.
 *
 * @author Shashank Agrawal
 * @version 0.3.0
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class EngagementTriggerHelper {

    private static final long TIME_TO_WAIT_MILLIS = 6 * 1000;

    /**
     * Update previously stored map format to {@link EmbeddedTrigger}.
     * Temporary
     *
     * @param context The application context.
     */
    private static void updateMapToEmbeddedTrigger(Context context) {
        List<HashMap<String, Object>> oldActiveTriggers = LocalStorageHelper.getList(context,
                Constants.STORAGE_ACTIVE_TRIGGERS);

        if (oldActiveTriggers.size() == 0) return;

        List<EmbeddedTrigger> activeTriggers = new ArrayList<>();

        LocalStorageHelper.remove(context, Constants.STORAGE_ACTIVE_TRIGGERS);

        for (HashMap<String, Object> t : oldActiveTriggers) {
            String engagementID = t.get("engagementID") != null
                    ? (String) t.get("engagementID")
                    : null;

            EmbeddedTrigger embeddedTrigger = new EmbeddedTrigger(
                    (String) t.get("triggerID"),
                    engagementID,
                    Long.parseLong((String) Objects.requireNonNull(t.get("duration"))) / 1000,
                    (Boolean) t.get("internal")
            );

            activeTriggers.add(embeddedTrigger);
        }

        LocalStorageHelper.putEmbeddedTriggersImmediately(context, Constants.STORAGE_ACTIVATED_TRIGGERS, activeTriggers);
    }

    /**
     * Store the current active trigger details in local storage for "late engagement tracking".
     *
     * @param context     The application context.
     * @param triggerData Engagement trigger.
     */
    public static void storeActiveTriggerDetails(Context context, TriggerData triggerData) {
        updateMapToEmbeddedTrigger(context);

        ArrayList<EmbeddedTrigger> activeTriggers = LocalStorageHelper.getEmbeddedTriggers(context,
                Constants.STORAGE_ACTIVATED_TRIGGERS);

        EmbeddedTrigger embeddedTrigger = new EmbeddedTrigger(
                triggerData.getId(),
                triggerData.getEngagementID() != null ? triggerData.getEngagementID() : null,
                triggerData.getExpireAt(),
                triggerData.getInternal() ? triggerData.getInternal() : null
        );

        activeTriggers.add(embeddedTrigger);
        if (BuildConfig.DEBUG) {
            Log.d(Constants.TAG, "Current active triggers: " + activeTriggers.toString());
        }

        LocalStorageHelper.putEmbeddedTriggersImmediately(context, Constants.STORAGE_ACTIVATED_TRIGGERS, activeTriggers);
    }

    /**
     * Get the list of non-expired active triggers from local storage for "late engagement tracking".
     *
     * @param context The application context.
     */
    public static ArrayList<EmbeddedTrigger> getActiveTriggers(Context context) {
        updateMapToEmbeddedTrigger(context);

        ArrayList<EmbeddedTrigger> allTriggers = LocalStorageHelper.getEmbeddedTriggers(context,
                Constants.STORAGE_ACTIVATED_TRIGGERS);

        ArrayList<EmbeddedTrigger> activeTriggers = new ArrayList<>();

        for (EmbeddedTrigger trigger : allTriggers) {
            // If it's validity has not yet expired
            if (trigger.getExpireAt() > new Date().getTime() / 1000) {
                activeTriggers.add(trigger);
            }
        }

        // Also update it immediately in local storage
        LocalStorageHelper.putEmbeddedTriggersImmediately(context, Constants.STORAGE_ACTIVATED_TRIGGERS, activeTriggers);

        return activeTriggers;
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
            Log.i(Constants.TAG, "Empty/null trigger data received");
            return;
        }

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(BaseElement.class, new ChildElementDeserializer())
                .create();

        TriggerData triggerData = gson.fromJson(rawTriggerData, TriggerData.class);

        storeActiveTriggerDetails(context, triggerData);
        renderInAppTrigger(context, triggerData);
    }

    /**
     * Start rendering the in-app trigger.
     *
     * @param context     context of the application.
     * @param triggerData received and parsed trigger data.
     */
    public static void renderInAppTrigger(Context context, TriggerData triggerData) {
        RuntimeData runtimeData = CooeeFactory.getRuntimeData();
        if (runtimeData.isInBackground()) {
            return;
        }

        try {
            Intent intent = new Intent(context, InAppTriggerActivity.class);
            Bundle sendBundle = new Bundle();
            sendBundle.putParcelable(Constants.INTENT_TRIGGER_DATA_KEY, triggerData);
            intent.putExtra("bundle", sendBundle);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception ex) {
            CooeeFactory.getSentryHelper().captureException("Couldn't show Engagement Trigger", ex);
        }
    }

    public static void renderInAppFromPushNotification(Context context, @NonNull Activity activity) {
        Bundle bundle = activity.getIntent().getBundleExtra(Constants.INTENT_BUNDLE_KEY);
        // Should not go ahead if bundle is null
        if (bundle == null) {
            return;
        }

        TriggerData triggerData = bundle.getParcelable(Constants.INTENT_TRIGGER_DATA_KEY);
        // Should not go ahead if triggerData is null or triggerData's id is null
        if (triggerData == null || triggerData.getId() == null) {
            return;
        }

        RuntimeData runtimeData = CooeeFactory.getRuntimeData();
        // If app is being launched from the "cold state"
        if (runtimeData.isFirstForeground()) {
            // Then wait for some time before showing the in-app
            new Timer().schedule(() -> renderInAppFromPushNotification(context, triggerData), TIME_TO_WAIT_MILLIS);
        } else {
            // Otherwise show it instantly
            // TODO Using 2 seconds delay as "App Foreground" is not called yet that means the below call be treated
            // as "App in Background" and it will now render the in-app. Need to use Database
            new Timer().schedule(() -> renderInAppFromPushNotification(context, triggerData), 2 * 1000);
        }
    }

    /**
     * Render the In-App trigger when a push notification was clicked.
     *
     * @param context     The application's context.
     * @param triggerData Data to render in-app.
     */
    public static void renderInAppFromPushNotification(Context context, TriggerData triggerData) {


        Event event = new Event("CE Notification Clicked", triggerData);
        CooeeFactory.getSafeHTTPService().sendEvent(event);
        // TODO Talk to Shwetank where to put this method
        storeActiveTriggerDetails(context, triggerData);
        loadLazyData(context, triggerData);
    }

    /**
     * Fetch Trigger InApp data from server
     *
     * @param context The application's context.
     */
    public static void loadLazyData(Context context, TriggerData triggerData) {
        InAppTriggerHelper.loadLazyData(triggerData, (InAppTrigger inAppTrigger) -> {
            triggerData.setInAppTrigger(inAppTrigger);
            renderInAppTrigger(context, triggerData);
        });
    }
}
