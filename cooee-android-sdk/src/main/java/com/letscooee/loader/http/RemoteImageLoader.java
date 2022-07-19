package com.letscooee.loader.http;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.letscooee.CooeeFactory;
import com.letscooee.utils.Closure;
import com.letscooee.utils.SentryHelper;
import java.util.List;
import java9.util.concurrent.CompletableFuture;

/**
 * Helper class to load an HTTP image from remote.
 *
 * @author Shashank Agrawal
 * @since 0.3.0
 */
public class RemoteImageLoader implements HttpResourceLoader<Bitmap> {

    private static final int MAX_ATTEMPTS = 3;

    private final SentryHelper sentryHelper;
    private final RequestBuilder<Bitmap> requestBuilder;

    private final CompletableFuture<Void> completableFuture = new CompletableFuture<>();
    private int imagesToCache;
    private int imagesCached;
    private int imagesAttempted;

    public RemoteImageLoader(Context context) {
        this.requestBuilder = Glide.with(context).asBitmap();
        this.sentryHelper = CooeeFactory.getSentryHelper();
    }

    /**
     * Loads the image from the given URL.
     *
     * @param url      The URL of the image to load.
     * @param callback The callback to be invoked when the image is loaded.
     */
    public void load(@NonNull String url, @NonNull Closure<Bitmap> callback) {
        this.load(url, callback, callback);
    }

    /**
     * Loads the image from the given URL and invokes the given callbacks on success and failure.
     *
     * @param url           The URL of the image to load.
     * @param callback      The callback to invoke on success.
     * @param errorCallback The callback to invoke on failure.
     */
    public void load(@NonNull String url, @NonNull Closure<Bitmap> callback, @NonNull Closure<Bitmap> errorCallback) {
        this.requestBuilder
                .load(url)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        callback.call(resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        // No need to do anything here.
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        errorCallback.call(null);
                    }
                });
    }

    public CompletableFuture<Void> cacheAll(@NonNull List<String> urls) {
        this.imagesToCache = urls.size();
        for (String imageURL : urls) {
            cache(imageURL);
        }

        return this.completableFuture;
    }

    private void cache(String url) {
        cacheWithAttempt(url, 1);
    }

    private void cacheWithAttempt(String url, int currentAttempt) {
        this.requestBuilder
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        if (currentAttempt <= MAX_ATTEMPTS) {
                            cacheWithAttempt(url, currentAttempt + 1);
                        } else {
                            imagesAttempted++;
                            sentryHelper.captureException("Failed to precache the image", e);
                            completeTask();
                        }

                        return true;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        imagesAttempted++;
                        imagesCached++;
                        completeTask();
                        return true;
                    }
                })
                .preload();
    }

    private void completeTask() {
        if (this.imagesToCache != this.imagesAttempted) {
            return;
        }

        if (this.imagesToCache == this.imagesCached) {
            this.completableFuture.complete(null);
        } else {
            this.completableFuture.cancel(false);
        }
    }

}
