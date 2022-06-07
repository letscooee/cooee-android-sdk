package com.letscooee.enums.trigger;

import java.util.HashMap;
import java.util.Map;

public enum PendingTriggerAction {
    // delete all PendingTrigger
    DELETE_ALL("da"),
    // delete all except latest PendingTrigger
    DELETE_ALL_EXCEPT_FIRST("daef"),
    // delete all except oldest PendingTrigger
    DELETE_ALL_EXCEPT_LAST("dael"),
    // delete latest(first) PendingTrigger
    DELETE_FIRST("df"),
    // delete oldest(last) PendingTrigger
    DELETE_LAST("dl"),
    // delete PendingTrigger with triggerId
    DELETE_ID("di");

    private final String action;

    PendingTriggerAction(String action) {
        this.action = action;
    }

    private static final Map<String, PendingTriggerAction> PENDING_TRIGGER_ACTION_HASH_MAP = new HashMap<>();

    static {
        for (PendingTriggerAction pendingTriggerAction : PendingTriggerAction.values()) {
            PENDING_TRIGGER_ACTION_HASH_MAP.put(pendingTriggerAction.action, pendingTriggerAction);
        }
    }

    public static PendingTriggerAction getPendingTriggerAction(String action) {
        return PENDING_TRIGGER_ACTION_HASH_MAP.get(action);
    }
}
