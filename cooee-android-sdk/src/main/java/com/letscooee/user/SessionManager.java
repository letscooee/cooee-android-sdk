package com.letscooee.user;

import android.content.Context;
import android.text.TextUtils;
import androidx.annotation.RestrictTo;
import com.letscooee.CooeeFactory;
import com.letscooee.models.Event;
import com.letscooee.utils.Constants;
import com.letscooee.utils.LocalStorageHelper;
import com.letscooee.utils.RuntimeData;
import com.letscooee.utils.Timer;
import org.bson.types.ObjectId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages the user's current session in the app.
 *
 * @author Ashish Gaikwad on 22/05/21
 * @version 0.2.10
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class SessionManager {

    private final Context context;
    private String currentSessionID;
    private Integer currentSessionNumber;
    @SuppressWarnings("unused")
    private Date currentSessionStartTime;
    private static SessionManager instance;
    private Runnable runnable;
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final RuntimeData runtimeData;
    private Timer timer = new Timer();

    private SessionManager(Context context) {
        this.context = context.getApplicationContext();
        this.runtimeData = RuntimeData.getInstance(this.context);
    }

    /**
     * Checks if stored session is expired (idle time already passed) or not.
     * if stored session is expired then conclude that session and return true.
     *
     * @return {@code true} if stored session is expired, Otherwise {@code false}.
     */
    public boolean checkSessionExpiry() {
        if (getSessionIdleTimeInSeconds() > Constants.IDLE_TIME_IN_SECONDS) {
            conclude();
            return true;
        }
        return false;
    }

    /**
     * Conclude the current session by sending an event to the server followed by
     * destroying it.
     */
    public void conclude() {
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("sessionID", this.getCurrentSessionID());
        requestData.put("occurred", new Date());

        // Remove active trigger after session is concluded
        LocalStorageHelper.remove(context, Constants.STORAGE_ACTIVE_TRIGGER);
        LocalStorageHelper.remove(context, Constants.STORAGE_ACTIVE_SESSION);
        LocalStorageHelper.remove(context, Constants.STORAGE_LAST_SESSION_USE_TIME);

        this.destroySession();
        CooeeFactory.getSafeHTTPService().sendSessionConcludedEvent(requestData);
    }

    /**
     * Destroy the current session.
     */
    public void destroySession() {
        this.currentSessionID = null;
        this.currentSessionNumber = null;
        this.currentSessionStartTime = null;
    }

    /**
     * Create a new session and always make sure that a session is created if not already started.
     *
     * @return The current session id.
     */
    public synchronized String getCurrentSessionID() {
        return this.getCurrentSessionID(true);
    }

    /**
     * Create a new session.
     *
     * @param createNew If a session does not exists and <code>createNew</code> is <true></true>,
     *                  then create a new session.
     * @return The current or new session id.
     */
    public synchronized String getCurrentSessionID(boolean createNew) {
        currentSessionID = LocalStorageHelper.getString(context, Constants.STORAGE_ACTIVE_SESSION, null);
        currentSessionNumber = LocalStorageHelper.getInt(context, Constants.STORAGE_SESSION_NUMBER, 0);

        if (createNew) {
            startNewSession();
        }

        LocalStorageHelper.putDate(context, Constants.STORAGE_LAST_SESSION_USE_TIME, new Date());

        return currentSessionID;
    }

    /**
     * Return the current session number.
     *
     * @return The current session number.
     */
    public Integer getCurrentSessionNumber() {
        return this.currentSessionNumber;
    }

    public static SessionManager getInstance(Context context) {
        //noinspection DoubleCheckedLocking
        if (instance == null) {
            synchronized (SessionManager.class) {
                if (instance == null) {
                    instance = new SessionManager(context);
                }
            }
        }

        return instance;
    }

    /**
     * Calculate the time difference between the current time and last session used time (i.e. idle time).
     * If last session used time is not available, then return 0.
     *
     * @return The time difference in seconds.
     */
    public long getSessionIdleTimeInSeconds() {
        Date lastSessionUseTime = LocalStorageHelper.getDate(context, Constants.STORAGE_LAST_SESSION_USE_TIME, null);
        if (lastSessionUseTime == null) {
            return 0;
        }

        return (new Date().getTime() - lastSessionUseTime.getTime()) / 1000;
    }

    /**
     * Check if  {@code timer} was stopped previously and start keep alive loop.
     */
    public void keepSessionAlive() {
        if (timer.isShutdown()) {
            timer = new Timer();
        }

        timer.schedule(runnable = () -> {
            timer.schedule(runnable, Constants.KEEP_ALIVE_TIME_IN_MS);
            this.pingServerToKeepAlive();
        }, Constants.KEEP_ALIVE_TIME_IN_MS);
    }

    /**
     * Send a beacon to backend server for keeping the session alive.
     */
    public void pingServerToKeepAlive() {
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("sessionID", this.getCurrentSessionID());

        CooeeFactory.getBaseHTTPService().keepAliveSession(requestData);
    }

    /**
     * Start a new session only if {@code currentSessionID} is empty.
     */
    public void startNewSession() {
        if (!TextUtils.isEmpty(currentSessionID)) {
            return;
        }

        currentSessionStartTime = new Date();
        currentSessionID = new ObjectId().toHexString();
        LocalStorageHelper.putString(context, Constants.STORAGE_ACTIVE_SESSION, currentSessionID);

        bumpSessionNumber();
    }

    /**
     * Stop the timer.
     */
    public void stopSessionAlive() {
        timer.stop();
    }

    /**
     * Bump the session number by 1.
     */
    private void bumpSessionNumber() {
        currentSessionNumber = LocalStorageHelper.getInt(context, Constants.STORAGE_SESSION_NUMBER, 0);
        currentSessionNumber += 1;

        LocalStorageHelper.putInt(context, Constants.STORAGE_SESSION_NUMBER, currentSessionNumber);
        this.sendSessionStarted();
    }

    /**
     * Send a session start event
     */
    private void sendSessionStarted() {
        CooeeFactory.getSafeHTTPService().sendEvent(new Event(Constants.EVENT_SESSION_STARTED));
    }

}
