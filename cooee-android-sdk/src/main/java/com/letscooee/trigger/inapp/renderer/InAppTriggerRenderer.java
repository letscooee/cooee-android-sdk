package com.letscooee.trigger.inapp.renderer;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.letscooee.enums.trigger.Gravity;
import com.letscooee.models.trigger.elements.BaseElement;
import com.letscooee.models.trigger.inapp.InAppTrigger;
import com.letscooee.trigger.inapp.TriggerContext;
import com.letscooee.trigger.inapp.renderer.utils.GravityUtil;

/**
 * Special purpose of InAppTriggerRenderer is to process background & click action of the of {@link InAppTrigger}
 *
 * @author Ashish Gaikwad 09/02/22
 * @since 1.1.2
 */
public class InAppTriggerRenderer extends AbstractInAppRenderer {

    private final InAppTrigger inAppTrigger;

    public InAppTriggerRenderer(Context context, ViewGroup parentElement, BaseElement element,
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
        processGravity();
        return newElement;
    }

    /**
     * Process the gravity of the {@link InAppTrigger} and applies it to the {@link com.letscooee.models.trigger.inapp.Container}
     */
    private void processGravity() {
        Gravity gravity = inAppTrigger.getGravity();
        View view = globalData.getTriggerParentLayout().getChildAt(1);
        RelativeLayout.LayoutParams layoutParams =
                (RelativeLayout.LayoutParams) view.getLayoutParams();
        GravityUtil.processGravity(gravity, layoutParams);
        view.setLayoutParams(layoutParams);
    }

    /**
     * Apply specific size to the {@link InAppTrigger} body
     */
    @Override
    protected void processSizeBlock() {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(MP, MP);
        materialCardView.setLayoutParams(layoutParams);

        FrameLayout.LayoutParams frameLayoutParams = new FrameLayout.LayoutParams(MP, MP);
        baseFrameLayout.setLayoutParams(frameLayoutParams);
        backgroundImage.setLayoutParams(frameLayoutParams);
    }
}
