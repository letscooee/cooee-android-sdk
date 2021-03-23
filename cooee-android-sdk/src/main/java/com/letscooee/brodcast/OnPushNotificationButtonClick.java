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
import com.letscooee.models.TriggerData;
import com.letscooee.utils.CooeeSDKConstants;
import com.letscooee.utils.PropertyNameException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ashish Gaikwad
 */
public class OnPushNotificationButtonClick extends BroadcastReceiver {

    private CooeeSDK sdk;

    /**
     * onReceive will get call when broadcast will get trigger and it will hold context and intent of current instance.
     * This will also check for which Type receiver is get triggered and will send controls accordingly.
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        sdk = CooeeSDK.getDefaultInstance(context);

        try {

            String TYPE = intent.getStringExtra("TYPE");
            if (TYPE.equals("CAROUSEL"))
                processCarouselData(context, intent);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final ArrayList<Bitmap> bitmaps = new ArrayList<>();

    /**
     * Will access trigger data from intent and will proceed to image loading
     *
     * @param context will come from onReceive method.
     * @param intent  will come from onReceive method.
     */
    private void processCarouselData(Context context, Intent intent) throws PropertyNameException {


        TriggerData triggerData = (TriggerData) intent.getExtras().getParcelable("TRIGGERDATA");

        Map recieved = new HashMap<String, Object>();
        recieved.put("triggerID", triggerData.getId());
        sdk.sendEvent("CE PN Action Click", recieved);

        assert triggerData != null;
        loadBitmapsForCarousel(triggerData.getCarouselData(), 0, triggerData, context, intent);


    }

    /**
     * Load all images from carousel data by calling it self recursively.
     *
     * @param carouselData will be array if CarouselData
     * @param position     will be position for array pointing.
     * @param triggerData  will instance of TriggerData which will hold all other PN data
     */
    private void loadBitmapsForCarousel(CarouselData[] carouselData, final int position, TriggerData triggerData, Context context, Intent intent) {
        if (position < carouselData.length) {

            try {
                Glide.with(context)
                        .asBitmap().load(carouselData[position].getImageUrl()).into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        bitmaps.add(resource);
                        loadBitmapsForCarousel(triggerData.getCarouselData(), position + 1, triggerData, context, intent);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        loadBitmapsForCarousel(triggerData.getCarouselData(), position + 1, triggerData, context, intent);
                    }
                });
            } catch (Exception ignored) {
            }

        } else {
            showCarouselNotification(context, triggerData, intent);
        }

    }

    /**
     * showCarouselNotification will get call after all image loading is done. It will show carousel notification
     * and will also handle click event for scrolling.
     *
     * @param triggerData will instance of TriggerData which will hold all other PN data
     */
    private void showCarouselNotification(Context context, TriggerData triggerData, Intent intent) {
        int notificationId = intent.getExtras().getInt("NOTIFICATIONID", 0);
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
        bundle.putInt("NOTIFICATIONID", notificationId);
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
                image.setTextColor(R.id.carouselProductBanner, Color.parseColor("" + data.getTextColor()));
                image.setOnClickPendingIntent(R.id.carouselProductBanner, appLaunchPendingIntent);
            }
            if (data.isShowButton()) {
                image.setViewVisibility(R.id.carouselProductButton, View.VISIBLE);
                image.setTextViewText(R.id.carouselProductButton, data.getText());
                image.setTextColor(R.id.carouselProductButton, Color.parseColor("" + data.getTextColor()));
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

    }

    /**
     * Get Notification title from trigger data
     *
     * @param triggerData Trigger data
     * @return title
     */
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
