package com.letscooee.user;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.RestrictTo;

import com.letscooee.CooeeFactory;
import com.letscooee.utils.Constants;
import com.letscooee.utils.LocalStorageHelper;
import com.letscooee.utils.RuntimeData;

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
            synchronized (SessionManager.class) {
                if (instance == null) {
                    instance = new SessionManager(context);
                }
            }
        }

        return instance;
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
        if (createNew) {
            startNewSession();
        }

        LocalStorageHelper.putDate(context, Constants.STORAGE_LAST_SESSION_USE_TIME, new Date());

        return currentSessionID;
    }

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
     * Conclude the current session by sending an event to the server followed by
     * destroying it.
     */
    public void conclude() {
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("sessionID", this.getCurrentSessionID());
        requestData.put("occurred", new Date());

        // TODO Confirm this when session is concluded after 30 min irrespective of app launch
        // Remove active trigger after session is concluded
        LocalStorageHelper.remove(context, Constants.STORAGE_ACTIVE_SESSION);
        LocalStorageHelper.remove(context, Constants.STORAGE_LAST_SESSION_USE_TIME);

        this.destroySession();
        CooeeFactory.getSafeHTTPService().sendSessionConcludedEvent(requestData);
    }

    public Integer getCurrentSessionNumber() {
        return this.currentSessionNumber;
    }

    private void bumpSessionNumber() {
        currentSessionNumber = LocalStorageHelper.getInt(context, Constants.STORAGE_SESSION_NUMBER, 0);
        currentSessionNumber += 1;

        LocalStorageHelper.putInt(context, Constants.STORAGE_SESSION_NUMBER, currentSessionNumber);
    }

    public void destroySession() {
        this.currentSessionID = null;
        this.currentSessionNumber = null;
        this.currentSessionStartTime = null;
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
     * Calculate the time difference between the current time and last session used time.
     * If last session used time is not available, then return 0.
     *
     * @return The time difference in seconds.
     */
    public long getLastSessionUsedDifferenceInSeconds() {
        Date lastSessionUseTime = LocalStorageHelper.getDate(context, Constants.STORAGE_LAST_SESSION_USE_TIME, null);

        return lastSessionUseTime != null ? (new Date().getTime() - lastSessionUseTime.getTime()) / 1000 : 0;
    }

    public boolean checkSessionExpiry() {
        if (getLastSessionUsedDifferenceInSeconds() > Constants.IDLE_TIME_IN_SECONDS) {
            conclude();
            return true;
        }
        return false;
    }
}
