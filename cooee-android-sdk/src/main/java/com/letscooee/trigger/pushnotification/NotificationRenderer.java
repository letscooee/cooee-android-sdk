package com.letscooee.trigger.pushnotification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import com.letscooee.CooeeFactory;
import com.letscooee.R;
import com.letscooee.loader.http.RemoteImageLoader;
import com.letscooee.models.Event;
import com.letscooee.models.trigger.TriggerData;
import com.letscooee.enums.trigger.PushNotificationImportance;
import com.letscooee.models.trigger.elements.PartElement;
import com.letscooee.models.trigger.elements.TextElement;
import com.letscooee.models.trigger.push.PushNotificationTrigger;
import com.letscooee.network.SafeHTTPService;
import com.letscooee.services.PushNotificationIntentService;
import com.letscooee.utils.Constants;

import java.util.Date;
import java.util.List;

/**
 * Main class to build and render a push notification from the received {@link TriggerData}.
 *
 * @author Shashank Agrawal
 * @since 0.3.0
 */
public abstract class NotificationRenderer {

    protected final Context context;
    protected final TriggerData triggerData;
    protected final PushNotificationTrigger pushTrigger;
    protected final RemoteViews smallContentViews;
    protected final RemoteViews bigContentViews;

    protected final RemoteImageLoader imageLoader;

    private final SafeHTTPService safeHTTPService;
    private final NotificationSound notificationSound;
    private final NotificationManager notificationManager;
    private final NotificationCompat.Builder notificationBuilder;

    private final PushNotificationImportance notificationImportance;

    private final int notificationID = (int) new Date().getTime();

    protected NotificationRenderer(Context context, TriggerData triggerData) {
        this.context = context;
        this.triggerData = triggerData;
        this.pushTrigger = triggerData.getPn();
        this.imageLoader = new RemoteImageLoader(context);
        this.notificationImportance = this.triggerData.getPn().getImportance();

        this.safeHTTPService = CooeeFactory.getSafeHTTPService();
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        this.notificationBuilder = new NotificationCompat.Builder(this.context, this.notificationImportance.getChannelID());
        this.notificationSound = new NotificationSound(context, triggerData.getPn(), notificationBuilder);

        this.smallContentViews = new RemoteViews(context.getPackageName(), R.layout.notification_small);
        this.bigContentViews = new RemoteViews(context.getPackageName(), this.getBigViewLayout());

        this.createChannel();
        this.setBuilder();
    }

    abstract int getBigViewLayout();

    abstract void updateSmallContentView();

    abstract void updateBigContentView();

    public NotificationCompat.Builder getBuilder() {
        return notificationBuilder;
    }

    protected void setTitleAndBody() {
        String title = null;
        String body = null;
        if (this.triggerData.getPn().getTitle() != null)
            title = getTextFromTextElement(this.triggerData.getPn().getTitle());

        if (this.triggerData.getPn().getBody() != null)
            body = getTextFromTextElement(this.triggerData.getPn().getBody());

        if (!TextUtils.isEmpty(title)) {
            this.notificationBuilder.setContentTitle(title);
            this.smallContentViews.setTextViewText(R.id.textViewTitle, title);
            this.bigContentViews.setTextViewText(R.id.textViewTitle, title);
        } else {
            this.smallContentViews.setViewVisibility(R.id.textViewTitle, View.INVISIBLE);
            this.bigContentViews.setViewVisibility(R.id.textViewTitle, View.INVISIBLE);

        }

        if (!TextUtils.isEmpty(body)) {
            this.notificationBuilder.setContentText(body);
            this.smallContentViews.setTextViewText(R.id.textViewInfo, body);
            this.bigContentViews.setTextViewText(R.id.textViewInfo, body);
        } else {
            this.smallContentViews.setViewVisibility(R.id.textViewInfo, View.INVISIBLE);
            this.bigContentViews.setViewVisibility(R.id.textViewInfo, View.INVISIBLE);
        }
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

        int defaults = 0;
        if (this.pushTrigger.lights) defaults |= NotificationCompat.DEFAULT_LIGHTS;
        if (this.pushTrigger.vibrate) defaults |= NotificationCompat.DEFAULT_VIBRATE;

        this.notificationBuilder.setDefaults(defaults);

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

        // TODO: 15/06/21 This following does not update if the channel is already created
        if (this.pushTrigger.lights) notificationChannel.enableLights(true);
        if (this.pushTrigger.vibrate) notificationChannel.enableVibration(true);

        this.notificationManager.createNotificationChannel(notificationChannel);
    }

    private void setDeleteIntent(Notification notification) {
        Intent deleteIntent = new Intent(this.context, PushNotificationIntentService.class);
        deleteIntent.putExtra(Constants.INTENT_TRIGGER_DATA_KEY, triggerData);
        deleteIntent.setAction(Constants.ACTION_DELETE_NOTIFICATION);

        notification.deleteIntent = PendingIntent.getService(context, 0, deleteIntent, PendingIntent.FLAG_ONE_SHOT);
    }

    private void checkForNotificationViewed() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }

        StatusBarNotification[] statusBarNotifications = notificationManager.getActiveNotifications();
        for (StatusBarNotification statusBarNotification : statusBarNotifications) {

            if (statusBarNotification.getId() == this.notificationID) {
                Event event = new Event("CE Notification Viewed", triggerData);
                this.safeHTTPService.sendEventWithoutNewSession(event);
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

    /**
     * Add actions to push notification.
     *
     * @param actions NotificationCompat.Action array
     */
    public void addActions(NotificationCompat.Action[] actions) {
        for (NotificationCompat.Action action : actions) {
            if (action != null && action.getTitle() != null) {
                notificationBuilder.addAction(action);
            }
        }
    }

    public void render() {
        Notification notification = notificationBuilder.build();
        this.setDeleteIntent(notification);

        this.notificationManager.notify(this.notificationID, notification);

        this.checkForNotificationViewed();
    }

    /**
     * Convert all parts to one string
     *
     * @param textElement instance {@link TextElement} containing {@link List} of
     *                    {@link PartElement}
     * @return single {@link String} from all parts
     */
    private String getTextFromTextElement(TextElement textElement) {
        StringBuilder text = new StringBuilder();
        List<PartElement> partElements = textElement.getParts();

        if (partElements == null || partElements.isEmpty()) {
            return text.toString();
        }

        for (PartElement partElement : partElements) {
            text.append(" " + partElement.getText());
        }

        return text.toString().replace("\n", "");
    }
}
