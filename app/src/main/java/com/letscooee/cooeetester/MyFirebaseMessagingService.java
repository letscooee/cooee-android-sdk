package com.letscooee.cooeetester;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;

/**
 * FirebaseMessagingService from application side
 *
 * @author Abhishek Taparia
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Log.d("Refreshed Token:", s);
    }
}
