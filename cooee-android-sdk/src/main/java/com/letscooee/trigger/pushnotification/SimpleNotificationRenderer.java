package com.letscooee.trigger.pushnotification;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import androidx.annotation.RestrictTo;
import com.letscooee.R;
import com.letscooee.models.trigger.TriggerData;
import com.letscooee.trigger.CooeeEmptyActivity;
import com.letscooee.utils.Constants;
import com.letscooee.utils.PendingIntentUtility;

/**
 * Class to build and render a simple push notification from the received {@link TriggerData}.
 *
 * @author Shashank Agrawal
 * @since 0.3.0
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class SimpleNotificationRenderer extends NotificationRenderer {

    public SimpleNotificationRenderer(Context context, TriggerData triggerData) {
        super(context, triggerData);
    }

    @Override
    int getBigViewLayout() {
        return R.layout.notification_large;
    }

    @Override
    int getSmallViewLayout() {
        return R.layout.notification_small;
    }

    @Override
    public boolean hasLargeImage() {
        return !TextUtils.isEmpty(getLargeImageUrl());
    }

    @Override
    public boolean hasSmallImage() {
        return !TextUtils.isEmpty(getSmallImageUrl());
    }

    @Override
    boolean cancelPushOnClick() {
        return true;
    }

    public String getSmallImageUrl() {
        return this.pushTrigger.getSmallImage();
    }

    public String getLargeImageUrl() {
        return this.pushTrigger.getLargeImage();
    }

    @Override
    public void addBigContentImage(int viewID, Bitmap bitmap) {
        if (hasBody()) {
            hideViewInBigContentView(R.id.textViewLargeBody);
            showViewInBigContentView(R.id.textViewSmallBody);
        }

        super.addBigContentImage(viewID, bitmap);
    }
}