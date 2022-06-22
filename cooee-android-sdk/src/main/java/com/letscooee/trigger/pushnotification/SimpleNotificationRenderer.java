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
        if (bitmap == null) {
            return;
        }

        if (hasBody()) {
            hideViewInBigContentView(R.id.textViewLargeBody);
            showViewInBigContentView(R.id.textViewSmallBody);
        }

        super.addBigContentImage(viewID, bitmap);
    }

    public void setContentIntent() {
        Intent appLaunchIntent = new Intent(context, CooeeEmptyActivity.class);

        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.INTENT_TRIGGER_DATA_KEY, triggerData);

        appLaunchIntent.putExtra(Constants.INTENT_BUNDLE_KEY, bundle);
        appLaunchIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent appLaunchPendingIntent = PendingIntentUtility.getActivity(
                context,
                triggerData.getId().hashCode(),
                appLaunchIntent
        );
        getBuilder().setContentIntent(appLaunchPendingIntent);
    }

    @Override
    public void render() {
        boolean hasSmallImage = hasSmallImage();
        boolean hasLargeImage = hasLargeImage();

        if (hasSmallImage && hasLargeImage) {
            this.imageLoader.load(getSmallImageUrl(), (Bitmap smallImageResource) -> {
                addSmallContentImage(R.id.imageViewLarge, smallImageResource);

                this.imageLoader.load(getLargeImageUrl(), (Bitmap largeImageResource) -> {
                    addBigContentImage(R.id.imageViewLarge, largeImageResource);
                    super.render();
                });
            });
        } else if (hasSmallImage) {
            this.imageLoader.load(getSmallImageUrl(), (Bitmap smallImageResource) -> {
                addSmallContentImage(R.id.imageViewLarge, smallImageResource);
                super.render();
            });
        } else if (hasLargeImage) {
            this.imageLoader.load(getLargeImageUrl(), (Bitmap largeImageResource) -> {
                addBigContentImage(R.id.imageViewLarge, largeImageResource);
                super.render();
            });
        } else {
            super.render();
        }
    }
}