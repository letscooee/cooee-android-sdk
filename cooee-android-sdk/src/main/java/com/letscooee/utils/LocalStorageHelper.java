package com.letscooee.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * LocalStorageHelper is used to store local shared preference data
 *
 * @author Abhishek Taparia
 */
public final class LocalStorageHelper {

    public static SharedPreferences getPreferences(Context context) {
        //TODO update hard coded value with CooeeSDKConstants's member
        return context.getSharedPreferences("cooee_sdk", Context.MODE_PRIVATE);
    }

    public static String getString(Context context, String key, String defaultValue) {
        return getPreferences(context).getString(key, defaultValue);
    }

    public static void putString(Context context, String key, String value) {
        SharedPreferences sharedPreferences = getPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit().putString(key, value);
        apply(editor);
    }

    public static boolean putStringImmediately(Context context, String key, String value) {
        SharedPreferences sharedPreferences = getPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit().putString(key, value);
        return commit(editor);
    }

    public static int getInt(Context context, String key, int defaultValue) {
        return getPreferences(context).getInt(key, defaultValue);
    }

    public static void putInt(Context context, String key, int value) {
        SharedPreferences sharedPreferences = getPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit().putInt(key, value);
        apply(editor);
    }

    public static boolean putIntImmediately(Context context, String key, int value) {
        SharedPreferences sharedPreferences = getPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit().putInt(key, value);
        return commit(editor);
    }

    public static boolean getBoolean(Context context, String key, boolean defaultValue) {
        return getPreferences(context).getBoolean(key, defaultValue);
    }

    public static void putBoolean(Context context, String key, boolean value) {
        SharedPreferences sharedPreferences = getPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit().putBoolean(key, value);
        apply(editor);
    }

    public static boolean putBooleanImmediately(Context context, String key, boolean value) {
        SharedPreferences sharedPreferences = getPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit().putBoolean(key, value);
        return commit(editor);
    }

    public static void apply(SharedPreferences.Editor editor) {
        try {
            editor.apply();
        } catch (Throwable t) {
            Log.e(CooeeSDKConstants.LOG_PREFIX, "Failed to update in shared preferences storage");
        }
    }

    public static boolean commit(SharedPreferences.Editor editor) {
        return editor.commit();
    }

    public static void remove(Context context, String key) {
        SharedPreferences sharedPreferences = getPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit().remove(key);
        apply(editor);
    }

}
