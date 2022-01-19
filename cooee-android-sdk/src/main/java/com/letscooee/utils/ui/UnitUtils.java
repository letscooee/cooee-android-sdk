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

    public static double STANDARD_RESOLUTION_HEIGHT = 1920;
    public static double STANDARD_RESOLUTION_WIDTH = 1080;
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
     * Use to remove unit character from string and will convert {@link String} to {@link Float}
     *
     * @param value       {@link String} value which want to process
     * @param replaceUnit it can be "px,%,vh,vw"
     * @return int
     */
    public static float parseToFloat(@NonNull String value, @NonNull String replaceUnit) {
        return Float.parseFloat(value.replace(replaceUnit, ""));
    }
}
