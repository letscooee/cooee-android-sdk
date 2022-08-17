package com.letscooee.retrofit;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import com.letscooee.BuildConfig;
import com.letscooee.CooeeFactory;
import com.letscooee.models.AuthenticationRequestBody;
import com.letscooee.models.DeviceAuthResponse;
import com.letscooee.schedular.CooeeJobUtils;
import com.letscooee.user.NewSessionExecutor;
import com.letscooee.utils.Constants;
import com.letscooee.utils.LocalStorageHelper;
import com.letscooee.utils.ManifestReader;
import com.letscooee.utils.SentryHelper;
import org.bson.types.ObjectId;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.Calendar;
import java.util.Date;

/**
 * Utility class to register user with server and to provide related data
 *
 * @author Ashish Gaikwad on 22/05/21
 * @version 0.2.10
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class DeviceAuthService {

    private final Context context;
    private final SentryHelper sentryHelper;
    private final APIService apiService;
    private final NewSessionExecutor sessionExecutor;
    private final ManifestReader manifestReader;

    private String sdkToken;
    private String userID;
    @SuppressWarnings("unused")
    private String deviceID;
    private String uuid;

    public DeviceAuthService(Context context, SentryHelper sentryHelper, ManifestReader manifestReader) {
        this.context = context.getApplicationContext();
        this.apiService = APIClient.getAPIService();
        this.sentryHelper = sentryHelper;
        this.manifestReader = manifestReader;
        this.sessionExecutor = new NewSessionExecutor(this.context);
    }

    public boolean hasToken() {
        String storageSDKToken = LocalStorageHelper.getString(context, Constants.STORAGE_SDK_TOKEN, null);
        return !TextUtils.isEmpty(storageSDKToken);
    }

    public String getUserID() {
        return this.userID;
    }

    /**
     * This method will pull user data (like SDK token & user ID) from the local storage (shared preference)
     * and populates it for further use.
     */
    public void populateUserDataFromStorage() {
        sdkToken = LocalStorageHelper.getString(context, Constants.STORAGE_SDK_TOKEN, null);
        if (TextUtils.isEmpty(sdkToken)) {
            Log.d(Constants.TAG, "No SDK token found in preference");
        }

        userID = LocalStorageHelper.getString(context, Constants.STORAGE_USER_ID, null);
        if (TextUtils.isEmpty(userID)) {
            Log.d(Constants.TAG, "No user ID found in preference");
        }

        this.updateAPIClient();
    }

    /**
     * Method will ensure that the SDK has acquired the token. If on the first time, token can't be pulled
     * from the server, calling this method will reattempt the same maximum within 1 minute.
     */
    public synchronized void acquireSDKToken() {
        if (this.hasToken()) {
            return;
        }

        Log.d(Constants.TAG, "Attempt to acquire SDK token");
        long lastCheckTime = LocalStorageHelper.getLong(context, Constants.STORAGE_LAST_TOKEN_ATTEMPT, 0);

        // We are attempting first time
        if (lastCheckTime == 0) {
            this.getSDKTokenFromServer();
        } else {
            Calendar calender = Calendar.getInstance();
            calender.setTimeInMillis(lastCheckTime);
            calender.add(Calendar.MINUTE, 1);

            // If the last attempt was
            if (new Date().after(calender.getTime())) {
                this.getSDKTokenFromServer();
            }
        }
    }

    /**
     * Make user registration with server (if not already) and acquire a SDK token which will be later used to authenticate
     * other endpoints.
     */
    private void getSDKTokenFromServer() {
        if (TextUtils.isEmpty(manifestReader.getAppID())) {
            Log.w(Constants.TAG, "Missing App credentials in AndroidManifest.xml",
                    new Exception("Check Integration https://docs.letscooee.com/developers/android/quickstart"));
            return;
        }

        uuid = new ObjectId().toHexString();
        AuthenticationRequestBody requestBody = getAuthenticationRequestBody();
        apiService.registerDevice(requestBody).enqueue(new Callback<DeviceAuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<DeviceAuthResponse> call, @NonNull Response<DeviceAuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DeviceAuthService.this.saveDeviceDataInStorage(response.body());

                    // Start the job immediately to make sure the pending tasks can be sent
                    CooeeJobUtils.triggerPendingTaskJobImmediately(DeviceAuthService.this.context);
                } else {
                    DeviceAuthService.this.sentryHelper.captureMessage("Unable to acquire token- " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<DeviceAuthResponse> call, @NonNull Throwable t) {
                Log.e(Constants.TAG, "Unable to acquire token", t);
            }
        });

        LocalStorageHelper.putLong(context, Constants.STORAGE_LAST_TOKEN_ATTEMPT, new Date().getTime());
    }

    private void saveDeviceDataInStorage(DeviceAuthResponse deviceAuthResponse) {
        this.setSDKToken(deviceAuthResponse.getSdkToken());
        this.setUserID(deviceAuthResponse.getUserID());
        this.setDeviceID(deviceAuthResponse.getDeviceID());
        this.updateAPIClient();

        LocalStorageHelper.putString(context, Constants.STORAGE_DEVICE_UUID, uuid);

    }

    /**
     * Populates value to the {@link #deviceID} and LocalStorage immediately.
     *
     * @param deviceID Device ID to be set
     */
    private void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
        LocalStorageHelper.putString(context, Constants.STORAGE_DEVICE_ID, deviceID);
    }

    /**
     * Populates value to the {@link #userID} and LocalStorage immediately.
     *
     * @param userID User ID to be set
     */
    private void setUserID(String userID) {
        this.userID = userID;
        LocalStorageHelper.putString(context, Constants.STORAGE_USER_ID, userID);
    }

    /**
     * Populates value to the {@link #sdkToken} and LocalStorage immediately.
     *
     * @param sdkToken SDK Token to be set
     */
    private void setSDKToken(String sdkToken) {
        this.sdkToken = sdkToken;
        LocalStorageHelper.putString(context, Constants.STORAGE_SDK_TOKEN, sdkToken);
    }

    private void updateAPIClient() {
        if (BuildConfig.DEBUG) {
            Log.i(Constants.TAG, "SDK Token: " + sdkToken);
            Log.i(Constants.TAG, "User ID: " + userID);
        }

        APIClient.setAPIToken(sdkToken);
        APIClient.setUserId(userID);
        APIClient.setAppVersion(CooeeFactory.getAppInfo().getVersion());
        this.sentryHelper.setUserId(userID);

        // Stopping ScreenshotUtility as backend not accepting any new screenshot requests
        /*if (!TextUtils.isEmpty(sdkToken)) {
            new ScreenshotUtility(context); // Initialize ScreenshotUtility
        }*/
    }

    /**
     * returns  AuthenticationRequestBody to be used in observer
     *
     * @return AuthenticationRequestBody
     */
    private AuthenticationRequestBody getAuthenticationRequestBody() {

        return new AuthenticationRequestBody(
                manifestReader.getAppID(),
                uuid,
                sessionExecutor.getImmutableDeviceProps());
    }

    /**
     * Update auth details if its available.
     * This is mainly used while profile merging.
     *
     * @param deviceAuthResponse response from server
     */
    public void checkAndUpdate(DeviceAuthResponse deviceAuthResponse) {
        if (deviceAuthResponse == null) {
            return;
        }

        // If userID is present then update it immediately
        if (!TextUtils.isEmpty(deviceAuthResponse.getUserID())) {
            this.setUserID(deviceAuthResponse.getUserID());
        }

        // If deviceID is present then update it immediately
        if (!TextUtils.isEmpty(deviceAuthResponse.getDeviceID())) {
            this.setDeviceID(deviceAuthResponse.getDeviceID());
        }

        // If sdkToken is present then update it immediately
        if (!TextUtils.isEmpty(deviceAuthResponse.getSdkToken())) {
            this.setSDKToken(deviceAuthResponse.getSdkToken());
        }

        // Populate updated details with APIClient
        this.updateAPIClient();
    }
}
