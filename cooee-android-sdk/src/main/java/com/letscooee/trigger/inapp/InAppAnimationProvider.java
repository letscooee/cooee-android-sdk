package com.letscooee.trigger.inapp;

import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import com.letscooee.utils.Constants;

/**
 * @author Shashank Agrawal
 * @since 1.0.0
 */
public class InAppAnimationProvider {

    private InAppAnimationProvider() {
    }

    public static Animation getEnterAnimation(com.letscooee.models.trigger.blocks.Animation animation) {
        if (animation == null || animation.getEnter() == null) {
            return getAnimation(1.0f, 0.0f, 0.0f, 0.0f);
        }

        switch (animation.getEnter()) {
            case SLIDE_IN_LEFT:
                return getAnimation(-1.0f, 0.0f, 0.0f, 0.0f);
            case SLIDE_IN_TOP:
                return getAnimation(0.0f, 0.0f, -1.0f, 0.0f);
            case SLIDE_IN_DOWN:
                return getAnimation(0.0f, 0.0f, 1.0f, 0.0f);
            case SLIDE_IN_TOP_LEFT:
                return getAnimation(-1.0f, 0.0f, -1.0f, 0.0f);
            case SLIDE_IN_TOP_RIGHT:
                return getAnimation(1.0f, 0.0f, -1.0f, 0.0f);
            case SLIDE_IN_BOTTOM_LEFT:
                return getAnimation(-1.0f, 0.0f, 1.0f, 0.0f);
            case SLIDE_IN_BOTTOM_RIGHT:
                return getAnimation(1.0f, 0.0f, 1.0f, 0.0f);
            default:
                return getAnimation(1.0f, 0.0f, 0.0f, 0.0f);
        }
    }

    public static Animation getExitAnimation(com.letscooee.models.trigger.blocks.Animation animation) {
        if (animation == null || animation.getExit() == null) {
            return getAnimation(0.0f, 1.0f, 0.0f, 0.0f);
        }

        switch (animation.getExit()) {
            case SLIDE_OUT_LEFT:
                return getAnimation(0.0f, -1.0f, 0.0f, 0.0f);
            case SLIDE_OUT_TOP:
                return getAnimation(0.0f, 0.0f, 0.0f, -1.0f);
            case SLIDE_OUT_DOWN:
                return getAnimation(0.0f, 0.0f, 0.0f, 1.0f);
            case SLIDE_OUT_TOP_LEFT:
                return getAnimation(0.0f, -1.0f, 0.0f, -1.0f);
            case SLIDE_OUT_TOP_RIGHT:
                return getAnimation(0.0f, 1.0f, 0.0f, -1.0f);
            case SLIDE_OUT_BOTTOM_LEFT:
                return getAnimation(0.0f, -1.0f, 0.0f, 1.0f);
            case SLIDE_OUT_BOTTOM_RIGHT:
                return getAnimation(0.0f, 1.0f, 0.0f, 1.0f);
            default:
                return getAnimation(0.0f, 1.0f, 0.0f, 0.0f);
        }
    }

    /**
     * Generate {@link TranslateAnimation} with given parameters.
     *
     * @param fromXDelta The start x position.
     * @param toXDelta   The end x position.
     * @param fromYDelta The start y position.
     * @param toYDelta   The end y position.
     * @return {@link Animation} generated with given parameters.
     */
    private static android.view.animation.Animation getAnimation(float fromXDelta, float toXDelta,
                                                                 float fromYDelta, float toYDelta) {
        android.view.animation.Animation animation = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, fromXDelta,
                Animation.RELATIVE_TO_PARENT, toXDelta,
                Animation.RELATIVE_TO_PARENT, fromYDelta,
                Animation.RELATIVE_TO_PARENT, toYDelta
        );
        animation.setDuration(Constants.ANIMATION_DURATION);
        animation.setInterpolator(new AccelerateInterpolator());
        return animation;
    }
}
