package com.letscooee;

import android.content.Context;

import com.letscooee.models.Event;
import com.letscooee.retrofit.UserAuthService;
import com.letscooee.task.CooeeExecutors;
import com.letscooee.trigger.inapp.InAppTriggerActivity;
import com.letscooee.user.NewSessionExecutor;
import com.letscooee.utils.InAppNotificationClickListener;
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
public class CooeeSDK implements InAppTriggerActivity.InAppListener {

    private static CooeeSDK cooeeSDK;

    private final Context context;
    private final RuntimeData runtimeData;
    private final SentryHelper sentryHelper;
    private final UserAuthService userAuthService;

    private WeakReference<InAppNotificationClickListener> inAppNotificationClickListener;

    /**
     * Private constructor for Singleton Class
     *
     * @param context application context
     */
    private CooeeSDK(@NotNull Context context) {
        this.context = context.getApplicationContext();
        this.runtimeData = RuntimeData.getInstance(context);
        this.sentryHelper = CooeeFactory.getSentryHelper();
        this.userAuthService = UserAuthService.getInstance(context);

        CooeeExecutors.getInstance().singleThreadExecutor().execute(() -> {
            this.userAuthService.populateUserDataFromStorage();
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
     * @throws PropertyNameException Custom Exception so that properties' key has no prefix as 'ce '
     */
    public void sendEvent(String eventName, Map<String, Object> eventProperties) throws PropertyNameException {
        for (String key : eventProperties.keySet()) {
            if (key.substring(0, 3).equalsIgnoreCase("ce ")) {
                throw new PropertyNameException();
            }
        }

        Event event = new Event(eventName, eventProperties);

        CooeeFactory.getSafeHTTPService().sendEvent(event);
    }

    /**
     * Send given user data to the server
     *
     * @param userData The common user data like name, email.
     * @throws PropertyNameException Custom Exception so that properties' key has no prefix as 'ce '
     */
    public void updateUserData(Map<String, Object> userData) throws PropertyNameException {
        updateUserProfile(userData, null);
    }

    /**
     * Send given user properties to the server
     *
     * @param userProperties The additional user properties.
     * @throws PropertyNameException Custom Exception so that properties' key has no prefix as 'ce '
     */
    public void updateUserProperties(Map<String, Object> userProperties) throws PropertyNameException {
        updateUserProfile(null, userProperties);
    }

    /**
     * Send the given user data and user properties to the server.
     *
     * @param userData       The common user data like name, email.
     * @param userProperties The additional user properties.
     * @throws PropertyNameException Custom Exception so that properties' key has no prefix as 'ce '
     */
    public void updateUserProfile(Map<String, Object> userData, Map<String, Object> userProperties) throws PropertyNameException {
        if (userProperties != null) {
            for (String key : userProperties.keySet()) {
                if (key.substring(0, 3).equalsIgnoreCase("ce ")) {
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
        CooeeFactory.getSafeHTTPService().updateUserProfile(userMap);
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

    public void setInAppNotificationButtonListener(InAppNotificationClickListener listener) {
        inAppNotificationClickListener = new WeakReference<>(listener);
    }

    @Override
    public void inAppNotificationDidClick(HashMap<String, Object> payload) {
        if (payload != null) {
            inAppNotificationClickListener.get().onInAppButtonClick(payload);
        }
    }

    /**
     * Set Bitmap to Engagement Trigger Activity while working in flutter
     *
     * @param base64 will contain bitmap in base64 format
     */
    public void setBitmap(String base64) {
        InAppTriggerActivity.setBitmap(base64);
    }

}
