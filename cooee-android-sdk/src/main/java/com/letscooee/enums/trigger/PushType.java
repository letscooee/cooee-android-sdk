package com.letscooee.enums.trigger;

import androidx.annotation.RestrictTo;

/**
 * Defines Push notification types.
 *
 * @author Ashish Gaikwad 20/05/22
 * @since 1.3.12
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public enum PushType {

    SIMPLE(1),
    CAROUSEL(2);

    private final int value;

    PushType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
