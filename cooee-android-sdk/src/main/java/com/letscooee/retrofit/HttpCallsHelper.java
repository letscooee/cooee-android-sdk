package com.letscooee.retrofit;

import android.content.Context;
import com.letscooee.CooeeFactory;
import com.letscooee.models.Event;
import com.letscooee.utils.Closure;

import java.util.Map;

/**
 * HttpCallsHelper will be used to create http calls to the server
 *
 * @author Abhishek Taparia
 */
@Deprecated
public final class HttpCallsHelper {

    public static void sendEvent(Context context, Event event, Closure closure) {
        CooeeFactory.getSafeHTTPService().sendEvent(event);
    }

    public static void sendUserProfile(Map<String, Object> userMap) {
        CooeeFactory.getSafeHTTPService().updateUserProfile(userMap);
    }
}
