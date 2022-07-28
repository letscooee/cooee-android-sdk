package com.letscooee.trigger;

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
import com.google.gson.Gson;
import com.letscooee.BuildConfig;
import com.letscooee.CooeeFactory;
import com.letscooee.exceptions.InvalidTriggerDataException;
import com.letscooee.models.Event;
import com.letscooee.models.trigger.EmbeddedTrigger;
import com.letscooee.models.trigger.TriggerData;
import com.letscooee.models.trigger.blocks.ClickAction;
import com.letscooee.room.trigger.PendingTrigger;
import com.letscooee.task.CooeeExecutors;
import com.letscooee.trigger.action.ClickActionExecutor;
import com.letscooee.trigger.cache.PendingTriggerService;
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

    private final Context context;
    private final RuntimeData runtimeData;
    private final PendingTriggerService pendingTriggerService;

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public EngagementTriggerHelper(Context context) {
        this.context = context;
        this.runtimeData = CooeeFactory.getRuntimeData();
        this.pendingTriggerService = CooeeFactory.getPendingTriggerService();
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
            triggerData = TriggerDataHelper.parse(rawTriggerData);
            storeActiveTriggerDetails(context, triggerData);
            render(triggerData);
        } catch (InvalidTriggerDataException e) {
            Log.e(Constants.TAG, e.getMessage(), e);
        }
    }

    /**
     * Start rendering the in-app trigger.
     *
     * @param triggerData received and parsed trigger data.
     */
    public void renderInAppTrigger(TriggerData triggerData) throws InvalidTriggerDataException {
        if (runtimeData.isInBackground()) {
            Log.i(Constants.TAG, "Won't render in-app. App is in background");
            return;
        }

        try {
            if (triggerData == null || !triggerData.containValidData()) {
                throw new InvalidTriggerDataException("Trying to render invalid trigger: " + triggerData);
            }

            boolean isInFullscreenMode = !isStatusBarVisible(runtimeData.getCurrentActivity());
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
            CooeeFactory.getSentryHelper().captureException("Failed to show In-App", ex);

            // Sends exception back to callers
            throw ex;
        }

        new PushTriggerHelper(context, triggerData).removePushFromTray();
    }

    public void renderInAppFromPushNotification(@NonNull Activity activity) {
        Bundle bundle = activity.getIntent().getBundleExtra(Constants.INTENT_BUNDLE_KEY);
        // Should not go ahead if bundle is null
        if (bundle == null) {
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
     * This is a safe method which ultimately calls {@link #handleOrganicLaunch()} and catches all the exception.
     */
    public void handleOrganicLaunchSafe() {
        try {
            this.handleOrganicLaunch();
        } catch (InvalidTriggerDataException e) {
            // Error already logged to Sentry
        } catch (Exception e) {
            // Make sure no exception is thrown which should crash the app launch/resume
            CooeeFactory.getSentryHelper().captureException("Unhandled exception in organic launch in-app ", e);
        }
    }

    /**
     * Show last queued InApp on organic app launch.
     */
    private void handleOrganicLaunch() throws InvalidTriggerDataException {
        Activity activity = this.runtimeData.getCurrentActivity();
        if (activity == null || activity instanceof PreventBlurActivity) {
            return;
        }

        PendingTrigger pendingTrigger = pendingTriggerService.peep();
        if (pendingTrigger == null) {
            return;
        }

        TriggerData triggerData = pendingTrigger.getTriggerData();

        render(triggerData);
    }

    /**
     * Calls {@link InAppTriggerHelper#render()} with the given trigger data.
     * To run {@link InAppTriggerHelper#render()} single worker thread operating off an unbounded queue is used.
     * Which allows {@link java9.util.concurrent.CompletableFuture} to stop rendering the in-app trigger
     * till all images are loaded.
     *
     * @param triggerData trigger data to be rendered.
     */
    private void render(TriggerData triggerData) {
        new CooeeExecutors().singleThreadExecutor().execute(() -> {
            try {
                new InAppTriggerHelper(context, triggerData).render();
            } catch (InvalidTriggerDataException e) {
                Log.d(Constants.TAG, e.getMessage(), e);
            }
        });
    }

    private void launchInApp(TriggerData triggerData, int sdkVersionCode) {

        // If app is being launched from the "cold state"
        if (runtimeData.isFirstForeground()) {
            // Then wait for some time before showing the in-app
            setTimeOut(triggerData, sdkVersionCode, TIME_TO_WAIT_MILLIS);
        } else {
            // Otherwise show it instantly
            // Using 2 seconds delay as "App Foreground" is not called yet that means the below call be treated
            // as "App in Background" and it will not render the in-app. Need to use Database
            setTimeOut(triggerData, sdkVersionCode, 2 * 1000L);
        }
    }

    /**
     * Runs runnable provides after given time.
     *
     * @param triggerData      Trigger data
     * @param sdkVersionCode   SDK version code
     * @param timeToWaitMillis Time to wait in milliseconds
     */
    private void setTimeOut(TriggerData triggerData, int sdkVersionCode, long timeToWaitMillis) {
        new Timer().schedule(() -> {
            try {
                renderInAppFromPushNotification(triggerData, sdkVersionCode);
            } catch (InvalidTriggerDataException e) {
                Log.e(Constants.TAG, e.getMessage());
            }
        }, timeToWaitMillis);
    }

    /**
     * Render the In-App trigger when a push notification was clicked.
     *
     * @param triggerData Data to render in-app.
     */
    public void renderInAppFromPushNotification(TriggerData triggerData, int sdkVersionCode) throws InvalidTriggerDataException {
        storeActiveTriggerDetails(context, triggerData);

        Event event = new Event(Constants.EVENT_NOTIFICATION_CLICKED, triggerData);
        CooeeFactory.getSafeHTTPService().sendEventWithoutSession(event);

        /*
         * If the SDK version is less than 10400 i.e v1.4.0, then we will render InApp with old way.
         * Else we will render InApp with new way i.e With PendingTrigger helpers.
         *
         * This sdkVersionCode comes from Push Notification click intent after version v1.3.12.
         * If the SDK version is less than 10312, then this sdkVersionCode will be 0.
         *
         * This sdkVersionCode will also help us to determine whether clicked push notification was
         * rendered via which sdk version.
         */
        if (sdkVersionCode < 10400) {
            render(triggerData);
        } else {
            new InAppTriggerHelper(context, triggerData).checkInPendingTriggerAndRender();
        }
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
     * Check if status bar is visible or not for current {@link Activity}
     *
     * @return {@code true} if status bar is visible, {@code false} otherwise
     */
    public static boolean isStatusBarVisible(Activity activity) {
        if (activity == null) {
            // Assume it's visible
            return true;
        }

        Rect rectangle = new Rect();
        Window window = activity.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
        int statusBarHeight = rectangle.top;
        return statusBarHeight != 0;
    }

}
