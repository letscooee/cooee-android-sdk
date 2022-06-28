package com.letscooee.trigger;

import android.content.Context;
import android.util.Log;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.letscooee.loader.http.RemoteImageLoader;
import com.letscooee.models.trigger.TriggerData;
import com.letscooee.models.trigger.blocks.Background;
import com.letscooee.models.trigger.elements.ImageElement;
import com.letscooee.models.trigger.inapp.InAppTrigger;
import com.letscooee.utils.Constants;
import java9.util.concurrent.CompletableFuture;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * A small helper class for in-app trigger for fetching data from server.
 *
 * @author Abhishek Taparia
 * @since 1.0.0
 */
public class InAppTriggerHelper {

    private final Context context;
    private final TriggerData triggerData;

    public InAppTriggerHelper(Context context, TriggerData triggerData) {
        this.context = context;
        this.triggerData = triggerData;
    }

    public void render() {
        Log.d(Constants.TAG, "Render " + triggerData);

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
    private void loadLazyData() throws ExecutionException, InterruptedException {
        new LazyTriggerLoader(triggerData).load();
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
