package com.letscooee.trigger.adapters;

import com.google.gson.*;
import com.letscooee.CooeeFactory;
import com.letscooee.enums.trigger.ElementType;
import com.letscooee.models.trigger.elements.BaseElement;

import java.lang.reflect.Type;

/**
 * A Gson deserializer to deserialize the children of {@link com.letscooee.models.trigger.inapp.Layer} or
 * {@link com.letscooee.models.trigger.elements.GroupElement}. This checks the {@code type} key in json and then idenifies
 * based on {@link ElementType#elementClass}.
 *
 * @author Shashank Agrawal
 * @since 1.0.0
 */
public class ChildElementDeserializer implements JsonDeserializer<BaseElement> {

    @Override
    public BaseElement deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!json.isJsonObject()) {
            return null;
        }

        String rawElementType = ((JsonObject) json).getAsJsonPrimitive("type").getAsString();

        try {
            ElementType elementType = ElementType.valueOf(rawElementType);
            return context.deserialize(json, elementType.elementClass);

        } catch (IllegalArgumentException e) {
            CooeeFactory.getSentryHelper().captureException("Unsupported element type " + rawElementType, e);
            // Track and suppress the exception. Try to render other elements
            return null;
        }
    }
}