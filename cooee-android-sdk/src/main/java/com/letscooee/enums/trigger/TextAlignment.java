package com.letscooee.enums.trigger;

import android.view.Gravity;

public enum TextAlignment {
    LEFT(Gravity.START),
    CENTER(Gravity.CENTER),
    RIGHT(Gravity.END);

    private final int value;

    TextAlignment(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
