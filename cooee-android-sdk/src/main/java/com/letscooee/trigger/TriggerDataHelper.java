package com.letscooee.trigger;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.letscooee.CooeeFactory;
import com.letscooee.enums.trigger.PushType;
import com.letscooee.exceptions.InvalidTriggerDataException;
import com.letscooee.exceptions.TriggerDataParseException;
import com.letscooee.models.trigger.TriggerData;
import com.letscooee.models.trigger.elements.BaseElement;
import com.letscooee.trigger.adapters.ChildElementDeserializer;
import com.letscooee.trigger.adapters.PermissionTypeDeserializer;
import com.letscooee.trigger.adapters.PushTypeDeserializer;
import com.letscooee.utils.PermissionType;

/**
 * Utility class to initialize Gson object.
 *
 * @author Ashish Gaikwad
 * @since 1.3.2
 */
public class TriggerDataHelper {

    private TriggerDataHelper() {
    }

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

    /**
     * Deserialize {@link TriggerData} from JSON string.
     *
     * @param jsonString JSON string to deserialize.
     * @return @{@link TriggerData} object.
     * @throws JsonSyntaxException if the JSON string is not valid.
     */
    @NonNull
    public static TriggerData parse(@NonNull String jsonString) throws InvalidTriggerDataException {
        TriggerData triggerData;

        try {
            triggerData = getGson().fromJson(jsonString, TriggerData.class);
        } catch (JsonSyntaxException e) {
            CooeeFactory.getSentryHelper().captureException(e);
            throw new TriggerDataParseException(e, jsonString);
        }

        if (TextUtils.isEmpty(triggerData.getId())) {
            throw new InvalidTriggerDataException("Trigger id is missing", triggerData);
        }

        if (triggerData.isCurrentlySupported()) {
            CooeeFactory.getSentryHelper().captureMessage("Unsupported payload version received " + triggerData.getVersion());
            throw new InvalidTriggerDataException("Unsupported payload version received", triggerData);
        }

        return triggerData;
    }

}
