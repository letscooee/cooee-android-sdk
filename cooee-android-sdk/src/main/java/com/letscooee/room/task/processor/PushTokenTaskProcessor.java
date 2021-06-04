package com.letscooee.room.task.processor;

import android.content.Context;
import com.letscooee.exceptions.HttpRequestFailedException;
import com.letscooee.room.task.PendingTask;
import com.letscooee.room.task.PendingTaskType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Process a {@link PendingTask} which is related to update the push notification token to the server.
 *
 * @author Shashank Agarwal
 * @since 0.3.0
 */
public class PushTokenTaskProcessor extends HttpTaskProcessor<Map<String, Object>> {

    public PushTokenTaskProcessor(Context context) {
        super(context);
    }

    protected void doHTTP(Map<String, Object> data) throws HttpRequestFailedException {
        this.baseHTTPService.updatePushToken(data);
    }

    public boolean canProcess(@NotNull PendingTask task) {
        return task.type == PendingTaskType.API_UPDATE_PUSH_TOKEN;
    }
}
