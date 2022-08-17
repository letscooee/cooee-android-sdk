package com.letscooee.trigger.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.letscooee.enums.trigger.InAppOrientation;
import java.lang.reflect.Type;

/**
 * Serialize and deserialize InAppTriggerOrientation.
 *
 * @author Ashish Gaikwad 08/07/22
 * @since 1.3.12
 */
public class InAppTriggerOrientationParser implements JsonSerializer<InAppOrientation>, JsonDeserializer<InAppOrientation> {
    @Override
    public InAppOrientation deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        int value = json.getAsInt();

        switch (value) {
            case 1:
                return InAppOrientation.PORTRAIT;
            case 2:
                return InAppOrientation.LANDSCAPE;
            default:
                return InAppOrientation.UNKNOWN;
        }
    }

    @Override
    public JsonElement serialize(InAppOrientation src, Type typeOfSrc, JsonSerializationContext context) {
        return context.serialize(src.getValue());
    }
}
