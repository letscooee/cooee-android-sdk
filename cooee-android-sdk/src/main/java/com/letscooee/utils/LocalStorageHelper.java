package com.letscooee.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.annotation.RestrictTo;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.letscooee.models.trigger.EmbeddedTrigger;

import io.sentry.Sentry;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LocalStorageHelper is used to store local shared preference data
 *
 * @author Abhishek Taparia
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public final class LocalStorageHelper {

    private static final String SHARED_PREFERENCE_NAME = "cooee_sdk";
    private static final Gson gson = new Gson();

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

    public static ArrayList<HashMap<String, Object>> getList(Context context, String key) {
        String stringList = getString(context, key, "");

        if (TextUtils.isEmpty(stringList)) return new ArrayList<>();

        ArrayList<HashMap<String, Object>> triggerHashMapList = null;

        try {
            triggerHashMapList = gson.fromJson(
                    stringList,
                    new TypeToken<ArrayList<HashMap<String, Object>>>() {
                    }.getType()
            );
        } catch (JsonSyntaxException ignored) {
        }

        return triggerHashMapList != null ? triggerHashMapList : new ArrayList<>();
    }

    public static ArrayList<EmbeddedTrigger> getEmbeddedTriggers(Context context, String key) {
        String stringList = getString(context, key, "");

        if (TextUtils.isEmpty(stringList)) return new ArrayList<>();

        ArrayList<EmbeddedTrigger> typedList = null;

        try {
            typedList = gson.fromJson(
                    stringList,
                    new TypeToken<ArrayList<EmbeddedTrigger>>() {
                    }.getType()
            );
        } catch (JsonSyntaxException exception) {
            Sentry.captureException(exception);

            remove(context, Constants.STORAGE_ACTIVATED_TRIGGERS);
        }

        return typedList != null ? typedList : new ArrayList<>();
    }

    public static void putEmbeddedTriggersImmediately(Context context, String key, List<EmbeddedTrigger> list) {
        putStringImmediately(context, key, gson.toJson(list));
    }

    public static EmbeddedTrigger getEmbeddedTrigger(Context context, String key, EmbeddedTrigger defaultValue) {
        String stringTrigger = getString(context, key, "");

        if (TextUtils.isEmpty(stringTrigger)) {
            return defaultValue;
        }

        return gson.fromJson(stringTrigger, EmbeddedTrigger.class);
    }

    public static void putEmbeddedTriggerImmediately(Context context, String key, EmbeddedTrigger trigger) {
        putStringImmediately(context, key, gson.toJson(trigger));
    }

    public static void putListImmediately(Context context, String key, ArrayList<HashMap<String, Object>> list) {
        putStringImmediately(context, key, gson.toJson(list));
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
        return gson.fromJson(sharedPreferences.getString(key, null), Map.class);
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
