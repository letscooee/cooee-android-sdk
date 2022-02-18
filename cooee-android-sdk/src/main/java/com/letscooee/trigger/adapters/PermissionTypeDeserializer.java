package com.letscooee.trigger.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.letscooee.CooeeFactory;
import com.letscooee.utils.PermissionType;

import java.lang.reflect.Type;

/**
 * A Gson deserializer to deserialize the only {@link PermissionType} in {@link com.letscooee.models.trigger.blocks.ClickAction}.
 * This checks the value of {@code pmpt} and returns appropriate {@link PermissionType} enum.
 *
 * @author Ashish Gaikwad
 * @since 1.3.2
 */
public class PermissionTypeDeserializer implements JsonDeserializer<PermissionType> {
    @Override
    public PermissionType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        int promptType = json.getAsInt();

        switch (promptType) {
            case 0:
                return null;
            case 1:
                return PermissionType.CAMERA;
            case 2:
                return PermissionType.LOCATION;
            case 4:
                return PermissionType.PHONE_DETAILS;
            case 5:
                return PermissionType.STORAGE;
            default:
                CooeeFactory.getSentryHelper().captureMessage("Unknown permission type: " + promptType);
                return null;
        }
    }
}
