package com.letscooee.trigger.inapp.renderer;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.letscooee.models.trigger.blocks.Border;
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

        // region
        // This is very specific condition handled here. MaterialCardView by default don't provide dash
        // border so we do it through {@link GradientDrawable} and apply that drawable on newElement.
        // MaterialCardView here is used for corner radius and background solid color(if applicable).
        Border border = elementData.getBorder();
        if (border == null || border.getStyle() != Border.Style.DASH) {
            return newElement;
        }

        int borderColor = border.getColor().getHexColor();
        int calculatedBorder = getScaledPixelAsInt(border.getWidth());

        float dashWidth = calculatedBorder * 2;

        GradientDrawable elementDrawable = new GradientDrawable();
        elementDrawable.setStroke(calculatedBorder, borderColor, dashWidth, calculatedBorder);
        elementDrawable.setCornerRadius(getScaledPixelAsFloat(border.getRadius()) - (calculatedBorder / 2));

        newElement.setBackground(elementDrawable);
        // endregion

        return newElement;
    }
}
