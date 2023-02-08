package com.letscooee.utils;

import android.Manifest;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Type of permission going to ask in app
 *
 * @author Ashish Gaikwad 03/08/21
 * @since 1.0.0
 */
public enum PermissionType {

    LOCATION(Manifest.permission.ACCESS_FINE_LOCATION),
    CAMERA(Manifest.permission.CAMERA),
    PHONE_DETAILS(Manifest.permission.READ_PHONE_STATE),
    STORAGE(Manifest.permission.WRITE_EXTERNAL_STORAGE),
    NOTIFICATION(Manifest.permission.POST_NOTIFICATIONS);

    private final String text;
    private static final Map<String, PermissionType> VALUES = new HashMap<>();

    /**
     * Constructor to write value of {@link PermissionType}
     *
     * @param text value for enum
     */
    PermissionType(final String text) {
        this.text = text;
    }

    static {
        for (PermissionType d : PermissionType.values()) {
            VALUES.put(d.toString(), d);
        }
    }

    /**
     * Returns value of {@link PermissionType}
     *
     * @return Returns value of {@link PermissionType} present in {@link #text}
     */
    @NonNull
    @Override
    public String toString() {
        return text;
    }

    /**
     * Convert {@code value} to {@link PermissionType}
     *
     * @param value appropriate value of specific {@link PermissionType}
     * @return Returns {@link PermissionType} for the given <code>value</code> or null
     */
    public static PermissionType getByValue(String value) {
        return VALUES.get(value);
    }
}
