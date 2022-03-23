package com.letscooee.device;

import android.os.Build;
import com.letscooee.BaseTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.google.common.truth.Truth.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.R)
public class AppInfoTest extends BaseTestCase {

    @Test
    public void getAppName() {
        String applicationName = applicationInfo.loadLabel(packageManager).toString();
        String appName = appInfo.getName();
        assertThat(appName).isEqualTo("com.letscooee.init.AppController");
    }

    @Test
    public void getAppVersion() {
        String appVersion = packageInfo.versionName + "+" + packageInfo.getLongVersionCode();
        String appVersionBySDK = appInfo.getVersion();
        assertThat(appVersionBySDK).isEqualTo(appVersion);
    }

    @Test
    public void getAppPackageName() {
        String packageName = packageInfo.packageName;
        String packageNameBySDK = appInfo.getPackageName();
        assertThat(packageNameBySDK).isEqualTo(packageName);
    }

    @Test
    public void getAppInstallDate() {
        String installDate = formatDateToString(packageInfo.firstInstallTime);
        String installDateBySDK = appInfo.getFirstInstallTime();
        assertThat(installDateBySDK).isEqualTo(installDate);
    }

    @Test
    public void getAppBuildDate() {
        String lastBuildDate = formatDateToString(packageInfo.lastUpdateTime);
        String lastBuildDateBySDK = appInfo.getLasBuildTime();
        assertThat(lastBuildDateBySDK).isEqualTo(lastBuildDate);
    }

    public static String formatDateToString(long dateInLong) {
        return new SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.ENGLISH)
                .format(new Date(dateInLong));
    }
}