package com.letscooee.trigger;

import android.content.Context;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.letscooee.CooeeFactory;
import com.letscooee.exceptions.InvalidTriggerDataException;
import com.letscooee.font.FontProcessor;
import com.letscooee.loader.http.RemoteImageLoader;
import com.letscooee.models.trigger.TriggerData;
import com.letscooee.models.trigger.blocks.Background;
import com.letscooee.models.trigger.elements.ImageElement;
import com.letscooee.models.trigger.inapp.InAppTrigger;
import com.letscooee.room.trigger.PendingTrigger;
import com.letscooee.trigger.cache.PendingTriggerService;
import com.letscooee.utils.Logger;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java9.util.concurrent.CompletableFuture;

/**
 * A small helper class for in-app trigger for fetching data from server.
 *
 * @author Abhishek Taparia
 * @since 1.0.0
 */
public class InAppTriggerHelper {

    private boolean checkInPendingTrigger;
    private final Context context;
    private final PendingTriggerService pendingTriggerService;
    private final TriggerData triggerData;
    private final Logger logger;

    public InAppTriggerHelper(Context context, TriggerData triggerData) {
        this.context = context;
        this.triggerData = triggerData;
        this.pendingTriggerService = CooeeFactory.getPendingTriggerService();
        this.logger = CooeeFactory.getLogger();
    }

    /**
     * Check if related InApp data is present in {@link PendingTrigger} DB and load it.
     * If not present, then load the InApp data from the server.
     */
    public void checkInPendingTriggerAndRender() throws InvalidTriggerDataException {
        PendingTrigger pendingTrigger = this.pendingTriggerService.findForTrigger(triggerData);
        if (pendingTrigger == null) {
            logger.verbose(triggerData + " is already displayed");
            return;
        }

        this.checkInPendingTrigger = true;
        this.render();
    }

    public void render() throws InvalidTriggerDataException {
        logger.debug("Render " + triggerData);

        try {
            this.loadLazyData();
            this.precacheImages();
            this.precacheFonts();
            new EngagementTriggerHelper(context).renderInAppTrigger(triggerData);
        } catch (ExecutionException | InterruptedException e) {
            return;
        } catch (InvalidTriggerDataException e) {
            throw e;
        }
    }

    /**
     * Load in-app data on a separate thread through a http call to server.
     */
    private void loadLazyData() throws ExecutionException, InterruptedException {
        LazyTriggerLoader loader = new LazyTriggerLoader(triggerData);

        if (this.checkInPendingTrigger) {
            loader.checkInPendingTriggerOrLoad();
        } else {
            loader.load();
        }
    }

    /**
     * Cache all the font required for the InApp
     */
    private void precacheFonts() {
        if (triggerData == null || triggerData.getInAppTrigger() == null) {
            return;
        }

        List<String> fontURLs = triggerData.getInAppTrigger().getFontURLs();
        FontProcessor.downloadFontFromURLs(context, fontURLs);
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
        if (imageList == null || imageList.isEmpty()) {
            return;
        }

        RemoteImageLoader loader = new RemoteImageLoader(this.context);
        CompletableFuture<Boolean> future = loader.cacheAll(imageList);

        boolean futureResult = future.get();

        if (!futureResult) {
            throw new InterruptedException("Fail to download images");
        }
    }

}
