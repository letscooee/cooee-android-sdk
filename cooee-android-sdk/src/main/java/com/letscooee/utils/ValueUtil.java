package com.letscooee.utils;

import androidx.annotation.RestrictTo;

import static com.letscooee.utils.Constants.PERCENT;
import static com.letscooee.utils.Constants.PIXEL;
import static com.letscooee.utils.Constants.VIEWPORT_HEIGHT;
import static com.letscooee.utils.Constants.VIEWPORT_WIDTH;

/**
 * @author Ashish Gaikwad 09/07/21
 * @since 1.0.0
 */

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class ValueUtil {

    /**
     * Use to remove unit character from string and will convert {@link String} to {@link Integer}
     *
     * @param value {@link String} value which want to process
     * @param replaceUnit
     * @return
     */
    public static int parseToInt(String value, String replaceUnit) {
        return Integer.parseInt(value.replace(replaceUnit, ""));
    }

    public static int getCalculatedValue(String value) {
        return parseToInt(value, PIXEL);
    }

    public static int getCalculatedValue(int deviceWidth, int deviceHeight, String value) {
        if (value.contains(PIXEL)) {

            return getCalculatedValue(value);
        } else if (value.contains(PERCENT)) {

            return ((parseToInt(value, PERCENT) * deviceHeight) / 100);
        } else if (value.contains(VIEWPORT_HEIGHT)) {

            return ((parseToInt(value, VIEWPORT_HEIGHT) * deviceHeight) / 100);

        } else if (value.contains(VIEWPORT_WIDTH)) {

            return ((parseToInt(value, VIEWPORT_WIDTH) * deviceWidth) / 100);
        }
        return 0;
    }
}
