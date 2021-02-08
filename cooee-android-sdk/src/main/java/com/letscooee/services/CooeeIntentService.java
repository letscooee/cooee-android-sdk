package com.letscooee.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.annotation.Nullable;

import com.letscooee.models.TriggerData;

/**
 * @author Abhishek Taparia
 */
public class CooeeIntentService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public CooeeIntentService() {
        super("CooeeServiceIntent");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        switch (intent.getAction()) {
            case "Notification": {
                TriggerData triggerData = intent.getParcelableExtra("triggerData");
                int i = intent.getIntExtra("buttonCount", -1);
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
                } catch (PendingIntent.CanceledException ignored) {
                }


                Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                getApplicationContext().sendBroadcast(it);
                break;
            }
        }
    }
}
