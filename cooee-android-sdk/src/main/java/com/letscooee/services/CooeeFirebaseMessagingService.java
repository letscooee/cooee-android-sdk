package com.letscooee.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
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
import com.letscooee.R;
import com.letscooee.campaign.EngagementTriggerActivity;
import com.letscooee.models.Event;
import com.letscooee.models.TriggerData;
import com.letscooee.retrofit.HttpCallsHelper;
import com.letscooee.utils.CooeeSDKConstants;

import java.util.HashMap;
import java.util.Map;

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
        if (remoteMessage.getData().get("messageType").equals("inapp"))
            showInAppMessaging(remoteMessage);
        else
            showNotification(remoteMessage);
    }

    private void showInAppMessaging(RemoteMessage remoteMessage) {
        Map<String, String> triggerDataMap = remoteMessage.getData();
        try {
        TriggerData triggerData = new TriggerData(triggerDataMap);
        Intent intent = new Intent(getApplicationContext(), EngagementTriggerActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("triggerData", triggerData);
        intent.putExtra("bundle", bundle);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(intent);
        } catch (Exception ex) {
            Log.d(CooeeSDKConstants.LOG_PREFIX, "Couldn't show Engagement Trigger");
            HttpCallsHelper.sendEvent(new Event("CE KPI", new HashMap<>()));
        }
    }

    private void showNotification(RemoteMessage remoteMessage) {
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");
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

        Bundle bundle = new Bundle();
        for (String key : remoteMessage.getData().keySet())
            bundle.putString(key, remoteMessage.getData().get(key));
        appLaunchIntent.putExtras(bundle);
        Log.d("bundle", bundle.toString());
        PendingIntent appLaunchPendingIntent = PendingIntent.getActivity(getApplicationContext(), 36644, appLaunchIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent actionButtonOneIntent = new Intent(getApplicationContext(), CooeeIntentService.class);
        actionButtonOneIntent.setAction("Notification");
        actionButtonOneIntent.putExtra("option", remoteMessage.getData().get("actionButtonOneText"));
        PendingIntent actionButtonOnePendingIntent = PendingIntent.getService(getApplicationContext(), 36645, actionButtonOneIntent, PendingIntent.FLAG_ONE_SHOT);

        Intent actionButtonTwoIntent = new Intent(getApplicationContext(), CooeeIntentService.class);
        actionButtonTwoIntent.setAction("Notification");
        actionButtonTwoIntent.putExtra("option", remoteMessage.getData().get("actionButtonTwoText"));
        PendingIntent actionButtonTwoPendingIntent = PendingIntent.getService(getApplicationContext(), 36646, actionButtonTwoIntent, PendingIntent.FLAG_ONE_SHOT);

        Intent actionButtonThreeIntent = new Intent(getApplicationContext(), CooeeIntentService.class);
        actionButtonThreeIntent.setAction("Notification");
        actionButtonThreeIntent.putExtra("option", remoteMessage.getData().get("actionButtonThreeText"));
        PendingIntent actionButtonThreePendingIntent = PendingIntent.getService(getApplicationContext(), 36647, actionButtonThreeIntent, PendingIntent.FLAG_ONE_SHOT);

        RemoteViews smallNotification = new RemoteViews(getPackageName(), R.layout.notification_small);
        smallNotification.setTextViewText(R.id.textViewTitle, title);
        smallNotification.setTextViewText(R.id.textViewInfo, body);

        RemoteViews largeNotification = new RemoteViews(getPackageName(), R.layout.notification_large);
        largeNotification.setTextViewText(R.id.textViewTitle, title);
        largeNotification.setTextViewText(R.id.textViewInfo, body);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), NOTIFICATION_CHANNEL_ID);

        Glide.with(getApplicationContext())
                .asBitmap().load(remoteMessage.getData().get("imageUrl")).into(new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
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
                        .setContentIntent(appLaunchPendingIntent)
                        .addAction(R.drawable.common_google_signin_btn_icon_dark, remoteMessage.getData().get("actionButtonOneText"), actionButtonOnePendingIntent)
                        .addAction(R.drawable.common_google_signin_btn_icon_dark, remoteMessage.getData().get("actionButtonTwoText"), actionButtonTwoPendingIntent)
                        .addAction(R.drawable.common_google_signin_btn_icon_dark, remoteMessage.getData().get("actionButtonThreeText"), actionButtonThreePendingIntent);
                Log.d("imageDownloaded", "true");
                try {
                    int id = Integer.parseInt(remoteMessage.getData().get("notificationId"));
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
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("userData", new HashMap<>());
        Map<String, String> userProperties = new HashMap<>();
        userProperties.put("Firebase Token", token);
        userMap.put("userProperties", userProperties);

        HttpCallsHelper.sendUserProfile(userMap, "Firebase Refreshed Token");
    }
}