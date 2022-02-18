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

        PermissionType permissionType;
        switch (promptType) {
            case 0:
                permissionType = null;
                break;
            case 1:
                permissionType = PermissionType.CAMERA;
                break;
            case 2:
                permissionType = PermissionType.LOCATION;
                break;
            case 4:
                permissionType = PermissionType.PHONE_DETAILS;
                break;
            case 5:
                permissionType = PermissionType.STORAGE;
                break;
            default:
                permissionType = null;
                CooeeFactory.getSentryHelper().captureMessage("Unknown permission type: " + promptType);
        }
        return permissionType;
    }
}
