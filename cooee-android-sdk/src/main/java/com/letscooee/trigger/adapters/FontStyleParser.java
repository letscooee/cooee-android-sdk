package com.letscooee.trigger.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.letscooee.enums.trigger.FontStyle;
import java.lang.reflect.Type;

/**
 * Serialize and deserialize {@link FontStyle}
 *
 * @author Ashish Gaikwad
 * @since 1.4.2
 */
public class FontStyleParser implements JsonSerializer<FontStyle>, JsonDeserializer<FontStyle> {
    @Override
    public FontStyle deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        int style = json.getAsInt();
        switch (style) {
            case 2:
                return FontStyle.ITALICS;
            case 3:
                return FontStyle.BOLD;
            case 4:
                return FontStyle.BOLD_ITALICS;
            default:
                return FontStyle.REGULAR;

        }
    }

    @Override
    public JsonElement serialize(FontStyle src, Type typeOfSrc, JsonSerializationContext context) {
        if (src == null) return context.serialize(FontStyle.REGULAR.getValue());
        return context.serialize(src.getValue());
    }
}
