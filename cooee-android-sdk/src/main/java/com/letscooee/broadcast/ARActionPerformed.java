package com.letscooee.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.letscooee.CooeeFactory;
import com.letscooee.CooeeSDK;
import com.letscooee.models.Event;
import com.letscooee.models.trigger.blocks.ClickAction;
import com.letscooee.trigger.action.ClickActionExecutor;
import com.letscooee.utils.CooeeCTAListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Listen the response sent via AR Android SDK and send it to {@link CooeeCTAListener}
 * implemented in {@link CooeeSDK}
 *
 * @author Ashish Gaikwad 14/08/21
 * @since 1.0.0
 */
public class ARActionPerformed extends BroadcastReceiver {

    private static final String EVENT_NAME = "name";
    private static final String EVENT_PROPERTIES = "props";
    private static final String EVENT_CTA = "cta";
    private static ClickAction lastARResponse = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        String intentType = intent.getStringExtra("intentType");

        if (!intentType.equals("arResponse")) {
            return;
        }

        String rawResponse = intent.getStringExtra("arData");

        if (TextUtils.isEmpty(rawResponse)) {
            return;
        }

        processAREvents(context, rawResponse);
    }

    /**
     * Send event to the server received in <code>rawResponse</code>; Then will check for CTA in
     * <code>rawResponse</code>,
     * if found any will send {@link ClickAction#getUserPropertiesToUpdate()} to the
     * {@link CooeeSDK#updateUserProfile(Map)}
     *
     * @param context     application context
     * @param rawResponse json string sent via AR as event.
     */
    private void processAREvents(Context context, String rawResponse) {
        CooeeFactory.getLogger().sdkDebug("AR Event: ", rawResponse);

        Gson gson = new Gson();
        Map<String, Object> arResponse = gson.fromJson(rawResponse, new TypeToken<Map<String, Object>>() {
        }.getType());

        String eventName = Objects.requireNonNull(arResponse.get(EVENT_NAME)).toString();
        Event event = new Event(eventName);

        if (arResponse.get(EVENT_PROPERTIES) != null) {
            //noinspection unchecked
            event.setProperties((Map<String, Object>) arResponse.get(EVENT_PROPERTIES));
        }

        CooeeFactory.getSafeHTTPService().sendEvent(event);

        if (arResponse.get(EVENT_CTA) == null) {
            return;
        }

        lastARResponse = gson.fromJson(gson.toJson(arResponse.get(EVENT_CTA)), ClickAction.class);

        new ClickActionExecutor(context, lastARResponse, null).execute();
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

        CooeeCTAListener listener = CooeeSDK.getDefaultInstance(context).getCTAListener();
        HashMap<String, Object> data = (HashMap<String, Object>) lastARResponse.getKeyValue();

        if (listener != null && data != null) {
            listener.onResponse(data);
        }

        lastARResponse = null;
    }

}
