package com.letscooee.trigger.inapp.renderer;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.letscooee.models.trigger.elements.BaseElement;
import com.letscooee.trigger.inapp.TriggerContext;

import java.util.ArrayList;

/**
 * Renders the top most container of the in-app.
 *
 * @author Shashank Agrawal
 * @since 1.0.0
 */
public class ContainerRenderer extends GroupRenderer {

    private ArrayList<BaseElement> layers;

    public ContainerRenderer(Context context, ViewGroup parentView, BaseElement element, ArrayList<BaseElement> layers,
                             TriggerContext globalData) {
        super(context, parentView, element, globalData);
        this.layers = layers;
    }

    protected void processChildren() {
        if (layers == null) {
            return;
        }

        for (BaseElement layer : layers) {
            new GroupRenderer(context, (ViewGroup) newElement, layer, globalData).render();
        }
    }

    @Override
    protected void processSizeBlock() {
        ViewGroup.LayoutParams layoutParams = parentElement.getLayoutParams();
        layoutParams.height = AbstractInAppRenderer.MP;
        layoutParams.width = AbstractInAppRenderer.MP;
        parentElement.setLayoutParams(layoutParams);

        RelativeLayout.LayoutParams cardLayoutParams = (RelativeLayout.LayoutParams) materialCardView.getLayoutParams();
        cardLayoutParams.height = AbstractInAppRenderer.MP;
        cardLayoutParams.width = AbstractInAppRenderer.MP;
        materialCardView.setLayoutParams(cardLayoutParams);
    }
}
