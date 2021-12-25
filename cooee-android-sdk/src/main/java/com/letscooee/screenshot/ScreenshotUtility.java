package com.letscooee.screenshot;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.RestrictTo;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.letscooee.CooeeFactory;
import com.letscooee.exceptions.HttpRequestFailedException;
import com.letscooee.task.CooeeExecutors;
import com.letscooee.utils.Constants;
import com.letscooee.utils.LocalStorageHelper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * Captures Activity screenshot and sends to server
 *
 * @author Ashish Gaikwad 23/12/21
 * @since 1.1.0
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class ScreenshotUtility {

    private static ScreenshotHelper screenshotHelper;
    private final Gson gson;
    private final Context context;

    public ScreenshotUtility(Context context) {
        this.context = context;
        this.gson = new Gson();
        initializeScreenshotHelper();
    }

    /**
     * Initialize <code>screenshotHelper</code> if app is in debug mode
     */
    private void initializeScreenshotHelper() {

        if (!CooeeFactory.getAppInfo().isDebuggable()) {
            return;
        }

        screenshotHelper = this::captureScreenShot;
    }

    /**
     * Captures the Screenshot of the provided {@link Activity} once it rendered on screen
     *
     * @param activity Instance of an currently resumed {@link Activity}
     */
    private void captureScreenShot(Activity activity) {
        String screenName = activity.getClass().getSimpleName();

        if (!isTimeToSendScreenshot(screenName)) {
            return;
        }

        View currentView = activity.getWindow().getDecorView().getRootView();
        currentView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                Bitmap screenShot = Bitmap.createBitmap(currentView.getMeasuredWidth(),
                        currentView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(screenShot);
                currentView.draw(canvas);

                if (screenShot != null) {
                    saveAndUploadBitmap(activity, screenShot, screenName);
                }
                currentView.removeOnLayoutChangeListener(this);
            }
        });

    }

    /**
     * Check if its correct time to take screenshot of the given <code>screenshot</code>
     *
     * @param screenName name of the screen
     * @return <code>true</code> if it's correct time take screenshot of the screen. Otherwise,
     * <code>false</code>
     */
    private boolean isTimeToSendScreenshot(String screenName) {
        String rawString = LocalStorageHelper.getString(context, Constants.STORAGE_SCREENSHOT_SYNC_TIME, null);
        if (TextUtils.isEmpty(rawString)) {
            return true;
        }

        Type type = new TypeToken<Map<String, Date>>() {
        }.getType();
        Map<String, Date> screenshotTimeMap = gson.fromJson(rawString, type);

        if (screenshotTimeMap == null) {
            return true;
        }

        Date lastScreenshotTime = screenshotTimeMap.get(screenName);
        Date currentTime = new Date();

        if (lastScreenshotTime == null) {
            return true;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(lastScreenshotTime);
        calendar.add(Calendar.HOUR, Constants.SCREENSHOT_SEND_INTERVAL_HOURS);

        return currentTime.after(calendar.getTime());
    }

    /**
     * Saves the screenshot to the app's temporary folder for upload operation
     *
     * @param activity     Instance of current {@link Activity}
     * @param screenShot   {@link Bitmap} which want to save
     * @param activityName Name of the current {@link Activity}
     */
    private void saveAndUploadBitmap(Activity activity, Bitmap screenShot, String activityName) {

        File file = new File(activity.getCacheDir(), activityName + ".png");
        try {
            //noinspection ResultOfMethodCallIgnored
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }


        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        screenShot.compress(Bitmap.CompressFormat.PNG, 0, byteArrayOutputStream);
        byte[] bitmapData = byteArrayOutputStream.toByteArray();

        FileOutputStream fileOutputStream;

        try {
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(bitmapData);
            fileOutputStream.flush();
            fileOutputStream.close();

            uploadFile(file, activityName);
        } catch (IOException exception) {
            CooeeFactory.getSentryHelper().captureException("Fail to save screenshot", exception);
        }

    }

    /**
     * Upload file to the server
     *
     * @param file         {@link File} from the storage
     * @param activityName Name of the {@link Activity}
     */
    private void uploadFile(File file, String activityName) {
        RequestBody fileToUpload = RequestBody.create(MediaType.parse("image/png"), file);
        RequestBody parameter = RequestBody.create(MediaType.parse("text/plain"), activityName);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), fileToUpload);

        CooeeExecutors.getInstance().networkExecutor().execute(() -> {
            try {
                Map<String, Object> response = CooeeFactory.getBaseHTTPService().uploadScreeShot(body, parameter);
                updateScreenshotSaveSuccessTime(response, activityName);
            } catch (HttpRequestFailedException e) {
                CooeeFactory.getSentryHelper().captureException(e);
            }
        });
    }

    /**
     * Update the last screenshot time for the {@link Activity}
     *
     * @param response     {@link Map} response revived from server
     * @param activityName Name of the {@link Activity}
     */
    private void updateScreenshotSaveSuccessTime(Map<String, Object> response, String activityName) {
        String rawString = LocalStorageHelper.getString(context, Constants.STORAGE_SCREENSHOT_SYNC_TIME, null);
        Map<String, Date> screenshotTimeMap;

        if (TextUtils.isEmpty(rawString)) {
            screenshotTimeMap = new HashMap<>();
        } else {

            Type type = new TypeToken<Map<String, Date>>() {
            }.getType();
            screenshotTimeMap = gson.fromJson(rawString, type);
        }

        if (((boolean) response.get("saved"))) {
            screenshotTimeMap.put(activityName, new Date());
        }


        LocalStorageHelper.putString(context, Constants.STORAGE_SCREENSHOT_SYNC_TIME, gson.toJson(screenshotTimeMap));
    }

    public static ScreenshotHelper getScreenshotHelper() {
        return screenshotHelper;
    }
}
