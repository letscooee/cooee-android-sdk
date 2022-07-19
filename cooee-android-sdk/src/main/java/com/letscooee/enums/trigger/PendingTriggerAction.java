package com.letscooee.enums.trigger;

import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.RestrictTo;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.letscooee.utils.Constants;

import java.util.HashMap;
import java.util.Map;

/**
 * Multiple actions can be performed on a pending trigger table.
 *
 * @author Ashish Gaikwad 07/06/22
 * @since 1.3.12
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public enum PendingTriggerAction {

    // delete all PendingTrigger
    DELETE_ALL("da"),

    // delete PendingTrigger with triggerId
    DELETE_ID("di");

    private final String action;

    PendingTriggerAction(String action) {
        this.action = action;
    }

    private static final Map<String, PendingTriggerAction> STRING_TO_ENUM_MAPPING = new HashMap<>();

    static {
        for (PendingTriggerAction pendingTriggerAction : PendingTriggerAction.values()) {
            STRING_TO_ENUM_MAPPING.put(pendingTriggerAction.action, pendingTriggerAction);
        }
    }

    /**
     * Get PendingTriggerAction from action.
     *
     * @param action action to get PendingTriggerAction from.
     * @return PendingTriggerAction from action.
     */
    public static PendingTriggerAction fromValue(String action) {
        return STRING_TO_ENUM_MAPPING.get(action);
    }

    public static Map<String, String> parseRawData(String rawData) {
        if (TextUtils.isEmpty(rawData)) {
            return null;
        }

        try {
            return new Gson().fromJson(rawData, new TypeToken<HashMap<String, String>>() {
            }.getType());
        } catch (JsonSyntaxException e) {
            Log.e(Constants.TAG, "Fail to parse pending trigger data: ", e);
            return null;
        }
    }

}
