package com.letscooee.trigger.inapp;

import com.letscooee.R;
import com.letscooee.models.trigger.blocks.Animation;

/**
 * @author Shashank Agrawal
 * @since 1.0.0
 */
public class InAppAnimationProvider {

    public static int getEnterAnimation(Animation animation) {
        if (animation == null || animation.getEnter() == null) {
            return R.anim.slide_in_right;
        }

        switch (animation.getEnter()) {
            case SLIDE_IN_LEFT:
                return android.R.anim.slide_in_left;
            case SLIDE_IN_RIGHT:
                return R.anim.slide_in_right;
            case SLIDE_IN_TOP:
                return R.anim.slide_in_up;
            case SLIDE_IN_DOWN:
                return R.anim.slide_in_down;
            default:
                return R.anim.slide_in_right;
        }
    }

    public static int getExitAnimation(Animation animation) {
        if (animation == null || animation.getExit() == null) {
            return R.anim.slide_out_right;
        }

        switch (animation.getExit()) {
            case SLIDE_OUT_LEFT:
                return R.anim.slide_out_left;
            case SLIDE_OUT_RIGHT:
                return R.anim.slide_out_right;
            case SLIDE_OUT_TOP:
                return R.anim.slide_out_up;
            case SLIDE_OUT_DOWN:
                return R.anim.slide_out_down;
            default:
                return R.anim.slide_out_right;
        }
    }
}