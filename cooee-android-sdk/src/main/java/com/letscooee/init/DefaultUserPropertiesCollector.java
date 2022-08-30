package com.letscooee.init;

import static android.content.Context.ACTIVITY_SERVICE;
import static androidx.core.content.ContextCompat.checkSelfPermission;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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
import com.letscooee.CooeeFactory;
import com.letscooee.utils.Constants;
import com.letscooee.utils.DateUtils;
import com.letscooee.utils.SentryHelper;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * DefaultUserPropertiesCollector collects various mobile properties/parameters
 *
 * @author Abhishek Taparia
 */
public class DefaultUserPropertiesCollector {

    private final Context context;
    private final SentryHelper sentryHelper;
    private static final String UNKNOWN = "Unknown";

    public DefaultUserPropertiesCollector(Context context) {
        this.context = context;
        this.sentryHelper = CooeeFactory.getSentryHelper();
    }

    /**
     * Get GPS coordinates of the device if user-permission is there
     *
     * @return String[] {latitude, longitude}
     */
    @SuppressLint("MissingPermission")
    public double[] getLocation() {
        Location bestLastLocation = null;

        if (!isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            return new double[]{0, 0};
        }

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);

        for (String provider : providers) {
            Location location = locationManager.getLastKnownLocation(provider);
            if (location == null) {
                continue;
            }
            if (bestLastLocation == null || location.getAccuracy() < bestLastLocation.getAccuracy()) {
                bestLastLocation = location;
            }
        }
        if (bestLastLocation == null) {
            return new double[]{0, 0};
        }
        return new double[]{bestLastLocation.getLatitude(), bestLastLocation.getLongitude()};
    }

    private boolean isPermissionGranted(String perm) {
        return checkSelfPermission(this.context, perm) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Get Network Details
     *
     * @return String[] {Network Operator Name,Network type}
     */
    @SuppressLint("MissingPermission")
    public String[] getNetworkData() {
        String[] networkData = new String[2];
        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        assert manager != null;

        if (!isPermissionGranted(Manifest.permission.READ_PHONE_STATE)) {
            networkData[0] = !manager.getNetworkOperatorName().isEmpty() ? manager.getNetworkOperatorName() : UNKNOWN;
            networkData[1] = "PNGRNT";
        } else {
            int networkType = manager.getNetworkType();
            networkData[0] = !manager.getNetworkOperatorName().isEmpty() ? manager.getNetworkOperatorName() : UNKNOWN;
            networkData[1] = getNetworkName(networkType);
        }
        return networkData;
    }


    /**
     * Get exact network name(2G/3G/4G/5G) by network type
     *
     * @param networkType network type from class TelephonyManager
     * @return network name(2G/3G/4G/5G)
     */
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
            case TelephonyManager.NETWORK_TYPE_GSM:
                return "3G";
            case TelephonyManager.NETWORK_TYPE_LTE:
            case TelephonyManager.NETWORK_TYPE_TD_SCDMA:
            case TelephonyManager.NETWORK_TYPE_IWLAN:
                return "4G";
            case TelephonyManager.NETWORK_TYPE_NR:
                return "5G";
            default:
                return UNKNOWN;
        }
    }

    /**
     * @return {@code true} when the bluetooth is on, otherwise {@code false}.
     */
    public boolean isBluetoothOn() {
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);

        if (bluetoothManager == null) {
            return false;
        }

        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();

        if (!isPermissionGranted(Manifest.permission.BLUETOOTH)) {
            return false;
        }

        return mBluetoothAdapter != null && mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON;
    }

    /**
     * Check if the device is connected to Wifi or not.
     *
     * @return "Y" if connected otherwise "N".
     */
    public boolean isConnectedToWifi() {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        assert connManager != null;
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        assert mWifi != null;
        return mWifi.isConnected();
    }

    /**
     * Get available internal storage of the device
     *
     * @return available storage in megabytes(MB)
     */
    public long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long availableBlocks = stat.getAvailableBlocksLong();
        return (availableBlocks * blockSize) / 0x100000L;
    }

    /**
     * Get total internal storage of the device
     *
     * @return total storage in megabytes(MB)
     */
    public long getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long totalBlocks = stat.getBlockCountLong();
        return (totalBlocks * blockSize) / 0x100000L;
    }

    /**
     * Get total RAM size of the device
     *
     * @return total RAM size in megabytes(MB)
     */
    public double getTotalRAMMemorySize() {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        assert activityManager != null;
        activityManager.getMemoryInfo(mi);
        return (double) mi.totalMem / 0x100000L;
    }

    /**
     * Get available RAM size of the device
     *
     * @return available RAM size in megabytes(MB)
     */
    public double getAvailableRAMMemorySize() {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        assert activityManager != null;
        activityManager.getMemoryInfo(mi);
        return (double) mi.availMem / 0x100000L;
    }

    /**
     * Get detailed information of the CPU
     *
     * @return CPU info
     */
    @SuppressWarnings("unused")
    public String getCPUInfo() {
        StringBuilder output = new StringBuilder();

        try {
            String[] data = {"top -m 5 -d 1"};
            ProcessBuilder processBuilder = new ProcessBuilder(data);
            Process process = processBuilder.start();
            InputStream inputStream = process.getInputStream();
            byte[] byteArray = new byte[1024];

            while (inputStream.read(byteArray) != -1) {
                output.append(new String(byteArray));
            }
            inputStream.close();

        } catch (Exception ex) {
            sentryHelper.captureException(ex);
        }

        return output.toString();
    }

    /**
     * Get device orientation
     *
     * @return "Landscape"/"Portrait"
     */
    public int getDeviceOrientation() {
        return context.getResources().getConfiguration().orientation;
    }

    /**
     * Get device battery information
     *
     * @return map battery info
     */
    public Map<String, Object> getBatteryInfo() {
        Map<String, Object> battery = new HashMap<>();

        // https://developer.android.com/training/monitoring-device-state/battery-monitoring#DetermineChargeState
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, filter);
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;

        battery.put("l", batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1));
        battery.put("c", isCharging);

        return battery;
    }

    /**
     * Get device screen resolution
     *
     * @return screen resolution(eg - 1080X720)
     */
    @SuppressWarnings("unused")
    public String getScreenResolution() {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int h = displayMetrics.heightPixels;
        int w = displayMetrics.widthPixels;
        return w + "X" + h;
    }

    /**
     * Get pixel density
     *
     * @return dpi (eg - 300dpi)
     */
    @SuppressWarnings("unused")
    public String getDpi() {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int densityPixel = displayMetrics.densityDpi;
        return densityPixel + "dpi";
    }

    /**
     * Get installed applications list
     *
     * @return list of installed applications
     */
    @SuppressWarnings("unused")
    public List<String> getInstalledApps() {
        List<String> installedApps = new ArrayList<>();
        @SuppressLint("QueryPermissionsNeeded") List<PackageInfo> packs = context.getPackageManager().getInstalledPackages(0);
        for (PackageInfo packageInfo : packs) {
            String app = packageInfo.applicationInfo.loadLabel(context.getPackageManager()).toString();
            installedApps.add(app);
        }
        return installedApps;
    }

    /**
     * Get host application package name
     *
     * @return package name
     */
    public String getPackageName() {
        Package aPackage = context.getClass().getPackage();
        if (aPackage != null) return aPackage.getName();
        return null;
    }

    /**
     * Get default locale of the device
     *
     * @return default locale with country code (eg - en-IN)
     */
    public String getLocale() {
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = context.getResources().getConfiguration().getLocales().get(0);
        } else {
            locale = context.getResources().getConfiguration().locale;
        }
        return locale.getLanguage() + "-" + locale.getCountry();
    }

    /**
     * Get host application's time of installation
     *
     * @return timestamp
     */
    public String getInstalledTime() {
        PackageManager pm = context.getPackageManager();
        ApplicationInfo appInfo = null;

        try {
            appInfo = pm.getApplicationInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            sentryHelper.captureException(e);
        }

        if (appInfo != null) {
            String appFile = appInfo.sourceDir;
            long installed = new File(appFile).lastModified();
            return DateUtils.getStringDateFromMS(installed, Constants.DATE_FORMAT_UTC, true);
        }
        return UNKNOWN;
    }
}
