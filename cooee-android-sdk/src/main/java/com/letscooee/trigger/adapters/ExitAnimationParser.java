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
 * Serialize and Deserialize {@link Animation.ExitAnimation}
 *
 * @author Ashish Gaikwad
 * @since 1.4.2
 */
public class ExitAnimationParser implements JsonDeserializer<Animation.ExitAnimation>,
        JsonSerializer<Animation.ExitAnimation> {

    @Override
    public Animation.ExitAnimation deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        int animation = json.getAsInt();

        switch (animation) {
            case 1:
                return Animation.ExitAnimation.NONE;
            case 2:
                return Animation.ExitAnimation.SLIDE_OUT_TOP;
            case 3:
                return Animation.ExitAnimation.SLIDE_OUT_DOWN;
            case 4:
                return Animation.ExitAnimation.SLIDE_OUT_LEFT;

            case 6:
                return Animation.ExitAnimation.SLIDE_OUT_TOP_LEFT;
            case 7:
                return Animation.ExitAnimation.SLIDE_OUT_TOP_RIGHT;
            case 8:
                return Animation.ExitAnimation.SLIDE_OUT_BOTTOM_LEFT;
            case 9:
                return Animation.ExitAnimation.SLIDE_OUT_BOTTOM_RIGHT;
            case 5:
            default:
                return Animation.ExitAnimation.SLIDE_OUT_RIGHT;
        }
    }

    @Override
    public JsonElement serialize(Animation.ExitAnimation src, Type typeOfSrc, JsonSerializationContext context) {
        return context.serialize(src.getId());
    }

}
