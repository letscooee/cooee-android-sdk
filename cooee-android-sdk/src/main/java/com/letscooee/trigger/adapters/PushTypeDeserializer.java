package com.letscooee.trigger.adapters;

import androidx.annotation.RestrictTo;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.letscooee.enums.trigger.PushType;

import java.lang.reflect.Type;

/**
 * A Gson deserializer to deserialize the {@link com.letscooee.models.trigger.push.PushNotificationTrigger} elements.
 * * This checks the {@code pt} key in json and then identifies
 * * based on {@link PushType}.
 *
 * @author Ashish Gaikwad 20/05/22
 * @since 1.3.12
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class PushTypeDeserializer implements JsonDeserializer<PushType>, JsonSerializer<PushType> {

    private final Gson gson = new Gson();

    @Override
    public PushType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        int pushType = json.getAsInt();

        switch (pushType) {
            case 2:
                return PushType.CAROUSEL;
            case 0:
            case 1:
            default:
                return PushType.SIMPLE;
        }
    }

    @Override
    public JsonElement serialize(PushType src, Type typeOfSrc, JsonSerializationContext context) {
        if (src == null) {
            return null;
        }

        if (src == PushType.CAROUSEL) {
            return gson.toJsonTree(2);
        }
        return gson.toJsonTree(1);
    }
}
