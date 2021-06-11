package com.letscooee.models.trigger;

import androidx.core.app.NotificationCompat;

import static com.letscooee.utils.Constants.*;

/**
 * Importance of the push notification.
 *
 * @author Shashank Agrawal
 * @since 0.3.0
 */
public enum PushNotificationImportance {

    DEFAULT(DEFAULT_CHANNEL_ID, DEFAULT_CHANNEL_NAME, NotificationCompat.PRIORITY_DEFAULT),
    HIGH(HIGH_CHANNEL_ID, HIGH_CHANNEL_NAME, NotificationCompat.PRIORITY_MAX);

    private final String channelID;
    private final String channelName;
    int priority;

    PushNotificationImportance(String channelID, String channelName, int priority) {
        this.channelID = channelID;
        this.channelName = channelName;
        this.priority = priority;
    }

    public String getChannelID() {
        return channelID;
    }

    public String getChannelName() {
        return channelName;
    }

    public int getPriority() {
        return priority;
    }
}
