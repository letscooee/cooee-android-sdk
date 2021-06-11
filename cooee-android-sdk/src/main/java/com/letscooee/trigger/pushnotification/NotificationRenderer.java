package com.letscooee.trigger.pushnotification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.service.notification.StatusBarNotification;
import android.widget.RemoteViews;
import androidx.core.app.NotificationCompat;
import com.letscooee.CooeeFactory;
import com.letscooee.R;
import com.letscooee.models.Event;
import com.letscooee.models.TriggerData;
import com.letscooee.models.trigger.PushNotificationTrigger;
import com.letscooee.network.SafeHTTPService;
import com.letscooee.services.CooeeIntentService;
import com.letscooee.utils.Constants;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Main class to build and render a push notification from the received {@link TriggerData}.
 *
 * @author Shashank Agrawal
 * @since 0.3.0
 */
public abstract class NotificationRenderer {

    protected final Context context;
    protected final PushNotificationTrigger triggerData;
    protected final RemoteViews smallContentViews;
    protected final RemoteViews bigContentViews;

    private final SafeHTTPService safeHTTPService;
    private final NotificationSound notificationSound;
    private final NotificationManager notificationManager;
    private final NotificationCompat.Builder notificationBuilder;

    private final int notificationID = (int) new Date().getTime();

    private int notificationPriority;
    private int notificationImportance;

    protected NotificationRenderer(Context context, PushNotificationTrigger triggerData) {
        this.context = context;
        this.triggerData = triggerData;

        this.safeHTTPService = CooeeFactory.getSafeHTTPService();
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        this.notificationBuilder = new NotificationCompat.Builder(this.context, Constants.NOTIFICATION_CHANNEL_ID);
        this.notificationSound = new NotificationSound(context, triggerData, notificationBuilder);

        this.smallContentViews = new RemoteViews(context.getPackageName(), R.layout.notification_small);
        this.bigContentViews = new RemoteViews(context.getPackageName(), R.layout.notification_carousel);

        this.decideImportance();
        this.createChannel();
        this.setBuilder();
        this.notificationSound.setSoundInNotification();
    }

    abstract void updateSmallContentView();

    abstract void updateBigContentView();

    public NotificationCompat.Builder getBuilder() {
        return notificationBuilder;
    }

    protected void setTitleAndBody() {
        String title = this.triggerData.getNotificationTitle();
        String body = this.triggerData.getNotificationBody();

        this.notificationBuilder
                .setContentTitle(title)
                .setContentText(body);

        this.smallContentViews.setTextViewText(R.id.textViewTitle, title);
        this.smallContentViews.setTextViewText(R.id.textViewInfo, body);
        this.bigContentViews.setTextViewText(R.id.textViewTitle, title);
        this.bigContentViews.setTextViewText(R.id.textViewInfo, body);
    }

    /**
     * Based on the data received from the backend, set the notification priority/importance.
     * The default will be HIGH/MAX.
     */
    private void decideImportance() {
        // The default value
        this.notificationPriority = NotificationCompat.PRIORITY_MAX;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // The default value
            this.notificationImportance = NotificationManager.IMPORTANCE_HIGH;
        }

        // TODO: 11/06/21 Decide importance and update those
    }

    private void setBuilder() {
        this.notificationBuilder
                // TODO: 11/06/21 Test this for carousel based notifications as it was false there
                .setAutoCancel(true)
                // TODO: 11/06/21 It should be the date of engagement trigger planned
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(context.getApplicationInfo().icon)
                .setCustomContentView(smallContentViews)
                .setCustomBigContentView(bigContentViews)
                .setPriority(this.notificationPriority)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle());

        this.setTitleAndBody();
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationChannel notificationChannel = new NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_ID,
                Constants.NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT);

        // TODO: 11/06/21 Make vibration and lights configurable
        notificationChannel.enableVibration(true);
        notificationChannel.enableLights(true);
        notificationChannel.setDescription("");
        notificationChannel.setImportance(this.notificationImportance);

        this.notificationSound.setSoundInChannel(notificationChannel);

        this.notificationManager.createNotificationChannel(notificationChannel);
    }

    private void setDeleteIntent(Notification notification) {
        Intent deleteIntent = new Intent(this.context, CooeeIntentService.class);
        deleteIntent.setAction("Notification Deleted");

        notification.deleteIntent = PendingIntent.getService(context, 0, deleteIntent, PendingIntent.FLAG_ONE_SHOT);
    }

    private void checkForNotificationViewed() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }

        StatusBarNotification[] statusBarNotifications = notificationManager.getActiveNotifications();
        for (StatusBarNotification statusBarNotification : statusBarNotifications) {

            if (statusBarNotification.getId() == this.notificationID) {
                Map<String, Object> eventProps = new HashMap<>();
                eventProps.put("triggerID", triggerData.getId());
                this.safeHTTPService.sendEvent(new Event("CE Notification Viewed", eventProps));
            }
        }
    }

    public int getNotificationID() {
        return this.notificationID;
    }

    public NotificationManager getNotificationManager() {
        return this.notificationManager;
    }

    public RemoteViews getSmallContentView() {
        return this.smallContentViews;
    }

    public RemoteViews getBigContentView() {
        return this.bigContentViews;
    }

    public void render() {
        Notification notification = notificationBuilder.build();
        this.setDeleteIntent(notification);

        this.notificationManager.notify(this.notificationID, notification);

        this.checkForNotificationViewed();
    }
}
