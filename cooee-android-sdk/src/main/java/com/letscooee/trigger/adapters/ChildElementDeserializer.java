package com.letscooee.trigger.adapters;

import com.google.gson.*;
import com.letscooee.CooeeFactory;
import com.letscooee.enums.trigger.ElementType;
import com.letscooee.models.trigger.elements.BaseElement;

import java.lang.reflect.Type;

/**
 * A Gson deserializer to deserialize the children of {@link com.letscooee.models.trigger.inapp.InAppTrigger} elements.
 * This checks the {@code t} key in json and then identifies
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

        Integer rawElementType = ((JsonObject) json).getAsJsonPrimitive("t").getAsInt();

        try {
            ElementType elementType;

            switch (rawElementType) {
                case 1:
                    elementType = ElementType.IMAGE;
                    break;
                case 2:
                    elementType = ElementType.TEXT;
                    break;
                case 3:
                    elementType = ElementType.BUTTON;
                    break;
                case 4:
                    elementType = ElementType.VIDEO;
                    break;
                case 100:
                    elementType = ElementType.SHAPE;
                    break;
                default:
                    CooeeFactory.getSentryHelper().captureMessage("Invalid element type");
                    return null;
            }
            return context.deserialize(json, elementType.elementClass);

        } catch (IllegalArgumentException e) {
            CooeeFactory.getSentryHelper().captureException("Unsupported element type " + rawElementType, e);
            // Track and suppress the exception. Try to render other elements
            return null;
        }
    }
}