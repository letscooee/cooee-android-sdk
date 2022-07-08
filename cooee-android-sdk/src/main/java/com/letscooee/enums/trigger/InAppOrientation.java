package com.letscooee.enums.trigger;

import android.content.pm.ActivityInfo;

/**
 * InApp orientation details.
 *
 * @author Ashish Gaikwad 08/07/22
 * @since 1.3.12
 */
public enum InAppOrientation {
    PORTRAIT(1, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT),
    LANDSCAPE(2, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE),
    UNKNOWN(0, ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

    private final int value;
    private final int screenOrientation;

    InAppOrientation(int value, int screenOrientation) {
        this.value = value;
        this.screenOrientation = screenOrientation;
    }

    public int getValue() {
        return value;
    }

    public int getScreenOrientation() {
        return screenOrientation;
    }
}
