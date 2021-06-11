package com.letscooee.pushnotification.renderer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.letscooee.models.TriggerData;
import com.letscooee.services.CooeeIntentService;
import com.letscooee.utils.Constants;

import java.util.Date;

/**
 * Main class to build and render a push notification from the received {@link TriggerData}.
 *
 * @author Shashank Agrawal
 * @since 0.3.0
 */
public class NotificationRenderer {

    private final Context context;
    private final TriggerData triggerData;
    private final NotificationSound notificationSound;
    private final NotificationManager notificationManager;
    private final NotificationCompat.Builder notificationBuilder;

    private final int notificationID = (int) new Date().getTime();

    public NotificationRenderer(Context context, TriggerData triggerData) {
        this.context = context;
        this.triggerData = triggerData;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        this.notificationBuilder = new NotificationCompat.Builder(this.context, Constants.NOTIFICATION_CHANNEL_ID);
        this.notificationSound = new NotificationSound(context, triggerData, notificationBuilder);

        this.createChannel();
        this.setBuilder();
        this.notificationSound.setSoundInNotification();
    }

    private void setBuilder() {
        this.notificationBuilder
                // TODO: 11/06/21 Test this for carousel based notifications as it was false there
                .setAutoCancel(true)
                // TODO: 11/06/21 It should be the date of engagement trigger planned
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(context.getApplicationInfo().icon)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle());
    }

    public NotificationCompat.Builder getBuilder() {
        return notificationBuilder;
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationChannel notificationChannel = new NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_ID,
                Constants.NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT);

        notificationChannel.enableVibration(true);
        notificationChannel.enableLights(true);
        notificationChannel.setDescription("");

        this.notificationSound.setSoundInChannel(notificationChannel);

        this.notificationManager.createNotificationChannel(notificationChannel);
    }

    private void setDeleteIntent(Notification notification) {
        Intent deleteIntent = new Intent(this.context, CooeeIntentService.class);
        deleteIntent.setAction("Notification Deleted");

        notification.deleteIntent = PendingIntent.getService(context, 0, deleteIntent, PendingIntent.FLAG_ONE_SHOT);
    }

    public int getNotificationID() {
        return this.notificationID;
    }

    public NotificationManager getNotificationManager() {
        return this.notificationManager;
    }

    public void render() {
        Notification notification = notificationBuilder.build();
        this.setDeleteIntent(notification);

        this.notificationManager.notify(this.notificationID, notification);
    }
}
