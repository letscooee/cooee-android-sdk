package com.letscooee.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.RestrictTo;

import com.bumptech.glide.Glide;
import com.letscooee.models.v3.block.Background;
import com.letscooee.models.v3.block.Border;
import com.letscooee.models.v3.block.Color;
import com.letscooee.models.v3.block.Glossy;
import com.letscooee.models.v3.block.Size;
import com.letscooee.models.v3.inapp.Container;

import jp.wasabeef.blurry.Blurry;

/**
 * @author Ashish Gaikwad 09/07/21
 * @since 1.0.0
 */

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class UIUtil {
    public static ViewGroup.LayoutParams generateLayoutParams(DisplayMetrics displayMetrics, Size size) {
        int deviceHeight = displayMetrics.heightPixels;
        int deviceWidth = displayMetrics.widthPixels;
        ViewGroup.LayoutParams layoutParams;

        if (size.getDisplay() == Size.Display.BLOCK) {
            layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
        } else {
            int calculatedHeight = size.getCalculatedHeight(deviceWidth, deviceHeight);
            int calculatedWidth = size.getCalculatedWidth(deviceWidth, deviceHeight);
            layoutParams = new ViewGroup.LayoutParams(calculatedWidth, calculatedHeight);
        }
        return layoutParams;
    }

    public static ImageView processBackground(Context context, Container container, Object imageContainer) {

        Background background = container.getBg();
        Border border = container.getBorder();
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        ImageView imageView = new ImageView(context);
        if (background.getSolid() != null) {
            if (border == null)
                processesSolid(imageView, background.getSolid());
            else
                processesSolidWithBorder(imageView, background.getSolid(), border, displayMetrics);

        } else if (background.getGlossy() != null) {

            processesGlassMorphism(context, imageView, background.getGlossy(),
                    border, imageContainer, displayMetrics);

        } else if (background.getImage() != null) {
            Glide.with(context).load(background.getImage().getUrl()).into(imageView);
            imageView.setAlpha((float) background.getImage().getAlpha());
        }
        return imageView;
    }

    private static void processesGlassMorphism(Context context, ImageView imageView, Glossy glossy,
                                               Border border, Object imageContainer,
                                               DisplayMetrics displayMetrics) {

        Blurry.Composer blurryComposer = Blurry.with(context)
                .radius(glossy.getRadius())
                .color(glossy.getColor().getSolidColor())
                .sampling(glossy.getSampling())
                .animate(500);

        if (imageContainer instanceof Bitmap) {
            blurryComposer
                    .from((Bitmap) imageContainer)
                    .into(imageView);
        } else {
            blurryComposer
                    .capture((ViewGroup) imageContainer)
                    .into(imageView);
        }

        if (border != null) {
            GradientDrawable drawable = new GradientDrawable();
            addBorder(drawable, border, displayMetrics);
            imageView.setBackground(drawable);
        }
    }

    private static void processesSolidWithBorder(ImageView imageView, Color color, Border border,
                                                 DisplayMetrics displayMetrics) {
        GradientDrawable drawable;

        if (color.getGrad() == null) {
            drawable = new GradientDrawable();
            drawable.setColor(color.getSolidColor());
        } else {
            drawable = color.getGrad().getGradient();
        }

        addBorder(drawable, border, displayMetrics);
        imageView.setBackground(drawable);
    }

    private static void addBorder(GradientDrawable drawable, Border border, DisplayMetrics displayMetrics) {
        if (border.getStyle() == Border.Style.SOLID) {
            drawable.setStroke(border.getWidth(displayMetrics.widthPixels, displayMetrics.heightPixels),
                    border.getColor().getSolidColor());
        } else {
            drawable.setStroke(border.getWidth(displayMetrics.widthPixels, displayMetrics.heightPixels),
                    border.getColor().getSolidColor(),
                    border.getDashWidth(displayMetrics.widthPixels, displayMetrics.heightPixels),
                    border.getDashGap(displayMetrics.widthPixels, displayMetrics.heightPixels));
        }
    }

    private static void processesSolid(ImageView imageView, Color solid) {
        if (solid.getGrad() == null) {
            imageView.setBackgroundColor(solid.getSolidColor());
        } else {
            imageView.setBackground(solid.getGrad().getGradient());
        }

    }
}
