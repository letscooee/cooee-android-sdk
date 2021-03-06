package com.letscooee.services;

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.letscooee.CooeeFactory;
import com.letscooee.R;
import com.letscooee.brodcast.OnPushNotificationButtonClick;
import com.letscooee.loader.http.RemoteImageLoader;
import com.letscooee.models.CarouselData;
import com.letscooee.models.Event;
import com.letscooee.models.TriggerButton;
import com.letscooee.models.TriggerData;
import com.letscooee.models.trigger.InAppTrigger;
import com.letscooee.models.trigger.PushNotificationTrigger;
import com.letscooee.pushnotification.PushProviderUtils;
import com.letscooee.trigger.CooeeEmptyActivity;
import com.letscooee.trigger.EngagementTriggerHelper;
import com.letscooee.trigger.pushnotification.CarouselNotificationRenderer;
import com.letscooee.trigger.pushnotification.NotificationRenderer;
import com.letscooee.trigger.pushnotification.SimpleNotificationRenderer;
import com.letscooee.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This helps connects with firebase for push notification
 *
 * @author Abhishek Taparia
 */
public class CooeeFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        sendTokenToServer(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if (remoteMessage.getData().size() == 0) {
            return;
        }

        this.handleTriggerData(remoteMessage.getData().get("triggerData"));
    }

    // TODO: 11/06/21 All code from below should be moved to their respective files

    private RemoteImageLoader imageLoader;

    private void handleTriggerData(String rawTriggerData) {
        if (TextUtils.isEmpty(rawTriggerData)) {
            Log.d(Constants.TAG, "No triggerData found on the notification payload");
            return;
        }

        if (imageLoader == null) {
            imageLoader = new RemoteImageLoader(getApplicationContext());
        }

        TriggerData triggerData;

        try {
            Gson gson = new Gson();

            HashMap<String, Object> baseTriggerData = gson.fromJson(rawTriggerData, new TypeToken<HashMap<String, Object>>() {
            }.getType());

            assert baseTriggerData != null;

            Double version = (Double) baseTriggerData.get("version");
            if (version == null || version >= 3) {
                Log.d(Constants.TAG, "Unsupported version received " + version);
                return;
            }

            // TODO: 11/06/21 Find a better way to find the kind of notification so that double gson parsing is not required
            //noinspection ConstantConditions
            if ((Boolean) baseTriggerData.get("showAsPN")) {
                triggerData = gson.fromJson(rawTriggerData, PushNotificationTrigger.class);
            } else {
                triggerData = gson.fromJson(rawTriggerData, InAppTrigger.class);
            }

        } catch (JsonSyntaxException e) {
            CooeeFactory.getSentryHelper().captureException(e);
            return;
        }

        if (triggerData.getId() == null) {
            return;
        }

        EngagementTriggerHelper.storeActiveTriggerDetails(getApplicationContext(), triggerData.getId(), triggerData.getDuration());

        if (triggerData instanceof PushNotificationTrigger) {
            Event event = new Event("CE Notification Received", triggerData);
            CooeeFactory.getSafeHTTPService().sendEventWithoutNewSession(event);

            if (triggerData.isCarousel()) {
                loadCarouselImages(triggerData.getCarouselData(), 0, (PushNotificationTrigger) triggerData);
            } else {
                showNotification((PushNotificationTrigger) triggerData);
            }
        } else {
            EngagementTriggerHelper.renderInAppTrigger(getApplicationContext(), triggerData);
        }
    }

    private final ArrayList<Bitmap> carouselBitmaps = new ArrayList<>();

    private void loadCarouselImages(CarouselData[] carouselData, final int i, PushNotificationTrigger triggerData) {
        if (i < carouselData.length) {
            this.imageLoader.load(carouselData[i].getImageUrl(), (Bitmap resource) -> {
                carouselBitmaps.add(resource);
                loadCarouselImages(triggerData.getCarouselData(), i + 1, triggerData);
            });
        } else {
            showCarouselNotification(triggerData);
        }
    }

    private void showCarouselNotification(PushNotificationTrigger triggerData) {
        String title = triggerData.getNotificationTitle();
        CarouselData[] images = triggerData.getCarouselData();

        if (images.length < 4) {
            return;
        }
        if (title == null) {
            return;
        }

        Context context = getApplicationContext();
        NotificationRenderer renderer = new CarouselNotificationRenderer(context, triggerData);

        RemoteViews views = renderer.getBigContentView();

        Bundle bundle = new Bundle();
        bundle.putInt("carouselPosition", triggerData.getCarouselOffset());
        bundle.putInt("notificationID", renderer.getNotificationID());
        bundle.putParcelable(Constants.INTENT_TRIGGER_DATA_KEY, triggerData);
        bundle.putString("intentType", "moveCarousel");

        Intent rightScrollIntent = new Intent(this, OnPushNotificationButtonClick.class);
        rightScrollIntent.putExtras(bundle);

        Intent leftScrollIntent = new Intent(this, OnPushNotificationButtonClick.class);
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

        if (images.length == 4) {
            views.setViewVisibility(R.id.right, View.INVISIBLE);
        }
        views.setOnClickPendingIntent(R.id.left, pendingIntentLeft);
        views.setOnClickPendingIntent(R.id.right, pendingIntentRight);
        views.setViewVisibility(R.id.left, View.INVISIBLE);

        for (int i = 0; i < carouselBitmaps.size(); i++) {
            RemoteViews image = new RemoteViews(getPackageName(), R.layout.row_notification_list);
            image.setImageViewBitmap(R.id.caroselImage, carouselBitmaps.get(i));

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

        renderer.render();
    }

    /**
     * Show push notification engagement trigger
     *
     * @param triggerData received from data payload
     */
    private void showNotification(PushNotificationTrigger triggerData) {
        Context context = getApplicationContext();
        NotificationRenderer renderer = new SimpleNotificationRenderer(context, triggerData);
        NotificationCompat.Builder notificationBuilder = renderer.getBuilder();

        Intent appLaunchIntent = new Intent(this, CooeeEmptyActivity.class);

        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.INTENT_TRIGGER_DATA_KEY, triggerData);

        appLaunchIntent.putExtra(Constants.INTENT_BUNDLE_KEY, bundle);
        appLaunchIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent appLaunchPendingIntent = PendingIntent.getActivity(this, triggerData.getId().hashCode(), appLaunchIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        renderer.addActions(createActionButtons(triggerData, renderer.getNotificationID()));

        notificationBuilder.setContentIntent(appLaunchPendingIntent);

        String smallImage = triggerData.getSmallImage();
        if (TextUtils.isEmpty(smallImage)) {
            renderer.render();
        } else {
            this.imageLoader.load(triggerData.getSmallImage(), (Bitmap resource) -> {
                renderer.getSmallContentView().setImageViewBitmap(R.id.imageViewLarge, resource);
                renderer.getBigContentView().setImageViewBitmap(R.id.imageViewLarge, resource);
                renderer.render();
            });
        }
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
                Intent actionButtonIntent = new Intent(getApplicationContext(), PushNotificationIntentService.class);
                actionButtonIntent.setAction(Constants.ACTION_PUSH_BUTTON_CLICK);
                actionButtonIntent.putExtra(Constants.INTENT_TRIGGER_DATA_KEY, triggerData);
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
}