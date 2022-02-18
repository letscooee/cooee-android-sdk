package com.letscooee.font;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.letscooee.CooeeFactory;
import com.letscooee.exceptions.HttpRequestFailedException;
import com.letscooee.models.AppFont;
import com.letscooee.utils.Constants;
import com.letscooee.utils.LocalStorageHelper;

import okhttp3.ResponseBody;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static android.text.TextUtils.isEmpty;

/**
 * Check for fonts and if those are not available it will download that font.
 *
 * @author Ashish Gaikwad 04/08/21
 * @since 1.0.0
 */
public class FontProcessor {

    private static final Gson gson = new Gson();

    public static void checkAndUpdateBrandFonts(Context context) {
        confirmFontsFromPreference(context);
        cacheBrandFonts(context);
    }

    /**
     * Fetch App config from server and cache the fonts.
     *
     * @param context current instance of {@link Context}
     */
    private static void cacheBrandFonts(Context context) {
        if (!isItTimeToRefreshFontsFromServer(context)) {
            Log.d(Constants.TAG, "Skipping font check as its before " + Constants.FONT_REFRESH_INTERVAL_DAYS + " days");
            return;
        }

        String appID = CooeeFactory.getManifestReader().getAppID();

        if (TextUtils.isEmpty(appID)) {
            Log.d(Constants.TAG, "Skipping font caching as appID is missing");
            return;
        }

        try {
            Map<String, Object> config = CooeeFactory.getBaseHTTPService().getAppConfig(appID);

            if (config == null) {
                return;
            }

            downloadFonts(context, config.get("fonts"));
            LocalStorageHelper.putLong(context, Constants.STORAGE_LAST_FONT_ATTEMPT, new Date().getTime());
        } catch (HttpRequestFailedException e) {
            CooeeFactory.getSentryHelper().captureException(e);
        }
    }

    private static void confirmFontsFromPreference(Context context) {
        String stringArray = LocalStorageHelper.getString(context, Constants.STORAGE_CACHED_FONTS, null);

        downloadFonts(context, stringArray);
    }

    /**
     * Check when was the last time the server was hit to check for updated fonts.
     *
     * @param context current instance of {@link Context}
     * @return returns true if this is the first attempt from the server or if it's been {@link Constants#FONT_REFRESH_INTERVAL_DAYS}
     * days we last hit the server.
     */
    private static boolean isItTimeToRefreshFontsFromServer(Context context) {
        Date today = new Date();
        Calendar calendar = Calendar.getInstance();
        long lastCheckDate = LocalStorageHelper.getLong(context, Constants.STORAGE_LAST_FONT_ATTEMPT, 0);

        if (lastCheckDate == 0) {
            return true;
        }

        calendar.setTimeInMillis(lastCheckDate);
        calendar.add(Calendar.DAY_OF_MONTH, Constants.FONT_REFRESH_INTERVAL_DAYS);
        return today.after(calendar.getTime());
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

        File fontDirectory = getFontsStorageDirectory(context);

        for (AppFont font : fontList) {
            File fontFile = getFontFile(fontDirectory, font.getName());
            if (fontFile.exists()) {
                continue;
            }
            downloadFont(font, fontFile);
        }

        LocalStorageHelper.putString(context, Constants.STORAGE_CACHED_FONTS, gson.toJson(fontList));
    }

    public static File getFontFile(File parentDirectory, String name) {
        return new File(parentDirectory, name + ".ttf");
    }

    /**
     * Check of {@link Constants#FONTS_DIRECTORY}; And creates if does not exist.
     *
     * @param context current instance of {@link Context}
     * @return Return instance {@link File}
     */
    public static File getFontsStorageDirectory(Context context) {
        if (!hasWriteStoragePermission(context)) {
            // Return the App's cache directory (can't be more than 1 MB individual file)
            return new File(context.getCacheDir().getAbsolutePath());
        }

        File fontDirectory = getExternalFontsDirectory(context);
        if (!fontDirectory.isDirectory()) {
            //noinspection ResultOfMethodCallIgnored
            fontDirectory.mkdir();
        }
        return fontDirectory;
    }

    /**
     * First check if we have permissions to write on the external mounted (SD Card) or otherwise
     * get app specific folder to write fonts.
     *
     * @param context current instance of {@link Context}
     * @return Return instance {@link File}
     */
    private static File getExternalFontsDirectory(Context context) {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return new File(context.getExternalFilesDir(null), Constants.FONTS_DIRECTORY);
        } else {
            return new File(context.getFilesDir(), Constants.FONTS_DIRECTORY);
        }
    }

    /**
     * Check for the {@link Manifest.permission#WRITE_EXTERNAL_STORAGE} permission is granted or not.
     *
     * @param context current instance of {@link Context}
     * @return Return true only if {@link Manifest.permission#WRITE_EXTERNAL_STORAGE} is granted, Otherwise return false
     */
    public static boolean hasWriteStoragePermission(Context context) {
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
