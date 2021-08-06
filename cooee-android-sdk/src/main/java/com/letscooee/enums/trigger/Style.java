package com.letscooee.enums.trigger;

import android.graphics.Typeface;

public enum Style {

    NORMAL(Typeface.NORMAL),
    BOLD(Typeface.BOLD),
    ITALIC(Typeface.ITALIC),
    BOLD_ITALIC(Typeface.BOLD_ITALIC);

    public final int typeface;

    Style(int typeface) {
        this.typeface = typeface;
    }
}
