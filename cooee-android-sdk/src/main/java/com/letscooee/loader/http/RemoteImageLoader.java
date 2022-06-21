package com.letscooee.loader.http;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.letscooee.utils.Closure;

/**
 * Helper class to load an HTTP image from remote.
 *
 * @author Shashank Agrawal
 * @since 0.3.0
 */
public class RemoteImageLoader implements HttpResourceLoader<Bitmap> {

    private final RequestBuilder<Bitmap> requestBuilder;

    public RemoteImageLoader(Context context) {
        this.requestBuilder = Glide.with(context).asBitmap();
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
}
