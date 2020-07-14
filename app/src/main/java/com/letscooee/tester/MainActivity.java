package com.letscooee.tester;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.letscooee.android.models.Campaign;
import com.letscooee.android.models.FirstOpen;
import com.letscooee.android.retrofit.RetrofitClient;
import com.letscooee.android.retrofit.ServerAPIService;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String IS_APP_FIRST_TIME_LAUNCH = "is_app_first_time_launch";
    private static final String SDK_TOKEN = "com.letscooee.tester";
    private static final int REQUEST_LOCATION = 1;

    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mSharedPreferencesEditor;

    private Button btnVideo, btnImage;
    private String latitude, longitude;
    private String[] loca;
    private LocationManager locationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSharedPreferences = getSharedPreferences(IS_APP_FIRST_TIME_LAUNCH, Context.MODE_PRIVATE);
        mSharedPreferencesEditor = mSharedPreferences.edit();

        appLaunch();

        btnImage = (Button) findViewById(R.id.btnImage);
        btnVideo = (Button) findViewById(R.id.btnVideo);

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            getLocation();
        }

        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {




                TelephonyManager manager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
                int networkType = manager.getNetworkType();
                String carrierName = manager.getNetworkOperatorName();
                String networkName = getNetworkName(networkType);


                Map<String, Object> parameters = new HashMap<>();
                parameters.put("name", "ImageButtonClick");
                Map<String, String> subParameters = new HashMap<>();
                subParameters.put("Latitude", latitude);
                subParameters.put("Longitude", longitude);
                subParameters.put("App Version", getAppVersion());
                subParameters.put("OS Version", Build.VERSION.RELEASE);
                subParameters.put("SDK Version", Build.VERSION.SDK_INT+"");
                subParameters.put("Carrier",carrierName);
                subParameters.put("Network Type",networkName);
                subParameters.put("Connected To Wifi",isConnectedToWifi());
                subParameters.put("Bluetooth Enabled",isBluetoothOn());

//                Log.i("Device",Build.DEVICE);
//                Log.i("Device",Build.BRAND);
//                Log.i("Device",Build.MODEL);
//                Log.i("OS Version",Build.VERSION.RELEASE);
//                Log.i("SDK Version",Build.VERSION.SDK_INT+"");
//                Log.i("Carrier",carrierName);
//                Log.i("Network Type",networkName);
//                Log.i("Is Connected To Wifi",isConnectedToWifi());
//                Log.i("Is Bluetooth On",isBluetoothOn());
//                Log.i("latitde/longitude", latitude + "" + longitude);
                parameters.put("userEventProperties", subParameters);
                ServerAPIService apiService = RetrofitClient.getServerAPIService();
                apiService.imageOpen(getSharedPreferences(SDK_TOKEN,Context.MODE_PRIVATE).getString(SDK_TOKEN,""),parameters).enqueue(new Callback<Campaign>() {
                    @Override
                    public void onResponse(Call<Campaign> call, Response<Campaign> response) {
                        Campaign campaign = response.body();
                        Log.i("subtitle",campaign.getSubtitle());
                        Log.i("MediaURL",campaign.getMediaURL());

//                        Dialog settingsDialog = new Dialog(getApplicationContext());
//                        settingsDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
//                        settingsDialog.setContentView(getLayoutInflater().inflate(R.layout.image_campaign, null));
//                        settingsDialog.show();

                        LayoutInflater layoutInflater = (LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                        View popupView = layoutInflater.inflate(R.layout.image_campaign, null);
                        final PopupWindow popupWindow = new PopupWindow(
                                popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);


                        ImageView iv=(ImageView)popupView.findViewById(R.id.imageView);
                        Picasso.with(getApplicationContext()).load(campaign.getMediaURL()).into(iv);

//                        TextView tv = (TextView)popupView.findViewById(R.id.txtClose);
                        iv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                popupWindow.dismiss();
                            }
                        });

                        popupWindow.showAtLocation(btnImage, Gravity.CENTER,0,0);
                    }

                    @Override
                    public void onFailure(Call<Campaign> call, Throwable t) {

                    }
                });

            }
        });


    }

    private String getAppVersion(){
        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return packageInfo.versionName;
    }

    private String isBluetoothOn(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            return "N";
        } else if (!mBluetoothAdapter.isEnabled()) {
            return "N";
        } else {
            return "Y";
        }
    }

    private String isConnectedToWifi(){
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (mWifi.isConnected()) {
            return "Y";
        }
        return "N";
    }

    private String getNetworkName(int networkType){

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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getLocation() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.i("permission","not there");
            return;
        }
        final String[] loca = new String[2];

        LocationServices.getFusedLocationProviderClient(MainActivity.this)
                .requestLocationUpdates(locationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                        LocationServices.getFusedLocationProviderClient(MainActivity.this)
                                .removeLocationUpdates(this);
                        if (locationResult != null && locationResult.getLocations().size() > 0) {
                            int lastLocationIndex = locationResult.getLocations().size() - 1;
                            double lati = locationResult.getLocations().get(lastLocationIndex).getLatitude();
                            double longi = locationResult.getLocations().get(lastLocationIndex).getLongitude();
                            Log.i("lat/long", lati + "  " +   longi);

                            latitude=lati+"";
                            longitude=longi+"";
                        }
                    }
                }, Looper.getMainLooper());

    }

    private void appLaunch(){

        if(isAppFirstTimeLaunch()){
            findViewById(R.id.textView).setBackgroundColor(Color.RED);
            ((TextView)findViewById(R.id.textView)).setText("APP is launch for first time.");
            ServerAPIService apiService = RetrofitClient.getServerAPIService();
            apiService.firstOpen().enqueue(new retrofit2.Callback<FirstOpen>() {
                @Override
                public void onResponse(Call<FirstOpen> call, Response<FirstOpen> response) {
                    if(response.isSuccessful()){
                        Log.i("bodyAllow", String.valueOf(response.body().getSdkToken()));
                        mSharedPreferences = getSharedPreferences(SDK_TOKEN, Context.MODE_PRIVATE);
                        mSharedPreferencesEditor= mSharedPreferences.edit();
                        assert response.body() != null;
                        String sdkToken=response.body().getSdkToken();
                        mSharedPreferencesEditor.putString(SDK_TOKEN,sdkToken);
//                        mSharedPreferencesEditor.commit();
                        mSharedPreferencesEditor.apply();
                    }
                    else{
                        Log.i("bodyError", String.valueOf(response.errorBody()));
                    }
                }

                @Override
                public void onFailure(Call<FirstOpen> call, Throwable t) {
                    Log.i("bodyError",t.toString());


                }
            });
        }
        else {
            findViewById(R.id.textView).setBackgroundColor(Color.GREEN);
            mSharedPreferences = getSharedPreferences(SDK_TOKEN, Context.MODE_PRIVATE);
            String sdk = mSharedPreferences.getString(SDK_TOKEN,"");
            ((TextView)findViewById(R.id.textView)).setText("App previously opened."+sdk);
        }
    }

    protected boolean isAppFirstTimeLaunch(){
        if(mSharedPreferences.getBoolean(IS_APP_FIRST_TIME_LAUNCH,true)){
            // App is open/launch for first time
            // Update the preference
            mSharedPreferencesEditor.putBoolean(IS_APP_FIRST_TIME_LAUNCH,false);
            mSharedPreferencesEditor.commit();
            mSharedPreferencesEditor.apply();

            return true;
        }else {
            // App previously opened
            return false;
        }
    }
}