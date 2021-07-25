package com.letscooee.utils.ui;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import static com.letscooee.utils.Constants.*;

/**
 * Utility class to process different units in px, % etc.
 *
 * @author Ashish Gaikwad 09/07/21
 * @since 1.0.0
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class UnitUtils {

    /**
     * Use to remove unit character from string and will convert {@link String} to {@link Integer}
     *
     * @param value       {@link String} value which want to process
     * @param replaceUnit it can be "px,%,vh,vw"
     * @return int
     */
    public static int parseToInt(@NonNull String value, @NonNull String replaceUnit) {
        return Integer.parseInt(value.replace(replaceUnit, ""));
    }

    public static int getCalculatedPixel(@NonNull String pixelString) {
        return parseToInt(pixelString, UNIT_PIXEL);
    }

    public static int getCalculatedValue(int deviceWidth, int deviceHeight, String value) {
        return getCalculatedValue(deviceWidth, deviceHeight, value, false);
    }

    public static int getCalculatedValue(int deviceWidth, int deviceHeight, String value, boolean isHeight) {
        if (TextUtils.isEmpty(value)) {
            return 0;
        }

        value = value.trim();
        if (value.contains(UNIT_PIXEL)) {
            return getCalculatedPixel(value);
        } else if (value.contains(UNIT_PERCENT)) {
            if (isHeight) {
                return ((parseToInt(value, UNIT_PERCENT) * deviceHeight) / 100);
            } else {
                return ((parseToInt(value, UNIT_PERCENT) * deviceWidth) / 100);
            }
        } else if (value.contains(UNIT_VIEWPORT_HEIGHT)) {
            return ((parseToInt(value, UNIT_VIEWPORT_HEIGHT) * deviceHeight) / 100);
        } else if (value.contains(UNIT_VIEWPORT_WIDTH)) {
            return ((parseToInt(value, UNIT_VIEWPORT_WIDTH) * deviceWidth) / 100);
        }
        return 0;
    }
}
