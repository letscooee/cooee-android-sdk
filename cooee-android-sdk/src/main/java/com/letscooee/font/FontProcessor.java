package com.letscooee.font;

import static com.letscooee.utils.Constants.TAG;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;
import androidx.core.content.ContextCompat;
import com.letscooee.CooeeFactory;
import com.letscooee.exceptions.HttpRequestFailedException;
import com.letscooee.utils.Constants;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import okhttp3.ResponseBody;

/**
 * Check for fonts and if those are not available it will download that font.
 *
 * @author Ashish Gaikwad 04/08/21
 * @since 1.0.0
 */
public class FontProcessor {

    /**
     * Check the font is already present or not from the list and download if needed.
     *
     * @param context  {@link Context} of the app
     * @param fontList {@link List} of the {@link String} URLs
     */
    public static void downloadFontFromURLs(Context context, List<String> fontList) {
        if (fontList == null || fontList.isEmpty()) {
            return;
        }

        File fontDirectory = getFontsStorageDirectory(context);

        for (String font : fontList) {
            String fileName = font.substring(font.lastIndexOf('/') + 1, font.length());
            File fontFile = getFontFile(fontDirectory, fileName);
            if (fontFile.exists()) {
                continue;
            }

            downloadFontRuntime(font, fontFile);
        }
    }

    /**
     * Download a file via HTTP request and saves at given file path.
     *
     * @param url      URL of the file from which font should be downloaded.
     * @param fontFile {@link File} path where we want to store the file.
     */
    private static void downloadFontRuntime(String url, File fontFile) {
        try {
            ResponseBody responseBody = CooeeFactory.getBaseHTTPService().downloadFont(url);
            if (responseBody == null) {
                return;
            }

            if (!fontFile.exists()) {
                //noinspection ResultOfMethodCallIgnored
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
            Log.d(TAG, "Font file downloaded at path: " + fontFile.getPath());
        } catch (HttpRequestFailedException | IOException e) {
            CooeeFactory.getSentryHelper().captureException(e);
            Log.e(TAG, "Fail to download Font with URL: " + url, e);
        }
    }

    /**
     * Generates final file path from the file name and and directory path
     *
     * @param parentDirectory path to the directory to store the file as instance of {@link File}
     * @param name            name oth the file with extension.
     * @return {@link File} instance pointing to the given file path.
     */
    public static File getFontFile(File parentDirectory, String name) {
        return new File(parentDirectory, name);
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

}
