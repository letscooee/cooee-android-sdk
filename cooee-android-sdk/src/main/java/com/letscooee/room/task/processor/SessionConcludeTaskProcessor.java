package com.letscooee.room.task.processor;

import android.content.Context;

import androidx.annotation.NonNull;

import com.letscooee.exceptions.HttpRequestFailedException;
import com.letscooee.room.task.PendingTask;
import com.letscooee.room.task.PendingTaskType;

import java.util.Map;

/**
 * Process a {@link PendingTask} which is the HTTP post call to server for concluding a session.
 *
 * @author Shashank Agarwal
 * @since 0.3.0
 */
public class SessionConcludeTaskProcessor extends HttpTaskProcessor<Map<String, Object>> {

    public SessionConcludeTaskProcessor(Context context) {
        super(context);
    }

    protected void doHTTP(Map<String, Object> data) throws HttpRequestFailedException {
        this.baseHTTPService.sendSessionConcludedEvent(data);
    }

    public boolean canProcess(@NonNull PendingTask task) {
        return task.type == PendingTaskType.API_SESSION_CONCLUDE;
    }
}
