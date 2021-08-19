package com.letscooee.trigger.inapp.renderer;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.flexbox.FlexboxLayout;
import com.letscooee.CooeeFactory;
import com.letscooee.models.trigger.blocks.Overflow;
import com.letscooee.models.trigger.elements.*;
import com.letscooee.trigger.inapp.InAppGlobalData;

/**
 * Renders a {@link com.letscooee.models.trigger.elements.GroupElement}.
 *
 * @author Shashank Agrawal
 * @since 1.0.0
 */
public class GroupRenderer extends AbstractInAppRenderer {

    public GroupRenderer(Context context, ViewGroup parentView, BaseElement element, InAppGlobalData globalData) {
        super(context, parentView, element, globalData);
    }

    @Override
    public View render() {
        if (!elementData.getSize().isDisplayFlex()) {
            // Group/Layer will always be FLEX OR INLINE_FLEX. If not, log it and suppress the error
            CooeeFactory.getSentryHelper().captureMessage("Non FLEX Group/Layer received");
        }

        newElement = new FlexboxLayout(context);

        insertNewElementInHierarchy();
        processCommonBlocks();
        processOverflow();
        processChildren();

        return newElement;
    }

    protected void processChildren() {
        GroupElement groupElementData = (GroupElement) this.elementData;

        for (BaseElement child : groupElementData.getChildren()) {
            if (child instanceof ImageElement) {
                new ImageRenderer(context, (ViewGroup) newElement, child, globalData).render();

            } else if (child instanceof ButtonElement) {
                new ButtonRenderer(context, (ViewGroup) newElement, child, globalData).render();

            } else if (child instanceof TextElement) {
                new TextRenderer(context, (ViewGroup) newElement, child, globalData).render();

            } else if (child instanceof VideoElement) {
                new VideoRenderer(context, (ViewGroup) newElement, child, globalData).render();

            } else if (child instanceof GroupElement) {
                new GroupRenderer(context, (ViewGroup) newElement, child, globalData).render();
            }
        }
    }

    private void processOverflow() {
        Overflow overflow = elementData.getOverflow();
        boolean shouldHideOverflow = overflow != null && overflow.isHidden();

        materialCardView.setClipChildren(shouldHideOverflow);
        materialCardView.setClipToOutline(shouldHideOverflow);
        parentLayoutOfNewElement.setClipChildren(shouldHideOverflow);
        parentLayoutOfNewElement.setClipToOutline(shouldHideOverflow);
        ((ViewGroup) newElement).setClipChildren(shouldHideOverflow);
        newElement.setClipToOutline(shouldHideOverflow);
    }
}