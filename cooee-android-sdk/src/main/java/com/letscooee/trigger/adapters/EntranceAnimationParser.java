package com.letscooee.trigger.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.letscooee.models.trigger.blocks.Animation;
import java.lang.reflect.Type;

/**
 * Serialize and Deserialize {@link Animation.EntranceAnimation}
 *
 * @author Ashish Gaikwad
 * @since 1.4.2
 */
public class EntranceAnimationParser implements JsonDeserializer<Animation.EntranceAnimation>,
        JsonSerializer<Animation.EntranceAnimation> {


    @Override
    public Animation.EntranceAnimation deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        int animation = json.getAsInt();

        switch (animation) {
            case 1:
                return Animation.EntranceAnimation.NONE;
            case 2:
                return Animation.EntranceAnimation.SLIDE_IN_TOP;
            case 3:
                return Animation.EntranceAnimation.SLIDE_IN_DOWN;
            case 4:
                return Animation.EntranceAnimation.SLIDE_IN_LEFT;

            case 6:
                return Animation.EntranceAnimation.SLIDE_IN_TOP_LEFT;
            case 7:
                return Animation.EntranceAnimation.SLIDE_IN_TOP_RIGHT;
            case 8:
                return Animation.EntranceAnimation.SLIDE_IN_BOTTOM_LEFT;
            case 9:
                return Animation.EntranceAnimation.SLIDE_IN_BOTTOM_RIGHT;
            case 5:
            default:
                return Animation.EntranceAnimation.SLIDE_IN_RIGHT;
        }
    }


    @Override
    public JsonElement serialize(Animation.EntranceAnimation src, Type typeOfSrc, JsonSerializationContext context) {
        return context.serialize(src.getId());
    }

}
