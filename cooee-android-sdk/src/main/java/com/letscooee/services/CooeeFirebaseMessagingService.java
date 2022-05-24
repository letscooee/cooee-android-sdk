package com.letscooee.services;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
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
import com.letscooee.font.FontProcessor;
import com.letscooee.loader.http.RemoteImageLoader;
import com.letscooee.models.Event;
import com.letscooee.models.trigger.TriggerData;
import com.letscooee.models.trigger.elements.ButtonElement;
import com.letscooee.models.trigger.push.PushNotificationTrigger;
import com.letscooee.pushnotification.PushProviderUtils;
import com.letscooee.trigger.CooeeEmptyActivity;
import com.letscooee.trigger.EngagementTriggerHelper;
import com.letscooee.trigger.InAppTriggerHelper;
import com.letscooee.trigger.pushnotification.NotificationRenderer;
import com.letscooee.trigger.pushnotification.SimpleNotificationRenderer;
import com.letscooee.utils.Constants;
import com.letscooee.utils.LocalStorageHelper;
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
    private InAppTriggerHelper inAppTriggerHelper;

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
        if (engagementTriggerHelper == null) {
            engagementTriggerHelper = new EngagementTriggerHelper(context);
        }

        if (TextUtils.isEmpty(rawTriggerData)) {
            Log.d(Constants.TAG, "No triggerData found on the notification payload");
            return;
        }

        if (imageLoader == null) {
            imageLoader = new RemoteImageLoader(context);
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
            Event event = new Event("CE Notification Received", triggerData);
            CooeeFactory.getSafeHTTPService().sendEventWithoutSession(event);

            showNotification(triggerData);
        } else {
            engagementTriggerHelper.loadLazyData(triggerData);
        }
    }

    private void showNotification(TriggerData triggerData) {
        NotificationRenderer renderer = new SimpleNotificationRenderer(context, triggerData);
        NotificationCompat.Builder notificationBuilder = renderer.getBuilder();

        Intent appLaunchIntent = new Intent(context, CooeeEmptyActivity.class);

        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.INTENT_TRIGGER_DATA_KEY, triggerData);

        appLaunchIntent.putExtra(Constants.INTENT_BUNDLE_KEY, bundle);
        appLaunchIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent appLaunchPendingIntent = PendingIntentUtility.getActivity(
                context,
                triggerData.getId().hashCode(),
                appLaunchIntent
        );

        renderer.addActions(createActionButtons(triggerData.getPn(), renderer.getNotificationID()));

        notificationBuilder.setContentIntent(appLaunchPendingIntent);

        String smallImage = triggerData.getPn().getSmallImage();
        String largeImage = triggerData.getPn().getLargeImage();
        if (TextUtils.isEmpty(smallImage) && TextUtils.isEmpty(largeImage)) {
            // Hide Image container to increase text width on PN
            renderer.hideViewInSmallContentView(R.id.icon_container);
            renderer.hideViewInBigContentView(R.id.icon_container);
            renderer.hideViewInBigContentView(R.id.textViewSmallBody);
            renderer.showViewInBigContentView(R.id.textViewLargeBody);
            renderer.render();
        } else if (!TextUtils.isEmpty(smallImage) && TextUtils.isEmpty(largeImage)) {
            this.imageLoader.load(smallImage, (Bitmap resource) -> {
                renderer.addSmallContentImage(R.id.imageViewLarge, resource);
                renderer.hideViewInBigContentView(R.id.icon_container);
                renderer.hideViewInBigContentView(R.id.textViewSmallBody);
                renderer.showViewInBigContentView(R.id.textViewLargeBody);
                renderer.render();
            });
        } else if (TextUtils.isEmpty(smallImage) && !TextUtils.isEmpty(largeImage)) {
            this.imageLoader.load(largeImage, (Bitmap resource) -> {
                renderer.hideViewInSmallContentView(R.id.icon_container);
                renderer.addBigContentImage(R.id.imageViewLarge, resource);
                renderer.render();
            });
        } else {
            this.imageLoader.load(smallImage, (Bitmap smallImageResource) -> {
                renderer.addSmallContentImage(R.id.imageViewLarge, smallImageResource);
                this.imageLoader.load(largeImage, (Bitmap largeImageResource) -> {
                    renderer.addBigContentImage(R.id.imageViewLarge, largeImageResource);
                    renderer.render();
                });
            });
        }

        loadAndStoreInApp(triggerData);
    }

    /**
     * Loads the in-app content and stores it in the database.
     *
     * @param triggerData The trigger data.
     */
    private void loadAndStoreInApp(TriggerData triggerData) {
        if (inAppTriggerHelper == null) {
            inAppTriggerHelper = new InAppTriggerHelper();
        }

        inAppTriggerHelper.loadLazyData(triggerData, (String rawInAppTrigger) -> {
            if (rawInAppTrigger == null) {
                return;
            }

            TriggerData inAppTriggerData = null;
            try {
                inAppTriggerData = TriggerData.fromJson(rawInAppTrigger);
            } catch (JsonSyntaxException e) {
                CooeeFactory.getSentryHelper().captureException("Fail to parse in-app trigger data", e);
            }

            if (inAppTriggerData == null || inAppTriggerData.getInAppTrigger() == null) {
                return;
            }

            Map<String, Object> storedTrigger = LocalStorageHelper.getMap(context, Constants.STORAGE_RAW_IN_APP_TRIGGER_KEY, new HashMap<>());
            storedTrigger.put(triggerData.getId(), rawInAppTrigger);

            LocalStorageHelper.putMap(context, Constants.STORAGE_RAW_IN_APP_TRIGGER_KEY, storedTrigger);
        });
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
