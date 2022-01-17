package com.letscooee.enums.trigger;

import java.util.HashMap;
import java.util.Map;

/**
 * Assigning origin of the in-app trigger. This enum is named Gravity as in Android it is used as Gravity.
 *
 * @author abhishek taparia
 */
public enum Gravity {

    TOP_LEFT((byte) 1),
    TOP_CENTER((byte) 2),
    TOP_RIGHT((byte) 3),
    CENTER_LEFT((byte) 4),
    CENTER((byte) 5),
    CENTER_RIGHT((byte) 6),
    BOTTOM_LEFT((byte) 7),
    BOTTOM_CENTER((byte) 8),
    BOTTOM_RIGHT((byte) 9);

    private static final Map<Byte, Gravity> byteToGravity = new HashMap<>();

    static {
        for (Gravity type : Gravity.values()) {
            byteToGravity.put(type.gravity, type);
        }
    }

    public final byte gravity;

    Gravity(byte gravity) {
        this.gravity = gravity;
    }

    public static Gravity fromByte(byte i) {
        Gravity type = byteToGravity.get(i);
        if (type == null) {
            return Gravity.CENTER;
        }

        return type;
    }
}
