package com.letscooee.enums.trigger;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

/**
 * Defines Push notification types.
 *
 * @author Ashish Gaikwad 20/05/22
 * @since 1.3.11
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public enum PushType {

    NORMAL(1),
    LARGE(2),
    SMALL(20);

    private final int value;

    PushType(int value) {
        this.value = value;
    }

    @NonNull
    @Override
    public String toString() {
        return "PushType{" +
                "value=" + value +
                '}';
    }
}
