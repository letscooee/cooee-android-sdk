package com.letscooee;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.NonNull;
import com.letscooee.device.DebugInfoActivity;
import com.letscooee.models.Event;
import com.letscooee.network.SafeHTTPService;
import com.letscooee.retrofit.DeviceAuthService;
import com.letscooee.task.CooeeExecutors;
import com.letscooee.utils.Constants;
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
    private final Context context;
    private static CooeeSDK cooeeSDK;
    private WeakReference<CooeeCTAListener> ctaListener;
    private final DeviceAuthService deviceAuthService;
    private final RuntimeData runtimeData;
    private final SafeHTTPService safeHTTPService;
    private final SentryHelper sentryHelper;

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

    public CooeeCTAListener getCTAListener() {
        return ctaListener == null ? null : ctaListener.get();
    }

    /**
     * Set call back listener to the Cooee SDK.
     * This call back listener will be fired when the user performs any action on the
     * InApp/Notification sent via Cooee.
     *
     * @param listener instance of the {@link CooeeCTAListener}
     * @see <a href="https://docs.letscooee.com/developers/android/cta-callback">CTA Callback</a>
     * for more details.
     */
    public void setCTAListener(CooeeCTAListener listener) {
        ctaListener = new WeakReference<>(listener);
    }

    /**
     * Create and return default instance for CooeeSDK (Singleton Class)
     *
     * @param context application context
     * @return {@link CooeeSDK} instance.
     * @see <a href="https://docs.letscooee.com/developers/android/get-started#OSeGV">
     * Get Started</a> for more details.
     */
    public static CooeeSDK getDefaultInstance(Context context) {
        if (cooeeSDK == null) {
            cooeeSDK = new CooeeSDK(context);
        }

        return cooeeSDK;
    }

    /**
     * Provides <code>userId</code> assigned by Cooee to the user.
     *
     * @return userID in {@link String} format
     */
    public String getUserID() {
        return this.deviceAuthService.getUserID();
    }

    /**
     * Logout user from the Cooee SDK.
     * This method must be called with apps logout functionality.
     *
     * @since 1.4.2
     */
    public void logout() {
        CooeeExecutors.getInstance().singleThreadExecutor().execute(this.safeHTTPService::logout);
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
     * @see <a href="https://docs.letscooee.com/developers/android/tracking-events">Track Events</a> for
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
     * Set current screen name where user navigated.
     *
     * @param screenName Name of the screen. Like Login, Cart, Wishlist, etc.
     * @see <a href="https://docs.letscooee.com/developers/android/tracking-screen">Tracking Screens</a>
     * for more details
     */
    public void setCurrentScreen(String screenName) {
        if (TextUtils.isEmpty(screenName)) {
            Log.v(Constants.TAG, "Trying to set empty screen name");
            return;
        }

        // Update screen name on main thread, because other threads may be in paused state till CPU gets free.(Edge case scenario)
        String previousScreenName = this.runtimeData.getCurrentScreenName();
        this.runtimeData.setCurrentScreenName(screenName);

        CooeeExecutors.getInstance().singleThreadExecutor().execute(() -> {
            /*
             * Properties will hold previous screen name.
             */
            Map<String, Object> properties = new HashMap<>();
            properties.put("ps", previousScreenName);

            Event event = new Event(Constants.EVENT_SCREEN_VIEW, properties);

            this.safeHTTPService.sendEvent(event);
        });

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
     * Send the given user data and user properties to the server.
     *
     * @param userData The common user data like name, email, etc.
     * @throws PropertyNameException if property name starts with "CE "
     * @throws NullPointerException  if userData is null
     * @see <a href="https://docs.letscooee.com/developers/android/tracking-properties">Tracking User Properties</a>
     * for more details.
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
