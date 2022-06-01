package com.letscooee.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

/**
 * Provides a utility class to create PendingIntent.
 *
 * @author Ashish Gaikwad
 * @since 1.3.5
 */
public class PendingIntentUtility {

    private PendingIntentUtility(){}

    /**
     * Android 12 added more security with the PendingIntents. Now we need to tell that if PendingIntent
     * can be mutated or not.
     *
     * @return The PendingIntent flags.
     * @see <a href="https://developer.android.com/about/versions/12/behavior-changes-12#pending-intent-mutability">
     * Android 12 PendingIntent Mutability</a>
     * @see <a href="https://developer.android.com/reference/android/app/PendingIntent#FLAG_IMMUTABLE"> FLAG_IMMUTABLE</a>
     * @see <a href="https://developer.android.com/reference/android/app/PendingIntent#FLAG_MUTABLE"> FLAG_MUTABLE</a>
     */
    public static int getMutability() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return PendingIntent.FLAG_IMMUTABLE;
        } else {
            return PendingIntent.FLAG_ONE_SHOT;
        }
    }

    /**
     * Creates a PendingIntent for {@link android.app.Service} with given context.
     *
     * @param context     The context.
     * @param requestCode The request code.
     * @param intent      The intent.
     * @return The PendingIntent.
     */
    public static PendingIntent getService(Context context, int requestCode, Intent intent) {
        int flags = getMutability();
        return getService(context, requestCode, intent, flags);
    }

    /**
     * Creates a PendingIntent for {@link android.app.Service} with given context.
     *
     * @param context     The context.
     * @param requestCode The request code.
     * @param intent      The intent.
     * @param flags       The flags.
     * @return The PendingIntent.
     */
    public static PendingIntent getService(Context context, int requestCode, Intent intent, int flags) {
        return PendingIntent.getService(context, requestCode, intent, flags);
    }

    /**
     * Creates a PendingIntent for {@link android.app.Activity} with given context.
     *
     * @param context     The context.
     * @param requestCode The request code.
     * @param intent      The intent.
     * @return The PendingIntent.
     */
    public static PendingIntent getActivity(Context context, int requestCode, Intent intent) {
        return PendingIntent.getActivity(context, requestCode, intent, getMutability());
    }
}
