package com.letscooee.trigger.inapp.renderer;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.letscooee.models.trigger.elements.BaseElement;
import com.letscooee.models.trigger.elements.ImageElement;
import com.letscooee.trigger.inapp.TriggerContext;

/**
 * @author shashank
 */
public class ImageRenderer extends AbstractInAppRenderer {

    public ImageRenderer(Context context, ViewGroup parentView, BaseElement element, TriggerContext globalData) {
        super(context, parentView, element, globalData);
    }

    @Override
    public View render() {
        ImageView imageView = new ImageView(context);
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        Glide.with(context).load(((ImageElement) elementData).getSrc()).into(imageView);

        newElement = imageView;
        insertNewElementInHierarchy();
        processCommonBlocks();

        return newElement;
    }
}