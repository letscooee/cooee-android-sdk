package com.letscooee.trigger.inapp.renderer;

import static com.letscooee.utils.Constants.TAG;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
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
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);

        Glide.with(context).load(((ImageElement) elementData).getSrc()).into(imageView);

        newElement = imageView;
        insertNewElementInHierarchy();
        processCommonBlocks();

        return newElement;
    }
}