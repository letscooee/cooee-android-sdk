package com.letscooee.trigger.pushnotification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;
import androidx.annotation.RestrictTo;
import androidx.core.app.NotificationCompat;
import com.letscooee.CooeeFactory;
import com.letscooee.R;
import com.letscooee.enums.trigger.PushNotificationImportance;
import com.letscooee.loader.http.RemoteImageLoader;
import com.letscooee.models.Event;
import com.letscooee.models.trigger.TriggerData;
import com.letscooee.models.trigger.elements.PartElement;
import com.letscooee.models.trigger.elements.TextElement;
import com.letscooee.models.trigger.push.PushNotificationTrigger;
import com.letscooee.network.SafeHTTPService;
import com.letscooee.services.PushNotificationIntentService;
import com.letscooee.utils.Constants;
import com.letscooee.utils.PendingIntentUtility;
import com.letscooee.utils.Timer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Main class to build and render a push notification from the received {@link TriggerData}.
 *
 * @author Shashank Agrawal
 * @since 0.3.0
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public abstract class NotificationRenderer {

    protected final Context context;
    protected final TriggerData triggerData;
    protected final PushNotificationTrigger pushTrigger;
    protected RemoteViews smallContentViews;
    protected RemoteViews bigContentViews;

    protected final RemoteImageLoader imageLoader;

    private final SafeHTTPService safeHTTPService;
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
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

        this.smallContentViews = new RemoteViews(context.getPackageName(), this.getSmallViewLayout());
        this.bigContentViews = new RemoteViews(context.getPackageName(), this.getBigViewLayout());

        this.createChannel();
        this.setBuilder();
    }

    abstract int getBigViewLayout();

    abstract int getSmallViewLayout();

    public abstract boolean hasLargeImage();

    public abstract boolean hasSmallImage();

    abstract boolean cancelPushOnClick();

    public NotificationCompat.Builder getBuilder() {
        return notificationBuilder;
    }

    protected void setTitleAndBody() {
        updateSmallViewContentText();
        updateBigViewContentText();
    }

    /**
     * Add Big view contains only if {@code bigContentViews} is not null
     */
    private void updateBigViewContentText() {
        if (hasTitle()) {
            this.notificationBuilder.setContentTitle(getTitle());
            this.bigContentViews.setTextViewText(R.id.textViewTitle, getTitle());
            showViewInBigContentView(R.id.textViewTitle);
        }

        if (hasBody()) {
            this.notificationBuilder.setContentText(getBody());
            this.bigContentViews.setTextViewText(R.id.textViewSmallBody, getBody());
            this.bigContentViews.setTextViewText(R.id.textViewLargeBody, getBody());
            showViewInBigContentView(R.id.textViewLargeBody);
        }
    }

    /**
     * Add Small view contains only if {@code smallContentViews} is not null
     */
    private void updateSmallViewContentText() {

        if (hasTitle()) {
            this.notificationBuilder.setContentTitle(getTitle());
            this.smallContentViews.setTextViewText(R.id.textViewTitle, getTitle());
            showViewInSmallContentView(R.id.textViewTitle);
        }

        if (hasBody()) {
            this.notificationBuilder.setContentText(getBody());
            this.smallContentViews.setTextViewText(R.id.textViewBody, getBody());
            showViewInSmallContentView(R.id.textViewBody);
        }
    }

    private void setBuilder() {
        Date sentAt = triggerData.getSentAt() == null ? new Date() : triggerData.getSentAt();

        this.notificationBuilder
                .setAutoCancel(cancelPushOnClick())
                .setWhen(sentAt.getTime())
                .setSmallIcon(context.getApplicationInfo().icon)
                .setCustomContentView(smallContentViews)
                .setCustomBigContentView(bigContentViews)
                .setPriority(this.notificationImportance.getPriority())
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setShowWhen(true);

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
        notification.deleteIntent = PendingIntentUtility.getService(context, notificationID, deleteIntent, getNotificationCancelFlag());
    }

    private int getNotificationCancelFlag() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // TODO 20/06/22 It is giving other notification pending intent. Why not to use FLAG_IMMUTABLE
            return PendingIntent.FLAG_MUTABLE;
        } else {
            return PendingIntent.FLAG_ONE_SHOT;
        }
    }

    private void checkForNotificationViewed() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }

        StatusBarNotification[] statusBarNotifications = notificationManager.getActiveNotifications();
        for (StatusBarNotification statusBarNotification : statusBarNotifications) {

            if (statusBarNotification.getId() == this.notificationID) {
                Event event = new Event("CE Notification Viewed", triggerData);
                this.safeHTTPService.sendEventWithoutSession(event);
            }
        }
    }

    public long getNotificationID() {
        return this.triggerData.getNotificationID();
    }

    @SuppressWarnings("unused")
    public RemoteViews getSmallContentView() {
        return this.smallContentViews;
    }

    /**
     * Return bigContentView
     *
     * @return bigContentView
     */
    @SuppressWarnings("unused")
    public RemoteViews getBigContentView() {
        return this.bigContentViews;
    }

    /**
     * Return if the notification has title
     *
     * @return true if the notification has title
     */
    protected boolean hasTitle() {
        return pushTrigger.getTitle() != null && !TextUtils.isEmpty(getTitle());
    }

    /**
     * Returns if the notification has a body
     *
     * @return true if the notification has a body
     */
    protected boolean hasBody() {
        return pushTrigger.getBody() != null && !TextUtils.isEmpty(getBody());
    }

    /**
     * Provide the title of the notification.
     *
     * @return The title of the notification.
     */
    protected String getTitle() {
        return getTextFromTextElement(pushTrigger.getTitle());
    }

    /**
     * Provide body text for the notification.
     *
     * @return Body text for the notification.
     */
    protected String getBody() {
        return getTextFromTextElement(pushTrigger.getBody());
    }

    /**
     * Hide provided view from {@code smallContentViews}
     *
     * @param viewID The view ID to hide
     */
    @SuppressWarnings("unused")
    protected void hideViewInSmallContentView(int viewID) {
        smallContentViews.setViewVisibility(viewID, View.GONE);
    }

    /**
     * Hide provided view from {@code bigContentViews}
     *
     * @param viewID The view ID to hide
     */
    protected void hideViewInBigContentView(int viewID) {
        bigContentViews.setViewVisibility(viewID, View.GONE);
    }

    /**
     * Show provided view in {@code largeContentViews}
     *
     * @param viewId The view ID to show
     */
    protected void showViewInBigContentView(int viewId) {
        bigContentViews.setViewVisibility(viewId, View.VISIBLE);
    }

    /**
     * Show provided view in {@code smallContentViews}
     *
     * @param viewId The view ID to show
     */
    protected void showViewInSmallContentView(int viewId) {
        smallContentViews.setViewVisibility(viewId, View.VISIBLE);
    }

    /**
     * Add Image to provided ID in {@code smallContentViews}
     *
     * @param viewID The view ID to add the image to (e.g. R.id.imageView)
     * @param bitmap The bitmap to add to the view
     */
    public void addSmallContentImage(int viewID, Bitmap bitmap) {
        if (bitmap == null) {
            return;
        }

        showViewInSmallContentView(R.id.image_container);
        smallContentViews.setImageViewBitmap(viewID, bitmap);
    }

    /**
     * Add Image to provided ID in {@code largeContentViews}
     *
     * @param viewID The view ID to add the image to (e.g. R.id.imageView)
     * @param bitmap The bitmap to add to the view
     */
    public void addBigContentImage(int viewID, Bitmap bitmap) {
        showViewInBigContentView(R.id.image_container);
        bigContentViews.setImageViewBitmap(viewID, bitmap);
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

         /*
         Will wait for 2 seconds before checking if the notification has been viewed
         This will let notification manager to display or do whatever with notification
         before we check if it has been viewed
         If notification is not displayed it will get removed from the notification manager
         and we will get the actual event of notification view.
         */
        new Timer().schedule(this::checkForNotificationViewed, 2000);
    }

    /**
     * Convert all parts to one string
     *
     * @param textElement instance {@link TextElement} containing {@link List} of
     *                    {@link PartElement}
     * @return single {@link String} from all parts
     */
    private String getTextFromTextElement(TextElement textElement) {
        List<PartElement> partElements = textElement.getParts();

        if (partElements == null || partElements.isEmpty()) {
            return "";
        }

        List<String> partTextList = new ArrayList<>();
        for (PartElement partElement : partElements) {
            String partText = partElement.getText().replace("\n", " ").trim();
            partTextList.add(partText);
        }

        return TextUtils.join(" ", partTextList);
    }
}
