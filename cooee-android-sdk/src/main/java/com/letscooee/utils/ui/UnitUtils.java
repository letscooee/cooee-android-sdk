package com.letscooee.utils.ui;

import android.content.res.Configuration;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import com.letscooee.BuildConfig;
import com.letscooee.CooeeFactory;
import com.letscooee.utils.Constants;

import static com.letscooee.utils.Constants.*;

/**
 * Utility class to process different units in px, % etc.
 *
 * @author Ashish Gaikwad 09/07/21
 * @since 1.0.0
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class UnitUtils {

    private static final double STANDARD_RESOLUTION_HEIGHT = 1920;
    private static final double STANDARD_RESOLUTION_WIDTH = 1080;
    private static int DISPLAY_WIDTH;
    private static int DISPLAY_HEIGHT;
    private static double SCALING_FACTOR;
    private static boolean IS_PORTRAIT;

    private UnitUtils() {
    }

    public static void checkOrientationAndFindScalingFactor() {
        IS_PORTRAIT = CooeeFactory.getDeviceInfo().getOrientation() == Configuration.ORIENTATION_PORTRAIT;
        DISPLAY_WIDTH = CooeeFactory.getDeviceInfo().getRunTimeDisplayWidth();
        DISPLAY_HEIGHT = CooeeFactory.getDeviceInfo().getRunTimeDisplayHeight();

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Display width: " + DISPLAY_WIDTH + ", height: " + DISPLAY_HEIGHT);
        }

        if (IS_PORTRAIT) {
            double longEdge = Math.min(STANDARD_RESOLUTION_WIDTH, STANDARD_RESOLUTION_HEIGHT);
            SCALING_FACTOR = DISPLAY_WIDTH / longEdge;
        } else {
            double longEdge = Math.max(STANDARD_RESOLUTION_WIDTH, STANDARD_RESOLUTION_HEIGHT);
            SCALING_FACTOR = DISPLAY_HEIGHT / longEdge;
        }
    }

    public static float getScaledPixel(float value) {
        checkOrientationAndFindScalingFactor();
        return (float) (value * SCALING_FACTOR);
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
            return webPixels * DISPLAY_HEIGHT / (int) STANDARD_RESOLUTION_HEIGHT;

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
            int webPixels = getCalculatedPixel(value);
            if (isHeight)
                return webPixels * DISPLAY_HEIGHT / (int) STANDARD_RESOLUTION_HEIGHT;
            else
                return webPixels * DISPLAY_WIDTH / (int) STANDARD_RESOLUTION_WIDTH;
        }
    }
}
