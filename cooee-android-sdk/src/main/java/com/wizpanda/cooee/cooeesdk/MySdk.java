package com.wizpanda.cooee.cooeesdk;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.squareup.picasso.Picasso;
import com.wizpanda.cooee.R;
import com.wizpanda.cooee.models.Campaign;
import com.wizpanda.cooee.models.SDKAuthentication;
import com.wizpanda.cooee.retrofit.APIClient;
import com.wizpanda.cooee.retrofit.ServerAPIService;
import com.wizpanda.cooee.utils.Constants;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import retrofit2.Call;
import retrofit2.Response;

/**
 * The MySdk class contains all the functions required by application to achieve the campaign tasks
 */
public class MySdk {
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mSharedPreferencesEditor;
    private Context context;

    private String latitude, longitude;


    private MySdk(Context context) {
        this.context = context;
        mSharedPreferences = context.getSharedPreferences(Constants.IS_APP_FIRST_TIME_LAUNCH, Context.MODE_PRIVATE);
        appLaunch();
    }

    public static MySdk getDefaultInstance(Context context) {
        return new MySdk(context);

    }

    //create image campaign layout and display it on screen
    private void createLayout(final Campaign campaign) {
        Activity activity = (Activity) context;
        ViewGroup viewGroup = (ViewGroup) ((ViewGroup) (activity.findViewById(android.R.id.content))).getChildAt(0);
        View popupView = LayoutInflater.from(context.getApplicationContext()).inflate(R.layout.image_campaign, null);
        final PopupWindow popupWindow = new PopupWindow(
                popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ImageView imageView = popupView.findViewById(R.id.imageView);
        Picasso.with(context.getApplicationContext()).load(campaign.getMediaURL()).into(imageView);
        Log.i("campaign", campaign.getMediaURL());
        TextView textView = popupView.findViewById(R.id.textView);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });
        popupWindow.showAtLocation(viewGroup, Gravity.CENTER, 0, 0);
    }

    //Runs every time app is launched
    private void appLaunch() {

        if (isAppFirstTimeLaunch()) {
            ServerAPIService apiService = APIClient.getServerAPIService();
            apiService.firstOpen().enqueue(new retrofit2.Callback<SDKAuthentication>() {
                @Override
                public void onResponse(Call<SDKAuthentication> call, Response<SDKAuthentication> response) {
                    if (response.isSuccessful()) {
                        assert response.body() != null;
                        Log.i("bodyAllow", String.valueOf(response.body().getSdkToken()));
                        mSharedPreferences = context.getSharedPreferences(Constants.SDK_TOKEN, Context.MODE_PRIVATE);
                        mSharedPreferencesEditor = mSharedPreferences.edit();
                        assert response.body() != null;
                        String sdkToken = response.body().getSdkToken();
                        mSharedPreferencesEditor.putString(Constants.SDK_TOKEN, sdkToken);
                        mSharedPreferencesEditor.apply();
                    } else {
                        Log.i("bodyError", String.valueOf(response.errorBody()));
                    }
                }

                @Override
                public void onFailure(Call<SDKAuthentication> call, Throwable t) {
                    Log.i("bodyError", t.toString());
                }
            });
        } else {
            mSharedPreferences = context.getSharedPreferences(Constants.SDK_TOKEN, Context.MODE_PRIVATE);
            String sdk = mSharedPreferences.getString(Constants.SDK_TOKEN, "");
            Log.i("SDK return", sdk);
        }
    }

    // checks if the app is launched for the first time
    private boolean isAppFirstTimeLaunch() {
        if (mSharedPreferences.getBoolean(Constants.IS_APP_FIRST_TIME_LAUNCH, true)) {
            // App is open/launch for first time
            // Update the preference
            mSharedPreferencesEditor = mSharedPreferences.edit();
            mSharedPreferencesEditor.putBoolean(Constants.IS_APP_FIRST_TIME_LAUNCH, false);
            mSharedPreferencesEditor.commit();
            mSharedPreferencesEditor.apply();

            return true;
        } else {
            // App previously opened
            return false;
        }
    }

    //get GPS coordinates of the device
    public String[] getLocation() {
        final Activity activity = ((Activity) this.context);
        final String[] location = new String[2];
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Constants.REQUEST_LOCATION);
        }


        LocationServices.getFusedLocationProviderClient(activity)
                .requestLocationUpdates(locationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                        LocationServices.getFusedLocationProviderClient(activity)
                                .removeLocationUpdates(this);
                        if (locationResult != null && locationResult.getLocations().size() > 0) {
                            int lastLocationIndex = locationResult.getLocations().size() - 1;
                            double lati = locationResult.getLocations().get(lastLocationIndex).getLatitude();
                            double longi = locationResult.getLocations().get(lastLocationIndex).getLongitude();
                            Log.i("lat/long", lati + "  " + longi);
                            latitude = location[0] = lati + "";
                            longitude = location[1] = longi + "";
                        }
                    }
                }, Looper.getMainLooper());
        return location;
    }

    //get Network Details like Carrier Name and Network type
    public String[] getNetworkData() {
        String[] networkData = new String[2];
        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        assert manager != null;
        int networkType = manager.getNetworkType();
        networkData[0] = manager.getNetworkOperatorName();
        networkData[1] = getNetworkName(networkType);
        return networkData;
    }

    //get exact network name by network type
    private String getNetworkName(int networkType) {

        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "2G";

            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "3G";
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "LTE";
            case TelephonyManager.NETWORK_TYPE_NR:
                return "5G";
            default:
                return "Unknown";
        }
    }


    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    //Sends event to the server and returns with the campaign details
    public void sendEvent(String imageCampaign) {
        String[] networkData = getNetworkData();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", "ImageButtonClick");
        Map<String, String> subParameters = new HashMap<>();
        subParameters.put("Latitude", latitude);
        subParameters.put("Longitude", longitude);
        subParameters.put("App Version", getAppVersion());
        subParameters.put("OS Version", Build.VERSION.RELEASE);
        subParameters.put("SDK Version", Build.VERSION.SDK_INT + "");
        subParameters.put("Carrier", networkData[0]);
        subParameters.put("Network Type", networkData[1]);
        subParameters.put("Connected To Wifi", isConnectedToWifi());
        subParameters.put("Bluetooth Enabled", isBluetoothOn());
        parameters.put("userEventProperties", subParameters);
        Campaign campaign = null;
        try {
            campaign = new MyAsyncClass().execute(subParameters).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        createLayout(campaign);
//        return campaign;
    }

    private class MyAsyncClass extends AsyncTask<Map<String, String>, Void, Campaign> {

        @Override
        protected Campaign doInBackground(Map<String, String>... strings) {
            ServerAPIService apiService = APIClient.getServerAPIService();
            Response<Campaign> response = null;
            try {
                response = apiService.imageOpen(context.getSharedPreferences(Constants.SDK_TOKEN, Context.MODE_PRIVATE).getString(Constants.SDK_TOKEN, ""), "ImageButtonClick", strings[0]).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response.body();
        }

        @Override
        protected void onPostExecute(Campaign campaign) {
            super.onPostExecute(campaign);
        }
    }

    //get app version
    private String getAppVersion() {
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return packageInfo.versionName;
    }

    // checks if bluetooth is on
    private String isBluetoothOn() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            return "N";
        } else if (!mBluetoothAdapter.isEnabled()) {
            return "N";
        } else {
            return "Y";
        }
    }

    //checks if wifi if on/connected
    private String isConnectedToWifi() {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (mWifi.isConnected()) {
            return "Y";
        }
        return "N";
    }
}
