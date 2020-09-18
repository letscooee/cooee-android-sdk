package com.letscooee.init;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.letscooee.BuildConfig;
import com.letscooee.models.AuthenticationRequestBody;
import com.letscooee.models.DeviceData;
import com.letscooee.models.SDKAuthentication;
import com.letscooee.retrofit.APIClient;
import com.letscooee.retrofit.ServerAPIService;
import com.letscooee.utils.CooeeSDKConstants;

import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.letscooee.utils.CooeeSDKConstants.LOG_PREFIX;

/**
 * @author Abhishek Taparia
 * PostLaunchActivity initilized when app is launched
 */
public class PostLaunchActivity {

    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mSharedPreferencesEditor;
    private Context context;

    public PostLaunchActivity(Context context) {
        this.context = context;
        this.mSharedPreferences = context.getSharedPreferences(CooeeSDKConstants.IS_APP_FIRST_TIME_LAUNCH, Context.MODE_PRIVATE);
    }


    //Runs every time app is launched
    public void appLaunch() {
        ServerAPIService apiService = APIClient.getServerAPIService();
        DefaultUserPropertiesCollector defaultUserPropertiesCollector = new DefaultUserPropertiesCollector(context);

        if (new FirstTimeLaunchManager(context).isAppFirstTimeLaunch()) {
//            TODO pickup appID and appSecret from properties file
            AuthenticationRequestBody authenticationRequestBody = new AuthenticationRequestBody(
//                    "5f34464357f7777dba0ba9c3",         AWS INSTANCE CREDENTIALS
//                    "vQ3YepEa0PDgaXLjNAzM",
                    "5f3a391f7124fe2cbde7fcd4",
                    "A8mrD1EYqeBkY5bjaeyq",
                    new DeviceData("ANDROID",
                            BuildConfig.VERSION_NAME + "",
                            defaultUserPropertiesCollector.getAppVersion(),
                            Build.VERSION.RELEASE));
            apiService.firstOpen(authenticationRequestBody).enqueue(new retrofit2.Callback<SDKAuthentication>() {
                @Override
                public void onResponse(Call<SDKAuthentication> call, Response<SDKAuthentication> response) {
                    if (response.isSuccessful()) {
                        assert response.body() != null;
                        Log.i(LOG_PREFIX + " bodyResponse", String.valueOf(response.body().getSdkToken()));
                        mSharedPreferences = context.getSharedPreferences(CooeeSDKConstants.SDK_TOKEN, Context.MODE_PRIVATE);
                        mSharedPreferencesEditor = mSharedPreferences.edit();
                        assert response.body() != null;
                        String sdkToken = response.body().getSdkToken();
                        mSharedPreferencesEditor.putString(CooeeSDKConstants.SDK_TOKEN, sdkToken);
                        mSharedPreferencesEditor.commit();
                    } else {
                        Log.i(LOG_PREFIX + " bodyError", String.valueOf(response.errorBody()));
                    }
                }

                @Override
                public void onFailure(Call<SDKAuthentication> call, Throwable t) {
                    Log.i(LOG_PREFIX + " bodyError", t.toString());
                }
            });
        } else {
            mSharedPreferences = context.getSharedPreferences(CooeeSDKConstants.SDK_TOKEN, Context.MODE_PRIVATE);
            String sdk = mSharedPreferences.getString(CooeeSDKConstants.SDK_TOKEN, "");
            Log.i(LOG_PREFIX + " SDK return", sdk);
            String[] location = defaultUserPropertiesCollector.getLocation();
            String[] networkData = defaultUserPropertiesCollector.getNetworkData();
            Map<String, String> userProperties = new HashMap<>();
            userProperties.put("os", "ANDROID");
            userProperties.put("cooeeSdkVersion", BuildConfig.VERSION_NAME);
            userProperties.put("appVersion", defaultUserPropertiesCollector.getAppVersion());
            userProperties.put("osVersion", Build.VERSION.RELEASE);
            userProperties.put("latitude", location[0]);
            userProperties.put("longitude", location[1]);
            userProperties.put("networkType", networkData[1]);
            userProperties.put("isBluetoothOn", defaultUserPropertiesCollector.isBluetoothOn());
            userProperties.put("isWifiConnected", defaultUserPropertiesCollector.isConnectedToWifi());
            userProperties.put("availableInternalMemorySize", defaultUserPropertiesCollector.getAvailableInternalMemorySize());
            userProperties.put("availableRAMMemorySize", defaultUserPropertiesCollector.getAvailableRAMMemorySize());
            userProperties.put("deviceOrientation", defaultUserPropertiesCollector.getDeviceOrientation());
            userProperties.put("batteryLevel", defaultUserPropertiesCollector.getBatteryLevel());
            userProperties.put("screenResolution", defaultUserPropertiesCollector.getScreenResolution());
            userProperties.put("packageName", defaultUserPropertiesCollector.getPackageName());
            String header = context.getSharedPreferences(CooeeSDKConstants.SDK_TOKEN, Context.MODE_PRIVATE).getString(CooeeSDKConstants.SDK_TOKEN, "");
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("userProperties", userProperties);
            apiService.updateProfile(header, userMap).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Log.d(LOG_PREFIX + " userProperties", response.code() + "");
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.i(LOG_PREFIX + " bodyError", t.toString());
                }
            });
        }
    }
}
