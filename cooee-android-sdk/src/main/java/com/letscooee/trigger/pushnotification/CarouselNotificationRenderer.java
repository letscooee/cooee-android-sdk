package com.letscooee.trigger.pushnotification;

import android.content.Context;
import com.letscooee.R;
import com.letscooee.models.TriggerData;
import com.letscooee.models.v3.CoreTriggerData;

/**
 * Class to build and render a carousel based push notification from the received {@link TriggerData}.
 *
 * @author Shashank Agrawal
 * @since 0.3.0
 */
public class CarouselNotificationRenderer extends NotificationRenderer {

    public CarouselNotificationRenderer(Context context, CoreTriggerData triggerData) {
        super(context, triggerData);
    }

    int getBigViewLayout() {
        return R.layout.notification_carousel;
    }

    void updateSmallContentView() {
        // TODO: 11/06/21 implement me
    }

    void updateBigContentView() {
        // TODO: 11/06/21 implement me
    }
}
