package com.letscooee.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.content.pm.PackageManager;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import com.letscooee.CooeeFactory;
import com.letscooee.models.Event;
import com.letscooee.models.trigger.TriggerData;
import com.letscooee.utils.Constants;

/**
 * @author Abhishek Taparia
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class PushNotificationIntentService extends IntentService {

    public PushNotificationIntentService() {
        super(PushNotificationIntentService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) {
            return;
        }

        TriggerData triggerData = intent.getParcelableExtra(Constants.INTENT_TRIGGER_DATA_KEY);

        switch (intent.getAction()) {
            case Constants.ACTION_PUSH_BUTTON_CLICK: {
                int notificationId = intent.getIntExtra("notificationId", 0);

                NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
                notificationManager.cancel(notificationId);

                PackageManager packageManager = getPackageManager();
                Intent appLaunchIntent = packageManager.getLaunchIntentForPackage(getApplicationContext().getPackageName());
                appLaunchIntent.putExtra(Constants.INTENT_TRIGGER_DATA_KEY, triggerData);

                TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                stackBuilder.addNextIntentWithParentStack(appLaunchIntent);

                PendingIntent appLaunchPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

                try {
                    appLaunchPendingIntent.send();
                } catch (PendingIntent.CanceledException e) {
                    CooeeFactory.getSentryHelper().captureException(e);
                }

                Intent newIntent = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                getApplicationContext().sendBroadcast(newIntent);
                break;
            }
            case Constants.ACTION_DELETE_NOTIFICATION: {
                sendCancelEventAndRemovePendingTrigger(triggerData);
                break;
            }
        }
    }

    /**
     * Send Cancelled event to server and removes related {@link com.letscooee.room.trigger.PendingTrigger}
     * from the {@link com.letscooee.room.CooeeDatabase}
     *
     * @param triggerData {@link TriggerData} for whom operation to be perform
     */
    private void sendCancelEventAndRemovePendingTrigger(TriggerData triggerData) {
        if (triggerData == null) {
            return;
        }

        Event event = new Event(Constants.EVENT_NOTIFICATION_CANCELLED, triggerData);
        CooeeFactory.getSafeHTTPService().sendEventWithoutSession(event);
        CooeeFactory.getPendingTriggerService().delete(triggerData);
    }
}
