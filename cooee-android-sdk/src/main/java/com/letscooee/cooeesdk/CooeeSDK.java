package com.letscooee.cooeesdk;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.letscooee.campaign.ImagePopUpActivity;
import com.letscooee.campaign.VideoPopUpActivity;
import com.letscooee.init.DefaultUserPropertiesCollector;
import com.letscooee.init.PostLaunchActivity;
import com.letscooee.models.Campaign;
import com.letscooee.models.Event;
import com.letscooee.models.UserProfile;
import com.letscooee.retrofit.APIClient;
import com.letscooee.retrofit.ServerAPIService;
import com.letscooee.utils.CooeeSDKConstants;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * @author Abhishek Taparia
 * The CooeeSdk class contains all the functions required by application to achieve the campaign tasks(Singleton Class)
 */
public class CooeeSDK {

    private Context context;
    private String[] location;
    private DefaultUserPropertiesCollector defaultUserPropertiesCollector;
    private static CooeeSDK cooeeSDK=null;

    private CooeeSDK(Context context) {
        this.context = context;
        new PostLaunchActivity(context).appLaunch();
        defaultUserPropertiesCollector = new DefaultUserPropertiesCollector(context);
        location = defaultUserPropertiesCollector.getLocation();
    }

    // create instance for CooeeSDK (Singleton Class)
    public static CooeeSDK getDefaultInstance(Context context) {
        if (cooeeSDK==null){
            cooeeSDK = new CooeeSDK(context);
        }
        return cooeeSDK;

    }


    //Sends event to the server and returns with the campaign details
    public void sendEvent(String eventName, Map<String, String> eventProperties) {
        String[] networkData = defaultUserPropertiesCollector.getNetworkData();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", "ImageButtonClick");
        Map<String, String> subParameters = eventProperties;
        subParameters.put("Latitude", location[0]);
        subParameters.put("Longitude", location[1]);
        subParameters.put("App Version", defaultUserPropertiesCollector.getAppVersion());
        subParameters.put("OS Version", Build.VERSION.RELEASE);
        subParameters.put("SDK Version", Build.VERSION.SDK_INT + "");
        subParameters.put("Carrier", networkData[0]);
        subParameters.put("Network Type", networkData[1]);
        subParameters.put("Connected To Wifi", defaultUserPropertiesCollector.isConnectedToWifi());
        subParameters.put("Bluetooth Enabled", defaultUserPropertiesCollector.isBluetoothOn());

        Event event = new Event(eventName, subParameters);

//        TODO : Check for null values
        Campaign campaign = null;
        try {
            campaign = new MyAsyncClass().execute(event).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

//        switch (campaign.getEventName()) {
//            case CooeeSDKConstants.IMAGE_CAMPAIGN: {
//                Intent intent = new Intent(context, ImagePopUpActivity.class);
//                intent.putExtra("title", campaign.getEventName());
//                intent.putExtra("mediaURL",campaign.getContent().getMediaUrl());
//                intent.putExtra("transitionSide",campaign.getContent().getLayout().getDirection());
//                intent.putExtra("autoClose",campaign.getContent().getLayout().getCloseBehaviour().getAutoCloseTime());
//                Log.d("getAutoClose() in SDK",campaign.getContent().getLayout().getCloseBehaviour().getAutoCloseTime()+"");
//                context.startActivity(intent);
//                break;
//            }
//            case CooeeSDKConstants.VIDEO_CAMPAIGN:
//                Intent intent = new Intent(context, VideoPopUpActivity.class);
//                intent.putExtra("title", campaign.getEventName());
//                intent.putExtra("mediaURL",campaign.getContent().getMediaUrl());
//                intent.putExtra("transitionSide",campaign.getContent().getLayout().getDirection());
//                intent.putExtra("autoClose",campaign.getContent().getLayout().getCloseBehaviour().getAutoCloseTime());
//                Log.d("getAutoClose() in SDK",campaign.getContent().getLayout().getCloseBehaviour().getAutoCloseTime()+"");
//                context.startActivity(intent);
//                break;
//            case CooeeSDKConstants.SPLASH_CAMPAIGN: {
////                TODO: create Splash Campaign Layout class
//                break;
//            }
//            default: {
//                Log.d(CooeeSDKConstants.LOG_PREFIX + " error", "No familiar campaign");
//            }
//        }
    }

    public void updateProfile(Map<String, Object> profile) {
        ServerAPIService apiService = APIClient.getServerAPIService();
        Call<UserProfile> call = apiService.updateProfile(context.getSharedPreferences(CooeeSDKConstants.SDK_TOKEN, Context.MODE_PRIVATE).getString(CooeeSDKConstants.SDK_TOKEN, ""), profile);
        final UserProfile userProfile = null;
        call.enqueue(new Callback<UserProfile>() {
            @Override
            public void onResponse(Call<UserProfile> call, Response<UserProfile> response) {
                if (response.body() == null) {
                    Log.i(CooeeSDKConstants.LOG_PREFIX + " body is ", "null");
                }
                Log.i(CooeeSDKConstants.LOG_PREFIX + " User data", response.toString());
            }

            @Override
            public void onFailure(Call<UserProfile> call, Throwable t) {

            }
        });
    }

    private class MyAsyncClass extends AsyncTask<Event, Void, Campaign> {

        @SafeVarargs
        @Override
        protected final Campaign doInBackground(Event... events) {
            ServerAPIService apiService = APIClient.getServerAPIService();
            Response<Campaign> response = null;
            Log.d("check", "out");
            try {
                String header = context.getSharedPreferences(CooeeSDKConstants.SDK_TOKEN, Context.MODE_PRIVATE).getString(CooeeSDKConstants.SDK_TOKEN, "");
                response = apiService.sendEvent(header, events[0]).execute();
                Log.d("ResponseCode", response.code() + "");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response.body();
        }
    }
}
