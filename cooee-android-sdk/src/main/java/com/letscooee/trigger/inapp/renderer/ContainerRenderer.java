package com.letscooee.trigger.inapp.renderer;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.letscooee.models.trigger.elements.BaseElement;
import com.letscooee.models.trigger.elements.ButtonElement;
import com.letscooee.models.trigger.elements.ImageElement;
import com.letscooee.models.trigger.elements.ShapeElement;
import com.letscooee.models.trigger.elements.TextElement;
import com.letscooee.models.trigger.elements.VideoElement;
import com.letscooee.models.trigger.inapp.InAppTrigger;
import com.letscooee.trigger.inapp.TriggerContext;

/**
 * Renders the topmost container of the in-app.
 *
 * @author Shashank Agrawal
 * @since 1.0.0
 */
public class ContainerRenderer extends AbstractInAppRenderer {

    private final InAppTrigger inAppTrigger;

    public ContainerRenderer(Context context, ViewGroup parentView, BaseElement element, InAppTrigger inAppTrigger,
                             TriggerContext globalData) {
        super(context, parentView, element, globalData);
        this.inAppTrigger = inAppTrigger;
    }

    @Override
    public View render() {
        newElement = new FrameLayout(context);

        insertNewElementInHierarchy();
        processCommonBlocks();
        if (inAppTrigger != null) {
            processChildren();
        }

        // For container it is necessary to be of size of device i.e. match_parent
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(MP, MP);
        baseFrameLayout.setLayoutParams(layoutParams);
        newElement.setLayoutParams(layoutParams);

        // Displaying elements with respect to client portal mobile viewport
        materialCardView.setLayoutParams(new RelativeLayout.LayoutParams(1080, 1920));

        // Moving backgroundImage from base frame layout and putting it in root view layout.
        baseFrameLayout.removeView(backgroundImage);
        globalData.getTriggerParentLayout().addView(backgroundImage);

        // Removing and Adding material view to render it on top layer
        globalData.getTriggerParentLayout().removeView(materialCardView);
        globalData.getTriggerParentLayout().addView(materialCardView);

        return newElement;
    }

    protected void processChildren() {

        for (BaseElement child : inAppTrigger.getElements()) {
            if (child instanceof ImageElement) {
                new ImageRenderer(context, (ViewGroup) newElement, child, globalData).render();

            } else if (child instanceof ButtonElement) {
                new ButtonRenderer(context, (ViewGroup) newElement, child, globalData).render();

            } else if (child instanceof TextElement) {
                new TextRenderer(context, (ViewGroup) newElement, child, globalData).render();

            } else if (child instanceof VideoElement) {
                new VideoRenderer(context, (ViewGroup) newElement, child, globalData).render();

            } else if (child instanceof ShapeElement) {
                new ShapeRenderer(context, (ViewGroup) newElement, child, globalData).render();

            }
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
