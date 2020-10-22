package com.letscooee.init;

import android.Manifest;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.LocationRequest;
import com.letscooee.BuildConfig;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * @author Abhishek Taparia
 * DefaultUserPropertiesCollector collects various mobile properties/parameters
 */
class DefaultUserPropertiesCollector {

    private Context context;

    public DefaultUserPropertiesCollector(Context context) {
        this.context = context;
    }

    //    get GPS coordinates of the device
    public String[] getLocation() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        Location location = null;
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this.context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this.context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return new String[]{null, null};
        }

        assert locationManager != null;
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        if (location == null) {
            return new String[]{null, null};
        }
        return new String[]{location.getLatitude() + "", location.getLongitude() + ""};
    }

    //    get Network Details like Carrier Name and Network type
    public String[] getNetworkData() {
        String[] networkData = new String[2];
        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        assert manager != null;
        int networkType = manager.getNetworkType();
        networkData[0] = manager.getNetworkOperatorName();
        networkData[1] = getNetworkName(networkType);
        return networkData;
    }

    //    get exact network name by network type
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
            case TelephonyManager.NETWORK_TYPE_TD_SCDMA:
            case TelephonyManager.NETWORK_TYPE_IWLAN:
                return "4G";
            case TelephonyManager.NETWORK_TYPE_NR:
                return "5G";
            default:
                return "Unknown";
        }
    }

    //     checks if bluetooth is on
    public String isBluetoothOn() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            return "N";
        } else if (!mBluetoothAdapter.isEnabled()) {
            return "N";
        } else {
            return "Y";
        }
    }

    //    get app version
    public String getAppVersion() {
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        assert packageInfo != null;
        return packageInfo.versionName;
    }

    //    checks if wifi if on/connected
    public String isConnectedToWifi() {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connManager != null;
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        assert mWifi != null;
        if (mWifi.isConnected()) {
            return "Y";
        }
        return "N";
    }

    //    get available internal storage
    public String getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long availableBlocks = stat.getAvailableBlocksLong();
        return String.valueOf((availableBlocks * blockSize) / 0x100000L);
    }

    //    get total internal storage
    public String getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long totalBytes = stat.getTotalBytes();
        return String.valueOf(totalBytes / 0x1000L);
    }

    //    get total RAM size
    public String getTotalRAMMemorySize() {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        assert activityManager != null;
        activityManager.getMemoryInfo(mi);
        double availableMegs = (double) mi.totalMem / 0x100000L;
        return String.valueOf(availableMegs);
    }

    //    get available RAM size
    public String getAvailableRAMMemorySize() {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        assert activityManager != null;
        activityManager.getMemoryInfo(mi);
        double availableMegs = (double) mi.availMem / 0x100000L;
        return String.valueOf(availableMegs);
    }

    //    get CPU information
    public String getCPUInfo() {
        StringBuilder output = new StringBuilder();
        try {
            String[] DATA = {"top -m 5 -d 1"};
            ProcessBuilder processBuilder = new ProcessBuilder(DATA);
            Process process = processBuilder.start();
            InputStream inputStream = process.getInputStream();
            byte[] byteArry = new byte[1024];

            while (inputStream.read(byteArry) != -1) {
                output.append(new String(byteArry));
            }
            inputStream.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return output.toString();
    }

    //    get device orientation
    public String getDeviceOrientation() {
        int orientation = context.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return "Landscape";
        }
        return "Portrait";
    }

    //    get battery percentage
    public String getBatteryLevel() {
        BatteryManager batteryManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        String batteryLevel = "Can't get Battery Info";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            assert batteryManager != null;
            batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) + "";
        }
        return batteryLevel;
    }

    //    get screen resolution and pixel density
    public String getScreenResolution() {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int h = displayMetrics.heightPixels;
        int w = displayMetrics.widthPixels;
        return w + "X" + h;
    }

    //    get pixel density
    public String getDpi() {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int densityPixel = displayMetrics.densityDpi;
        return densityPixel + "dpi";
    }

    //    get installed applications list
    public List<String> getInstalledApps() {
        List<String> installedApps = new ArrayList<>();
        List<PackageInfo> packs = context.getPackageManager().getInstalledPackages(0);
        for (PackageInfo packageInfo : packs) {
            String app = packageInfo.applicationInfo.loadLabel(context.getPackageManager()).toString();
            installedApps.add(app);
        }
        return installedApps;
    }

    //    get package name
    public String getPackageName() {
        Package aPackage = context.getClass().getPackage();
        if (aPackage != null) return aPackage.getName();
        return null;
    }

    public String getLocale(){
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = context.getResources().getConfiguration().getLocales().get(0);
        } else {
            locale = context.getResources().getConfiguration().locale;
        }
        return locale.getLanguage()+"-"+locale.getCountry();
    }

    public String getInstalledTime(){
        PackageManager pm = context.getPackageManager();
        ApplicationInfo appInfo = null;
        try {
            appInfo = pm.getApplicationInfo(BuildConfig.LIBRARY_PACKAGE_NAME, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        assert appInfo != null;
        String appFile = appInfo.sourceDir;
        long installed = new File(appFile).lastModified();
        return new Date(installed).toString();
    }
}
