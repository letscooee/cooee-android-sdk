package com.letscooee.services;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.letscooee.CooeeFactory;
import com.letscooee.enums.trigger.PendingTriggerAction;
import com.letscooee.exceptions.InvalidTriggerDataException;
import com.letscooee.font.FontProcessor;
import com.letscooee.models.Event;
import com.letscooee.models.trigger.TriggerData;
import com.letscooee.pushnotification.PushProviderUtils;
import com.letscooee.room.trigger.PendingTrigger;
import com.letscooee.trigger.InAppTriggerHelper;
import com.letscooee.trigger.TriggerDataHelper;
import com.letscooee.trigger.cache.PendingTriggerService;
import com.letscooee.trigger.pushnotification.SimpleNotificationRenderer;
import com.letscooee.utils.Constants;

import java.util.Map;

/**
 * Process received payload and work accordingly
 *
 * @author Ashish Gaikwad 12/07/21
 * @since 1.0.0
 */
public class CooeeFirebaseMessagingService extends FirebaseMessagingService {

    private Context context;
    private PendingTriggerService pendingTriggerService;

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
        this.context = context;
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
        initializeVariables();
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

    /**
     * This method handles trigger data received via FCM.
     * <p> This method is open only for {@code com.letscooee} group.</p>
     *
     * @param rawTriggerData {@link String} raw trigger data received via FCM.
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public void handleTriggerData(String rawTriggerData) {
        initializeVariables();
        TriggerData triggerData;

        try {
            triggerData = TriggerDataHelper.parse(rawTriggerData);
        } catch (InvalidTriggerDataException e) {
            return;
        }

        if (triggerData.getPn() != null) {
            Event event = new Event(Constants.EVENT_NOTIFICATION_RECEIVED, triggerData);
            CooeeFactory.getSafeHTTPService().sendEventWithoutSession(event);

            showNotification(triggerData);
        } else {
            // This is just for testing locally when sending in-app only previews
            new InAppTriggerHelper(context, triggerData).render();
        }
    }

    private void initializeVariables() {
        if (this.context == null) {
            this.context = getApplicationContext();
        }
        if (this.pendingTriggerService == null) {
            this.pendingTriggerService = new PendingTriggerService(context);
        }
    }

    private void showNotification(TriggerData triggerData) {
        SimpleNotificationRenderer renderer = new SimpleNotificationRenderer(context, triggerData);
        try {
            renderer.render();
        } catch (Exception e) {
            e.printStackTrace();
            CooeeFactory.getSentryHelper().captureException("Unable to render push", e);
        }

        pendingTriggerService.newTrigger(triggerData);
    }

}
