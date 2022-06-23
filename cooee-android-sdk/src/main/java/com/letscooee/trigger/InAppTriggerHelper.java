package com.letscooee.trigger;

import com.google.gson.Gson;
import com.letscooee.CooeeFactory;
import com.letscooee.exceptions.HttpRequestFailedException;
import com.letscooee.models.trigger.TriggerData;
import com.letscooee.task.CooeeExecutors;
import com.letscooee.utils.Closure;

/**
 * A small helper class for in-app trigger for fetching data from server.
 *
 * @author Abhishek Taparia
 * @since 1.0.0
 */
public class InAppTriggerHelper {

    private static final Gson gson = new Gson();

    /**
     * Load in-app data on a separate thread through a http call to server.
     *
     * @param triggerData engagement trigger {@link TriggerData}
     * @param callback    callback on complete
     */
    public void loadLazyData(TriggerData triggerData, Closure<String> callback) {
        CooeeExecutors.getInstance().singleThreadExecutor().execute(() -> {
            String rawInAppTrigger = gson.toJson(doHTTPForLazyData(triggerData.getId()));

            if (rawInAppTrigger == null) {
                return;
            }

            callback.call(rawInAppTrigger);
        });
    }

    /**
     * Perform HTTP call to get IAN(In-App Notification Data) from server.
     *
     * @param id trigger id received from FCM
     * @return response data from server
     */
    private Object doHTTPForLazyData(String id) {
        try {
            return CooeeFactory.getBaseHTTPService().getLazyData(id);
        } catch (HttpRequestFailedException e) {
            CooeeFactory.getSentryHelper().captureException(e);
        }
        return null;
    }
}
