package com.letscooee.enums.trigger;

/**
 * Font Styles
 *
 * @author Ashish Gaikwad
 * @since 1.4.2
 */
public enum FontStyle {
    REGULAR(1),
    ITALICS(2),
    BOLD(3),
    BOLD_ITALICS(4);

    private final int value;

    FontStyle(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
