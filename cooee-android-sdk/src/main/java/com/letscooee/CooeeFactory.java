package com.letscooee;

import android.content.Context;
import androidx.annotation.RestrictTo;
import com.letscooee.network.BaseHTTPService;
import com.letscooee.network.SafeHTTPService;
import com.letscooee.utils.SentryHelper;

/**
 * A factory pattern utility class to provide the singleton instances of various classes.
 *
 * @author Shashank Agrawal
 * @since 0.3.0
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class CooeeFactory {

    private static SentryHelper sentryHelper;
    private static BaseHTTPService baseHTTPService;
    private static SafeHTTPService safeHTTPService;

    private CooeeFactory() {
    }

    public static void init(Context context) {
        sentryHelper = SentryHelper.getInstance(context);
        baseHTTPService = new BaseHTTPService(context);
        safeHTTPService = new SafeHTTPService(context);
    }

    public static SentryHelper getSentryHelper() {
        return sentryHelper;
    }

    public static BaseHTTPService getBaseHTTPService() {
        return baseHTTPService;
    }

    public static SafeHTTPService getSafeHTTPService() {
        return safeHTTPService;
    }
}
