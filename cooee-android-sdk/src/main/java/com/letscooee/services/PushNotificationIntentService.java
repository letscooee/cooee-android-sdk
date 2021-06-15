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
import com.letscooee.models.TriggerData;
import com.letscooee.utils.Constants;

import java.util.HashMap;

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

        switch (intent.getAction()) {
            case Constants.ACTION_PUSH_BUTTON_CLICK: {
                TriggerData triggerData = intent.getParcelableExtra("triggerData");
                int notificationId = intent.getIntExtra("notificationId", 0);

                NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
                notificationManager.cancel(notificationId);

                PackageManager packageManager = getPackageManager();
                Intent appLaunchIntent = packageManager.getLaunchIntentForPackage(getApplicationContext().getPackageName());
                appLaunchIntent.putExtra("triggerData", triggerData);

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
                Event event = new Event("CE Notification Cancelled");
                CooeeFactory.getSafeHTTPService().sendEventWithoutNewSession(event);
                break;
            }
        }
    }
}
