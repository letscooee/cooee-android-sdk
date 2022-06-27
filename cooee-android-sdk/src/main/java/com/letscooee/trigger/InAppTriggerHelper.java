package com.letscooee.trigger;

import com.google.gson.Gson;
import com.letscooee.CooeeFactory;
import com.letscooee.exceptions.HttpRequestFailedException;
import com.letscooee.models.trigger.TriggerData;
import com.letscooee.task.CooeeExecutors;
import java9.util.concurrent.CompletableFuture;

import java.util.Map;
import java.util.concurrent.ExecutionException;

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
     */
    public void loadLazyData(TriggerData triggerData) throws ExecutionException, InterruptedException {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        CooeeExecutors.getInstance().singleThreadExecutor().execute(() -> {
            String triggerID = triggerData.getId();
            Map<String, Object> response;

            try {
                response = CooeeFactory.getBaseHTTPService().getLazyData(triggerID);
            } catch (HttpRequestFailedException e) {
                CooeeFactory.getSentryHelper().captureException(e);
                completableFuture.cancel(false);
                return;
            }

            String rawInAppTrigger = gson.toJson(response);

            if (rawInAppTrigger == null) {
                completableFuture.complete(null);
                return;
            }

            TriggerData partialData = TriggerData.fromJson(rawInAppTrigger);
            triggerData.setInAppTrigger(partialData.getInAppTrigger());
            completableFuture.complete(null);
        });

        completableFuture.get();
    }

}
