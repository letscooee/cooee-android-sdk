package com.letscooee.network;

import android.content.Context;
import com.letscooee.ContextAware;
import com.letscooee.models.Event;
import com.letscooee.room.task.PendingTask;
import com.letscooee.room.task.PendingTaskService;
import com.letscooee.room.task.PendingTaskType;
import com.letscooee.trigger.EngagementTriggerHelper;
import com.letscooee.user.SessionManager;
import com.letscooee.utils.RuntimeData;
import com.letscooee.utils.SentryHelper;

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

    public SafeHTTPService(Context context) {
        super(context);
        this.pendingTaskService = PendingTaskService.getInstance(context);
        this.sessionManager = SessionManager.getInstance(context);
        this.runtimeData = RuntimeData.getInstance(context);
    }

    public void sendEvent(Event event) {
        event.setSessionID(sessionManager.getCurrentSessionID());
        event.setScreenName(runtimeData.getCurrentScreenName());
        event.setSessionNumber(sessionManager.getCurrentSessionNumber());
        event.setActiveTriggers(EngagementTriggerHelper.getActiveTriggers(context));

        // TODO: 03/06/21 Make it pass to each header
        Map<String, Object> properties = event.getProperties();
        if (SentryHelper.getInstance(context).isAppInDebugMode()) {
            properties.put("appDebug", 1);
        }

        PendingTask pendingTask = pendingTaskService.newTask(event);
        // TODO: 03/06/21 Process in a different thread
        pendingTaskService.processTask(pendingTask);
    }

    public void updateUserProfile(Map<String, Object> requestData) {
        requestData.put("sessionID", sessionManager.getCurrentSessionID());
        PendingTask pendingTask = pendingTaskService.newTask(requestData, PendingTaskType.API_UPDATE_PROFILE);
        pendingTaskService.processTask(pendingTask);
    }

    public void sendSessionConcludedEvent(Map<String, Object> requestData) {
        PendingTask pendingTask = pendingTaskService.newTask(requestData, PendingTaskType.API_SESSION_CONCLUDE);
        pendingTaskService.processTask(pendingTask);
    }

    public void updatePushToken(Map<String, Object> requestData) {
        PendingTask pendingTask = pendingTaskService.newTask(requestData, PendingTaskType.API_UPDATE_PUSH_TOKEN);
        pendingTaskService.processTask(pendingTask);
    }
}
