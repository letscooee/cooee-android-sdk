package com.letscooee.trigger.pushnotification;

import android.content.Context;
import androidx.annotation.RestrictTo;
import com.letscooee.R;
import com.letscooee.models.trigger.TriggerData;

/**
 * Class to build and render a carousel based push notification from the received {@link TriggerData}.
 *
 * @author Shashank Agrawal
 * @since 0.3.0
 */
@SuppressWarnings("unused")
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class CarouselNotificationRenderer extends NotificationRenderer {

    public CarouselNotificationRenderer(Context context, TriggerData triggerData) {
        super(context, triggerData);
    }

    int getBigViewLayout() {
        return R.layout.notification_carousel;
    }

    @Override
    int getSmallViewLayout() {
        return R.layout.notification_small;
    }

    @Override
    public boolean hasLargeImage() {
        return false;
    }

    @Override
    public boolean hasSmallImage() {
        return false;
    }

    @Override
    boolean cancelPushOnClick() {
        return false;
    }
}
