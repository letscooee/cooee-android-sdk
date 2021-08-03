package com.letscooee.utils;

import android.Manifest;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public enum PermissionType {

    LOCATION(Manifest.permission.ACCESS_FINE_LOCATION),
    CAMERA(Manifest.permission.CAMERA),
    PHONE_DETAILS(Manifest.permission.READ_PHONE_STATE);


    private final String text;
    private static final Map<String, PermissionType> lookup = new HashMap<>();

    /**
     * @param text
     */
    PermissionType(final String text) {
        this.text = text;
    }

    static {
        for (PermissionType d : PermissionType.values()) {
            lookup.put(d.toString(), d);
        }
    }

    @NonNull
    @Override
    public String toString() {
        return text;
    }

    public static PermissionType getByValue(String value) {
        return lookup.get(value);
    }
}
