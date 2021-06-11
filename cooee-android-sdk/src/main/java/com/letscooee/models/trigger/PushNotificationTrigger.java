package com.letscooee.models.trigger;

import android.text.TextUtils;
import com.letscooee.models.TriggerData;

public class PushNotificationTrigger extends TriggerData {

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
