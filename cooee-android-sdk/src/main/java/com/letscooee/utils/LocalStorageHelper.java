package com.letscooee.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.RestrictTo;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.sentry.Sentry;

/**
 * LocalStorageHelper is used to store local shared preference data
 *
 * @author Abhishek Taparia
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public final class LocalStorageHelper {

    private static final String SHARED_PREFERENCE_NAME = "cooee_sdk";

    public static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
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

    public static ArrayList<HashMap<String, String>> getList(Context context, String key) {
        String stringList = getString(context, key, "");

        Gson gson = new Gson();
        ArrayList<HashMap<String, String>> triggerHashMapList = null;

        try {
            triggerHashMapList = gson.fromJson(
                    stringList,
                    new TypeToken<ArrayList<HashMap<String, String>>>() {
                    }.getType()
            );
        } catch (JsonSyntaxException exception) {
            Sentry.captureException(exception);

            // Remove all activeTriggers when wrong format of triggerId is saved in shared preferences
            remove(context, CooeeSDKConstants.STORAGE_ACTIVE_TRIGGERS);
            return new ArrayList<>();
        }

        return triggerHashMapList != null ? triggerHashMapList : new ArrayList<>();
    }

    public static void putListImmediately(Context context, String key, ArrayList<HashMap<String, String>> list) {
        putStringImmediately(context, key, new Gson().toJson(list));
    }

    public static void apply(SharedPreferences.Editor editor) {
        editor.apply();
    }

    public static boolean commit(SharedPreferences.Editor editor) {
        return editor.commit();
    }

    public static void remove(Context context, String key) {
        SharedPreferences sharedPreferences = getPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit().remove(key);
        apply(editor);
    }

    public static void putTouchMapString(Context context, String key, String string) {
        SharedPreferences sharedPreferences = getPreferences(context);
        Map map = getTouchMap(context, key);
        if (map == null) {
            map = new HashMap<Long, Object>();
        }
        map.put(new Date().getTime() + "", string);
        SharedPreferences.Editor editor = sharedPreferences.edit().putString(key, map.toString());
        commit(editor);
    }

    public static Map<Object, Object> getTouchMap(Context context, String key) {
        SharedPreferences sharedPreferences = getPreferences(context);
        Map map = new Gson().fromJson(sharedPreferences.getString(key, null), Map.class);
        return map;
    }

    public static void putLong(Context context, String key, long value) {
        SharedPreferences sharedPreferences = getPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit().putLong(key, value);
        apply(editor);
    }

    public static long getLong(Context context, String key, long defaultValue) {
        return getPreferences(context).getLong(key, defaultValue);
    }
}
