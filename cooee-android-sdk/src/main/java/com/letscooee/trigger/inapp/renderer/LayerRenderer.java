package com.letscooee.trigger.inapp.renderer;

import android.content.Context;
import android.view.ViewGroup;
import com.letscooee.models.trigger.elements.BaseElement;
import com.letscooee.trigger.inapp.TriggerContext;

/**
 * @author shashank
 */
public class LayerRenderer extends GroupRenderer {

    public LayerRenderer(Context context, ViewGroup parentView, BaseElement element, TriggerContext globalData) {
        super(context, parentView, element, globalData);
    }
}
