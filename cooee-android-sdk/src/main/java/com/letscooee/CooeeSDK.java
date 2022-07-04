package com.letscooee;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import com.letscooee.device.DebugInfoActivity;
import com.letscooee.models.Event;
import com.letscooee.network.SafeHTTPService;
import com.letscooee.retrofit.DeviceAuthService;
import com.letscooee.task.CooeeExecutors;
import com.letscooee.utils.CooeeCTAListener;
import com.letscooee.utils.PropertyNameException;
import com.letscooee.utils.RuntimeData;
import com.letscooee.utils.SentryHelper;
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
    private final DeviceAuthService deviceAuthService;
    private final SafeHTTPService safeHTTPService;

    private WeakReference<CooeeCTAListener> ctaListener;

    /**
     * Private constructor for Singleton Class
     *
     * @param context application context
     */
    private CooeeSDK(@NonNull Context context) {
        this.context = context.getApplicationContext();
        this.runtimeData = CooeeFactory.getRuntimeData();
        this.sentryHelper = CooeeFactory.getSentryHelper();
        this.safeHTTPService = CooeeFactory.getSafeHTTPService();
        this.deviceAuthService = CooeeFactory.getDeviceAuthService();

        CooeeExecutors.getInstance().singleThreadExecutor().execute(this.deviceAuthService::acquireSDKToken);
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
     * @param eventName Name the event like onDeviceReady
     * @throws PropertyNameException if property name starts with "CE "
     * @throws NullPointerException  if eventName is null
     * @see <a href="https://docs.letscooee.com/developers/android/track-events">Track Events</a> for
     * more details.
     */
    public void sendEvent(String eventName) throws PropertyNameException, NullPointerException {
        sendEvent(eventName, new HashMap<>());
    }

    /**
     * Sends custom events to the server and returns with the campaign details(if any)
     *
     * @param eventName       Name the event like onDeviceReady
     * @param eventProperties Properties associated with the event
     * @throws PropertyNameException if property name starts with "CE "
     * @throws NullPointerException  if eventName is null
     * @see <a href="https://docs.letscooee.com/developers/android/track-events">Track Events</a> for
     * more details.
     */
    public void sendEvent(String eventName, Map<String, Object> eventProperties)
            throws PropertyNameException, NullPointerException {
        if (eventName == null) {
            throw new NullPointerException("Event name cannot be null");
        }

        containsSystemDataPrefix(eventProperties);
        CooeeExecutors.getInstance().singleThreadExecutor().execute(() -> {
            Event event = new Event(eventName, eventProperties);
            this.safeHTTPService.sendEvent(event);
        });
    }

    /**
     * Send given user data to the server
     *
     * @param userData The common user data like name, email.
     * @throws PropertyNameException if property name starts with "CE "
     * @deprecated use {@link CooeeSDK#updateUserProfile(Map)} instead
     */
    @Deprecated
    public void updateUserData(Map<String, Object> userData) throws PropertyNameException {
        updateUserProfile(userData, null);
    }

    /**
     * Send given user properties to the server
     *
     * @param userProperties The additional user properties.
     * @throws PropertyNameException if property name starts with "CE "
     * @deprecated use {@link CooeeSDK#updateUserProfile(Map)} instead
     */
    @Deprecated
    public void updateUserProperties(Map<String, Object> userProperties) throws PropertyNameException {
        updateUserProfile(null, userProperties);
    }

    /**
     * Send the given user data and user properties to the server.
     *
     * @param userData       The common user data like name, email.
     * @param userProperties The additional user properties.
     * @throws PropertyNameException if property name starts with "CE "
     * @deprecated use {@link CooeeSDK#updateUserProfile(Map)} instead
     */
    @Deprecated
    public void updateUserProfile(Map<String, Object> userData, Map<String, Object> userProperties) throws PropertyNameException {
        Map<String, Object> userMap = new HashMap<>();
        if (userData != null) {
            userMap.putAll(userData);
        }

        if (userProperties != null) {
            userMap.putAll(userProperties);
        }

        updateUserProfile(userMap);
    }

    /**
     * Send the given user data and user properties to the server.
     *
     * @param userData The common user data like name, email, etc.
     * @throws PropertyNameException if property name starts with "CE "
     * @throws NullPointerException  if userData is null
     * @see <a href="https://docs.letscooee.com/developers/android/track-props">Tracking User Properties</a>
     */
    public void updateUserProfile(Map<String, Object> userData) throws PropertyNameException, NullPointerException {
        if (userData == null) {
            throw new NullPointerException("userData cannot be null");
        }

        containsSystemDataPrefix(userData);
        CooeeExecutors.getInstance().singleThreadExecutor().execute(() -> {
            this.sentryHelper.setUserInfo(userData);
            this.safeHTTPService.updateUserProfile(userData);
        });
    }

    /**
     * Set current screen name where user navigated.
     *
     * @param screenName Name of the screen. Like Login, Cart, Wishlist etc.
     */
    public void setCurrentScreen(String screenName) {
        this.runtimeData.setCurrentScreenName(screenName);

        // TODO: 04/07/22 Should we send properties here? As event sends screen name by default.
        //  Added screenName in property as web SDK adds it.
        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put("screenName", screenName);

        Event event = new Event("CE Screen View", eventProperties);
        safeHTTPService.sendEvent(event);
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
        return this.deviceAuthService.getUserID();
    }

    public void setCTAListener(CooeeCTAListener listener) {
        ctaListener = new WeakReference<>(listener);
    }

    public CooeeCTAListener getCTAListener() {
        return ctaListener == null ? null : ctaListener.get();
    }

    /**
     * Launch {@link DebugInfoActivity} activity which holds debug information.
     * This information is useful to debug problem with the SDK.
     */
    public void showDebugInfo() {
        Intent intent = new Intent(context, DebugInfoActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    /**
     * Check if map key starts with {@link #SYSTEM_DATA_PREFIX}
     *
     * @param map map to check
     * @throws PropertyNameException if map key starts with {@link #SYSTEM_DATA_PREFIX}
     */
    private void containsSystemDataPrefix(Map<String, Object> map) throws PropertyNameException {
        if (map == null) {
            return;
        }

        for (String key : map.keySet()) {
            if (key.length() > 3 && key.substring(0, 3).equalsIgnoreCase(SYSTEM_DATA_PREFIX)) {
                throw new PropertyNameException();
            }
        }
    }
}
