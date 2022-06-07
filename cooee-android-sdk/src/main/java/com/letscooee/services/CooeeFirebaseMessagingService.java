package com.letscooee.services;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.letscooee.CooeeFactory;
import com.letscooee.R;
import com.letscooee.enums.trigger.PendingTriggerAction;
import com.letscooee.font.FontProcessor;
import com.letscooee.loader.http.RemoteImageLoader;
import com.letscooee.models.Event;
import com.letscooee.models.trigger.TriggerData;
import com.letscooee.models.trigger.elements.ButtonElement;
import com.letscooee.models.trigger.push.PushNotificationTrigger;
import com.letscooee.pushnotification.PushProviderUtils;
import com.letscooee.room.CooeeDatabase;
import com.letscooee.room.trigger.PendingTrigger;
import com.letscooee.trigger.EngagementTriggerHelper;
import com.letscooee.trigger.cache.CacheTriggerContent;
import com.letscooee.trigger.pushnotification.SimpleNotificationRenderer;
import com.letscooee.utils.Constants;
import com.letscooee.utils.PendingIntentUtility;
import java.util.HashMap;
import java.util.Map;

/**
 * Process received payload and work accordingly
 *
 * @author Ashish Gaikwad 12/07/21
 * @since 1.0.0
 */
public class CooeeFirebaseMessagingService extends FirebaseMessagingService {

    private Context context;
    private EngagementTriggerHelper engagementTriggerHelper;
    private CacheTriggerContent cachePayloadContent;
    private CooeeDatabase cooeeDatabase;
    private PendingTrigger pendingTrigger;

    @SuppressWarnings("unused")
    public CooeeFirebaseMessagingService() {
    }

    /**
     * Initialise {@link CooeeFirebaseMessagingService} manually.
     *
     * @param context {@link Context}
     */
    @SuppressWarnings("unused")
    public CooeeFirebaseMessagingService(Context context) {
        this.context = context;
    }

    @Override
    public void onNewToken(@NonNull String token) {
        sendTokenToServer(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        this.context = getApplicationContext();

        if (remoteMessage.getData().size() == 0) {
            return;
        }

        FontProcessor.downloadFonts(context, remoteMessage.getData().get("fonts"));
        this.handleTriggerData(remoteMessage.getData().get("triggerData"));
        this.handlePendingTrigger(remoteMessage.getData().get("pendingTrigger"));
    }

    /**
     * Perform operations inf Pending trigger directly from the received payload.
     *
     * @param rawPendingTriggerAction {@link PendingTrigger}
     */
    private void handlePendingTrigger(String rawPendingTriggerAction) {
        if (TextUtils.isEmpty(rawPendingTriggerAction)) {
            return;
        }
        Map<String, String> pendingTriggerMap;
        try {
            pendingTriggerMap = new Gson().fromJson(rawPendingTriggerAction, new TypeToken<HashMap<String, String>>() {
            }.getType());
        } catch (JsonSyntaxException e) {
            Log.v(Constants.TAG, "Fail to parse pending trigger data: " + e.getMessage());
            return;
        }

        if (this.cachePayloadContent == null) {
            this.cachePayloadContent = new CacheTriggerContent(context);
        }

        PendingTriggerAction pendingTriggerAction = PendingTriggerAction.valueOf(pendingTriggerMap.get("a"));
        String triggerId = pendingTriggerMap.get("ti");

        cachePayloadContent.updatePendingTriggerAction(pendingTriggerAction, triggerId);
    }

    /**
     * Send firebase token to server
     *
     * @param token received from Firebase
     */
    private void sendTokenToServer(String token) {
        Log.d(Constants.TAG, "FCM token received- " + token);
        PushProviderUtils.pushTokenRefresh(token);
    }

    private RemoteImageLoader imageLoader;

    public void handleTriggerData(String rawTriggerData) {
        if (TextUtils.isEmpty(rawTriggerData)) {
            Log.d(Constants.TAG, "No triggerData found on the notification payload");
            return;
        }

        if (engagementTriggerHelper == null) {
            engagementTriggerHelper = new EngagementTriggerHelper(context);
        }

        if (cooeeDatabase == null) {
            cooeeDatabase = CooeeDatabase.getInstance(context);
        }

        if (imageLoader == null) {
            imageLoader = new RemoteImageLoader(context);
        }

        if (cachePayloadContent == null) {
            cachePayloadContent = new CacheTriggerContent(context);
        }

        TriggerData triggerData;

        try {
            Gson gson = new Gson();

            HashMap<String, Object> baseTriggerData = gson.fromJson(rawTriggerData, new TypeToken<HashMap<String, Object>>() {
            }.getType());

            assert baseTriggerData != null;

            Double version = (Double) baseTriggerData.get("v");
            if (version == null || version >= 5 || version < 4) {
                Log.d(Constants.TAG, "Unsupported payload version received " + version);
                return;
            }

            triggerData = TriggerData.fromJson(rawTriggerData);

        } catch (JsonSyntaxException e) {
            CooeeFactory.getSentryHelper().captureException(e);
            return;
        }

        if (triggerData.getId() == null) {
            return;
        }

        if (triggerData.getPn() != null) {
            pendingTrigger = cachePayloadContent.newTrigger(triggerData);
            Event event = new Event("CE Notification Received", triggerData);
            CooeeFactory.getSafeHTTPService().sendEventWithoutSession(event);

            showNotification(triggerData);
        } else {
            engagementTriggerHelper.loadLazyData(triggerData);
        }
    }

    private void showNotification(TriggerData triggerData) {
        SimpleNotificationRenderer renderer = new SimpleNotificationRenderer(context, triggerData);
        renderer.setContentIntent();
        renderer.addActions(createActionButtons(triggerData.getPn(), renderer.getNotificationID()));
        renderer.render();

        cachePayloadContent.loadAndSaveTriggerData(pendingTrigger, triggerData);
    }

    private NotificationCompat.Action[] createActionButtons(PushNotificationTrigger triggerData, int notificationID) {
        if (triggerData.getButtons() == null) {
            return new NotificationCompat.Action[0];
        }

        NotificationCompat.Action[] actions = new NotificationCompat.Action[triggerData.getButtons().size()];
        int requestCode = notificationID;
        int i = 0;
        for (ButtonElement triggerButton : triggerData.getButtons()) {
            String title = triggerButton.getText();

            Intent actionButtonIntent = new Intent(getApplicationContext(), PushNotificationIntentService.class);
            actionButtonIntent.setAction(Constants.ACTION_PUSH_BUTTON_CLICK);
            actionButtonIntent.putExtra(Constants.INTENT_TRIGGER_DATA_KEY, triggerData);
            actionButtonIntent.putExtra("notificationId", notificationID);

            PendingIntent pendingIntent = PendingIntentUtility.getService(
                    getApplicationContext(),
                    requestCode++,
                    actionButtonIntent
            );

            actions[i++] = new NotificationCompat.Action(R.drawable.common_google_signin_btn_icon_dark, title, pendingIntent);

        }

        return actions;
    }
}
