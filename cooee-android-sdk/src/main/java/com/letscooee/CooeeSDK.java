package com.letscooee;

import android.content.Context;
import android.util.Log;

import com.letscooee.init.PostLaunchActivity;
import com.letscooee.models.Event;
import com.letscooee.retrofit.APIClient;
import com.letscooee.retrofit.HttpCallsHelper;
import com.letscooee.retrofit.ServerAPIService;
import com.letscooee.utils.Closure;
import com.letscooee.utils.CooeeSDKConstants;
import com.letscooee.utils.LocalStorageHelper;
import com.letscooee.utils.PropertyNameException;

import java.util.HashMap;
import java.util.Map;

/**
 * The CooeeSDK class contains all the functions required by application to achieve the campaign tasks(Singleton Class)
 *
 * @author Abhishek Taparia
 */
public class CooeeSDK {

    private static CooeeSDK cooeeSDK = null;

    private final Context context;
    private final ServerAPIService apiService;

    private String currentScreenName = "";
    private String uuid = "";

    /**
     * Private constructor for Singleton Class
     *
     * @param context application context
     */
    private CooeeSDK(Context context) {
        this.context = context;
        new PostLaunchActivity(context);

        this.apiService = APIClient.getServerAPIService();
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
    public void sendEvent(String eventName, Map<String, String> eventProperties) throws PropertyNameException {
        for (String key : eventProperties.keySet()) {
            if (key.substring(0, 3).equalsIgnoreCase("ce ")) {
                throw new PropertyNameException();
            }
        }

        Event event = new Event(eventName, eventProperties);

        HttpCallsHelper.sendEvent(event, new Closure() {
            @Override
            public void call(Map<String, Object> data) {
                PostLaunchActivity.createTrigger(context, data);
            }
        });
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

        HttpCallsHelper.sendUserProfile(userMap, "Manual", new Closure() {
            @Override
            public void call(Map<String, Object> data) {
                if (data.get("id") != null){
                    LocalStorageHelper.putString(context, CooeeSDKConstants.STORAGE_UUID, data.get("id").toString());
                }
            }
        });
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
        if (uuid.isEmpty()){
            uuid = LocalStorageHelper.getString(context, CooeeSDKConstants.STORAGE_UUID, "");
        }
        return uuid;
    }
}
