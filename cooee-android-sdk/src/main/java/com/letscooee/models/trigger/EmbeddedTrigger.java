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
    @SuppressWarnings("unused")
    private Boolean expired;

    public EmbeddedTrigger(TriggerData triggerData) {
        this(triggerData.getId(), triggerData.getEngagementID(), triggerData.getExpireAt());
    }

    public EmbeddedTrigger(String triggerID, String engagementID, Long expireAt) {
        this.triggerID = triggerID;
        this.engagementID = engagementID;
        this.expireAt = expireAt;
        this.expired = isExpired();
    }

    public String getTriggerID() {
        return triggerID;
    }

    public String getEngagementID() {
        return engagementID;
    }

    public void setExpired(Boolean expired) {
        this.expired = expired;
    }

    public boolean isExpired() {
        return this.expireAt < new Date().getTime();
    }

    /**
     * Update {@link #expired} value at runtime
     */
    public void updateStatus() {
        this.expired = isExpired();
    }
}
