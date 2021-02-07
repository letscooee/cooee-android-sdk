package com.letscooee.services;

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
import com.letscooee.R;
import com.letscooee.init.AppController;
import com.letscooee.init.PostLaunchActivity;
import com.letscooee.models.TriggerButton;
import com.letscooee.models.TriggerData;
import com.letscooee.retrofit.HttpCallsHelper;
import com.letscooee.utils.CooeeSDKConstants;

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
        Log.d("TriggerDataInReceived", triggerData.toString());
        if (triggerData.isShowAsPN()) {
            showNotification(triggerData);
        } else {
            showInAppMessaging(triggerData);
        }
    }

    private void showInAppMessaging(TriggerData triggerData) {
        // Don't show inapp notification if app is in background
        if (!AppController.isBackground) {
            PostLaunchActivity.createTrigger(getApplicationContext(), triggerData);
        }
    }

    private void showNotification(TriggerData triggerData) {
        String title = (triggerData.getTitle().getNotificationText().isEmpty() || triggerData.getTitle().getNotificationText() == null)
                ? triggerData.getTitle().getText()
                : triggerData.getTitle().getNotificationText();
        String body = (triggerData.getMessage().getNotificationText().isEmpty() || triggerData.getMessage().getNotificationText() == null)
                ? triggerData.getMessage().getText()
                : triggerData.getMessage().getNotificationText();

        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "Cooee";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Cooee",
                    NotificationManager.IMPORTANCE_DEFAULT);

            notificationChannel.setDescription("");
            notificationManager.createNotificationChannel(notificationChannel);
        }

        PackageManager packageManager = getPackageManager();
        Intent appLaunchIntent = packageManager.getLaunchIntentForPackage(getApplicationContext().getPackageName());
        appLaunchIntent.putExtra("triggerData", triggerData);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(appLaunchIntent);

        PendingIntent appLaunchPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Action[] actions = new NotificationCompat.Action[triggerData.getButtons().length];
        int requestCode = 36644;
        int i = 0;
        for (TriggerButton triggerButton : triggerData.getButtons()) {
            actions[i++] = createActionButtonPendingIntent(triggerButton, requestCode++);
        }

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
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), NOTIFICATION_CHANNEL_ID);
                notificationBuilder = addAction(notificationBuilder, actions);

                smallNotification.setImageViewBitmap(R.id.imageViewLarge, resource);
                largeNotification.setImageViewBitmap(R.id.imageViewLarge, resource);
                notificationBuilder.setAutoCancel(true)
                        .setWhen(System.currentTimeMillis())
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                        .setCustomContentView(smallNotification)
                        .setCustomBigContentView(largeNotification)

                        .setContentTitle(title)
                        .setContentText(body)
                        .setContentInfo("Info")
                        .setContentIntent(appLaunchPendingIntent);
                try {
                    int id = triggerData.getId();
                    notificationManager.notify(id, notificationBuilder.build());
                } catch (Exception e) {
                    notificationManager.notify(36648, notificationBuilder.build());
                }
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {

            }
        });
    }

    private void sendTokenToServer(String token) {
        HttpCallsHelper.setFirebaseToken(token);
    }

    private NotificationCompat.Action createActionButtonPendingIntent(TriggerButton button, int requestCode) {
        if (button.isShowInPN()) {
            Intent actionButtonIntent = new Intent(getApplicationContext(), CooeeIntentService.class);
            actionButtonIntent.setAction("Notification");
            actionButtonIntent.putExtra("action", button.getAction());
            PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), requestCode, actionButtonIntent, PendingIntent.FLAG_ONE_SHOT);
            return new NotificationCompat.Action(R.drawable.common_google_signin_btn_icon_dark, button.getNotificationText(), pendingIntent);
        }
        return new NotificationCompat.Action(R.drawable.common_google_signin_btn_icon_dark, null, null);
    }

    private NotificationCompat.Builder addAction(NotificationCompat.Builder builder, NotificationCompat.Action[] actions) {
        for (NotificationCompat.Action action : actions) {
            builder.addAction(action);
        }
        return builder;
    }
}