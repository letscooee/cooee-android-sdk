package com.letscooee.services;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.letscooee.utils.CooeeSDKConstants;

/**
 * @author Abhishek Taparia
 * MyFirebaseMessagingService helps connects with firebase for push notification
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(CooeeSDKConstants.LOG_PREFIX, "Refreshed token: " + token);
    }
}
