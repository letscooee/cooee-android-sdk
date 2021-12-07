package com.letscooee.enums.gesture;

import androidx.annotation.RestrictTo;

/**
 * Specify limited type of shake
 *
 * @author Ashish Gaikwad 29/11/21
 * @since 1.1.0
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public enum ShakeDensity {
    NONE(0),
    LOW(10),
    MEDIUM(20),
    HIGH(30);

    public final int shakeVolume;

    ShakeDensity(int shakeVolume) {
        this.shakeVolume = shakeVolume;
    }
}
