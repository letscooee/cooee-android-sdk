package com.letscooee.trigger.inapp.renderer;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.flexbox.FlexboxLayout;
import com.letscooee.models.trigger.blocks.Flex;
import com.letscooee.models.trigger.elements.*;
import com.letscooee.trigger.inapp.TriggerContext;

/**
 * Renders a {@link com.letscooee.models.trigger.elements.GroupElement}.
 *
 * @author Shashank Agrawal
 * @since 1.0.0
 */
public class GroupRenderer extends AbstractInAppRenderer {

    public GroupRenderer(Context context, ViewGroup parentView, BaseElement element, TriggerContext globalData) {
        super(context, parentView, element, globalData);
    }

    @Override
    public View render() {
        newElement = new FlexboxLayout(context);

        applyFlexParentProperties((GroupElement) elementData);
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
        boolean shouldHideOverflow = ((GroupElement) elementData).isOverflowHidden();

        materialCardView.setClipChildren(shouldHideOverflow);
        materialCardView.setClipToOutline(shouldHideOverflow);
        baseFrameLayout.setClipChildren(shouldHideOverflow);
        baseFrameLayout.setClipToOutline(shouldHideOverflow);
        ((ViewGroup) newElement).setClipChildren(shouldHideOverflow);
        newElement.setClipToOutline(shouldHideOverflow);
    }

    private void applyFlexParentProperties(GroupElement elementData) {
        if (!(newElement instanceof FlexboxLayout)) {
            return;
        }

        Flex flex = elementData.getFlexProperties();
        FlexboxLayout layout = (FlexboxLayout) newElement;

        layout.setFlexDirection(flex.getDirection());
        layout.setFlexWrap(flex.getWrap());
        layout.setJustifyContent(flex.getJustifyContent());
        layout.setAlignItems(flex.getAlignItems());
        layout.setAlignContent(flex.getAlignContent());
    }
}