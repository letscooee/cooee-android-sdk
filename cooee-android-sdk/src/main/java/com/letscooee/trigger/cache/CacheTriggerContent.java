package com.letscooee.trigger.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.gson.JsonSyntaxException;
import com.letscooee.CooeeFactory;
import com.letscooee.models.trigger.TriggerData;
import com.letscooee.models.trigger.blocks.Background;
import com.letscooee.models.trigger.elements.BaseElement;
import com.letscooee.models.trigger.elements.ImageElement;
import com.letscooee.models.trigger.inapp.InAppTrigger;
import com.letscooee.trigger.InAppTriggerHelper;
import com.letscooee.utils.Constants;
import com.letscooee.utils.LocalStorageHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Store the payload and cache all the content of InApp to local cache.
 *
 * @author Ashish Gaikwad 25/05/22
 * @since 1.3.11
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class CacheTriggerContent {

    private final Context context;
    private InAppTriggerHelper inAppTriggerHelper;
    private OnInAppContentLoadedListener contentLoadedListener;
    private List<String> imageList;
    private List<String> loadedImageList;

    public CacheTriggerContent(Context context) {
        this.context = context;
    }

    /**
     * Loads <code>ian</code> for the {@code triggerData} and stores it in the local storage.
     *
     * @param triggerData the trigger data to be loaded and cached.
     */
    public void storePayloadAndLoadResources(TriggerData triggerData) {
        if (triggerData == null || TextUtils.isEmpty(triggerData.getId())) {
            return;
        }

        if (inAppTriggerHelper == null) {
            inAppTriggerHelper = new InAppTriggerHelper();
        }

        inAppTriggerHelper.loadLazyData(triggerData, (String rawInAppTrigger) -> {
            if (rawInAppTrigger == null) {
                return;
            }

            TriggerData inAppTriggerData;
            try {
                inAppTriggerData = TriggerData.fromJson(rawInAppTrigger);
            } catch (JsonSyntaxException e) {
                CooeeFactory.getSentryHelper().captureException("Fail to parse in-app trigger data", e);
                return;
            }

            if (inAppTriggerData.getInAppTrigger() == null) {
                return;
            }

            Map<String, Object> storedTrigger = LocalStorageHelper.getMap(context,
                    Constants.STORAGE_RAW_IN_APP_TRIGGER_KEY, new HashMap<>());
            storedTrigger.put(triggerData.getId(), rawInAppTrigger);

            LocalStorageHelper.putMap(context, Constants.STORAGE_RAW_IN_APP_TRIGGER_KEY, storedTrigger);
        });
    }

    /**
     * Loops {@link InAppTrigger#getElements()} to loads the
     * {@link Background#getImage()} and {@link ImageElement#getSrc()} and stores it in the cache.
     * <p>
     * It used {@link Glide}'s {@link DiskCacheStrategy#ALL} to cache the images.
     *
     * @param inAppTriggerData the trigger data to be loaded and cached.
     */
    public void loadAndCacheIANContent(TriggerData inAppTriggerData) {
        if (inAppTriggerData == null || inAppTriggerData.getInAppTrigger() == null) {
            return;
        }

        List<BaseElement> elementList = inAppTriggerData.getInAppTrigger().getElements();

        if (elementList == null || elementList.isEmpty()) {
            return;
        }

        imageList = new ArrayList<>();
        loadedImageList = new ArrayList<>();
        for (BaseElement element : elementList) {
            Background background = element.getBg();
            if (background != null && background.getImage() != null) {
                imageList.add(background.getImage().getSrc());
            }

            if (element instanceof ImageElement && !TextUtils.isEmpty(((ImageElement) element).getSrc())) {
                imageList.add(((ImageElement) element).getSrc());
            }
        }

        for (String imageURL : imageList) {
            loadImage(imageURL, 1);
        }

    }

    /**
     * Loads the image from the {@code imageUrl} and stores it in the cache.
     *
     * @param imageURL the image url to be loaded.
     * @param attempt  current attempt to load the image.
     */
    public void loadImage(String imageURL, int attempt) {
        Glide.with(context)
                .asBitmap()
                .load(imageURL)
                .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .listener(new RequestListener<Bitmap>() {

                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        if (attempt <= 3) {
                            loadImage(imageURL, attempt + 1);
                        } else {
                            loadedImageList.add(imageURL);
                            if (loadedImageList.size() == imageList.size()) {
                                sendCallback();
                            }
                        }

                        return true;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        loadedImageList.add(imageURL);
                        if (loadedImageList.size() == imageList.size()) {
                            sendCallback();
                        }

                        return true;
                    }
                })
                .preload();
    }

    /**
     * Sends the callback to the {@link com.letscooee.trigger.EngagementTriggerHelper} to notify
     * that the images are loaded.
     */
    private void sendCallback() {
        if (contentLoadedListener != null) {
            contentLoadedListener.onInAppContentLoaded();
        }
        imageList = null;
        contentLoadedListener = null;
    }

    /**
     * set {@link OnInAppContentLoadedListener} to class.
     *
     * @param contentLoadedListener {@link OnInAppContentLoadedListener}
     */
    public void setContentLoadedListener(OnInAppContentLoadedListener contentLoadedListener) {
        this.contentLoadedListener = contentLoadedListener;
    }
}
