package com.letscooee.cooeesdk;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.letscooee.campaign.ImagePopUpActivity;
import com.letscooee.campaign.VideoPopUpActivity;
import com.letscooee.init.PostLaunchActivity;
import com.letscooee.models.Campaign;
import com.letscooee.models.Event;
import com.letscooee.retrofit.APIClient;
import com.letscooee.retrofit.ServerAPIService;
import com.letscooee.utils.CooeeSDKConstants;
import com.letscooee.utils.PropertyNameException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.HashMap;
import java.util.Map;

/**
 * The CooeeSDK class contains all the functions required by application to achieve the campaign tasks(Singleton Class)
 *
 * @author Abhishek Taparia
 */
public class CooeeSDK {

    private Context context;
    private static CooeeSDK cooeeSDK = null;
    private String currentScreenName = "";
    private ServerAPIService apiService;

    /**
     * Private constructor for Singleton Class
     *
     * @param context application context
     */
    private CooeeSDK(Context context) {
        this.context = context;
        new PostLaunchActivity(context).appLaunch();

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

        if (this.currentScreenName != null && !this.currentScreenName.equals("")) {
            eventProperties.put("CE Screen Name", this.currentScreenName);
        }

        Event event = new Event(eventName, eventProperties);

        PostLaunchActivity.observable.subscribe((String sdkToken) -> {
            apiService.sendEvent(sdkToken, event).enqueue(new Callback<Campaign>() {
                @Override
                public void onResponse(@NonNull Call<Campaign> call, @NonNull Response<Campaign> response) {
                    Log.d(CooeeSDKConstants.LOG_PREFIX, "User Event Sent Response Code : " + response.code());
                    Campaign campaign = response.body();
                    createCampaign(campaign);
                }

                @Override
                public void onFailure(@NonNull Call<Campaign> call, @NonNull Throwable t) {
                    Log.e(CooeeSDKConstants.LOG_PREFIX, "User Event Sent Error Message : " + t.toString());
                }
            });
        }, (Throwable error) -> {
            Log.e(CooeeSDKConstants.LOG_PREFIX, "Observable Error : " + error.toString());
        }, () -> {
            Log.d(CooeeSDKConstants.LOG_PREFIX, "Observable Completed");
        });
    }

    /**
     * Create graphical view from campaign details received from events.
     *
     * @param campaign Campaign details received from server
     */
    private void createCampaign(Campaign campaign) {
        // TODO: Create all the available type of campaign/ Enhancement of this is yet to be done
        if (campaign != null && campaign.getEventName() != null) {
            Bundle bundle = new Bundle();
            bundle.putString("title", campaign.getEventName());
            bundle.putString("mediaURL", campaign.getContent().getMediaUrl());
            bundle.putString("transitionSide", campaign.getContent().getLayout().getDirection());
            bundle.putString("autoClose", campaign.getContent().getLayout().getCloseBehaviour().getAutoCloseTime() + "");

            switch (campaign.getEventName()) {
                case CooeeSDKConstants.IMAGE_CAMPAIGN: {
                    Intent intent = new Intent(this.context, ImagePopUpActivity.class);
                    intent.putExtras(bundle);
                    this.context.startActivity(intent);
                    break;
                }
                case CooeeSDKConstants.VIDEO_CAMPAIGN:
                    Intent intent = new Intent(this.context, VideoPopUpActivity.class);
                    intent.putExtras(bundle);
                    this.context.startActivity(intent);
                    break;
                case CooeeSDKConstants.SPLASH_CAMPAIGN: {
                    // TODO create Splash Campaign Layout class
                    break;
                }
                default: {
                    Log.e(CooeeSDKConstants.LOG_PREFIX + " error", "No familiar campaign");
                }
            }
        }
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
        for (String key : userProperties.keySet()) {
            if (key.substring(0, 3).equalsIgnoreCase("ce ")) {
                throw new PropertyNameException();
            }
        }

        Map<String, Object> userMap = new HashMap<>();
        if (userData != null) {
            userMap.put("userData", userData);
        }
        userMap.put("userProperties", userProperties);

        PostLaunchActivity.observable.subscribe((String sdkToken) -> {
            Call<ResponseBody> call = apiService.updateProfile(sdkToken, userMap);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    Log.i(CooeeSDKConstants.LOG_PREFIX, "Manual User Profile Response Code : " + response.code());
                }

                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    Log.e(CooeeSDKConstants.LOG_PREFIX, "Manual User Profile Error Message : " + t.toString());
                }
            });
        }, (Throwable error) -> {
            Log.e(CooeeSDKConstants.LOG_PREFIX, "Observable Error : " + error.toString());
        }, () -> {
            Log.d(CooeeSDKConstants.LOG_PREFIX, "Observable Completed");
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
}
