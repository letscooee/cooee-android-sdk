package com.letscooee.trigger.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import com.letscooee.CooeeFactory;
import com.letscooee.enums.trigger.ElementType;
import com.letscooee.models.trigger.elements.BaseElement;
import com.letscooee.models.trigger.elements.ButtonElement;
import com.letscooee.models.trigger.elements.ImageElement;
import com.letscooee.models.trigger.elements.ShapeElement;
import com.letscooee.models.trigger.elements.TextElement;
import com.letscooee.models.trigger.elements.VideoElement;
import java.lang.reflect.Type;

/**
 * A Gson deserializer to deserialize the children of {@link com.letscooee.models.trigger.inapp.InAppTrigger} elements.
 * This checks the {@code t} key in json and then identifies
 * based on {@link ElementType#elementClass}.
 *
 * @author Shashank Agrawal
 * @since 1.0.0
 */
public class ChildElementDeserializer implements JsonDeserializer<BaseElement>, JsonSerializer<BaseElement> {

    @Override
    public BaseElement deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!json.isJsonObject()) {
            return null;
        }

        int rawElementType = ((JsonObject) json).getAsJsonPrimitive("t").getAsInt();

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

    @Override
    public JsonElement serialize(BaseElement element, Type typeOfSrc, JsonSerializationContext context) {

        if (element == null) {
            return null;
        }

        try {
            JsonObject jsonElement = (JsonObject) context.serialize(element);

            if (element instanceof ImageElement) {
                jsonElement.addProperty("t", 1);
            } else if (element instanceof ButtonElement) {
                jsonElement.addProperty("t", 3);
            } else if (element instanceof TextElement) {
                jsonElement.addProperty("t", 2);
            } else if (element instanceof VideoElement) {
                jsonElement.addProperty("t", 4);
            } else if (element instanceof ShapeElement) {
                jsonElement.addProperty("t", 100);
            }
            return jsonElement;
        } catch (JsonSyntaxException e) {
            CooeeFactory.getSentryHelper().captureException("Error serializing element", e);
            return null;
        }
    }
}