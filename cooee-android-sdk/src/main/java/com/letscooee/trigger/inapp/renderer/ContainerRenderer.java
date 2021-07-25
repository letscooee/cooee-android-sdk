package com.letscooee.trigger.inapp.renderer;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import com.google.android.flexbox.FlexboxLayout;
import com.letscooee.models.trigger.blocks.Size;
import com.letscooee.models.trigger.elements.BaseElement;
import com.letscooee.trigger.inapp.InAppGlobalData;

/**
 * Renders the top most container of the in-app.
 *
 * @author Shashank Agrawal
 * @since 1.0.0
 */
public class ContainerRenderer extends AbstractInAppRenderer {

    public ContainerRenderer(Context context, ViewGroup parentView, BaseElement element, InAppGlobalData globalData) {
        super(context, parentView, element, globalData);
    }

    @Override
    public View render() {
        if (elementData.getSize().getDisplay() == Size.Display.FLEX) {
            newElement = new FlexboxLayout(context);
        } else {
            newElement = new RelativeLayout(context);
        }

        parentElement.addView(newElement);
        processCommonBlocks();

        return newElement;
    }
}
