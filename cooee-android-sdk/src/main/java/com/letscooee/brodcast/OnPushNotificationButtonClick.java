package com.letscooee.brodcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationManagerCompat;

import com.letscooee.CooeeSDK;
import com.letscooee.utils.PropertyNameException;

import java.util.HashMap;

public class OnPushNotificationButtonClick extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        CooeeSDK sdk = CooeeSDK.getDefaultInstance(context);
        try {
            int notificationId = intent.getIntExtra("notificationId", 0);
            sdk.sendEvent("PN_Action_Click", new HashMap<>());
            NotificationManagerCompat.from(context).cancel(null, notificationId);
        } catch (PropertyNameException e) {
            e.printStackTrace();
        }
    }
}
