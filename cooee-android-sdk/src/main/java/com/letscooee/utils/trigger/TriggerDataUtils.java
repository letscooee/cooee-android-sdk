package com.letscooee.utils.trigger;

import android.text.TextUtils;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.letscooee.enums.trigger.PushType;
import com.letscooee.models.trigger.elements.BaseElement;
import com.letscooee.trigger.adapters.ChildElementDeserializer;
import com.letscooee.trigger.adapters.PermissionTypeDeserializer;
import com.letscooee.trigger.adapters.PushTypeDeserializer;
import com.letscooee.utils.Constants;
import com.letscooee.utils.PermissionType;

import java.lang.reflect.Type;
import java.util.HashMap;

/**
 * Utility class to initialize Gson object.
 *
 * @author Ashish Gaikwad
 * @since 1.3.2
 */
public class TriggerDataUtils {

    private static final Type MAP_TYPE_TOKEN = new TypeToken<HashMap<String, Object>>() {
    }.getType();

    private TriggerDataUtils() {
    }

    private static Gson gson;

    /**
     * Initialize Gson object which can be used to deserialize payload.
     *
     * @return Gson object.
     */
    public static Gson getGson() {
        if (gson == null) {
            gson = new GsonBuilder()
                    .registerTypeAdapter(BaseElement.class, new ChildElementDeserializer())
                    .registerTypeAdapter(PermissionType.class, new PermissionTypeDeserializer())
                    .registerTypeAdapter(PushType.class, new PushTypeDeserializer())
                    .create();
        }
        return gson;
    }

    public static HashMap<String, Object> parse(String rawData) throws JsonSyntaxException {
        if (TextUtils.isEmpty(rawData)) {
            Log.d(Constants.TAG, "No data to parse");
            return null;
        }

        HashMap<String, Object> parsedData = new Gson().fromJson(rawData, MAP_TYPE_TOKEN);
        if (parsedData == null || parsedData.isEmpty()) {
            return null;
        }

        return parsedData;
    }

}
