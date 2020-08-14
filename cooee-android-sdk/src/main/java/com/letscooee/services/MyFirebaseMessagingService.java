package com.letscooee.services;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;

/**
 * @author Abhishek Taparia
 * MyFirebaseMessagingService helps connects with firebase for push notification
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String token) {
        Log.d("TAG", "Refreshed token: " + token);
    }
}
