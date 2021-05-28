package com.letscooee.utils;

import android.content.Context;
import android.text.TextUtils;
import androidx.annotation.RestrictTo;
import org.bson.types.ObjectId;

import java.util.Date;

/**
 * Manages the sessionId
 *
 * @author Ashish Gaikwad on 22/05/21
 * @version 0.2.10
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class SessionManager {

    private static SessionManager instance;

    private final Context context;
    private final RuntimeData runtimeData;

    private String currentSessionID;
    private Integer currentSessionNumber;
    private Date currentSessionStartTime;

    private SessionManager(Context context) {
        this.context = context.getApplicationContext();
        this.runtimeData = RuntimeData.getInstance(this.context);
    }

    public static SessionManager getInstance(Context context) {
        if (instance == null) {
            synchronized (SentryHelper.class) {
                if (instance == null) {
                    instance = new SessionManager(context);
                }
            }
        }

        return instance;
    }

    public String getCurrentSessionID() {
        if (TextUtils.isEmpty(currentSessionID)) {
            startNewSession();
        }

        return currentSessionID;
    }

    private void startNewSession() {
        currentSessionStartTime = new Date();
        currentSessionID = new ObjectId().toHexString();

        bumpSessionNumber();
    }

    /**
     * When the app come back from background (B) to foreground (F) and if the user's ideal time
     * {@link CooeeSDKConstants.IDLE_TIME_IN_SECONDS} is elapsed; this method will return the duration (in seconds)
     * from the session start to the time app went to background.
     * <p>
     * Hence this method will throw an exception if the app is not coming from the backend,
     * instead it is just the app launch.
     *
     * @return Total session duration in seconds
     */
    public long getTotalSessionDurationInSeconds() {
        if (this.runtimeData.isFirstForeground()) {
            throw new IllegalStateException("This is the first time in foreground after launch");
        }

        return ((this.runtimeData.getLastEnterBackground().getTime() - this.currentSessionStartTime.getTime()) / 1000);
    }

    public Integer getCurrentSessionNumber() {
        return LocalStorageHelper.getInt(context, CooeeSDKConstants.STORAGE_SESSION_NUMBER, 0);
    }

    private void bumpSessionNumber() {
        currentSessionNumber = getCurrentSessionNumber();
        currentSessionNumber += 1;

        LocalStorageHelper.putInt(context, CooeeSDKConstants.STORAGE_SESSION_NUMBER, currentSessionNumber);
    }

    public void destroySession() {
        this.currentSessionID = null;
        this.currentSessionNumber = null;
        this.currentSessionStartTime = null;
    }
}
