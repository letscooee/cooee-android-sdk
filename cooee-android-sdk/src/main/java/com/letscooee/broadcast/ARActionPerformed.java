package com.letscooee.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.letscooee.CooeeFactory;
import com.letscooee.CooeeSDK;
import com.letscooee.models.Event;
import com.letscooee.models.trigger.blocks.ClickAction;
import com.letscooee.utils.CooeeCTAListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Listen the response sent via AR Android SDK and send it to {@link CooeeCTAListener}
 * implemented in {@link CooeeSDK}
 *
 * @author Ashish Gaikwad 14/08/21
 * @since 1.0.0
 */
public class ARActionPerformed extends BroadcastReceiver {

    private static ClickAction lastARResponse = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        String intentType = intent.getStringExtra("intentType");

        if (!intentType.equals("arResponse")) {
            return;
        }

        lastARResponse = new Gson().fromJson(intent.getStringExtra("arData"),
                new TypeToken<ClickAction>() {
                }.getType());

        @SuppressWarnings("unchecked")
        Map<String, Object> userProperty = lastARResponse.getUserPropertiesToUpdate();
        Event event = new Event("CE AR Closed", userProperty);
        CooeeFactory.getSafeHTTPService().sendEvent(event);
    }

    /**
     * Called once app comes to foreground and send data to listener
     *
     * @param context {@link Context}
     */
    public static void processLastARResponse(Context context) {
        if (lastARResponse == null) {
            return;
        }

        CooeeCTAListener listener = CooeeSDK.getDefaultInstance(context).getCtaListener();
        HashMap<String, Object> data = (HashMap<String, Object>) lastARResponse.getKeyValue();

        if (listener != null && data != null) {
            listener.onResponse(data);
        }

        lastARResponse = null;
    }
}
