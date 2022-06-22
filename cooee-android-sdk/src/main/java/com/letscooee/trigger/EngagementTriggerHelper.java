package com.letscooee.trigger;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.core.app.NotificationManagerCompat;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.letscooee.BuildConfig;
import com.letscooee.CooeeFactory;
import com.letscooee.models.Event;
import com.letscooee.models.trigger.EmbeddedTrigger;
import com.letscooee.models.trigger.TriggerData;
import com.letscooee.models.trigger.blocks.ClickAction;
import com.letscooee.room.CooeeDatabase;
import com.letscooee.room.trigger.PendingTrigger;
import com.letscooee.trigger.action.ClickActionExecutor;
import com.letscooee.trigger.cache.CacheTriggerContent;
import com.letscooee.trigger.inapp.InAppTriggerActivity;
import com.letscooee.trigger.inapp.PreventBlurActivity;
import com.letscooee.trigger.inapp.TriggerContext;
import com.letscooee.utils.Constants;
import com.letscooee.utils.LocalStorageHelper;
import com.letscooee.utils.RuntimeData;
import com.letscooee.utils.Timer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;

/**
 * A small helper class for any kind of engagement trigger like caching or retrieving from local storage.
 *
 * @author Shashank Agrawal
 * @version 0.3.0
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class EngagementTriggerHelper {

    private static final long TIME_TO_WAIT_MILLIS = 6 * 1000L;
    private static boolean shouldRenderOrganicInApp = true;

    private final Context context;
    @SuppressLint("StaticFieldLeak")
    private static Activity currentActivity;
    private final CacheTriggerContent cacheTriggerContent;
    private final CooeeDatabase cooeeDatabase;

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public EngagementTriggerHelper(Context context) {
        this.context = context;
        cacheTriggerContent = new CacheTriggerContent(context);
        cooeeDatabase = CooeeDatabase.getInstance(context);
    }

    /**
     * Update previously stored map format to {@link EmbeddedTrigger}.
     * Temporary. This should be removed in the next version release as it a migration for map to
     * {@link EmbeddedTrigger}.
     *
     * @param context The application context.
     */
    private static void updateMapToEmbeddedTrigger(Context context) {
        List<HashMap<String, Object>> oldActiveTriggers = LocalStorageHelper.getList(context,
                Constants.STORAGE_ACTIVE_TRIGGERS);

        if (oldActiveTriggers.isEmpty()) return;

        List<EmbeddedTrigger> activeTriggers = new ArrayList<>();

        LocalStorageHelper.remove(context, Constants.STORAGE_ACTIVE_TRIGGERS);

        for (HashMap<String, Object> trigger : oldActiveTriggers) {
            String oldDuration = (String) Objects.requireNonNull(trigger.get("duration"));
            Long expireAt = Long.parseLong(oldDuration) / 1000;

            EmbeddedTrigger embeddedTrigger = new EmbeddedTrigger(
                    (String) trigger.get("triggerID"),
                    (String) trigger.get("engagementID"),
                    expireAt
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

        EmbeddedTrigger embeddedTrigger = new EmbeddedTrigger(triggerData);

        if (!embeddedTrigger.isExpired()) {
            activeTriggers.add(embeddedTrigger);
        }

        if (BuildConfig.DEBUG) {
            Log.d(Constants.TAG, "Current active triggers: " + activeTriggers);
        }

        setActiveTrigger(context, triggerData);
        LocalStorageHelper.putEmbeddedTriggersImmediately(context, Constants.STORAGE_ACTIVATED_TRIGGERS, activeTriggers);
    }

    /**
     * Get the list of non-expired active triggers from local storage for "late engagement tracking".
     *
     * @param context The application context.
     */
    public static List<EmbeddedTrigger> getActiveTriggers(Context context) {
        updateMapToEmbeddedTrigger(context);

        ArrayList<EmbeddedTrigger> allTriggers = LocalStorageHelper.getEmbeddedTriggers(context,
                Constants.STORAGE_ACTIVATED_TRIGGERS);

        ListIterator<EmbeddedTrigger> iterator = allTriggers.listIterator();

        while (iterator.hasNext()) {
            // If it's validity has not yet expired
            if (iterator.next().isExpired()) {
                iterator.remove();
            }
        }

        // Also update it immediately in local storage
        LocalStorageHelper.putEmbeddedTriggersImmediately(context, Constants.STORAGE_ACTIVATED_TRIGGERS, allTriggers);

        return allTriggers;
    }

    /**
     * Start rendering the in-app trigger from the raw response received from the backend API.
     *
     * @param data Data received from the backend
     */
    public void renderInAppTriggerFromResponse(Map<String, Object> data) {
        if (data == null) {
            return;
        }

        Object triggerData = data.get("triggerData");
        if (triggerData == null) {
            return;
        }

        renderInAppTriggerFromJSONString(new Gson().toJson(triggerData));
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public void renderInAppTriggerFromJSONString(String rawTriggerData) {
        if (TextUtils.isEmpty(rawTriggerData)) {
            Log.i(Constants.TAG, "Empty/null trigger data received");
            return;
        }

        TriggerData triggerData;
        try {
            triggerData = TriggerData.fromJson(rawTriggerData);
        } catch (JsonSyntaxException e) {
            CooeeFactory.getSentryHelper().captureException(e);
            return;
        }

        storeActiveTriggerDetails(context, triggerData);
        cacheTriggerContent.setContentLoadedListener(() -> renderInAppTrigger(triggerData));
        cacheTriggerContent.loadAndCacheInAppContent(triggerData);

    }

    /**
     * Start rendering the in-app trigger.
     *
     * @param triggerData received and parsed trigger data.
     */
    public void renderInAppTrigger(TriggerData triggerData) {
        if (triggerData == null || TextUtils.isEmpty(triggerData.getId())) {
            return;
        }

        RuntimeData runtimeData = CooeeFactory.getRuntimeData();
        if (runtimeData.isInBackground()) {
            Log.i(Constants.TAG, "Won't render in-app. App is in background");
            return;
        }

        try {
            boolean isInFullscreenMode = !isStatusBarVisible();
            Intent intent = new Intent(context, InAppTriggerActivity.class);
            Bundle sendBundle = new Bundle();
            sendBundle.putParcelable(Constants.INTENT_TRIGGER_DATA_KEY, triggerData);
            sendBundle.putBoolean(Constants.IN_APP_FULLSCREEN_FLAG_KEY, isInFullscreenMode);
            intent.putExtra("bundle", sendBundle);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);

            // Store trigger in-case in-app is sent
            setActiveTrigger(context, triggerData);
        } catch (Exception ex) {
            CooeeFactory.getSentryHelper().captureException("Couldn't show Engagement Trigger", ex);
            return;
        }

        PendingTrigger pendingTrigger = cooeeDatabase.pendingTriggerDAO().getPendingTriggerWithTriggerId(triggerData.getId());

        if (pendingTrigger == null) {
            return;
        }

        NotificationManagerCompat.from(context).cancel((int) pendingTrigger.notificationId);
        Log.v(Constants.TAG, "Deleting PendingTrigger( triggerId=" + triggerData.getId() + ")");
        cooeeDatabase.pendingTriggerDAO().deletePendingTrigger(pendingTrigger);
    }

    public void renderInAppFromPushNotification(@NonNull Activity activity) {
        Bundle bundle = activity.getIntent().getBundleExtra(Constants.INTENT_BUNDLE_KEY);
        // Should not go ahead if bundle is null
        if (bundle == null) {
            handleOrganicLaunch(activity);
            return;
        }

        TriggerData triggerData = bundle.getParcelable(Constants.INTENT_TRIGGER_DATA_KEY);
        int sdkVersionCode = bundle.getInt(Constants.INTENT_SDK_VERSION_CODE_KEY, 0);
        // Should not go ahead if triggerData is null or triggerData's id is null
        if (triggerData == null || triggerData.getId() == null) {
            return;
        }

        ClickAction pushClickAction = triggerData.getPn().getClickAction();

        if (pushClickAction == null) {
            launchInApp(triggerData, sdkVersionCode);
            return;
        }

        int launchFeature = pushClickAction.getLaunchFeature();

        if (launchFeature == 0 || launchFeature == 1) {
            launchInApp(triggerData, sdkVersionCode);
        } else {
            TriggerContext triggerContext = new TriggerContext();
            triggerContext.setTriggerData(triggerData);

            new ClickActionExecutor(context, pushClickAction, triggerContext).execute();
        }
    }

    /**
     * Will render InApp on organic App Launch.
     *
     * @param activity {@link Activity} instance.
     */
    private void handleOrganicLaunch(Activity activity) {
        // Can not use runtimeData.isFirstForeground() because it's not updated till first background
        // event, And this method is called  every activity resume event because we need instance
        // of activity for glassmorphism.
        if (!shouldRenderOrganicInApp) {
            return;
        }

        shouldRenderOrganicInApp = false;

        if (activity == null || activity instanceof PreventBlurActivity) {
            return;
        }

        PendingTrigger pendingTrigger = cacheTriggerContent.getPendingTrigger();

        if (pendingTrigger == null) {
            return;
        }

        TriggerData triggerData;
        try {
            triggerData = TriggerData.fromJson(pendingTrigger.triggerData);
        } catch (JsonSyntaxException e) {
            CooeeFactory.getSentryHelper().captureException(e);
            return;
        }

        if (TextUtils.isEmpty(triggerData.getId())) {
            return;
        }

        if (!pendingTrigger.loadedLazyData) {
            loadLazyData(TriggerData.fromJson(pendingTrigger.triggerData));
            return;
        }
        cacheTriggerContent.setContentLoadedListener(() -> renderInAppTrigger(triggerData));
        cacheTriggerContent.loadAndCacheInAppContent(triggerData);
    }


    private void launchInApp(TriggerData triggerData, int sdkVersionCode) {
        RuntimeData runtimeData = CooeeFactory.getRuntimeData();
        // If app is being launched from the "cold state"
        if (runtimeData.isFirstForeground()) {
            // Then wait for some time before showing the in-app
            new Timer().schedule(() -> renderInAppFromPushNotification(triggerData, sdkVersionCode), TIME_TO_WAIT_MILLIS);
        } else {
            // Otherwise show it instantly
            // Using 2 seconds delay as "App Foreground" is not called yet that means the below call be treated
            // as "App in Background" and it will now render the in-app. Need to use Database
            new Timer().schedule(() -> renderInAppFromPushNotification(triggerData, sdkVersionCode), 2 * 1000);
        }
    }

    /**
     * Render the In-App trigger when a push notification was clicked.
     *
     * @param triggerData Data to render in-app.
     */
    public void renderInAppFromPushNotification(TriggerData triggerData, int sdkVersionCode) {
        storeActiveTriggerDetails(context, triggerData);

        Event event = new Event("CE Notification Clicked", triggerData);
        CooeeFactory.getSafeHTTPService().sendEventWithoutSession(event);

        /*
         * If the SDK version is less than 10312 i.e v1.3.12, then we will render InApp with old way.
         * Else we will render InApp with new way i.e With PendingTrigger helpers.
         *
         * This sdkVersionCode comes from Push Notification click intent after version v1.3.12.
         * If the SDK version is less than 10312, then this sdkVersionCode will be 0.
         *
         * This sdkVersionCode will also help us to determine whether clicked push notification was
         * rendered via which sdk version.
         */
        if (sdkVersionCode < 10312) {
            loadLazyData(triggerData);
        } else {
            checkAndLoadInApp(triggerData);
        }
    }

    /**
     * Check if related InApp data is present at LocalStorage and load it.
     * If not present, then load the InApp data from the server.
     *
     * @param triggerData Data to render in-app.
     */
    private void checkAndLoadInApp(TriggerData triggerData) {
        if (triggerData == null) {
            return;
        }

        PendingTrigger pendingTrigger = cooeeDatabase.pendingTriggerDAO().getPendingTriggerWithTriggerId(triggerData.getId());

        if (pendingTrigger == null) {
            Log.v(Constants.TAG, "Trigger with ID " + triggerData.getId() + " is already displayed");
            return;
        }

        if (!pendingTrigger.loadedLazyData) {
            loadLazyData(triggerData);
            return;
        }

        TriggerData storedTrigger = TriggerData.fromJson((String) pendingTrigger.triggerData);
        cacheTriggerContent.setContentLoadedListener(() -> renderInAppTrigger(storedTrigger));
        cacheTriggerContent.loadAndCacheInAppContent(storedTrigger);
    }

    /**
     * Fetch Trigger InApp data from server
     *
     * @param triggerData Data to render in-app.
     */
    public void loadLazyData(TriggerData triggerData) {
        new InAppTriggerHelper().loadLazyData(triggerData, (String rawInAppTrigger) -> {
            TriggerData inAppTriggerData = TriggerData.fromJson(rawInAppTrigger);
            triggerData.setInAppTrigger(inAppTriggerData.getInAppTrigger());
            cacheTriggerContent.setContentLoadedListener(() -> renderInAppTrigger(triggerData));
            cacheTriggerContent.loadAndCacheInAppContent(triggerData);
        });
    }

    /**
     * Set active trigger for the session
     *
     * @param context     The application's context.
     * @param triggerData Data to render in-app.
     */
    private static void setActiveTrigger(Context context, TriggerData triggerData) {
        LocalStorageHelper.putEmbeddedTriggerImmediately(context, Constants.STORAGE_ACTIVE_TRIGGER,
                new EmbeddedTrigger(triggerData));
    }

    /**
     * Keeps track of the currently active {@link Activity}.
     *
     * @param currentActivity The currently active {@link Activity}.
     */
    public static void setCurrentActivity(Activity currentActivity) {
        EngagementTriggerHelper.currentActivity = currentActivity;
    }

    /**
     * Check if status bar is visible or not for current {@link Activity}
     *
     * @return {@code true} if status bar is visible, {@code false} otherwise
     */
    private boolean isStatusBarVisible() {
        if (currentActivity == null) {
            return true;
        }

        Rect rectangle = new Rect();
        Window window = currentActivity.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
        int statusBarHeight = rectangle.top;
        return statusBarHeight != 0;
    }

    /**
     * Change {@code shouldRenderOrganicInApp} to {@code true} so pending InApp logic start working
     */
    public static void allowPendingInAppRendering() {
        EngagementTriggerHelper.shouldRenderOrganicInApp = true;
    }
}
