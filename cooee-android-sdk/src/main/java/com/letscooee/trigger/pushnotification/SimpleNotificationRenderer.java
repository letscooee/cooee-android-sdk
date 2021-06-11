package com.letscooee.trigger.pushnotification;

import android.content.Context;
import com.letscooee.R;
import com.letscooee.models.TriggerData;
import com.letscooee.models.trigger.PushNotificationTrigger;

/**
 * Class to build and render a simple push notification from the received {@link TriggerData}.
 *
 * @author Shashank Agrawal
 * @since 0.3.0
 */
public class SimpleNotificationRenderer extends NotificationRenderer {

    public SimpleNotificationRenderer(Context context, PushNotificationTrigger triggerData) {
        super(context, triggerData);
    }

    void updateSmallContentView() {
        // TODO: 11/06/21 implement me
    }

    void updateBigContentView() {
        // TODO: 11/06/21 implement me
    }
}