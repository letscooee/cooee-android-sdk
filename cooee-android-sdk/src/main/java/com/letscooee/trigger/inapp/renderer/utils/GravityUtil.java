package com.letscooee.trigger.inapp.renderer.utils;

import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import com.letscooee.enums.trigger.Gravity;

/**
 * Process InApp Gravity i.e position of the element in parent
 *
 * @author Ashish Gaikwad 08/02/22
 * @since 1.1.2
 */
public class GravityUtil {

    private GravityUtil() {
    }

    /**
     * Adds rule to the <code>layoutParams</code> wrt {@link Gravity} sent in parameters and returns
     * gravity for the view
     *
     * @param gravity      Instance {@link Gravity}
     * @param layoutParams {@link RelativeLayout.LayoutParams} of the view to which rules to be added
     * @return {@link android.view.Gravity}
     */
    public static int processGravity(@NonNull Gravity gravity, @NonNull RelativeLayout.LayoutParams layoutParams) {
        switch (gravity) {
            case TOP_LEFT: {
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
                return android.view.Gravity.START;
            }
            case TOP_CENTER: {
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
                layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
                return android.view.Gravity.CENTER_HORIZONTAL;
            }
            case TOP_RIGHT: {
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);
                return android.view.Gravity.END;
            }
            case CENTER_LEFT: {
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
                layoutParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
                return android.view.Gravity.START | android.view.Gravity.CENTER;
            }
            case CENTER_RIGHT: {
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);
                layoutParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
                return android.view.Gravity.END | android.view.Gravity.CENTER;
            }
            case BOTTOM_LEFT: {
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                return android.view.Gravity.START | android.view.Gravity.BOTTOM;
            }
            case BOTTOM_CENTER: {
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
                return android.view.Gravity.CENTER | android.view.Gravity.BOTTOM;
            }
            case BOTTOM_RIGHT: {
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                return android.view.Gravity.END | android.view.Gravity.BOTTOM;
            }
            default: {
                layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                return android.view.Gravity.CENTER;
            }
        }
    }
}
