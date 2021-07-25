package com.letscooee.trigger.inapp.renderer;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import com.letscooee.models.trigger.elements.BaseElement;
import com.letscooee.trigger.inapp.InAppGlobalData;

/**
 * @author shashank
 */
public class LayerRenderer extends GroupRenderer {

    public LayerRenderer(Context context, ViewGroup parentView, BaseElement element, InAppGlobalData globalData) {
        super(context, parentView, element, globalData);
    }

    public View render() {
        super.render();
        newElement.setClipToOutline(true);
        return newElement;
    }
}
