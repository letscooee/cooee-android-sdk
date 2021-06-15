package com.letscooee.pushnotification;

import androidx.annotation.RestrictTo;
import com.letscooee.CooeeFactory;

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
            return;
        }

        lastSentToken = token;
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("firebaseToken", token);

        CooeeFactory.getSafeHTTPService().updatePushToken(requestData);
    }
}
