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
import org.jetbrains.annotations.NotNull;

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

    public void load(@NotNull String url, @NotNull Closure<Bitmap> callback) {
        this.requestBuilder
                .load(url)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        callback.call(resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        // no-op
                    }
                });
    }
}
