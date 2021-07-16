package com.letscooee.trigger.pushnotification;

import android.app.NotificationChannel;
import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import com.letscooee.R;
import com.letscooee.models.v3.PushNotificationData;

/**
 * Provides sound for the notification.
 *
 * @author Shashank Agrawal
 * @since 0.3.0
 */
class NotificationSound {

    private final String packageName;
    private final PushNotificationData triggerData;
    private final NotificationCompat.Builder notificationBuilder;

    public NotificationSound(Context context, PushNotificationData triggerData, NotificationCompat.Builder notificationBuilder) {
        this.triggerData = triggerData;
        this.packageName = context.getPackageName();
        this.notificationBuilder = notificationBuilder;
    }

    private Uri getDefault() {
        Uri sound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + this.packageName + "/" + R.raw.notification_sound);

        // TODO: 11/06/21 Check if sound exist and handle exception
        return sound;
    }

    public void setSoundInNotification() {
        this.notificationBuilder.setSound(this.getDefault());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void setSoundInChannel(NotificationChannel notificationChannel) {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build();

        notificationChannel.setSound(this.getDefault(), audioAttributes);
    }
}
