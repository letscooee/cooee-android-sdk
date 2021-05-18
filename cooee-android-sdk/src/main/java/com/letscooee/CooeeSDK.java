package com.letscooee;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.letscooee.init.PostLaunchActivity;
import com.letscooee.models.Event;
import com.letscooee.retrofit.HttpCallsHelper;
import com.letscooee.trigger.EngagementTriggerActivity;
import com.letscooee.utils.CooeeSDKConstants;
import com.letscooee.utils.InAppNotificationClickListener;
import com.letscooee.utils.LocalStorageHelper;
import com.letscooee.utils.PropertyNameException;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import io.sentry.Sentry;
import io.sentry.protocol.User;

/**
 * The CooeeSDK class contains all the functions required by application to achieve the campaign tasks(Singleton Class)
 *
 * @author Abhishek Taparia
 */
public class CooeeSDK implements EngagementTriggerActivity.InAppListener {

    private static CooeeSDK cooeeSDK = null;

    private final Context context;

    private String currentScreenName = "";
    private String uuid = "";

    private WeakReference<InAppNotificationClickListener> inAppNotificationClickListener;

    /**
     * Private constructor for Singleton Class
     *
     * @param context application context
     */
    private CooeeSDK(Context context) {
        this.context = context;
        new PostLaunchActivity(context);
        setSentryUser(getUUID(), new HashMap<>());
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

        HttpCallsHelper.sendEvent(context, event, data -> PostLaunchActivity.createTrigger(context, data));
    }

    /**
     * Send given user data to the server
     *
     * @param userData The common user data like name, email.
     * @throws PropertyNameException Custom Exception so that properties' key has no prefix as 'ce '
     */
    public void updateUserData(Map<String, String> userData) throws PropertyNameException {
        updateUserProfile(userData, null);
    }

    /**
     * Send given user properties to the server
     *
     * @param userProperties The additional user properties.
     * @throws PropertyNameException Custom Exception so that properties' key has no prefix as 'ce '
     */
    public void updateUserProperties(Map<String, String> userProperties) throws PropertyNameException {
        updateUserProfile(null, userProperties);
    }

    /**
     * Send the given user data and user properties to the server.
     *
     * @param userData       The common user data like name, email.
     * @param userProperties The additional user properties.
     * @throws PropertyNameException Custom Exception so that properties' key has no prefix as 'ce '
     */
    public void updateUserProfile(Map<String, String> userData, Map<String, String> userProperties) throws PropertyNameException {
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

        HttpCallsHelper.sendUserProfile(userMap, "Manual", data -> {
            if (data.get("id") != null) {
                LocalStorageHelper.putString(context, CooeeSDKConstants.STORAGE_USER_ID, data.get("id").toString());
                setSentryUser(data.get("id").toString(), userData);
            }
        });
    }

    private void setSentryUser(String id, Map<String, String> userData) {
        User user = new User();
        user.setId(id);

        if (!TextUtils.isEmpty(userData.get("name"))) {
            user.setUsername(userData.get("name"));
        }

        if (!TextUtils.isEmpty(userData.get("email"))) {
            user.setEmail(userData.get("email"));
        }

        if (!TextUtils.isEmpty(userData.get("mobile"))) {
            Map<String, String> userDataExtra = new HashMap<>();
            userDataExtra.put("mobile", userData.get("mobile"));
            user.setOthers(userDataExtra);
        }

        Sentry.setUser(user);
    }

    /**
     * Manually update screen name
     *
     * @param screenName Screen name given by user
     */
    public void setCurrentScreen(String screenName) {
        if (screenName == null || (!this.currentScreenName.isEmpty() && this.currentScreenName.equals(screenName))) {
            return;
        }
        Log.d(CooeeSDKConstants.LOG_PREFIX, "Updated screen : " + screenName);
        this.currentScreenName = screenName;
    }

    /**
     * Get manually updated screen name
     *
     * @return current screen name
     */
    public String getCurrentScreenName() {
        return this.currentScreenName;
    }

    public String getUUID() {
        if (uuid.isEmpty()) {
            uuid = LocalStorageHelper.getString(context, CooeeSDKConstants.STORAGE_USER_ID, "");
        }
        return uuid;
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
        EngagementTriggerActivity.setBitmap(base64);
    }

}
