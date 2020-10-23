package com.letscooee.init;

import android.content.Context;
import android.content.SharedPreferences;
import com.letscooee.utils.CooeeSDKConstants;

/**
 * FirstTimeLaunchManager initialized to check if app is launched for the first time.
 *
 * @author Abhishek Taparia
 */
public class FirstTimeLaunchManager {

    private SharedPreferences mSharedPreferences;

    public FirstTimeLaunchManager(Context context) {
        if (context != null) {
            mSharedPreferences = context.getSharedPreferences(CooeeSDKConstants.IS_APP_FIRST_TIME_LAUNCH, Context.MODE_PRIVATE);
        }
    }

    public boolean isAppFirstTimeLaunch() {
        if (mSharedPreferences != null && mSharedPreferences.getBoolean(CooeeSDKConstants.IS_APP_FIRST_TIME_LAUNCH, true)) {
            // App is open/launch for first time, update the preference
            SharedPreferences.Editor mSharedPreferencesEditor = mSharedPreferences.edit();
            mSharedPreferencesEditor.putBoolean(CooeeSDKConstants.IS_APP_FIRST_TIME_LAUNCH, false);
            mSharedPreferencesEditor.apply();
            return true;
        } else {
            // App previously opened
            return false;
        }
    }
}
