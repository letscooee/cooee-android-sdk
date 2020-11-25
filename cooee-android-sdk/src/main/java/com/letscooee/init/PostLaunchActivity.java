package com.letscooee.init;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.letscooee.BuildConfig;
import com.letscooee.async.AuthSyncNetworkClass;
import com.letscooee.models.*;
import com.letscooee.retrofit.APIClient;
import com.letscooee.retrofit.ServerAPIService;
import com.letscooee.utils.CooeeSDKConstants;

import io.reactivex.rxjava3.core.Observable;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.net.ConnectException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * PostLaunchActivity initialized when app is launched
 *
 * @author Abhishek Taparia
 */
public class PostLaunchActivity {

    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mSharedPreferencesEditor;
    private Context context;
    private DefaultUserPropertiesCollector defaultUserPropertiesCollector;
    private ServerAPIService apiService;
    private boolean isFirstSubscriber = true;

    public static Observable<String> observable;
    public static String SESSION_START_TIME;
    public static String CURRENT_SESSION_ID = "";

    /**
     * Public Constructor
     *
     * @param context application context
     */
    public PostLaunchActivity(Context context) {
        if (context == null) {
            return;
        }

        this.context = context;
        this.mSharedPreferences = context.getSharedPreferences(CooeeSDKConstants.IS_APP_FIRST_TIME_LAUNCH, Context.MODE_PRIVATE);
        this.defaultUserPropertiesCollector = new DefaultUserPropertiesCollector(context);
        this.apiService = APIClient.getServerAPIService();

        observable = Observable.create(subscriber -> {
            if (isAppFirstTimeLaunch() && isFirstSubscriber) {
                isFirstSubscriber = false;
                AuthenticationRequestBody authenticationRequestBody = getAuthenticationRequestBody();

                Response<SDKAuthentication> response = new AuthSyncNetworkClass().execute(authenticationRequestBody).get();
                if (response == null) {
                    subscriber.onError(new ConnectException());
                } else if (response.isSuccessful()) {
                    assert response.body() != null;
                    String sdkToken = response.body().getSdkToken();
                    Log.i(CooeeSDKConstants.LOG_PREFIX + " bodyResponse", sdkToken);
                    mSharedPreferences = context.getSharedPreferences(CooeeSDKConstants.SDK_TOKEN, Context.MODE_PRIVATE);
                    mSharedPreferencesEditor = mSharedPreferences.edit();
                    mSharedPreferencesEditor.putString(CooeeSDKConstants.SDK_TOKEN, sdkToken);
                    mSharedPreferencesEditor.commit();
                    appFirstOpen();
                    subscriber.onNext(sdkToken);
                }
            } else {
                mSharedPreferences = context.getSharedPreferences(CooeeSDKConstants.SDK_TOKEN, Context.MODE_PRIVATE);
                String sdk = mSharedPreferences.getString(CooeeSDKConstants.SDK_TOKEN, "");
                Log.i(CooeeSDKConstants.LOG_PREFIX + " SDK return", sdk);
                subscriber.onNext(sdk);
                subscriber.onComplete();
            }
        });
    }

    /**
     * Runs every time app is launched
     */
    public void appLaunch() {
        if (this.context == null) {
            return;
        }

        observable.subscribe((String sdkToken) -> {
            Log.i(CooeeSDKConstants.LOG_PREFIX, "Sdk Token : " + sdkToken);
        }, (Throwable error) -> {
            Log.e(CooeeSDKConstants.LOG_PREFIX, "Observable Error : " + error.toString());
            mSharedPreferences.edit().remove(CooeeSDKConstants.IS_APP_FIRST_TIME_LAUNCH).commit();
            isFirstSubscriber = true;
        }, () -> {
            Log.d(CooeeSDKConstants.LOG_PREFIX, "Observable Completed");
            successiveAppLaunch();
        });
    }

    /**
     * Check if app is launched for first time
     *
     * @return true if app is launched for first time, else false
     */
    private boolean isAppFirstTimeLaunch() {
        if (this.mSharedPreferences != null && this.mSharedPreferences.getBoolean(CooeeSDKConstants.IS_APP_FIRST_TIME_LAUNCH, true)) {
            // App is open/launch for first time, update the preference
            this.mSharedPreferencesEditor = this.mSharedPreferences.edit();
            this.mSharedPreferencesEditor.putBoolean(CooeeSDKConstants.IS_APP_FIRST_TIME_LAUNCH, false);
            this.mSharedPreferencesEditor.apply();
            return true;
        } else {
            // App previously opened
            return false;
        }
    }

    /**
     * Get app credentials if passed as metadata from host application's manifest file
     *
     * @return String[]{appId,appSecret}
     */
    private String[] getAppCredentials() {
        ApplicationInfo app;

        try {
            app = this.context.getPackageManager().getApplicationInfo(this.context.getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return new String[]{null, null};
        }

        Bundle bundle = app.metaData;
        String appId = bundle.getString("COOEE_APP_ID");
        String appSecret = bundle.getString("COOEE_APP_SECRET");
        return new String[]{appId, appSecret};
    }

    /**
     * returns  AuthenticationRequestBody to be used in observer
     *
     * @return AuthenticationRequestBody
     */
    private AuthenticationRequestBody getAuthenticationRequestBody() {
        String[] appCredentials = getAppCredentials();
        return new AuthenticationRequestBody(
                appCredentials[0],
                appCredentials[1],
                new DeviceData("ANDROID",
                        BuildConfig.VERSION_NAME + "",
                        defaultUserPropertiesCollector.getAppVersion(),
                        Build.VERSION.RELEASE));
    }

    /**
     * Runs when app is opened for the first time after sdkToken is received from server asynchronously
     */
    private void appFirstOpen() {
        Map<String, String> userProperties = new HashMap<>();
        userProperties.put("CE First Launch Time", new Date().toString());
        userProperties.put("CE Installed Time", defaultUserPropertiesCollector.getInstalledTime());
        sendUserProperties(userProperties);

        Map<String, String> eventProperties = new HashMap<>();
        eventProperties.put("CE Source", "SYSTEM");
        eventProperties.put("CE App Version", defaultUserPropertiesCollector.getAppVersion());
        Event event = new Event("CE App Installed", eventProperties);

        sendEvent(event);

        createSession();
    }

    /**
     * Runs every time when app is opened for a new session
     */
    private void successiveAppLaunch() {
        sendUserProperties(null);

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

        Event event = new Event("CE App Launched", eventProperties);
        sendEvent(event);

        createSession();
    }

    /**
     * Send sdk events asynchronously
     *
     * @param event event name and properties
     */
    private void sendEvent(Event event) {
        observable.subscribe((String sdkToken) -> {
            apiService.sendEvent(sdkToken, event).enqueue(new Callback<Campaign>() {
                @Override
                public void onResponse(@NonNull Call<Campaign> call, @NonNull Response<Campaign> response) {
                    Log.i(CooeeSDKConstants.LOG_PREFIX, " Event Sent Response Code : " + response.code());
                }

                @Override
                public void onFailure(@NonNull Call<Campaign> call, @NonNull Throwable t) {
                    //TODO: Saving the request locally so that it can be sent later
                    Log.e(CooeeSDKConstants.LOG_PREFIX, "Event Sent Error Message : " + t.toString());
                }
            });
        }, (Throwable error) -> {
            Log.e(CooeeSDKConstants.LOG_PREFIX, "Observable Error : " + error.toString());
        }, () -> {
            Log.d(CooeeSDKConstants.LOG_PREFIX, "Observable Completed");
        });
    }

    /**
     * Sends default user properties to the server
     *
     * @param userProps additional user properties
     */
    private void sendUserProperties(Map<String, String> userProps) {
        observable.subscribe((String sdkToken) -> {
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
                    Log.i(CooeeSDKConstants.LOG_PREFIX, "User Properties Response Code : " + response.code());
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    //TODO: Saving the request locally so that it can be sent later
                    Log.e(CooeeSDKConstants.LOG_PREFIX, "User Properties Error Message : " + t.toString());
                }
            });
        }, (Throwable error) -> {
            Log.e(CooeeSDKConstants.LOG_PREFIX, "Observable Error : " + error.toString());
        }, () -> {
            Log.d(CooeeSDKConstants.LOG_PREFIX, "Observable Completed");
        });
    }

    /**
     * Create new session on every launch
     */
    private void createSession() {
        SESSION_START_TIME = new Date().toString();
        CURRENT_SESSION_ID = createSessionId();
    }

    /**
     * Create session id
     *
     * @return session id
     */
    private String createSessionId() {
        return Math.abs((int) System.currentTimeMillis()) + "";
    }
}
