package com.letscooee.trigger;

import android.util.Log;
import androidx.annotation.RestrictTo;
import com.google.gson.Gson;
import com.letscooee.CooeeFactory;
import com.letscooee.exceptions.HttpRequestFailedException;
import com.letscooee.exceptions.InvalidTriggerDataException;
import com.letscooee.models.trigger.TriggerData;
import com.letscooee.room.trigger.PendingTrigger;
import com.letscooee.task.CooeeExecutors;
import com.letscooee.trigger.cache.PendingTriggerService;
import com.letscooee.utils.Constants;
import java9.util.concurrent.CompletableFuture;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import static com.google.firebase.messaging.Constants.TAG;

/**
 * A small helper class for in-app trigger for fetching data from server.
 *
 * @author Abhishek Taparia
 * @since 1.0.0
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class LazyTriggerLoader {

    private TriggerData triggerData;
    private final PendingTriggerService pendingTriggerService;
    private final CompletableFuture<Void> completableFuture = new CompletableFuture<>();

    public LazyTriggerLoader(TriggerData triggerData) {
        this.triggerData = triggerData;
        this.pendingTriggerService = CooeeFactory.getPendingTriggerService();
    }

    /**
     * Check if trigger data is present in {@link PendingTrigger} DB or load it from server.
     * If not present, then load the InApp data from the server.
     * <p>
     * TODO Merge this method with {@link #load()} method after 1st Oct 2022
     */
    public void checkInPendingTriggerOrLoad() throws ExecutionException, InterruptedException {
        PendingTrigger pendingTrigger = this.pendingTriggerService.findForTrigger(triggerData);
        if (pendingTrigger == null) {
            Log.v(Constants.TAG, "" + triggerData + " is not available in DB");
            this.load();
            return;
        }

        try {
            // Update the global variable as it is coming from DB
            triggerData = pendingTrigger.getTriggerData();
        } catch (InvalidTriggerDataException e) {
            return;
        }

        this.load();
    }

    /**
     * Load in-app data on a separate thread through a http call to server.
     */
    public void load() throws ExecutionException, InterruptedException {
        if (this.triggerData.shouldLazyLoad()) {
            Log.d(TAG, "Nothing to lazy load for " + triggerData);
            return;
        }

        Log.d(TAG, "Lazy load " + triggerData);
        CooeeExecutors.getInstance().singleThreadExecutor().execute(this::doHTTP);

        completableFuture.get();
    }

    private void doHTTP() {
        String triggerID = triggerData.getId();
        Map<String, Object> response;

        try {
            response = CooeeFactory.getBaseHTTPService().getLazyData(triggerID);
        } catch (HttpRequestFailedException e) {
            CooeeFactory.getSentryHelper().captureException(e);
            completableFuture.obtrudeException(e);
            return;
        }

        String rawTriggerData = new Gson().toJson(response);
        if (rawTriggerData == null) {
            completableFuture.complete(null);
            return;
        }

        TriggerData partialData;
        try {
            partialData = TriggerDataHelper.parse(rawTriggerData);
        } catch (InvalidTriggerDataException e) {
            completableFuture.obtrudeException(e);
            return;
        }

        triggerData.setInAppTrigger(partialData.getInAppTrigger());
        completableFuture.complete(null);
    }

}
