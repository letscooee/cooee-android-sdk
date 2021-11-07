package com.letscooee.models.trigger;

import java.util.Date;

/**
 * This class store data related to triggers which are active/activated by clicking on the trigger notification or by
 * looking an in-app trigger(in future). This would be commonly sent with events {@link com.letscooee.models.Event}
 * as <code>activeTrigger</code>.
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

    public boolean isExpired() {
        return getExpireAt() < new Date().getTime() / 1000;
    }

}
