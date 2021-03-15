package com.letscooee.brodcast;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.letscooee.CooeeSDK;
import com.letscooee.R;
import com.letscooee.models.CarouselData;
import com.letscooee.models.Event;
import com.letscooee.models.TriggerData;
import com.letscooee.utils.CooeeSDKConstants;
import com.letscooee.utils.PropertyNameException;

import java.util.ArrayList;
import java.util.HashMap;

import static com.letscooee.services.CooeeFirebaseMessagingService.sendEvent;

public class OnPushNotificationButtonClick extends BroadcastReceiver {

    private CooeeSDK sdk;

    @Override
    public void onReceive(Context context, Intent intent) {
        sdk = CooeeSDK.getDefaultInstance(context);
        try {

            String TYPE = intent.getStringExtra("TYPE");
            if (TYPE.equals("CAROUSEL"))
                handleCarousel(context, intent);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final ArrayList<Bitmap> bitmaps = new ArrayList<>();

    private void handleCarousel(Context context, Intent intent) throws PropertyNameException {
        sdk.sendEvent("PN_Action_Click", new HashMap<>());


        TriggerData triggerData = (TriggerData) intent.getExtras().getParcelable("TRIGGERDATA");

        assert triggerData != null;
        loadBitmaps(triggerData.getCarouselData(), 0, triggerData, context, intent);


    }

    private void loadBitmaps(CarouselData[] carouselData, final int i, TriggerData triggerData, Context context, Intent intent) {
        if (i < carouselData.length) {

            try {
                Glide.with(context)
                        .asBitmap().load(carouselData[i].getImageUrl()).into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        bitmaps.add(resource);
                        loadBitmaps(triggerData.getCarouselData(), i + 1, triggerData, context, intent);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
            } catch (Exception ignored) {
            }

        } else {
            showCarouselNotification(context, triggerData, intent);
        }

    }

    private void showCarouselNotification(Context context, TriggerData triggerData, Intent intent) {
        int notificationId = intent.getExtras().getInt("notificationId", 0);
        int POSITION = intent.getExtras().getInt("POSITION", 0);
        assert triggerData != null;
        String title = getNotificationTitle(triggerData);
        String body = getNotificationBody(triggerData);

        if (title == null) {
            return;
        }
        Log.d("TAG", "showCarouselNotification: Position " + POSITION);
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    CooeeSDKConstants.NOTIFICATION_CHANNEL_ID,
                    CooeeSDKConstants.NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_LOW);

            notificationChannel.setDescription("");
            notificationChannel.setSound(null, null);
            notificationManager.createNotificationChannel(notificationChannel);
        }


        RemoteViews smallNotification = new RemoteViews(context.getPackageName(), R.layout.notification_small);
        smallNotification.setTextViewText(R.id.textViewTitle, title);
        smallNotification.setTextViewText(R.id.textViewInfo, body);

        RemoteViews largeNotification = new RemoteViews(context.getPackageName(), R.layout.notification_large);
        largeNotification.setTextViewText(R.id.textViewTitle, title);
        largeNotification.setTextViewText(R.id.textViewInfo, body);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.notification_carousel);
        views.setTextViewText(R.id.textViewTitle, title);
        views.setTextViewText(R.id.textViewInfo, body);

        if (POSITION == triggerData.getCarouselData().length - 3) {
            views.setViewVisibility(R.id.right, View.INVISIBLE);
        } else {
            views.setViewVisibility(R.id.right, View.VISIBLE);
        }
        if (POSITION < 1) {
            views.setViewVisibility(R.id.left, View.INVISIBLE);
        } else {
            views.setViewVisibility(R.id.left, View.VISIBLE);
        }

        Bundle bundle = new Bundle();
        bundle.putInt("POSITION", POSITION + 1);
        bundle.putInt("notificationId", notificationId);
        bundle.putParcelable("TRIGGERDATA", triggerData);
        bundle.putString("TYPE", "CAROUSEL");

        Intent rightScrollIntent = new Intent(context, OnPushNotificationButtonClick.class);
        rightScrollIntent.putExtras(bundle);
        bundle.putInt("POSITION", POSITION - 1);
        Intent leftScrollIntent = new Intent(context, OnPushNotificationButtonClick.class);
        leftScrollIntent.putExtras(bundle);

        PendingIntent pendingIntentLeft = PendingIntent.getBroadcast(
                context,
                1,
                leftScrollIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        PendingIntent pendingIntentRight = PendingIntent.getBroadcast(
                context,
                0,
                rightScrollIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);


        views.setOnClickPendingIntent(R.id.left, pendingIntentLeft);
        views.setOnClickPendingIntent(R.id.right, pendingIntentRight);

        for (int i = POSITION; i < triggerData.getCarouselData().length; i++) {
            RemoteViews image = new RemoteViews(context.getPackageName(), R.layout.row_notification_list);
            image.setImageViewBitmap(R.id.caroselImage, bitmaps.get(i));
            CarouselData data = triggerData.getCarouselData()[i];

            PackageManager packageManager = context.getPackageManager();
            Intent appLaunchIntent = packageManager.getLaunchIntentForPackage(context.getPackageName());
            appLaunchIntent.putExtra("carouselData", data);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addNextIntentWithParentStack(appLaunchIntent);

            PendingIntent appLaunchPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            image.setOnClickPendingIntent(R.id.caroselImage, appLaunchPendingIntent);

            if (data.isShowBanner()) {
                image.setViewVisibility(R.id.carouselProductBanner, View.VISIBLE);
                image.setTextViewText(R.id.carouselProductBanner, data.getText());
                image.setTextColor(R.id.carouselProductBanner, Color.parseColor("#" + data.getTextColor()));
                image.setOnClickPendingIntent(R.id.carouselProductBanner, appLaunchPendingIntent);
            }
            if (data.isShowButton()) {
                image.setViewVisibility(R.id.carouselProductButton, View.VISIBLE);
                image.setTextViewText(R.id.carouselProductButton, data.getText());
                image.setTextColor(R.id.carouselProductButton, Color.parseColor("#" + data.getTextColor()));
                image.setOnClickPendingIntent(R.id.carouselProductButton, appLaunchPendingIntent);
            }

            views.addView(R.id.lvNotificationList, image);
        }


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
                context,
                CooeeSDKConstants.NOTIFICATION_CHANNEL_ID);


        notificationBuilder.setAutoCancel(false)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(context.getApplicationInfo().icon)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(smallNotification)
                .setCustomBigContentView(views)
                .setContentTitle(title)

                .setContentText(body);


        Notification notification = notificationBuilder.build();
        notificationManager.notify(notificationId, notification);

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StatusBarNotification[] statusBarNotifications = notificationManager.getActiveNotifications();
            for (StatusBarNotification statusBarNotification : statusBarNotifications) {
                if (statusBarNotification.getId() == notificationId) {
                    sendEvent(context, new Event("CE Notification Viewed", new HashMap<>()));
                }
            }
        }*/

    }

    private String getNotificationTitle(TriggerData triggerData) {
        String title;
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
}
