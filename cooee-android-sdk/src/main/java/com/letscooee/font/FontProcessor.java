package com.letscooee.font;

import static android.text.TextUtils.isEmpty;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.letscooee.CooeeFactory;
import com.letscooee.exceptions.HttpRequestFailedException;
import com.letscooee.models.AppFont;
import com.letscooee.utils.Constants;
import com.letscooee.utils.LocalStorageHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;

/**
 * Check for donts and if thos are not available it will download that font
 *
 * @author Ashish Gaikwad 04/08/21
 * @since 1.0.0
 */
public class FontProcessor {
    private static final Gson gson = new Gson();

    /**
     * Fetch App config from Server
     *
     * @param context current instance of {@link Context}
     */
    public static void cacheBrandFonts(Context context) {
        checkDataFromPreference(context);

        if (checkLastFontRequestDue(context)) {
            return;
        }

        try {
            Map<String, Object> config = CooeeFactory.getBaseHTTPService().getAppConfig(
                    CooeeFactory.getManifestReader().getAppID()
            );

            if (config == null) {
                return;
            }

            downloadFonts(context, config.get("fonts"));
            LocalStorageHelper.putLong(context, Constants.STORAGE_LAST_FONT_ATTEMPT, new Date().getTime());
        } catch (HttpRequestFailedException e) {
            CooeeFactory.getSentryHelper().captureException(e);
        }
    }

    private static void checkDataFromPreference(Context context) {
        String stringArray = LocalStorageHelper.getString(context, Constants.STORAGE_CACHED_FONTS, null);

        downloadFonts(context, stringArray);
    }

    /**
     * Check for past request date
     *
     * @param context current instance of {@link Context}
     * @return returns true if request is getting called before 7 days otherwise false
     */
    private static boolean checkLastFontRequestDue(Context context) {
        Date today = new Date();
        Calendar calendar = Calendar.getInstance();
        long lastCheckDate = LocalStorageHelper.getLong(context, Constants.STORAGE_LAST_FONT_ATTEMPT, 0);

        if (lastCheckDate > 0) {
            return false;
        }
        calendar.setTimeInMillis(lastCheckDate);
        calendar.add(Calendar.DAY_OF_MONTH, Constants.INTERVAL_DAYS);
        if (today.before(calendar.getTime())) {
            Log.d(Constants.TAG, "Skipping font check as its before " + Constants.INTERVAL_DAYS + " days");
            return true;
        }

        return false;
    }

    public static void downloadFonts(Context context, Object fontList) {
        downloadFonts(context, gson.toJson(fontList));
    }

    public static void downloadFonts(Context context, String rawFontList) {
        if (isEmpty(rawFontList)) {
            return;
        }
        ArrayList<AppFont> outputList = gson.fromJson(rawFontList, new TypeToken<ArrayList<AppFont>>() {
        }.getType());

        downloadFonts(context, outputList);
    }

    /**
     * Check if font is present at file system or not. Otherwise proceed to download font
     *
     * @param context  current instance of {@link Context}
     * @param fontList {@link ArrayList} of {@link AppFont}
     */
    public static void downloadFonts(Context context, List<AppFont> fontList) {

        if (fontList == null || fontList.isEmpty()) {
            Log.d(Constants.TAG, "Received empty font list");
            return;
        }

        File fontDirectory = getInternalStorage(context);

        if (fontDirectory == null) {
            fontDirectory = new File(context.getCacheDir().getAbsolutePath());
        }

        for (AppFont font : fontList) {
            File fontFile = new File(fontDirectory, font.getName() + ".ttf");
            if (fontFile.exists()) {
                continue;
            }
            downloadFont(font, fontFile);
        }

        LocalStorageHelper.putString(context, Constants.STORAGE_CACHED_FONTS, gson.toJson(fontList));
    }

    public static File getInternalStorage(Context context) {
        if (hasWriteStoragePermission(context)) {
            return null;
        }

        File fontDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/" + Constants.DIRECTORY_NAME);
        if (!fontDirectory.isDirectory()) {
            //noinspection ResultOfMethodCallIgnored
            fontDirectory.mkdir();
        }
        return fontDirectory;
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
     * @param fontData will instance of {@link AppFont}
     * @param fontFile will be instance of {@link File} to write new downloaded file
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void downloadFont(AppFont fontData, File fontFile) {
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
            byte[] bytes = new byte[1024 * 1024];

            while ((read = inputStream.read(bytes)) != -1) {
                fileOutputStream.write(bytes, 0, read);
            }
            fileOutputStream.flush();
        } catch (HttpRequestFailedException | IOException e) {
            CooeeFactory.getSentryHelper().captureException(e);
        }
    }
}
