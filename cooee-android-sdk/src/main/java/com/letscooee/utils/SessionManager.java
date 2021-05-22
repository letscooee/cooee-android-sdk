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
    private String currentSessionId;
    private int currentSessionNumber;
    private Date lastUsed;
    private Context context;
    private static SessionManager sessionManager;

    private SessionManager(Context context) {
        this.context = context;
    }

    public static SessionManager getInstance(Context context) {
        if (sessionManager == null) {
            sessionManager = new SessionManager(context);
        }
        return sessionManager;
    }

    public String getCurrentSessionId() {
        lastUsed = new Date();
        if (TextUtils.isEmpty(currentSessionId)) {
            createSessionId();
        }
        return currentSessionId;
    }

    private void createSessionId() {
        currentSessionId = new ObjectId().toHexString();
        bumpSessionNumber();
    }

    public int getCurrentSessionNumber() {
        return LocalStorageHelper.getInt(context, CooeeSDKConstants.STORAGE_SESSION_NUMBER, 0);
    }

    public void bumpSessionNumber() {
        int sessionNumber = getCurrentSessionNumber();
        sessionNumber += 1;
        LocalStorageHelper.putInt(context, CooeeSDKConstants.STORAGE_SESSION_NUMBER, sessionNumber);
    }

    public void destroySession() {
        this.currentSessionId = null;
    }
}
