package com.letscooee.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
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
import com.google.gson.JsonSyntaxException;
import com.letscooee.BuildConfig;
import com.letscooee.CooeeFactory;
import com.letscooee.R;
import com.letscooee.brodcast.OnPushNotificationButtonClick;
import com.letscooee.models.CarouselData;
import com.letscooee.models.Event;
import com.letscooee.models.TriggerButton;
import com.letscooee.models.TriggerData;
import com.letscooee.pushnotification.PushProviderUtils;
import com.letscooee.pushnotification.renderer.NotificationRenderer;
import com.letscooee.retrofit.APIClient;
import com.letscooee.trigger.CooeeEmptyActivity;
import com.letscooee.trigger.EngagementTriggerHelper;
import com.letscooee.utils.Constants;
import com.letscooee.utils.LocalStorageHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.sentry.Sentry;

/**
 * MyFirebaseMessagingService helps connects with firebase for push notification
 *
 * @author Abhishek Taparia
 */
public class CooeeFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(Constants.LOG_PREFIX, "Firebase Refreshed token: " + token);
        sendTokenToServer(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if (remoteMessage.getData().size() == 0) {
            return;
        }

        String rawTriggerData = remoteMessage.getData().get("triggerData");
        if (TextUtils.isEmpty(rawTriggerData)) {
            Log.d(Constants.LOG_PREFIX, "No triggerData found on the notification payload");
            return;
        }

        TriggerData triggerData;

        try {
            triggerData = new Gson().fromJson(rawTriggerData, TriggerData.class);

        } catch (JsonSyntaxException e) {
            Log.e(Constants.LOG_PREFIX, "Unable to parse the trigger data", e);
            // TODO Change this to use SentryHelper once these code are moved to a separate class
            Sentry.captureException(e);

            return;
        }

        if (triggerData.getId() == null) {
            return;
        }

        EngagementTriggerHelper.storeActiveTriggerDetails(getApplicationContext(), triggerData.getId(), triggerData.getDuration());

        Map<String, Object> eventProps = new HashMap<>();
        eventProps.put("triggerID", triggerData.getId());

        if (triggerData.isShowAsPN()) {
            sendEvent(getApplicationContext(), new Event("CE Notification Received", eventProps));
            if (triggerData.isCarousel()) {
                loadBitmaps(triggerData.getCarouselData(), 0, triggerData);
            } else {
                showNotification(triggerData);
            }
        } else {
            showInAppMessaging(triggerData);
        }
    }

    private final ArrayList<Bitmap> bitmaps = new ArrayList<>();

    private void loadBitmaps(CarouselData[] carouselData, final int i, TriggerData triggerData) {
        if (i < carouselData.length) {

            try {
                Glide.with(getApplicationContext())
                        .asBitmap().load(carouselData[i].getImageUrl()).into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        bitmaps.add(resource);
                        loadBitmaps(triggerData.getCarouselData(), i + 1, triggerData);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
            } catch (Exception e) {
                Sentry.captureException(e);
            }

        } else {
            showCarouselNotification(triggerData);
        }

    }

    private void showCarouselNotification(TriggerData triggerData) {
        String title = getNotificationTitle(triggerData);
        String body = getNotificationBody(triggerData);
        CarouselData[] images = triggerData.getCarouselData();
        if (images.length < 4) {
            return;
        }
        if (title == null) {
            return;
        }

        Context context = getApplicationContext();
        NotificationRenderer renderer = new NotificationRenderer(context, triggerData);
        NotificationCompat.Builder notificationBuilder = renderer.getBuilder();
        NotificationManager notificationManager = renderer.getNotificationManager();

        RemoteViews smallNotification = new RemoteViews(getPackageName(), R.layout.notification_small);
        smallNotification.setTextViewText(R.id.textViewTitle, title);
        smallNotification.setTextViewText(R.id.textViewInfo, body);

        RemoteViews views = new RemoteViews(getPackageName(), R.layout.notification_carousel);
        views.setTextViewText(R.id.textViewTitle, title);
        views.setTextViewText(R.id.textViewInfo, body);

        Bundle bundle = new Bundle();
        bundle.putInt("POSITION", triggerData.getCarouselOffset());
        bundle.putInt("NOTIFICATIONID", renderer.getNotificationID());
        bundle.putParcelable("TRIGGERDATA", triggerData);
        bundle.putString("TYPE", "CAROUSEL");

        Intent rightScrollIntent = new Intent(this, OnPushNotificationButtonClick.class);
        rightScrollIntent.putExtras(bundle);

        Intent leftScrollIntent = new Intent(this, OnPushNotificationButtonClick.class);
        leftScrollIntent.putExtras(bundle);

        PendingIntent pendingIntentLeft = PendingIntent.getBroadcast(
                getApplicationContext(),
                1,
                leftScrollIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        PendingIntent pendingIntentRight = PendingIntent.getBroadcast(
                getApplicationContext(),
                0,
                rightScrollIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        if (images.length == 4) {
            views.setViewVisibility(R.id.right, View.INVISIBLE);
        }
        views.setOnClickPendingIntent(R.id.left, pendingIntentLeft);
        views.setOnClickPendingIntent(R.id.right, pendingIntentRight);
        views.setViewVisibility(R.id.left, View.INVISIBLE);

        for (int i = 0; i < bitmaps.size(); i++) {
            RemoteViews image = new RemoteViews(getPackageName(), R.layout.row_notification_list);
            image.setImageViewBitmap(R.id.caroselImage, bitmaps.get(i));

            CarouselData data = triggerData.getCarouselData()[i];

            PackageManager packageManager = getPackageManager();
            Intent appLaunchIntent = packageManager.getLaunchIntentForPackage(getApplicationContext().getPackageName());
            appLaunchIntent.putExtra("carouselData", data);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
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

        notificationBuilder
                .setCustomContentView(smallNotification)
                .setCustomBigContentView(views)
                .setContentTitle(title)
                .setContentText(body);

        renderer.render();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StatusBarNotification[] statusBarNotifications = notificationManager.getActiveNotifications();
            for (StatusBarNotification statusBarNotification : statusBarNotifications) {
                if (statusBarNotification.getId() == renderer.getNotificationID()) {
                    Map<String, Object> eventProps = new HashMap<>();
                    eventProps.put("triggerID", triggerData.getId());
                    sendEvent(getApplicationContext(), new Event("CE Notification Viewed", eventProps));
                }
            }
        }
    }

    /**
     * Show inapp engagement trigger
     *
     * @param triggerData received from data payload
     */
    private void showInAppMessaging(TriggerData triggerData) {
        EngagementTriggerHelper.renderInAppTrigger(getApplicationContext(), triggerData);
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

        Context context = getApplicationContext();
        NotificationRenderer renderer = new NotificationRenderer(context, triggerData);
        NotificationCompat.Builder notificationBuilder = renderer.getBuilder();
        NotificationManager notificationManager = renderer.getNotificationManager();

        Intent appLaunchIntent = new Intent(this, CooeeEmptyActivity.class);

        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.INTENT_TRIGGER_DATA_KEY, triggerData);
        appLaunchIntent.putExtra(Constants.INTENT_BUNDLE_KEY, bundle);
        appLaunchIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent appLaunchPendingIntent = PendingIntent.getActivity(this, triggerData.getId().hashCode(), appLaunchIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteViews smallNotification = new RemoteViews(getPackageName(), R.layout.notification_small);
        smallNotification.setTextViewText(R.id.textViewTitle, title);
        smallNotification.setTextViewText(R.id.textViewInfo, body);

        RemoteViews largeNotification = new RemoteViews(getPackageName(), R.layout.notification_large);
        largeNotification.setTextViewText(R.id.textViewTitle, title);
        largeNotification.setTextViewText(R.id.textViewInfo, body);

        Glide.with(getApplicationContext())
                .asBitmap().load(triggerData.getImageUrl1()).into(new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                addAction(notificationBuilder, createActionButtons(triggerData, renderer.getNotificationID()));

                smallNotification.setImageViewBitmap(R.id.imageViewLarge, resource);
                largeNotification.setImageViewBitmap(R.id.imageViewLarge, resource);
                notificationBuilder
                        .setCustomContentView(smallNotification)
                        .setCustomBigContentView(largeNotification)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setContentIntent(appLaunchPendingIntent);

                renderer.render();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    StatusBarNotification[] statusBarNotifications = notificationManager.getActiveNotifications();
                    for (StatusBarNotification statusBarNotification : statusBarNotifications) {
                        if (statusBarNotification.getId() == renderer.getNotificationID()) {
                            Map<String, Object> eventProps = new HashMap<>();
                            eventProps.put("triggerID", triggerData.getId());
                            sendEvent(getApplicationContext(), new Event("CE Notification Viewed", eventProps));
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
        CooeeFactory.getSafeHTTPService().sendEvent(event);
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
        if (BuildConfig.DEBUG) {
            Log.d(Constants.LOG_PREFIX, "FCM token received- " + token);
        }

        PushProviderUtils.pushTokenRefresh(token);
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
     */
    private void addAction(NotificationCompat.Builder builder, NotificationCompat.Action[] actions) {
        for (NotificationCompat.Action action : actions) {
            if (action != null && action.getTitle() != null) {
                builder.addAction(action);
            }
        }
    }
}