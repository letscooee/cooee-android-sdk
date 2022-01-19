package com.letscooee.trigger.inapp.renderer;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.letscooee.models.trigger.elements.BaseElement;
import com.letscooee.trigger.inapp.TriggerContext;

public class ShapeRenderer extends AbstractInAppRenderer {
    protected ShapeRenderer(Context context, ViewGroup parentElement, BaseElement element, TriggerContext globalData) {
        super(context, parentElement, element, globalData);
    }

    @Override
    public View render() {
        newElement = new RelativeLayout(context);

        insertNewElementInHierarchy();
        processCommonBlocks();

        return newElement;
    }
}
