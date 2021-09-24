package com.letscooee.models.trigger.blocks;

import android.graphics.drawable.GradientDrawable;

/**
 * Angles for the gradient
 *
 * @author Ashish Gaikwad 17/08/21
 * @since 1.0.0
 */
public enum Orientation {

    DEGREE0(GradientDrawable.Orientation.RIGHT_LEFT),
    DEGREE45(GradientDrawable.Orientation.TR_BL),
    DEGREE90(GradientDrawable.Orientation.TOP_BOTTOM),
    DEGREE135(GradientDrawable.Orientation.TL_BR),
    DEGREE180(GradientDrawable.Orientation.LEFT_RIGHT),
    DEGREE225(GradientDrawable.Orientation.BL_TR),
    DEGREE270(GradientDrawable.Orientation.BOTTOM_TOP),
    DEGREE315(GradientDrawable.Orientation.BR_TL),
    DEGREE360(GradientDrawable.Orientation.RIGHT_LEFT);

    private final GradientDrawable.Orientation value;

    Orientation(GradientDrawable.Orientation value) {
        this.value = value;
    }

    public GradientDrawable.Orientation getValue() {
        return value;
    }
}