package com.letscooee.models;

import androidx.annotation.RestrictTo;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class DebugInformation {

    private final String key;
    private final String value;
    private final boolean sharable;

    public DebugInformation(String key, String value) {
        this(key, value, false);
    }

    public DebugInformation(String key, String value, boolean sharable) {
        this.key = key;
        this.value = value;
        this.sharable = sharable;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public boolean isSharable() {
        return sharable;
    }
}
