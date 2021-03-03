package com.letscooee.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.letscooee.brodcast.OnPushNotificationButtonClick;
import com.letscooee.R;
import com.letscooee.init.AppController;
import com.letscooee.init.PostLaunchActivity;
import com.letscooee.models.Event;
import com.letscooee.models.TriggerButton;
import com.letscooee.models.TriggerData;
import com.letscooee.retrofit.APIClient;
import com.letscooee.retrofit.HttpCallsHelper;
import com.letscooee.utils.CooeeSDKConstants;
import com.letscooee.utils.LocalStorageHelper;
import com.letscooee.utils.Utility;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import io.reactivex.rxjava3.subjects.ReplaySubject;

/**
 * MyFirebaseMessagingService helps connects with firebase for push notification
 *
 * @author Abhishek Taparia
 */
public class CooeeFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(CooeeSDKConstants.LOG_PREFIX, "Firebase Refreshed token: " + token);
        sendTokenToServer(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if (remoteMessage.getData().size() <= 0) {
            return;
        }

        Gson gson = new Gson();
        TriggerData triggerData = gson.fromJson(remoteMessage.getData().get("triggerData"), TriggerData.class);

        updateTriggerInStorage(triggerData);

        if (triggerData.getId() == null) {
            return;
        }

        if (triggerData.isShowAsPN()) {
            sendEvent(getApplicationContext(), new Event("CE Notification Received", new HashMap<>()));

            showNotification(triggerData);
        } else {
            showInAppMessaging(triggerData);
        }
    }

    private void updateTriggerInStorage(TriggerData triggerData) {
        storeTriggerID(LocalStorageHelper.getString(getApplicationContext(), CooeeSDKConstants.STORAGE_ACTIVE_TRIGGERS, "")
                , triggerData.getId()
                , String.valueOf(new Date().getTime() + 600 * 1000));
    }

    public ArrayList<HashMap<String, String>> storeTriggerID(String hashMapsString, String id, String time) {
        ArrayList<HashMap<String, String>> hashMaps = new ArrayList<>();

        if (!hashMapsString.equals("")) {
            hashMaps = Utility.getArrayListFromString(hashMapsString);
        }

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("triggerID", id);
        hashMap.put("duration", time);

        hashMaps.add(hashMap);

        LocalStorageHelper.putString(getApplicationContext(), CooeeSDKConstants.STORAGE_ACTIVE_TRIGGERS, hashMaps.toString());

        return hashMaps;
    }

    /**
     * Show inapp engagement trigger
     *
     * @param triggerData received from data payload
     */
    private void showInAppMessaging(TriggerData triggerData) {
        // Don't show inapp notification if app is in background
        if (!AppController.isBackground) {
            PostLaunchActivity.createTrigger(getApplicationContext(), triggerData);
        }
    }

    /**
     * Show push notification engagement trigger
     *
     * @param triggerData received from data payload
     */
    private void showNotification(TriggerData triggerData) {
        String title = getNotificationTitle(triggerData);
        String body = getNotificationBody(triggerData);

        if (title == null) {
            return;
        }

        NotificationManager notificationManager = (NotificationManager) getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    CooeeSDKConstants.NOTIFICATION_CHANNEL_ID,
                    CooeeSDKConstants.NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT);

            notificationChannel.setDescription("");
            notificationManager.createNotificationChannel(notificationChannel);
        }

        int notificationId = (int) new Date().getTime();
        PackageManager packageManager = getPackageManager();
        Intent appLaunchIntent = packageManager.getLaunchIntentForPackage(getApplicationContext().getPackageName());
        appLaunchIntent.putExtra("triggerData", triggerData);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(appLaunchIntent);

        PendingIntent appLaunchPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteViews smallNotification = new RemoteViews(getPackageName(), R.layout.notification_small);
        smallNotification.setTextViewText(R.id.textViewTitle, title);
        smallNotification.setTextViewText(R.id.textViewInfo, body);

        RemoteViews largeNotification = new RemoteViews(getPackageName(), R.layout.notification_large);
        largeNotification.setTextViewText(R.id.textViewTitle, title);
        largeNotification.setTextViewText(R.id.textViewInfo, body);

        Glide.with(getApplicationContext())
                .asBitmap().load(triggerData.getImageUrl()).into(new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
                        getApplicationContext(),
                        CooeeSDKConstants.NOTIFICATION_CHANNEL_ID);
                notificationBuilder = addAction(notificationBuilder, createActionButtons(triggerData, notificationId));

                smallNotification.setImageViewBitmap(R.id.imageViewLarge, resource);
                largeNotification.setImageViewBitmap(R.id.imageViewLarge, resource);
                notificationBuilder.setAutoCancel(true)
                        .setWhen(System.currentTimeMillis())
                        .setSmallIcon(getApplicationInfo().icon)
                        .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                        .setCustomContentView(smallNotification)
                        .setCustomBigContentView(largeNotification)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setContentIntent(appLaunchPendingIntent);

                Intent deleteIntent = new Intent(getApplicationContext(), CooeeIntentService.class);
                deleteIntent.setAction("Notification Deleted");

                Notification notification = notificationBuilder.build();
                notification.deleteIntent = PendingIntent.getService(
                        getApplicationContext(),
                        0,
                        deleteIntent,
                        PendingIntent.FLAG_ONE_SHOT);
                notificationManager.notify(notificationId, notification);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    StatusBarNotification[] statusBarNotifications = notificationManager.getActiveNotifications();
                    for (StatusBarNotification statusBarNotification : statusBarNotifications) {
                        if (statusBarNotification.getId() == notificationId) {
                            sendEvent(getApplicationContext(), new Event("CE Notification Viewed", new HashMap<>()));
                        }
                    }
                }
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {

            }
        });
    }

    /**
     * Send notification kpi tracking event
     *
     * @param event kpi event
     */
    public static void sendEvent(Context context, Event event) {
        APIClient.setAPIToken(LocalStorageHelper.getString(context, CooeeSDKConstants.STORAGE_SDK_TOKEN, ""));

        HttpCallsHelper.sendEventWithoutSDKState(event, null);
    }

    /**
     * Get Notification title from trigger data
     *
     * @param triggerData Trigger data
     * @return title
     */
    private String getNotificationTitle(TriggerData triggerData) {
        String title = null;
        if (triggerData.getTitle().getNotificationText() != null && !triggerData.getTitle().getNotificationText().isEmpty()) {
            title = triggerData.getTitle().getNotificationText();
        } else {
            title = triggerData.getTitle().getText();
        }
        return title;
    }

    /**
     * Get Notification body from trigger data
     *
     * @param triggerData Trigger data
     * @return body
     */
    private String getNotificationBody(TriggerData triggerData) {
        String body = "";
        if (triggerData.getMessage().getNotificationText() != null && !triggerData.getMessage().getNotificationText().isEmpty()) {
            body = triggerData.getMessage().getNotificationText();
        } else if (triggerData.getMessage().getText() != null && !triggerData.getMessage().getText().isEmpty()) {
            body = triggerData.getMessage().getText();
        }
        return body;
    }

    /**
     * Send firebase token to server
     *
     * @param token received from Firebase
     */
    private void sendTokenToServer(String token) {
        HttpCallsHelper.setFirebaseToken(token);
    }

    /**
     * Create action button in notifications
     *
     * @param triggerData    received from data payload
     * @param notificationId notification id
     * @return NotificationCompat.Action array
     */
    private NotificationCompat.Action[] createActionButtons(TriggerData triggerData, int notificationId) {
        NotificationCompat.Action[] actions = new NotificationCompat.Action[triggerData.getButtons().length];
        int requestCode = 36644 + notificationId;
        int i = 0;
        for (TriggerButton triggerButton : triggerData.getButtons()) {
            String title = null;
            if (triggerButton.getNotificationText() != null) {
                title = triggerButton.getNotificationText();
            } else if (triggerButton.getText() != null) {
                title = triggerButton.getText();
            }

            if (triggerButton.isShowInPN()) {
                Intent actionButtonIntent = new Intent(getApplicationContext(), CooeeIntentService.class);
                actionButtonIntent.setAction("Notification");
                actionButtonIntent.putExtra("triggerData", triggerData);
                actionButtonIntent.putExtra("buttonCount", i);
                actionButtonIntent.putExtra("notificationId", notificationId);
                PendingIntent pendingIntent = PendingIntent.getService(
                        getApplicationContext(),
                        requestCode++,
                        actionButtonIntent,
                        PendingIntent.FLAG_ONE_SHOT);
                actions[i++] = new NotificationCompat.Action(R.drawable.common_google_signin_btn_icon_dark, title, pendingIntent);
            }
        }

        return actions;
    }

    /**
     * Add action to NotificationCompat Builder
     *
     * @param builder NotificationCompat Builder
     * @param actions NotificationCompat.Action array
     * @return NotificationCompat Builder
     */
    private NotificationCompat.Builder addAction(NotificationCompat.Builder builder, NotificationCompat.Action[] actions) {
        for (NotificationCompat.Action action : actions) {
            if (action != null && action.getTitle() != null) {
                builder.addAction(action);
            }
        }
        return builder;
    }
}