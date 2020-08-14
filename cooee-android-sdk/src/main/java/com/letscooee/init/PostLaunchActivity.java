package com.letscooee.init;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.letscooee.models.Campaign;
import com.letscooee.BuildConfig;
import com.letscooee.models.AuthenticationRequestBody;
import com.letscooee.models.DeviceData;
import com.letscooee.models.SDKAuthentication;
import com.letscooee.retrofit.APIClient;
import com.letscooee.retrofit.ServerAPIService;
import com.letscooee.utils.CooeeSDKConstants;

import java.io.IOException;
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
                    "5f2a8b217124fea524ebd6e1",
                    "A57DQgq9vGQJmjw6Ra8r",
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
                        mSharedPreferencesEditor.apply();
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
            Map<String, String> appLaunchEventProperties = new HashMap<>();
            appLaunchEventProperties.put("os", "ANDROID");
            appLaunchEventProperties.put("cooeeSdkVersion", BuildConfig.VERSION_NAME);
            appLaunchEventProperties.put("appVersion", defaultUserPropertiesCollector.getAppVersion());
            appLaunchEventProperties.put("osVersion", Build.VERSION.RELEASE);
            appLaunchEventProperties.put("location", "[" + location[0] + ", " + location[1] + "]");
            appLaunchEventProperties.put("networkOperator", networkData[0]);
            appLaunchEventProperties.put("networkType", networkData[1]);
            appLaunchEventProperties.put("isBluetoothOn", defaultUserPropertiesCollector.isBluetoothOn());
            appLaunchEventProperties.put("isWifiConnected", defaultUserPropertiesCollector.isConnectedToWifi());
            appLaunchEventProperties.put("availableInternalMemorySize", defaultUserPropertiesCollector.getAvailableInternalMemorySize());
            appLaunchEventProperties.put("availableRAMMemorySize", defaultUserPropertiesCollector.getAvailableRAMMemorySize());
            appLaunchEventProperties.put("cpuInfo", defaultUserPropertiesCollector.getCPUInfo());
            appLaunchEventProperties.put("deviceOrientation", defaultUserPropertiesCollector.getDeviceOrientation());
            appLaunchEventProperties.put("batteryLevel", defaultUserPropertiesCollector.getBatteryLevel());
            appLaunchEventProperties.put("screenResolution", defaultUserPropertiesCollector.getScreenResolution());
            appLaunchEventProperties.put("packageName", defaultUserPropertiesCollector.getPackageName());

            String header = context.getSharedPreferences(CooeeSDKConstants.SDK_TOKEN, Context.MODE_PRIVATE).getString(CooeeSDKConstants.SDK_TOKEN, "");
            Map<String, Object> deviceData = new HashMap<>();
            deviceData.put("deviceData", appLaunchEventProperties);
            apiService.updateProfile(header, deviceData).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Log.d(LOG_PREFIX, response.code() + "");
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.i(LOG_PREFIX + " bodyError", t.toString());
                }
            });
        }
    }
}
