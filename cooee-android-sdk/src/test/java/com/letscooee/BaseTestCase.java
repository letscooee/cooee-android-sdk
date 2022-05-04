package com.letscooee;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import androidx.test.core.app.ApplicationProvider;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.letscooee.device.AppInfo;
import com.letscooee.models.trigger.TriggerData;
import com.letscooee.room.CooeeDatabase;
import com.letscooee.trigger.CooeeEmptyActivity;
import com.letscooee.trigger.adapters.TriggerGsonDeserializer;
import com.letscooee.utils.Constants;
import junit.framework.TestCase;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.SQLiteMode;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static org.robolectric.annotation.SQLiteMode.Mode.NATIVE;

@SQLiteMode(NATIVE)
@Config(sdk = Build.VERSION_CODES.S)
@RunWith(RobolectricTestRunner.class)
public abstract class BaseTestCase extends TestCase {

    protected AppInfo appInfo;
    protected ApplicationInfo applicationInfo;
    protected PackageManager packageManager;
    protected PackageInfo packageInfo;
    protected Context context;
    protected String samplePayload;
    protected Map<String, Object> payloadMap;
    protected TriggerData triggerData;
    protected Gson gson = new Gson();
    protected TriggerData expiredTriggerData;
    protected CooeeEmptyActivity activity;
    protected CooeeEmptyActivity activityWithNoBundle;
    protected CooeeEmptyActivity activityWithNoTriggerData;
    protected CooeeDatabase database;

    static {
        BuildConfig.IS_TESTING.set(true);
    }

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
            Log.e(Constants.TAG, "Error: ", e);
        }
    }

    @After
    @Override
    public void tearDown() throws Exception {
        appInfo = null;
        applicationInfo = null;
        packageManager = null;
        packageInfo = null;
        context = null;
        samplePayload = null;
        payloadMap = null;
        triggerData = null;
        gson = null;
        expiredTriggerData = null;
        activity = null;
        activityWithNoBundle = null;
        activityWithNoTriggerData = null;
        database = null;
    }

    protected void loadPayload() {
        try {
            InputStream inputStream = context.getAssets().open("payload_2.json");
            samplePayload = new Scanner(inputStream).useDelimiter("\\A").next();
        } catch (IOException e) {
            Log.e(Constants.TAG, "Error: ", e);
        }

        payloadMap = gson.fromJson(samplePayload, new TypeToken<HashMap<String, Object>>() {
        }.getType());
    }

    protected void createActivity() {
        Intent intent = new Intent(context, CooeeEmptyActivity.class);
        activityWithNoBundle = Robolectric.buildActivity(CooeeEmptyActivity.class, intent).create().get();

        Bundle bundle = new Bundle();
        intent.putExtra(Constants.INTENT_BUNDLE_KEY, bundle);
        activityWithNoTriggerData = Robolectric.buildActivity(CooeeEmptyActivity.class, intent).create().get();

        bundle.putParcelable(Constants.INTENT_TRIGGER_DATA_KEY, triggerData);
        intent.putExtra(Constants.INTENT_BUNDLE_KEY, bundle);

        activity = Robolectric.buildActivity(CooeeEmptyActivity.class, intent).create().get();
    }

    protected void makeActiveTrigger() {
        try {
            JSONObject jsonObject = new JSONObject(samplePayload);
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, 5);
            jsonObject.put("expireAt", calendar.getTimeInMillis());
            triggerData = TriggerGsonDeserializer.getGson().fromJson(jsonObject.toString(), TriggerData.class);
        } catch (JSONException e) {
            Log.e(Constants.TAG, "Error: ", e);
        }
    }
}
