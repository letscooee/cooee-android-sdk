package com.letscooee.brodcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.letscooee.CooeeFactory;
import com.letscooee.CooeeSDK;
import com.letscooee.models.Event;
import com.letscooee.trigger.inapp.InAppTriggerActivity;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

public class ARActionPerformed extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String intentType = intent.getStringExtra("intentType");

        if (!intentType.equals("arResponse")) {
            return;
        }

        WeakReference<InAppTriggerActivity.InAppListener> listener = new WeakReference<>(CooeeSDK.getDefaultInstance(context));
        HashMap<String, Object> action = new Gson().fromJson(intent.getStringExtra("arData"), new TypeToken<HashMap<String, Object>>() {
        }.getType());
        new Handler().postAtTime(() -> listener.get().inAppNotificationDidClick(action), 3000);

        Map<String, Object> userProperty = (Map<String, Object>) action.get("userProperty");
        Event event = new Event("CE AR Closed", userProperty);
        CooeeFactory.getSafeHTTPService().sendEvent(event);
    }
}
