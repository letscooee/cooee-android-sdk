package com.letscooee.brodcast;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import androidx.core.app.NotificationCompat;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.letscooee.CooeeFactory;
import com.letscooee.CooeeSDK;
import com.letscooee.R;
import com.letscooee.models.Event;
import com.letscooee.models.trigger.TriggerData;
import com.letscooee.trigger.inapp.InAppTriggerActivity;
import com.letscooee.utils.Constants;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ashish Gaikwad
 */
public class OnPushNotificationButtonClick extends BroadcastReceiver {

    /**
     * onReceive will get call when broadcast will get trigger and it will hold context and intent of current instance.
     * This will also check for which Type receiver is get triggered and will send controls accordingly.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String intentType = intent.getStringExtra("intentType");

        try {
            if (intentType.equals("moveCarousel")) {
                processCarouselData(context, intent);
            }else if (intentType.equals("arResponse")) {

                WeakReference<InAppTriggerActivity.InAppListener> listener = new WeakReference<>(CooeeSDK.getDefaultInstance(context));
                HashMap<String, Object> action = new Gson().fromJson(intent.getStringExtra("arData"), new TypeToken<HashMap<String, Object>>() {
                }.getType());
                new Handler().postAtTime(() -> listener.get().inAppNotificationDidClick(action), 3000);

                Map<String, Object> userProperty = (Map<String, Object>) action.get("userProperty");
                Event event = new Event("CE AR Closed", userProperty);
                CooeeFactory.getSafeHTTPService().sendEvent(event);
            }

        } catch (Exception e) {
            CooeeFactory.getSentryHelper().captureException(e);
        }
    }

    private final ArrayList<Bitmap> bitmaps = new ArrayList<>();

    /**
     * Will access trigger data from intent and will proceed to image loading
     *
     * @param context will come from onReceive method.
     * @param intent  will come from onReceive method.
     */
    private void processCarouselData(Context context, Intent intent) {
        TriggerData triggerData = intent
                .getExtras()
                .getParcelable(Constants.INTENT_TRIGGER_DATA_KEY);

        Event event = new Event("CE PN Carousel Move", triggerData);
        CooeeFactory.getSafeHTTPService().sendEventWithoutNewSession(event);

        //loadBitmapsForCarousel(triggerData.getCarouselData(), 0, triggerData, context, intent);
    }

    /**
     * Load all images from carousel data by calling it self recursively.
     *
     * @param carouselData will be array if CarouselData
     * @param position     will be position for array pointing.
     * @param triggerData  will instance of TriggerData which will hold all other PN data
     */
    /*private void loadBitmapsForCarousel(CarouselData[] carouselData, final int position, CoreTriggerData triggerData,
                                        Context context, Intent intent) {
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
            } catch (Exception e) {
                Sentry.captureException(e);
            }

        } else {
            showCarouselNotification(context, triggerData, intent);
        }

    }*/

    /**
     * This will get call after all image loading is done. It will show carousel notification
     * and will also handle click event for scrolling.
     *
     * @param triggerData will be instance of TriggerData which will hold all other PN data
     */
    private void showCarouselNotification(Context context, TriggerData triggerData, Intent intent) {
        int notificationId = intent.getExtras().getInt("notificationID", 0);
        int position = intent.getExtras().getInt("carouselPosition", 1);
        assert triggerData != null;
        String title = triggerData.getPn().getTitle().getText();
        String body = triggerData.getPn().getBody().getText();

        if (title == null) {
            return;
        }
        Log.d(Constants.TAG, "showCarouselNotification: Position " + position);
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    Constants.DEFAULT_CHANNEL_ID,
                    Constants.DEFAULT_CHANNEL_NAME,
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

        int carouselOffset = 0;//triggerData.getCarouselOffset();
        int totalImages = 0;//triggerData.getCarouselData().length;

        if (position + carouselOffset >= totalImages || position > totalImages) {
            views.setViewVisibility(R.id.right, View.INVISIBLE);
        } else {
            views.setViewVisibility(R.id.right, View.VISIBLE);
        }
        if (position < 1) {
            views.setViewVisibility(R.id.left, View.INVISIBLE);
        } else {
            views.setViewVisibility(R.id.left, View.VISIBLE);
        }

        Bundle bundle = new Bundle();
        bundle.putInt("carouselPosition", position + carouselOffset);
        bundle.putInt("notificationID", notificationId);
        bundle.putParcelable("triggerData", triggerData);
        bundle.putString("intentType", "moveCarousel");

        Intent rightScrollIntent = new Intent(context, OnPushNotificationButtonClick.class);
        rightScrollIntent.putExtras(bundle);
        bundle.putInt("carouselPosition", position - carouselOffset);
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

        /*for (int i = position; i < triggerData.getCarouselData().length; i++) {
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
                image.setTextColor(R.id.carouselProductBanner, Color.parseColor(data.getTextColor()));
                image.setOnClickPendingIntent(R.id.carouselProductBanner, appLaunchPendingIntent);
            }
            if (data.isShowButton()) {
                image.setViewVisibility(R.id.carouselProductButton, View.VISIBLE);
                image.setTextViewText(R.id.carouselProductButton, data.getText());
                image.setTextColor(R.id.carouselProductButton, Color.parseColor(data.getTextColor()));
                image.setOnClickPendingIntent(R.id.carouselProductButton, appLaunchPendingIntent);
            }

            views.addView(R.id.lvNotificationList, image);
        }*/

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
                context,
                Constants.DEFAULT_CHANNEL_ID);

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
}
