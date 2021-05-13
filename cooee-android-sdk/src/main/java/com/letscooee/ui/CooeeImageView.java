package com.letscooee.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.letscooee.models.DataBlock;

public class CooeeImageView {

    public static ImageView generateImageView(Context context, DataBlock data) {
        ImageView imageView = new ImageView(context);

        ViewGroup.LayoutParams layoutParams = UIUtils.setHeightWidth(data);
        imageView.setLayoutParams(layoutParams);

        imageView.setScaleType(ImageView.ScaleType.FIT_XY);

        Glide.with(context).asDrawable().load(data.getUrl()).into(new CustomTarget<Drawable>() {
            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                imageView.setImageDrawable(resource);

            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {

            }
        });

        if (data.getxPosition()!=null){
            imageView.setX( (data.getxPosition()).floatValue());
        }
        if (data.getyPosition()!=null){
            imageView.setY( (data.getyPosition()).floatValue());
        }

        return imageView;
    }
}
