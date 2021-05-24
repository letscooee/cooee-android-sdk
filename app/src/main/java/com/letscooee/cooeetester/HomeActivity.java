package com.letscooee.cooeetester;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.letscooee.CooeeSDK;
import com.letscooee.brodcast.OnPushNotificationButtonClick;
import com.letscooee.cooeetester.databinding.ActivityHomeBinding;
import com.letscooee.utils.CooeeSDKConstants;
import com.letscooee.utils.InAppNotificationClickListener;
import com.letscooee.utils.PropertyNameException;

import java.util.Date;
import java.util.HashMap;

import io.sentry.Sentry;

public class HomeActivity extends AppCompatActivity implements InAppNotificationClickListener {
    private CooeeSDK cooeeSDK;
    private final String TAG = "HomeActivity";
    private ActivityHomeBinding binding;
    private Context context;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        context = this;
        cooeeSDK = CooeeSDK.getDefaultInstance(this);
        cooeeSDK.setInAppNotificationButtonListener(this);

        new CountDownTimer(1000, 3000) {
            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {

                binding.ivSplash.setVisibility(View.GONE);
            }
        }.start();

        try {
            cooeeSDK.setCurrentScreen(TAG);

        } catch (Exception ignored) {
        }

        try {
            cooeeSDK.sendEvent("onCreate", new HashMap<>());
        } catch (Exception ignored) {
        }

        try {
            Log.d(TAG, "************************************UUID : " + cooeeSDK.getUUID());
        } catch (Exception ignored) {
        }

        binding.btnSendImageEvent.setOnClickListener(view -> {
            try {
                cooeeSDK.sendEvent("image", new HashMap<>());
                Log.d(TAG, "****************************Image Event Sent");
            } catch (Exception e) {
                Log.e(TAG, "******************************Failed Image Event", e);
            }
        });

        binding.btnSendVideoEvent.setOnClickListener(view -> {
            try {
                cooeeSDK.sendEvent("video", new HashMap<>());
                Log.d(TAG, "****************************Video Event Sent");
            } catch (PropertyNameException e) {
                Log.e(TAG, "******************************Failed Video Event", e);
                e.printStackTrace();
            }
        });
        binding.btnProfile.setOnClickListener(view -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });
        binding.btnSendNotification.setOnClickListener(v -> {
            String title = "getNotificationTitle(triggerData)";
            String body = "getNotificationBody(triggerData)";
            int[] images = {R.drawable.banner, R.drawable.homepage, R.drawable.logo, R.drawable.blur_image, R.drawable.circle_transparent};
            if (title == null) {
                return;
            }

            NotificationManager notificationManager = (NotificationManager) getApplicationContext()
                    .getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel notificationChannel = new NotificationChannel(
                        CooeeSDKConstants.NOTIFICATION_CHANNEL_ID,
                        CooeeSDKConstants.NOTIFICATION_CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_DEFAULT);

                notificationChannel.setDescription("");
                notificationManager.createNotificationChannel(notificationChannel);
            }

            int notificationId = (int) new Date().getTime();
            RemoteViews smallNotification = new RemoteViews(getPackageName(), R.layout.notification_small);
            smallNotification.setTextViewText(R.id.textViewTitle, title);
            smallNotification.setTextViewText(R.id.textViewInfo, body);

            RemoteViews largeNotification = new RemoteViews(getPackageName(), R.layout.notification_large);
            largeNotification.setTextViewText(R.id.textViewTitle, title);
            largeNotification.setTextViewText(R.id.textViewInfo, body);

            RemoteViews views = new RemoteViews(getPackageName(), R.layout.notification_carousel);
            views.setTextViewText(R.id.textViewTitle, title);
            views.setTextViewText(R.id.textViewInfo, body);

            Intent rightScrollIntent = new Intent(this, OnPushNotificationButtonClick.class);
            rightScrollIntent.putExtra("EVENT", "right");
            rightScrollIntent.putExtra("POSITION", 1);
            rightScrollIntent.putExtra("notificationId", notificationId);
            rightScrollIntent.putExtra("TRIGGERDATA", "new TriggerData()");
            rightScrollIntent.putExtra("TYPE", "CAROUSEL");

            Intent leftScrollIntent = new Intent(this, OnPushNotificationButtonClick.class);
            leftScrollIntent.putExtra("EVENT", "left");
            leftScrollIntent.putExtra("POSITION", 1);
            leftScrollIntent.putExtra("notificationId", notificationId);
            leftScrollIntent.putExtra("TRIGGERDATA", "new TriggerData()");
            leftScrollIntent.putExtra("TYPE", "CAROUSEL");

            PendingIntent pendingIntentLeft = PendingIntent.getBroadcast(
                    getApplicationContext(),
                    1,
                    leftScrollIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            PendingIntent pendingIntentRight = PendingIntent.getBroadcast(
                    getApplicationContext(),
                    0,
                    rightScrollIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);


            views.setOnClickPendingIntent(R.id.left, pendingIntentLeft);
            views.setOnClickPendingIntent(R.id.right, pendingIntentRight);
            views.setViewVisibility(R.id.left, View.INVISIBLE);
            for (int i = 0; i < 5; i++) {
                RemoteViews image = new RemoteViews(getPackageName(), R.layout.row_notification_list);
                /*image.setImageViewBitmap(R.id.caroselImage, BitmapFactory.decodeResource(context.getResources(),
                        images[i]));*/
                image.setTextViewText(com.letscooee.R.id.position, "" + (i + 1));

                views.addView(R.id.lvNotificationList, image);
            }


            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
                    getApplicationContext(),
                    CooeeSDKConstants.NOTIFICATION_CHANNEL_ID);
            //notificationBuilder = addAction(notificationBuilder, createActionButtons(triggerData, notificationId));

                    /*smallNotification.setImageViewBitmap(R.id.imageViewLarge, resource);
                    largeNotification.setImageViewBitmap(R.id.imageViewLarge, resource);*/
            notificationBuilder.setAutoCancel(false)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(getApplicationInfo().icon)
                    .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                    .setCustomContentView(smallNotification)
                    .setCustomBigContentView(views)
                    .setContentTitle(title)
                    .setContentText(body);


            /*Intent deleteIntent = new Intent(getApplicationContext(), CooeeIntentService.class);
            deleteIntent.setAction("Notification Deleted");*/

            Notification notification = notificationBuilder.build();
            /*notification.deleteIntent = PendingIntent.getService(
                    getApplicationContext(),
                    0,
                    deleteIntent,
                    PendingIntent.FLAG_ONE_SHOT);*/
            notificationManager.notify(notificationId, notification);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                StatusBarNotification[] statusBarNotifications = notificationManager.getActiveNotifications();
                for (StatusBarNotification statusBarNotification : statusBarNotifications) {
                    if (statusBarNotification.getId() == notificationId) {
                        //sendEvent(getApplicationContext(), new Event("CE Notification Viewed", new HashMap<>()));
                    }
                }
            }
                /*}

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {

                }
            });*/
        });
        binding.tvUid.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("label", cooeeSDK.getUUID());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onDestroy() {
        try {
            cooeeSDK.sendEvent("onDestroy", new HashMap<>());
        } catch (Exception ignored) {
        }

        super.onDestroy();
    }

    @Override
    public void onInAppButtonClick(HashMap<String, Object> hashMap) {
        Log.d(TAG, "onInAppButtonClick: ************************************" + hashMap.toString());
        for (String key : hashMap.keySet()) {
            Log.d("Type ::", key + " -> " + hashMap.get(key).getClass().getName());
        }
    }
}