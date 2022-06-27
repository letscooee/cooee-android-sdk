package com.letscooee.trigger;

import android.content.Context;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;
import com.letscooee.CooeeFactory;
import com.letscooee.exceptions.HttpRequestFailedException;
import com.letscooee.loader.http.RemoteImageLoader;
import com.letscooee.models.trigger.TriggerData;
import com.letscooee.models.trigger.blocks.Background;
import com.letscooee.models.trigger.elements.ImageElement;
import com.letscooee.models.trigger.inapp.InAppTrigger;
import com.letscooee.task.CooeeExecutors;
import java9.util.concurrent.CompletableFuture;

import java.util.List;
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

    private final Context context;
    private final TriggerData triggerData;

    public InAppTriggerHelper(Context context, TriggerData triggerData) {
        this.context = context;
        this.triggerData = triggerData;
    }

    public void render() {
        try {
            this.loadLazyData();
            this.precacheImages();
        } catch (ExecutionException | InterruptedException e) {
            // Error handled
            return;
        }

        // TODO clean this up
        new EngagementTriggerHelper(context).renderInAppTrigger(triggerData);
    }

    /**
     * Load in-app data on a separate thread through a http call to server.
     */
    public void loadLazyData() throws ExecutionException, InterruptedException {
        if (this.triggerData.getInAppTrigger() != null) {
            return;
        }

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

    @Deprecated
    public void precacheImagesAndRender() {
        try {
            this.precacheImages();
        } catch (ExecutionException | InterruptedException e) {
            return;
        }

        // TODO clean this up
        new EngagementTriggerHelper(context).renderInAppTrigger(triggerData);
    }

    /**
     * Loops {@link InAppTrigger#getElements()} to loads the
     * {@link Background#getImage()} and {@link ImageElement#getSrc()} and stores it in the cache.
     * <p>
     * It uses {@link Glide}'s {@link DiskCacheStrategy#ALL} to cache the images.
     */
    private void precacheImages() throws ExecutionException, InterruptedException {
        if (triggerData == null || triggerData.getInAppTrigger() == null) {
            return;
        }

        List<String> imageList = triggerData.getInAppTrigger().getImageURLs();
        RemoteImageLoader loader = new RemoteImageLoader(this.context);
        CompletableFuture<Void> future = loader.cacheAll(imageList);

        future.get();
    }

}
