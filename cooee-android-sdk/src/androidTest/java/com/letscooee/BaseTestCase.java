package com.letscooee;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import androidx.test.core.app.ApplicationProvider;
import com.letscooee.device.AppInfo;
import junit.framework.TestCase;
import org.junit.Before;

public class BaseTestCase extends TestCase {
    protected AppInfo appInfo;
    protected ApplicationInfo applicationInfo;
    protected PackageManager packageManager;
    protected PackageInfo packageInfo;
    protected Context context;

    @Before
    @Override
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        applicationInfo = context.getApplicationInfo();
        packageManager = context.getPackageManager();
        appInfo = AppInfo.getInstance(context);
        try {
            packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}
