package com.letscooee.init;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import com.letscooee.BuildConfig;
import com.letscooee.models.AuthenticationRequestBody;
import com.letscooee.models.DeviceData;
import com.letscooee.models.SDKAuthentication;
import com.letscooee.retrofit.APIClient;
import com.letscooee.retrofit.ServerAPIService;
import com.letscooee.utils.CooeeSDKConstants;

import retrofit2.Call;
import retrofit2.Response;

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

        if (new FirstTimeLaunchManager(context).isAppFirstTimeLaunch()) {
            ServerAPIService apiService = APIClient.getServerAPIService();
            DefaultUserPropertiesCollector defaultUserPropertiesCollector = new DefaultUserPropertiesCollector(context);
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
                        Log.i(CooeeSDKConstants.LOG_PREFIX + " bodyResponse", String.valueOf(response.body().getSdkToken()));
                        mSharedPreferences = context.getSharedPreferences(CooeeSDKConstants.SDK_TOKEN, Context.MODE_PRIVATE);
                        mSharedPreferencesEditor = mSharedPreferences.edit();
                        String sdkToken = response.body().getSdkToken();
                        mSharedPreferencesEditor.putString(CooeeSDKConstants.SDK_TOKEN, sdkToken);
                        mSharedPreferencesEditor.commit();
                    } else {
                        Log.i(CooeeSDKConstants.LOG_PREFIX + " bodyError", String.valueOf(response.errorBody()));
                    }
                }

                @Override
                public void onFailure(Call<SDKAuthentication> call, Throwable t) {
                    Log.i(CooeeSDKConstants.LOG_PREFIX + " bodyError", t.toString());
                }
            });
        } else {
            mSharedPreferences = context.getSharedPreferences(CooeeSDKConstants.SDK_TOKEN, Context.MODE_PRIVATE);
            String sdk = mSharedPreferences.getString(CooeeSDKConstants.SDK_TOKEN, "");
            Log.i(CooeeSDKConstants.LOG_PREFIX + " SDK return", sdk);
        }
    }
}
