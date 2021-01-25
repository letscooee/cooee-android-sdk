package com.letscooee.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.util.Log;
import androidx.annotation.Nullable;

/**
 * @author Abhishek Taparia
 */
public class CooeeIntentService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public CooeeIntentService() {
        super("CooeeServiceIntent");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        switch (intent.getAction()) {
            case "Notification": {
                //TODO send which action button is clicked
                Log.d("ServiceIntent", intent.toString());
                NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
                notificationManager.cancel(34);
                Log.d("Option Choosen", intent.getStringExtra("option"));
                break;
            }
        }
    }
}
