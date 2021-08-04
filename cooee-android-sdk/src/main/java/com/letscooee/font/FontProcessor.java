package com.letscooee.font;

import static android.text.TextUtils.isEmpty;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.letscooee.CooeeFactory;
import com.letscooee.exceptions.HttpRequestFailedException;
import com.letscooee.models.FontData;
import com.letscooee.utils.Constants;
import com.letscooee.utils.LocalStorageHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import okhttp3.ResponseBody;

/**
 * Check for donts and if thos are not available it will download that font
 *
 * @author Ashish Gaikwad 04/08/21
 * @since 1.0.0
 */

public class FontProcessor {

    /**
     * Fetch App config from Server
     *
     * @param context current instance of {@link Context}
     */
    public static void fetchFontFile(Context context) {
        if (checkLastFontRequestDue(context)) {
            return;
        }
        ArrayList<FontData> fontResponseList = null;
        try {
            Map<String, Object> config = CooeeFactory.getBaseHTTPService().requestFont(
                    CooeeFactory.getManifestReader().getAppID()
            );

            if (config == null) {
                return;
            }

            fontResponseList = (ArrayList<FontData>) config.get("fonts");
            LocalStorageHelper.putLong(context, Constants.STORAGE_LAST_FONT_ATTEMPT, new Date().getTime());
        } catch (HttpRequestFailedException e) {
            CooeeFactory.getSentryHelper().captureException(e);
            return;
        }
        checkFontPresence(context, fontResponseList);
    }

    /**
     * Check for past request date
     *
     * @param context current instance of {@link Context}
     * @return returns tru if request is getting call before 7 days otherwise false
     */
    private static boolean checkLastFontRequestDue(Context context) {
        Date today = new Date();
        Calendar calendar = Calendar.getInstance();
        long lastCheckDate = LocalStorageHelper.getLong(context, Constants.STORAGE_LAST_FONT_ATTEMPT, 0);
        if (lastCheckDate > 0) {
            calendar.setTimeInMillis(lastCheckDate);
            calendar.add(Calendar.DAY_OF_MONTH, 7);
            if (today.before(calendar.getTime())) {
                Log.d(Constants.TAG, "Skipping font check as its before 7 days");
                return true;
            }
        }
        return false;
    }

    /**
     * Check if font is present at file system or not. Otherwise proceed to download font
     *
     * @param context          current instance of {@link Context}
     * @param fontResponseList {@link ArrayList} of {@link FontData}
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void checkFontPresence(Context context, ArrayList<FontData> fontResponseList) {

        if (fontResponseList == null || fontResponseList.isEmpty()) {
            Log.d(Constants.TAG, "Received empty font list");
            return;
        }

        String basePath = context.getCacheDir().getAbsolutePath();
        if (hasWriteStoragePermission(context)) {
            basePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Cooee";
        }

        File fontDirectory = new File(basePath);
        if (!fontDirectory.isDirectory()) {
            fontDirectory.mkdir();
        }

        for (FontData response : fontResponseList) {
            File fontFile = new File(fontDirectory, response.getName() + ".ttf");
            if (fontFile.exists()) {
                continue;
            }
            downloadFont(response, fontFile);
        }
    }

    /**
     * Check for the {@link Manifest.permission#WRITE_EXTERNAL_STORAGE} permission is granted or not
     *
     * @param context current instance of {@link Context}
     * @return Return true only if {@link Manifest.permission#WRITE_EXTERNAL_STORAGE} is granted, Otherwise return false
     */
    private static boolean hasWriteStoragePermission(Context context) {

        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Download file from web and store at given {@link File}
     *
     * @param fontData will instance of {@link FontData}
     * @param fontFile will be instance of {@link File} to write new downloaded file
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void downloadFont(FontData fontData, File fontFile) {
        if (fontData == null || isEmpty(fontData.getUrl())) {
            return;
        }

        try {
            ResponseBody responseBody = CooeeFactory.getBaseHTTPService().downloadFont(fontData.getUrl());
            if (responseBody == null) {
                Log.i(Constants.TAG, fontData.getName() + " font download failed");
                return;
            }

            if (fontFile.exists()) {
                fontFile.createNewFile();
            }

            InputStream inputStream = responseBody.byteStream();
            FileOutputStream fileOutputStream = new FileOutputStream(fontFile);
            int read;
            byte[] bytes = new byte[4 * 1024];

            while ((read = inputStream.read(bytes)) != -1) {
                fileOutputStream.write(bytes, 0, read);
            }
        } catch (HttpRequestFailedException | IOException e) {
            e.printStackTrace();
        }
    }
}
