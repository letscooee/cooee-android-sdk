package com.letscooee.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Checks for the internet availability.
 *
 * @author Ashish Gaikwad on 18/5/21
 * @version 0.1
 */
public class ConnectionManager {

    /**
     * isNetworkAvailable Will used to check if device is connected to internet and will return result in boolean
     *
     * @param context will be the application context
     * @return true or false
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
