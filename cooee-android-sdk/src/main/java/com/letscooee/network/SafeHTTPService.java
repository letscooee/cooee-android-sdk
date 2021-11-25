package com.letscooee.network;

import android.content.Context;
import android.text.TextUtils;

import com.letscooee.ContextAware;
import com.letscooee.models.Event;
import com.letscooee.room.task.PendingTask;
import com.letscooee.room.task.PendingTaskService;
import com.letscooee.room.task.PendingTaskType;
import com.letscooee.task.CooeeExecutors;
import com.letscooee.trigger.EngagementTriggerHelper;
import com.letscooee.user.SessionManager;
import com.letscooee.utils.RuntimeData;

import java.util.HashMap;
import java.util.Map;

/**
 * A safe HTTP service which saves the data in {@link com.letscooee.room.CooeeDatabase} before attempting
 * via {@link BaseHTTPService}. If the network call fails because of any reason, the {@link com.letscooee.schedular.job.PendingTaskJob}
 * will reattempt sending the data to the API.
 *
 * @author Shashank Agrawal
 * @since 0.3.0
 */
public class SafeHTTPService extends ContextAware {

    private final PendingTaskService pendingTaskService;
    private final SessionManager sessionManager;
    private final RuntimeData runtimeData;

    public SafeHTTPService(Context context, PendingTaskService pendingTaskService, SessionManager sessionManager,
                           RuntimeData runtimeData) {
        super(context);
        this.pendingTaskService = pendingTaskService;
        this.sessionManager = sessionManager;
        this.runtimeData = runtimeData;
    }

    /**
     * Send a new event and make sure that a session always exists.
     *
     * @param event The new event to be posted to the server safely.
     */
    public void sendEvent(Event event) {
        this.sendEvent(event, true);
    }

    /**
     * Send a new event and make sure if the session is not already created then do not create
     * a new session (skip session creation).
     * 0
     *
     * @param event
     */
    public void sendEventWithoutNewSession(Event event) {
        this.sendEvent(event, false);
    }

    private void sendEvent(Event event, boolean createSession) {
        String sessionID = sessionManager.getCurrentSessionID(createSession);

        if (!TextUtils.isEmpty(sessionID)) {
            event.setSessionID(sessionID);
            event.setSessionNumber(sessionManager.getCurrentSessionNumber());
        }

        event.setScreenName(runtimeData.getCurrentScreenName());
        event.setActiveTriggers(EngagementTriggerHelper.getActiveTriggers(context));

        PendingTask pendingTask = pendingTaskService.newTask(event);
        this.attemptTaskImmediately(pendingTask);
    }

    public void updateUserProfile(Map<String, Object> requestData) {
        requestData.put("sessionID", sessionManager.getCurrentSessionID());
        PendingTask pendingTask = pendingTaskService.newTask(requestData, PendingTaskType.API_UPDATE_PROFILE);
        this.attemptTaskImmediately(pendingTask);
    }

    public void updateDeviceProperty(Map<String, Object> requestData) {
        requestData.put("sessionID", sessionManager.getCurrentSessionID());
        PendingTask pendingTask = pendingTaskService.newTask(requestData, PendingTaskType.API_DEVICE_PROPERTY);
        this.attemptTaskImmediately(pendingTask);
    }

    public void sendSessionConcludedEvent(Map<String, Object> requestData) {
        PendingTask pendingTask = pendingTaskService.newTask(requestData, PendingTaskType.API_SESSION_CONCLUDE);
        this.attemptTaskImmediately(pendingTask);
    }

    public void updatePushToken(Map<String, Object> requestData) {
        PendingTask pendingTask = pendingTaskService.newTask(requestData, PendingTaskType.API_UPDATE_PUSH_TOKEN);
        this.attemptTaskImmediately(pendingTask);
    }

    /**
     * Executes the newly created {@code pendingTask} immediately. This newly task will be processed in a new
     * thread (outside the main thread) as the network calls are synchronous in {@link BaseHTTPService}.
     *
     * @param pendingTask Task to attempt execution.
     */
    private void attemptTaskImmediately(PendingTask pendingTask) {
        CooeeExecutors.getInstance().networkExecutor().execute(() -> {
            pendingTaskService.processTask(pendingTask);
        });
    }

    public void updateDeviceProps(Map<String, Object> userProperties) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("userProperties", userProperties);
        userMap.put("userData", new HashMap<>());

        updateUserProfile(userMap);
    }
}
