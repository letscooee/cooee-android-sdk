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
import com.letscooee.models.trigger.PushNotificationImportance;
import com.letscooee.models.trigger.PushNotificationTrigger;
import com.letscooee.network.SafeHTTPService;
import com.letscooee.services.CooeeIntentService;

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

    private final PushNotificationImportance notificationImportance;

    private final int notificationID = (int) new Date().getTime();

    protected NotificationRenderer(Context context, PushNotificationTrigger triggerData) {
        this.context = context;
        this.triggerData = triggerData;
        this.notificationImportance = this.triggerData.getImportance();

        this.safeHTTPService = CooeeFactory.getSafeHTTPService();
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        this.notificationBuilder = new NotificationCompat.Builder(this.context, this.notificationImportance.getChannelID());
        this.notificationSound = new NotificationSound(context, triggerData, notificationBuilder);

        this.smallContentViews = new RemoteViews(context.getPackageName(), R.layout.notification_small);
        this.bigContentViews = new RemoteViews(context.getPackageName(), this.getBigViewLayout());

        this.createChannel();
        this.setBuilder();
        this.notificationSound.setSoundInNotification();
    }

    abstract int getBigViewLayout();

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

    private void setBuilder() {
        this.notificationBuilder
                // TODO: 11/06/21 Test this for carousel based notifications as it was false there
                .setAutoCancel(true)
                // TODO: 11/06/21 It should be the date of engagement trigger planned
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(context.getApplicationInfo().icon)
                .setCustomContentView(smallContentViews)
                .setCustomBigContentView(bigContentViews)
                .setPriority(this.notificationImportance.getPriority())
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle());

        this.setTitleAndBody();
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationChannel notificationChannel = new NotificationChannel(
                notificationImportance.getChannelID(),
                notificationImportance.getChannelName(),
                NotificationManager.IMPORTANCE_HIGH);

        notificationChannel.setDescription("");

        if (notificationImportance == PushNotificationImportance.DEFAULT) {
            notificationChannel.setImportance(NotificationManager.IMPORTANCE_DEFAULT);
        } else {
            notificationChannel.setImportance(NotificationManager.IMPORTANCE_HIGH);
        }

        if (this.triggerData.pn != null) {
            if (this.triggerData.pn.lights) notificationChannel.enableLights(true);
            if (this.triggerData.pn.vibrate) notificationChannel.enableVibration(true);

            if (this.triggerData.pn.sound) {
                this.notificationSound.setSoundInChannel(notificationChannel);
            }
        }

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
