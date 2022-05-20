package com.letscooee.trigger.adapters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.letscooee.enums.trigger.PushType;
import com.letscooee.models.trigger.elements.BaseElement;
import com.letscooee.utils.PermissionType;

/**
 * TriggerGsonDeserializer is Utility class to initialize Gson object.
 *
 * @author Ashish Gaikwad
 * @since 1.3.2
 */
public class TriggerGsonDeserializer {

    private  TriggerGsonDeserializer(){}

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
}
