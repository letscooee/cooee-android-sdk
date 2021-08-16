package com.letscooee.trigger.inapp.renderer;

import android.content.Context;
import android.view.ViewGroup;
import com.letscooee.models.trigger.elements.BaseElement;
import com.letscooee.models.trigger.inapp.Layer;
import com.letscooee.trigger.inapp.InAppGlobalData;

import java.util.ArrayList;

/**
 * Renders the top most container of the in-app.
 *
 * @author Shashank Agrawal
 * @since 1.0.0
 */
public class ContainerRenderer extends GroupRenderer {

    private ArrayList<Layer> layers;

    public ContainerRenderer(Context context, ViewGroup parentView, BaseElement element, ArrayList<Layer> layers,
                             InAppGlobalData globalData) {
        super(context, parentView, element, globalData);
        this.layers = layers;
    }

    protected void processChildren() {
        if (layers == null) {
            return;
        }

        for (Layer layer : layers) {
            new LayerRenderer(context, (ViewGroup) newElement, layer, globalData).render();
        }
    }
}
