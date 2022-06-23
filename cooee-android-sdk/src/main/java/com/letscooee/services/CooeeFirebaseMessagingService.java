package com.letscooee.services;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.JsonSyntaxException;
import com.letscooee.CooeeFactory;
import com.letscooee.R;
import com.letscooee.enums.trigger.PendingTriggerAction;
import com.letscooee.font.FontProcessor;
import com.letscooee.models.Event;
import com.letscooee.models.trigger.TriggerData;
import com.letscooee.models.trigger.elements.ButtonElement;
import com.letscooee.models.trigger.push.PushNotificationTrigger;
import com.letscooee.pushnotification.PushProviderUtils;
import com.letscooee.room.trigger.PendingTrigger;
import com.letscooee.trigger.EngagementTriggerHelper;
import com.letscooee.trigger.cache.PendingTriggerService;
import com.letscooee.trigger.pushnotification.SimpleNotificationRenderer;
import com.letscooee.utils.Constants;
import com.letscooee.utils.PendingIntentUtility;

import java.util.Map;

/**
 * Process received payload and work accordingly
 *
 * @author Ashish Gaikwad 12/07/21
 * @since 1.0.0
 */
public class CooeeFirebaseMessagingService extends FirebaseMessagingService {

    private final Context context;
    private final EngagementTriggerHelper engagementTriggerHelper;
    private final PendingTriggerService pendingTriggerService;

    private PendingTrigger pendingTrigger;

    @SuppressWarnings("unused")
    public CooeeFirebaseMessagingService() {
        this(null);
    }

    /**
     * Initialise {@link CooeeFirebaseMessagingService} manually.
     *
     * @param context {@link Context}
     */
    public CooeeFirebaseMessagingService(Context context) {
        this.context = context != null ? context : getApplicationContext();
        this.engagementTriggerHelper = new EngagementTriggerHelper(context);
        this.pendingTriggerService = new PendingTriggerService(context);
    }

    @Override
    public void onNewToken(@NonNull String token) {
        sendTokenToServer(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Map<String, String> payload = remoteMessage.getData();
        if (payload.size() == 0) {
            return;
        }

        FontProcessor.downloadFonts(context, payload.get("fonts"));
        this.handleTriggerData(payload.get("triggerData"));
        this.handlePendingTriggerDeletion(payload.get("pendingTrigger"));
    }

    /**
     * Perform delete operations in Pending trigger directly from the received payload.
     *
     * @param rawData {@link PendingTrigger}
     */
    private void handlePendingTriggerDeletion(String rawData) {
        Map<String, String> deletionAction = PendingTriggerAction.parseRawData(rawData);
        if (deletionAction == null) {
            return;
        }

        PendingTriggerAction action = PendingTriggerAction.fromValue(deletionAction.get("a"));
        String triggerID = deletionAction.get("ti");

        pendingTriggerService.delete(action, triggerID);
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

    public void handleTriggerData(String rawTriggerData) {
        TriggerData triggerData;

        try {
            triggerData = TriggerData.fromJson(rawTriggerData);
            if (triggerData.isCurrentlySupported()) {
                Log.d(Constants.TAG, "Unsupported payload version received " + triggerData.getVersion());
                return;
            }
        } catch (JsonSyntaxException e) {
            CooeeFactory.getSentryHelper().captureException(e);
            return;
        }

        if (triggerData.getId() == null) {
            return;
        }

        if (triggerData.getPn() != null) {
            pendingTrigger = pendingTriggerService.newTrigger(triggerData);
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

        pendingTrigger.notificationId = renderer.getNotificationID();
        pendingTriggerService.loadAndSaveTriggerData(pendingTrigger, triggerData);
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
