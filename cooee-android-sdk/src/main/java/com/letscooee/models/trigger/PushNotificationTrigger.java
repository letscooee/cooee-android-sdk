package com.letscooee.models.trigger;

import android.text.TextUtils;
import com.letscooee.models.TriggerData;

/**
 * A engagement trigger payload received from the service which is for rendering
 * a push notification.
 *
 * @author Shashank Agrawal
 * @since 0.3.0
 */
public class PushNotificationTrigger extends TriggerData {

    public PushNotification pn;

    public PushNotificationImportance getImportance() {
        if (pn == null || pn.importance == null) {
            return PushNotificationImportance.HIGH;
        }
        return pn.importance;
    }

    public String getSmallImage() {
        return this.imageUrl1;
    }

    public String getLargeImage() {
        return this.imageUrl1;
    }

    /**
     * Get notification title from the data
     *
     * @return the notification title
     */
    public String getNotificationTitle() {
        if (!TextUtils.isEmpty(this.getTitle().getNotificationText())) {
            return this.getTitle().getNotificationText();
        }
        return this.getTitle().getText();
    }

    public String getNotificationBody() {
        if (!TextUtils.isEmpty(this.getMessage().getNotificationText())) {
            return this.getMessage().getNotificationText();
        } else if (!TextUtils.isEmpty(this.getMessage().getText())) {
            return this.getMessage().getText();
        }
        return null;
    }
}
