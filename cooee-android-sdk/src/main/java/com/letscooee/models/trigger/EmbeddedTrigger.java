package com.letscooee.models.trigger;

import org.bson.types.ObjectId;

/**
 * Trigger class to be sent as active trigger with events.
 *
 * @author Abhishek Taparia
 * @since 1.1.0
 */
public class EmbeddedTrigger {

    private final String triggerID;
    private final String engagementID;
    private final Long expireAt;
    private final Boolean internal;

    public EmbeddedTrigger(String triggerID, String engagementID, Long expireAt, Boolean internal) {
        this.triggerID = triggerID;
        this.engagementID = engagementID;
        this.expireAt = expireAt;
        this.internal = internal;
    }

    public String getTriggerID() {
        return triggerID;
    }

    public String getEngagementID() {
        return engagementID;
    }

    public Long getExpireAt() {
        return expireAt;
    }

    public Boolean getInternal() {
        return internal;
    }
}
