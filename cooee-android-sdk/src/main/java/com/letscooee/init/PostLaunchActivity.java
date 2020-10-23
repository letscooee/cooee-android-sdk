package com.letscooee.init;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;


import androidx.annotation.NonNull;

import com.letscooee.BuildConfig;
import com.letscooee.models.AuthenticationRequestBody;
import com.letscooee.models.Campaign;
import com.letscooee.models.DeviceData;
import com.letscooee.models.Event;
import com.letscooee.models.SDKAuthentication;
import com.letscooee.retrofit.APIClient;
import com.letscooee.retrofit.ServerAPIService;
import com.letscooee.utils.CooeeSDKConstants;

import java.util.Calendar;
import java.util.Date;
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
    private DefaultUserPropertiesCollector defaultUserPropertiesCollector;
    private ServerAPIService apiService;

    public PostLaunchActivity(Context context) {
        if (context != null) {
            this.context = context;
            this.mSharedPreferences = context.getSharedPreferences(CooeeSDKConstants.IS_APP_FIRST_TIME_LAUNCH, Context.MODE_PRIVATE);
            this.defaultUserPropertiesCollector = new DefaultUserPropertiesCollector(context);
            this.apiService = APIClient.getServerAPIService();
        }
    }


    //Runs every time app is launched
    public void appLaunch() {
        if (this.context != null) {
            if (new FirstTimeLaunchManager(context).isAppFirstTimeLaunch()) {
                ApplicationInfo app = null;
                try {
                    app = this.context.getPackageManager().getApplicationInfo(this.context.getPackageName(), PackageManager.GET_META_DATA);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                assert app != null;
                Bundle bundle = app.metaData;
                String appId = bundle.getString("COOEE_APP_ID");
                String appSecret = bundle.getString("COOEE_APP_SECRET");
                AuthenticationRequestBody authenticationRequestBody = new AuthenticationRequestBody(
                        appId,
                        appSecret,
                        new DeviceData("ANDROID",
                                BuildConfig.VERSION_NAME + "",
                                defaultUserPropertiesCollector.getAppVersion(),
                                Build.VERSION.RELEASE));
                this.apiService.firstOpen(authenticationRequestBody).enqueue(new retrofit2.Callback<SDKAuthentication>() {
                    @Override
                    public void onResponse(@NonNull Call<SDKAuthentication> call, @NonNull Response<SDKAuthentication> response) {
                        if (response.isSuccessful()) {
                            assert response.body() != null;
                            Log.i(LOG_PREFIX + " bodyResponse", String.valueOf(response.body().getSdkToken()));
                            mSharedPreferences = context.getSharedPreferences(CooeeSDKConstants.SDK_TOKEN, Context.MODE_PRIVATE);
                            mSharedPreferencesEditor = mSharedPreferences.edit();
                            assert response.body() != null;
                            String sdkToken = response.body().getSdkToken();
                            mSharedPreferencesEditor.putString(CooeeSDKConstants.SDK_TOKEN, sdkToken);
                            mSharedPreferencesEditor.commit();
                            sendUserProperties(sdkToken);
                        } else {
                            Log.e(LOG_PREFIX + " bodyError", String.valueOf(response.errorBody()));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<SDKAuthentication> call, @NonNull Throwable t) {
                        Log.e(LOG_PREFIX + " bodyError", t.toString());
                        new Handler(Looper.getMainLooper())
                                .post(() -> Toast.makeText(context, "Not connected to server, check your internet", Toast.LENGTH_SHORT).show());
                    }
                });
            } else {
                mSharedPreferences = this.context.getSharedPreferences(CooeeSDKConstants.SDK_TOKEN, Context.MODE_PRIVATE);
                String sdk = mSharedPreferences.getString(CooeeSDKConstants.SDK_TOKEN, "");
                Log.i(LOG_PREFIX + " SDK return", sdk);
                sendUserProperties(sdk);
            }
        }

    }

    private void sendUserProperties(String sdkToken) {
        apiService = APIClient.getServerAPIService();
        defaultUserPropertiesCollector = new DefaultUserPropertiesCollector(context);
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
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("userProperties", userProperties);
        apiService.updateProfile(sdkToken, userMap).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                Log.i(LOG_PREFIX + " userProperties", response.code() + "");
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(LOG_PREFIX + " bodyError", t.toString());
                new Handler(Looper.getMainLooper())
                        .post(() -> Toast.makeText(context, "Not connected to server, check your internet", Toast.LENGTH_SHORT).show());
            }
        });

        Date date = Calendar.getInstance().getTime();
        Map<String, String> eventProperties = new HashMap<>();
        eventProperties.put("time", date.toString());
        Event event = new Event("isForeground", eventProperties);
        apiService.sendEvent(sdkToken, event).enqueue(new Callback<Campaign>() {
            @Override
            public void onResponse(@NonNull Call<Campaign> call, @NonNull Response<Campaign> response) {
                Log.i(LOG_PREFIX + " Event Sent", response.code() + "");
            }

            @Override
            public void onFailure(@NonNull Call<Campaign> call, @NonNull Throwable t) {
                new Handler(Looper.getMainLooper())
                        .post(() -> Toast.makeText(context, "Not connected to server, check your internet", Toast.LENGTH_SHORT).show());
            }
        });
    }
}
