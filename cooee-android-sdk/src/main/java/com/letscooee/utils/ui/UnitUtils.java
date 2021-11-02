package com.letscooee.utils.ui;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import com.letscooee.BuildConfig;
import com.letscooee.CooeeFactory;

import static com.letscooee.utils.Constants.*;

/**
 * Utility class to process different units in px, % etc.
 *
 * @author Ashish Gaikwad 09/07/21
 * @since 1.0.0
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class UnitUtils {

    private static final int STANDARD_RESOLUTION = 720;
    private static final int DISPLAY_WIDTH;
    private static final int DISPLAY_HEIGHT;

    static {
        DISPLAY_WIDTH = CooeeFactory.getDeviceInfo().getDisplayWidth();
        DISPLAY_HEIGHT = CooeeFactory.getDeviceInfo().getDisplayHeight();

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Display width: " + DISPLAY_WIDTH + ", height: " + DISPLAY_HEIGHT);
        }
    }

    private UnitUtils() {
    }

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

    /**
     * Use to remove unit character from string and will convert {@link String} to {@link Float}
     *
     * @param value       {@link String} value which want to process
     * @param replaceUnit it can be "px,%,vh,vw"
     * @return int
     */
    public static float parseToFloat(@NonNull String value, @NonNull String replaceUnit) {
        return Float.parseFloat(value.replace(replaceUnit, ""));
    }

    public static int getCalculatedPixel(@NonNull String pixelString) {
        return parseToInt(pixelString, UNIT_PIXEL);
    }

    public static Integer getCalculatedValue(int parentWidth, int parentHeight, String value) {
        return getCalculatedValue(parentWidth, parentHeight, value, false);
    }

    public static Integer getCalculatedValue(View parent, String value) {
        return getCalculatedValue(parent, value, false);
    }

    public static Integer getCalculatedValue(View parent, String value, boolean isHeight) {
        return getCalculatedValue(parent.getMeasuredWidth(), parent.getMeasuredHeight(), value, isHeight);
    }

    public static Integer getCalculatedValue(int parentWidth, int parentHeight, String value, boolean isHeight) {
        if (TextUtils.isEmpty(value)) {
            return null;
        }

        value = value.trim().toLowerCase();
        if (value.contains(UNIT_PIXEL)) {
            int webPixels = getCalculatedPixel(value);
            // TODO: 26/07/21 Consider landscape mode here
            return webPixels * DISPLAY_HEIGHT / STANDARD_RESOLUTION;

        } else if (value.contains(UNIT_PERCENT)) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Parent width: " + parentWidth + ", height: " + parentHeight);
            }

            if (isHeight) {
                return ((parseToInt(value, UNIT_PERCENT) * parentHeight) / 100);
            } else {
                return ((parseToInt(value, UNIT_PERCENT) * parentWidth) / 100);
            }

        } else if (value.contains(UNIT_VIEWPORT_HEIGHT)) {
            return ((parseToInt(value, UNIT_VIEWPORT_HEIGHT) * DISPLAY_HEIGHT) / 100);

        } else if (value.contains(UNIT_VIEWPORT_WIDTH)) {
            return ((parseToInt(value, UNIT_VIEWPORT_WIDTH) * DISPLAY_WIDTH) / 100);
        } else {
            // TODO: 02/11/21 calculation aspect ratio
            return getCalculatedPixel(value);
        }
    }
}
