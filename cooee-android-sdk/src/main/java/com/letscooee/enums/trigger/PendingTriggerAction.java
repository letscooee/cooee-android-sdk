package com.letscooee.enums.trigger;

import androidx.annotation.RestrictTo;

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

}
