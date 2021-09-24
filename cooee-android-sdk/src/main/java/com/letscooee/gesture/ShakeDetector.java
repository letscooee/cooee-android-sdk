package com.letscooee.gesture;

import static android.hardware.SensorManager.SENSOR_DELAY_GAME;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.letscooee.device.DebugInfoActivity;
import com.letscooee.utils.Closure;

/**
 * Detects when user shakes the phone and display {@link DebugInfoActivity}
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

    private Closure<Object> shakeCallback;
    private final int minShakeCount;

    private SensorManager sensorManager;
    private int runtimeShakeCount = 0;
    private float lastXPosition = 1;
    private float lastYPosition = 1;
    private float lastZPosition = 1;
    private long lastUpdateTime;
    private long lastForceTime;
    private long lastShakeTime;

    /**
     * Registers {@link SensorEventListener} with {@link Sensor#TYPE_ACCELEROMETER} to the
     * {@link SensorManager}
     *
     * @param activity      instance current activity
     * @param minShakeCount minimum shake count to trigger closer
     */
    public ShakeDetector(Activity activity, int minShakeCount) {
        this.minShakeCount = minShakeCount;

        if (this.minShakeCount <= 0) {
            return;
        }

        sensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SENSOR_DELAY_GAME);
    }

    /**
     * Register callback to get a response when shake gesture is detected
     *
     * @param shakeCallback instance of {@link Closure<Object>}
     */
    public void onShake(Closure<Object> shakeCallback) {
        this.shakeCallback = shakeCallback;
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
                if (++runtimeShakeCount >= minShakeCount && (currentTimeMillis - lastShakeTime > SHAKE_DURATION)) {
                    lastShakeTime = currentTimeMillis;
                    this.shakeCallback.call(null);
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
        if (sensorManager != null)
            sensorManager.unregisterListener(this);
    }
}
