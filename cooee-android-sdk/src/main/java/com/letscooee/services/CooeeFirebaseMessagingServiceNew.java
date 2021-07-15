package com.letscooee.services;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.letscooee.CooeeFactory;
import com.letscooee.loader.http.RemoteImageLoader;
import com.letscooee.models.v3.CoreTriggerData;
import com.letscooee.models.v3.PushNotificationData;
import com.letscooee.pushnotification.PushProviderUtils;
import com.letscooee.trigger.EngagementTriggerHelper;
import com.letscooee.utils.Constants;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class CooeeFirebaseMessagingServiceNew extends FirebaseMessagingService {

    Context context;

    public CooeeFirebaseMessagingServiceNew(Context context) {
        this.context = context;
    }

    @Override
    public void onNewToken(@NonNull @NotNull String token) {
        sendTokenToServer(token);
    }

    @Override
    public void onMessageReceived(@NonNull @NotNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if (remoteMessage.getData().size() == 0) {
            return;
        }

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

        CoreTriggerData triggerData;

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


            if (baseTriggerData.get("pn") != null) {
                triggerData = gson.fromJson(rawTriggerData, PushNotificationData.class);
            } else {
                triggerData = gson.fromJson(rawTriggerData, CoreTriggerData.class);
            }

        } catch (JsonSyntaxException e) {
            CooeeFactory.getSentryHelper().captureException(e);
            return;
        }

        if (triggerData.getId() == null) {
            return;
        }

        EngagementTriggerHelper.storeActiveTriggerDetails(context, triggerData.getId(), triggerData.getDuration());

        if (triggerData instanceof PushNotificationData) {
            //Event event = new Event("CE Notification Received", triggerData);
            //CooeeFactory.getSafeHTTPService().sendEventWithoutNewSession(event);

            //if (triggerData.isCarousel()) {
            //    loadCarouselImages(triggerData.getCarouselData(), 0, (PushNotificationTrigger) triggerData);
            // } else {
            //   showNotification((PushNotificationTrigger) triggerData);
            //}
        } else {
            EngagementTriggerHelper.renderInAppTriggerNew(context, triggerData);
        }
    }
}
