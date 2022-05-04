package com.letscooee.device;

import com.letscooee.BaseTestCase;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.google.common.truth.Truth.assertThat;

public class AppInfoTest extends BaseTestCase {

    @Test
    public void testAppName() {
        String appName = appInfo.getName();
        assertThat(appName).isEqualTo("com.letscooee.init.AppController");
    }

    @Test
    public void testAppVersion() {
        String appVersion = packageInfo.versionName + "+" + packageInfo.getLongVersionCode();
        String appVersionBySDK = appInfo.getVersion();
        assertThat(appVersionBySDK).isEqualTo(appVersion);
    }

    @Test
    public void testAppPackageName() {
        String packageName = packageInfo.packageName;
        String packageNameBySDK = appInfo.getPackageName();
        assertThat(packageNameBySDK).isEqualTo(packageName);
    }

    @Test
    public void testAppInstallDate() {
        String installDate = formatDateToString(packageInfo.firstInstallTime);
        String installDateBySDK = appInfo.getFirstInstallTime();
        assertThat(installDateBySDK).isEqualTo(installDate);
    }

    @Test
    public void testAppBuildDate() {
        String lastBuildDate = formatDateToString(packageInfo.lastUpdateTime);
        String lastBuildDateBySDK = appInfo.getLastBuildTime();
        assertThat(lastBuildDateBySDK).isEqualTo(lastBuildDate);
    }

    public static String formatDateToString(long dateInLong) {
        return new SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.ENGLISH)
                .format(new Date(dateInLong));
    }
}