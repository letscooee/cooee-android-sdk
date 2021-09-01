package com.letscooee.gesture;

import static android.hardware.SensorManager.SENSOR_DELAY_GAME;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.letscooee.device.CooeeDebugInfoActivity;

/**
 * Detects when user shakes the phone and display {@link CooeeDebugInfoActivity}
 * ref. http://android.hlidskialf.com/blog/code/android-shake-detection-listener
 *
 * @author Ashish Gaikwad 01/09/21
 * @since 1.0.0
 */
public class ShakeDetector implements SensorEventListener {

    private static final int SHAKE_THRESHOLD = 350;
    private static final int TIME_THRESHOLD = 100;
    private static final int SHAKE_TIMEOUT = 500;
    private static final int SHAKE_DURATION = 1500;

    private final Activity activity;

    private SensorManager sensorManager;
    private int shakeCount = 3;
    private int runtimeShakeCount = 0;
    private float lastXPosition = 1;
    private float lastYPosition = 1;
    private float lastZPosition = 1;
    private long lastUpdateTime;
    private long lastForceTime;
    private long lastShakeTime;

    public ShakeDetector(Activity activity) {
        this.activity = activity;
    }

    /**
     * Registers {@link SensorEventListener} with {@link Sensor#TYPE_ACCELEROMETER} to the
     * {@link SensorManager}
     *
     * @param shakeCount int value; If value less than equals to zero {@link SensorEventListener}
     *                   will not register.
     */
    public void setShakeCount(int shakeCount) {
        this.shakeCount = shakeCount;
        if (shakeCount <= 0) {
            return;
        }
        sensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SENSOR_DELAY_GAME);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        long currentTimeMillis = System.currentTimeMillis();

        if ((currentTimeMillis - lastForceTime) > SHAKE_TIMEOUT) {
            runtimeShakeCount = 0;
        }

        if ((currentTimeMillis - lastUpdateTime) > TIME_THRESHOLD) {
            long timeDifference = (currentTimeMillis - lastUpdateTime);
            float currentX = event.values[0];
            float currentY = event.values[1];
            float currentZ = event.values[2];
            float shakeSpeed = Math.abs(currentX + currentY + currentZ - lastXPosition - lastYPosition - lastZPosition) / timeDifference * 10000;

            if (shakeSpeed > SHAKE_THRESHOLD) {
                if (++runtimeShakeCount >= shakeCount && (currentTimeMillis - lastShakeTime > SHAKE_DURATION)) {
                    lastShakeTime = currentTimeMillis;
                    Intent intent = new Intent(activity, CooeeDebugInfoActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    activity.startActivity(intent);
                }
                lastForceTime = currentTimeMillis;
            }

            lastUpdateTime = currentTimeMillis;
            lastXPosition = currentX;
            lastYPosition = currentY;
            lastZPosition = currentZ;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    /**
     * Unregister {@link SensorEventListener} from {@link com.letscooee.user.SessionManager}
     */
    public void unregisterListener() {
        sensorManager.unregisterListener(this);
    }
}
