package com.letscooee.trigger.inapp.renderer;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.letscooee.models.trigger.elements.BaseElement;
import com.letscooee.models.trigger.inapp.InAppTrigger;
import com.letscooee.trigger.inapp.TriggerContext;

/**
 * Special purpose ofInAppBodyRenderer is to process background & click action of the of {@link InAppTrigger}
 *
 * @author Ashish Gaikwad 09/02/22
 * @since 1.1.2
 */
public class InAppBodyRenderer extends AbstractInAppRenderer {

    private final InAppTrigger inAppTrigger;

    public InAppBodyRenderer(Context context, ViewGroup parentElement, BaseElement element,
                             InAppTrigger inAppTrigger, TriggerContext globalData) {
        super(context, parentElement, element, globalData);

        this.inAppTrigger = inAppTrigger;
    }

    @Override
    public View render() {
        newElement = globalData.getTriggerParentLayout();
        processCommonBlocks();
        new ContainerRenderer(context, parentElement, inAppTrigger.getContainer(),
                inAppTrigger, globalData).render();
        return newElement;
    }

    @Override
    protected void processSizeBlock() {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(MP, MP);
        materialCardView.setLayoutParams(layoutParams);
        FrameLayout.LayoutParams frameLayoutParams = new FrameLayout.LayoutParams(MP, MP);
        baseFrameLayout.setLayoutParams(frameLayoutParams);
        backgroundImage.setLayoutParams(frameLayoutParams);
    }
}
