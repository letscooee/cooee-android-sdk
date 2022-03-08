package com.letscooee.pushnotification;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import androidx.annotation.RestrictTo;
import com.letscooee.CooeeFactory;
import com.letscooee.utils.Constants;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for push notifications.
 *
 * @author Shashank Agrawal
 * @since 0.3.0
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class PushProviderUtils {

    private static String lastSentToken;

    public static synchronized void pushTokenRefresh(String token) {
        if (lastSentToken != null && lastSentToken.equals(token)) {
            Log.d(Constants.TAG, "Not sending the same FCM token");
            return;
        }

        lastSentToken = token;
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("pushToken", token);

        CooeeFactory.getSafeHTTPService().updatePushToken(requestData);
    }

    public static String getLastSentToken() {
        return lastSentToken;
    }

    /**
     * Creates a PendingIntent for the given context.
     * Android 12 added more security with the PendingIntents. Now we need to tell that if PendingIntent
     * can be mutated or not.
     *
     * @param context     The context.
     * @param requestCode The request code.
     * @param intent      The intent.
     * @return The PendingIntent.
     * @see <a href="https://developer.android.com/about/versions/12/behavior-changes-12#pending-intent-mutability">
     * Android 12 PendingIntent Mutability</a>
     * @see <a href="https://developer.android.com/reference/android/app/PendingIntent#FLAG_IMMUTABLE"> FLAG_IMMUTABLE</a>
     * @see <a href="https://developer.android.com/reference/android/app/PendingIntent#FLAG_MUTABLE"> FLAG_MUTABLE</a>
     * @since 1.3.5
     */
    public static PendingIntent getPendingIntentService(Context context, int requestCode, Intent intent) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return PendingIntent.getService(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
            );
        }

        return PendingIntent.getService(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_ONE_SHOT
        );
    }
}
