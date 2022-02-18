package com.letscooee.trigger;

import com.google.gson.Gson;
import com.letscooee.CooeeFactory;
import com.letscooee.exceptions.HttpRequestFailedException;
import com.letscooee.models.trigger.TriggerData;
import com.letscooee.models.trigger.inapp.InAppTrigger;
import com.letscooee.task.CooeeExecutors;
import com.letscooee.utils.Closure;
import com.letscooee.trigger.adapters.TriggerGsonDeserializer;

/**
 * A small helper class for in-app trigger for fetching data from server.
 *
 * @author Abhishek Taparia
 * @since 1.0.0
 */
public class InAppTriggerHelper {

    /**
     * Load in-app data on a separate thread through a http call to server.
     *
     * @param triggerData engagement trigger {@link TriggerData}
     * @param callback    callback on complete
     */
    public static void loadLazyData(TriggerData triggerData, Closure<InAppTrigger> callback) {
        CooeeExecutors.getInstance().singleThreadExecutor().execute(() -> {

            InAppTrigger inAppTrigger = getIANFromRawIAN(doHTTPForIAN(triggerData.getId()));

            if (inAppTrigger == null) {
                return;
            }

            callback.call(inAppTrigger);

        });
    }

    /**
     * Perform HTTP call to get IAN(In-App Notification Data) from server.
     *
     * @param id trigger id received from FCM
     * @return response data from server
     */
    private static Object doHTTPForIAN(String id) {
        try {
            return CooeeFactory.getBaseHTTPService().getIANTrigger(id).get("ian");
        } catch (HttpRequestFailedException e) {
            CooeeFactory.getSentryHelper().captureException(e);
        }
        return null;
    }

    /**
     * Convert raw in-app data received from {@link #doHTTPForIAN} to InAppTrigger instance.
     *
     * @param rawInApp raw in-app data
     * @return InAppTrigger instance
     */
    private static InAppTrigger getIANFromRawIAN(Object rawInApp) {
        if (rawInApp == null) {
            return null;
        }

        Gson gson = TriggerGsonDeserializer.getGson();

        return gson.fromJson(gson.toJson(rawInApp), InAppTrigger.class);
    }
}
