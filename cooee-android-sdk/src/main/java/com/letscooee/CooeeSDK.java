package com.letscooee;

import android.content.Context;
import android.content.Intent;

import com.letscooee.device.DebugInfoActivity;
import com.letscooee.models.Event;
import com.letscooee.network.SafeHTTPService;
import com.letscooee.retrofit.UserAuthService;
import com.letscooee.task.CooeeExecutors;
import com.letscooee.user.NewSessionExecutor;
import com.letscooee.utils.CooeeCTAListener;
import com.letscooee.utils.PropertyNameException;
import com.letscooee.utils.RuntimeData;
import com.letscooee.utils.SentryHelper;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * The CooeeSDK class contains all the functions required by application to achieve the campaign tasks(Singleton Class)
 *
 * @author Abhishek Taparia
 */
public class CooeeSDK {

    private static final String SYSTEM_DATA_PREFIX = "CE ";

    private static CooeeSDK cooeeSDK;

    private final Context context;
    private final RuntimeData runtimeData;
    private final SentryHelper sentryHelper;
    private final UserAuthService userAuthService;
    private final SafeHTTPService safeHTTPService;

    private WeakReference<CooeeCTAListener> ctaListener;

    /**
     * Private constructor for Singleton Class
     *
     * @param context application context
     */
    private CooeeSDK(@NotNull Context context) {
        this.context = context.getApplicationContext();
        this.runtimeData = CooeeFactory.getRuntimeData();
        this.sentryHelper = CooeeFactory.getSentryHelper();
        this.safeHTTPService = CooeeFactory.getSafeHTTPService();
        this.userAuthService = CooeeFactory.getUserAuthService();

        CooeeExecutors.getInstance().singleThreadExecutor().execute(() -> {
            this.userAuthService.acquireSDKToken();

            new NewSessionExecutor(this.context).execute();
        });
    }

    /**
     * Create and return default instance for CooeeSDK (Singleton Class)
     *
     * @param context application context
     * @return CooeeSDK
     */
    public static CooeeSDK getDefaultInstance(Context context) {
        if (cooeeSDK == null) {
            cooeeSDK = new CooeeSDK(context);
        }

        return cooeeSDK;
    }

    /**
     * Sends custom events to the server and returns with the campaign details(if any)
     *
     * @param eventName       Name the event like onDeviceReady
     * @param eventProperties Properties associated with the event
     */
    public void sendEvent(String eventName, Map<String, Object> eventProperties) {
        for (String key : eventProperties.keySet()) {
            if (key.substring(0, 3).equalsIgnoreCase(SYSTEM_DATA_PREFIX)) {
                throw new PropertyNameException();
            }
        }

        Event event = new Event(eventName, eventProperties);

        this.safeHTTPService.sendEvent(event);
    }

    /**
     * Send given user data to the server
     *
     * @param userData The common user data like name, email.
     */
    public void updateUserData(Map<String, Object> userData) {
        updateUserProfile(userData, null);
    }

    /**
     * Send given user properties to the server
     *
     * @param userProperties The additional user properties.
     */
    public void updateUserProperties(Map<String, Object> userProperties) {
        updateUserProfile(null, userProperties);
    }

    /**
     * Send the given user data and user properties to the server.
     *
     * @param userData       The common user data like name, email.
     * @param userProperties The additional user properties.
     */
    public void updateUserProfile(Map<String, Object> userData, Map<String, Object> userProperties) {
        if (userProperties != null) {
            for (String key : userProperties.keySet()) {
                if (key.substring(0, 3).equalsIgnoreCase(SYSTEM_DATA_PREFIX)) {
                    throw new PropertyNameException();
                }
            }
        }

        Map<String, Object> userMap = new HashMap<>();
        if (userData == null) {
            userMap.put("userData", new HashMap<>());
        } else {
            userMap.put("userData", userData);
        }

        if (userProperties == null) {
            userMap.put("userProperties", new HashMap<>());
        } else {
            userMap.put("userProperties", userProperties);
        }

        this.sentryHelper.setUserInfo(userData);
        this.safeHTTPService.updateUserProfile(userMap);
    }

    /**
     * Set current screen name where user navigated.
     *
     * @param screenName Name of the screen. Like Login, Cart, Wishlist etc.
     */
    public void setCurrentScreen(String screenName) {
        this.runtimeData.setCurrentScreenName(screenName);
    }

    /**
     * Get manually updated screen name
     *
     * @return current screen name
     */
    @Deprecated
    public String getCurrentScreenName() {
        return this.runtimeData.getCurrentScreenName();
    }

    @Deprecated
    public String getUUID() {
        return this.getUserID();
    }

    public String getUserID() {
        return this.userAuthService.getUserID();
    }

    public void setCTAListener(CooeeCTAListener listener) {
        ctaListener = new WeakReference<>(listener);
    }

    public CooeeCTAListener getCTAListener() {
        return ctaListener == null ? null : ctaListener.get();
    }

    /**
     * Launch {@link DebugInfoActivity} activity which holds debug information.
     * These information is useful to debug problem with the SDK.
     */
    public void showDebugInfo() {
        Intent intent = new Intent(context, DebugInfoActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }
}
