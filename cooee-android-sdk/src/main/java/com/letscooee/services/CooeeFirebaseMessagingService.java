package com.letscooee.services;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
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
import com.letscooee.models.AppFont;
import com.letscooee.models.trigger.TriggerData;
import com.letscooee.models.trigger.push.PushNotificationTrigger;
import com.letscooee.models.trigger.elements.ButtonElement;
import com.letscooee.pushnotification.PushProviderUtils;
import com.letscooee.trigger.CooeeEmptyActivity;
import com.letscooee.trigger.EngagementTriggerHelper;
import com.letscooee.trigger.pushnotification.NotificationRenderer;
import com.letscooee.trigger.pushnotification.SimpleNotificationRenderer;
import com.letscooee.utils.Constants;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Process received payload and work accordingly
 *
 * @author Ashish Gaikwad 12/07/21
 * @since 1.0.0
 */

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class CooeeFirebaseMessagingService extends FirebaseMessagingService {

    Context context;

    public CooeeFirebaseMessagingService(Context context) {
        this.context = context;
    }

    @Override
    public void onNewToken(@NonNull @NotNull String token) {
        sendTokenToServer(token);
    }

    @Override
    public void onMessageReceived(@NonNull @NotNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        this.context = getApplicationContext();
        if (remoteMessage.getData().size() == 0) {
            return;
        }

        FontProcessor.downloadFonts(context,remoteMessage.getData().get("fonts"));
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

            Double version = (Double) baseTriggerData.get("version");
            if (version == null || version >= 4 || version < 3) {
                Log.d(Constants.TAG, "Unsupported payload version received " + version);
                return;
            }


            triggerData = gson.fromJson(rawTriggerData, TriggerData.class);

        } catch (JsonSyntaxException e) {
            CooeeFactory.getSentryHelper().captureException(e);
            return;
        }

        if (triggerData.getId() == null) {
            return;
        }

        EngagementTriggerHelper.storeActiveTriggerDetails(context, triggerData.getId(), triggerData.getDuration());

        if (triggerData.getPn() != null) {
            Event event = new Event("CE Notification Received", triggerData);
            CooeeFactory.getSafeHTTPService().sendEventWithoutNewSession(event);

            showNotification(triggerData);
        } else {
            EngagementTriggerHelper.renderInAppTrigger(context, triggerData);
        }
    }

    private void showNotification(TriggerData triggerData) {
        //Context context = getApplicationContext();
        NotificationRenderer renderer = new SimpleNotificationRenderer(context, triggerData);
        NotificationCompat.Builder notificationBuilder = renderer.getBuilder();

        Intent appLaunchIntent = new Intent(context, CooeeEmptyActivity.class);

        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.INTENT_TRIGGER_DATA_KEY, triggerData);

        appLaunchIntent.putExtra(Constants.INTENT_BUNDLE_KEY, bundle);
        appLaunchIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent appLaunchPendingIntent = PendingIntent.getActivity(context, triggerData.getId().hashCode(), appLaunchIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        renderer.addActions(createActionButtons(triggerData.getPn(), renderer.getNotificationID()));

        notificationBuilder.setContentIntent(appLaunchPendingIntent);

        String smallImage = triggerData.getPn().getSmallImage();
        if (TextUtils.isEmpty(smallImage)) {
            renderer.render();
        } else {
            this.imageLoader.load(triggerData.getPn().getSmallImage(), (Bitmap resource) -> {
                renderer.getSmallContentView().setImageViewBitmap(R.id.imageViewLarge, resource);
                renderer.getBigContentView().setImageViewBitmap(R.id.imageViewLarge, resource);
                renderer.render();
            });
        }
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

            PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), requestCode++, actionButtonIntent,
                    PendingIntent.FLAG_ONE_SHOT);

            actions[i++] = new NotificationCompat.Action(R.drawable.common_google_signin_btn_icon_dark, title, pendingIntent);

        }

        return actions;
    }
}
