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
import com.letscooee.cooeesdk.CooeeSDK;
import com.letscooee.models.AuthenticationRequestBody;
import com.letscooee.models.Campaign;
import com.letscooee.models.DeviceData;
import com.letscooee.models.Event;
import com.letscooee.models.SDKAuthentication;
import com.letscooee.retrofit.APIClient;
import com.letscooee.retrofit.ServerAPIService;
import com.letscooee.utils.CooeeSDKConstants;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
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

                            Map<String, String> userProperties = new HashMap<>();
                            userProperties.put("CE First Launch Time", new Date().toString());
                            sendUserProperties(sdkToken, userProperties);

                            Map<String, String> eventProperties = new HashMap<>();
                            eventProperties.put("CE Source", "SYSTEM");
                            eventProperties.put("CE App Version", defaultUserPropertiesCollector.getAppVersion());
                            Event event = new Event("CE App Installed", eventProperties);
                            apiService.sendEvent(sdkToken, event).enqueue(new Callback<Campaign>() {
                                @Override
                                public void onResponse(@NonNull Call<Campaign> call, @NonNull Response<Campaign> response) {
                                    Log.i(LOG_PREFIX + " Event Sent", response.code() + "");
                                }

                                @Override
                                public void onFailure(@NonNull Call<Campaign> call, @NonNull Throwable t) {
                                    Log.e(LOG_PREFIX + " bodyError", t.toString());
                                }
                            });

                        } else {
                            Log.e(LOG_PREFIX + " bodyError", String.valueOf(response.errorBody()));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<SDKAuthentication> call, @NonNull Throwable t) {
                        Log.e(LOG_PREFIX + " bodyError", t.toString());
                    }
                });
            } else {
                mSharedPreferences = this.context.getSharedPreferences(CooeeSDKConstants.SDK_TOKEN, Context.MODE_PRIVATE);
                String sdk = mSharedPreferences.getString(CooeeSDKConstants.SDK_TOKEN, "");
                Log.i(LOG_PREFIX + " SDK return", sdk);
                sendUserProperties(sdk, null);
                String[] networkData = defaultUserPropertiesCollector.getNetworkData();
                Map<String, String> eventProperties = new HashMap<>();
                eventProperties.put("CE Source", "SYSTEM");
                eventProperties.put("CE App Version", defaultUserPropertiesCollector.getAppVersion());
                eventProperties.put("CE SDK Version", BuildConfig.VERSION_NAME);
                eventProperties.put("CE OS Version", Build.VERSION.RELEASE);
                eventProperties.put("CE Network Provider", networkData[0]);
                eventProperties.put("CE Network Type", networkData[1]);
                eventProperties.put("CE Bluetooth On", defaultUserPropertiesCollector.isBluetoothOn());
                eventProperties.put("CE Wifi Connected", defaultUserPropertiesCollector.isConnectedToWifi());
                eventProperties.put("CE Device Battery", defaultUserPropertiesCollector.getBatteryLevel());
                Event event = new Event("CE App Installed", eventProperties);
                apiService.sendEvent(sdk, event).enqueue(new Callback<Campaign>() {
                    @Override
                    public void onResponse(@NonNull Call<Campaign> call, @NonNull Response<Campaign> response) {
                        Log.i(LOG_PREFIX + " Event Sent", response.code() + "");
                    }

                    @Override
                    public void onFailure(@NonNull Call<Campaign> call, @NonNull Throwable t) {
                        Log.e(LOG_PREFIX + " bodyError", t.toString());
                    }
                });
            }
        }

    }

    private void sendUserProperties(String sdkToken, Map<String, String> userProps) {
        apiService = APIClient.getServerAPIService();
        defaultUserPropertiesCollector = new DefaultUserPropertiesCollector(context);
        String[] location = defaultUserPropertiesCollector.getLocation();
        String[] networkData = defaultUserPropertiesCollector.getNetworkData();
        Map<String, String> userProperties = new HashMap<>();
        if (userProps != null) {
            userProperties = new HashMap<>(userProps);
        }
        userProperties.put("CE OS", "ANDROID");
        userProperties.put("CE SDK Version", BuildConfig.VERSION_NAME);
        userProperties.put("CE App Version", defaultUserPropertiesCollector.getAppVersion());
        userProperties.put("CE OS Version", Build.VERSION.RELEASE);
        userProperties.put("CE Device Manufacturer", Build.MANUFACTURER);
        userProperties.put("CE Device Model", Build.MODEL);
        userProperties.put("CE Latitude", location[0]);
        userProperties.put("CE Longitude", location[1]);
        userProperties.put("CE Network Operator", networkData[0]);
        userProperties.put("CE Network Type", networkData[1]);
        userProperties.put("CE Bluetooth On", defaultUserPropertiesCollector.isBluetoothOn());
        userProperties.put("CE Wifi Connected", defaultUserPropertiesCollector.isConnectedToWifi());
        userProperties.put("CE Available Internal Memory", defaultUserPropertiesCollector.getAvailableInternalMemorySize());
        userProperties.put("CE Total Internal Memory", defaultUserPropertiesCollector.getTotalInternalMemorySize());
        userProperties.put("CE Available RAM", defaultUserPropertiesCollector.getAvailableRAMMemorySize());
        userProperties.put("CE Total RAM", defaultUserPropertiesCollector.getTotalRAMMemorySize());
        userProperties.put("CE Device Orientation", defaultUserPropertiesCollector.getDeviceOrientation());
        userProperties.put("CE Device Battery", defaultUserPropertiesCollector.getBatteryLevel());
        userProperties.put("CE Screen Resolution", defaultUserPropertiesCollector.getScreenResolution());
        userProperties.put("CE DPI", defaultUserPropertiesCollector.getDpi());
        userProperties.put("CE Device Locale", defaultUserPropertiesCollector.getLocale());
        userProperties.put("CE Last Launch Time", new Date().toString());
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
            }
        });

        /*Date date = Calendar.getInstance().getTime();
         Map<String, String> eventProperties = new HashMap<>();
         eventProperties.put("time", date.toString());
         Event event = new Event("isForeground", eventProperties);
         apiService.sendEvent(sdkToken, event).enqueue(new Callback<Campaign>() {
        @Override public void onResponse(@NonNull Call<Campaign> call, @NonNull Response<Campaign> response) {
        Log.i(LOG_PREFIX + " Event Sent", response.code() + "");
        }

        @Override public void onFailure(@NonNull Call<Campaign> call, @NonNull Throwable t) {
        Log.e(LOG_PREFIX + " bodyError", t.toString());
        }
        });*/
    }
}
