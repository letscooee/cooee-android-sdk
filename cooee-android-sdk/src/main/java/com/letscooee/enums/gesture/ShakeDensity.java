package com.letscooee.enums.gesture;

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
