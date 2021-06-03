package com.letscooee.user;

import android.content.Context;
import android.text.TextUtils;
import androidx.annotation.RestrictTo;
import com.letscooee.CooeeFactory;
import com.letscooee.utils.CooeeSDKConstants;
import com.letscooee.utils.LocalStorageHelper;
import com.letscooee.utils.RuntimeData;
import com.letscooee.utils.SentryHelper;
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
    public long getTotalDurationInSeconds() {
        if (this.runtimeData.isFirstForeground()) {
            throw new IllegalStateException("This is the first time in foreground after launch");
        }

        return ((this.runtimeData.getLastEnterBackground().getTime() - this.currentSessionStartTime.getTime()) / 1000);
    }

    /**
     * Conclude the current session by sending an event to the server followed by
     * destroying it.
     */
    public void conclude() {
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("sessionID", this.getCurrentSessionID());
        requestData.put("occurred", new Date());
        requestData.put("duration", this.getTotalDurationInSeconds());

        CooeeFactory.getSafeHTTPService().sendSessionConcludedEvent(requestData);
        this.destroySession();
    }

    public Integer getCurrentSessionNumber() {
        return this.currentSessionNumber;
    }

    private void bumpSessionNumber() {
        currentSessionNumber = LocalStorageHelper.getInt(context, CooeeSDKConstants.STORAGE_SESSION_NUMBER, 0);
        currentSessionNumber += 1;

        LocalStorageHelper.putInt(context, CooeeSDKConstants.STORAGE_SESSION_NUMBER, currentSessionNumber);
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
}
