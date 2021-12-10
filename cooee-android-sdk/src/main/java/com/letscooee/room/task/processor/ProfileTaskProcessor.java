package com.letscooee.room.task.processor;

import android.content.Context;

import androidx.annotation.NonNull;

import com.letscooee.exceptions.HttpRequestFailedException;
import com.letscooee.room.task.PendingTask;
import com.letscooee.room.task.PendingTaskType;

import java.util.Map;

/**
 * Process a {@link PendingTask} which is related to updating user properties to backend API.
 *
 * @author Shashank Agarwal
 * @since 0.3.0
 */
public class ProfileTaskProcessor extends HttpTaskProcessor<Map<String, Object>> {

    public ProfileTaskProcessor(Context context) {
        super(context);
    }

    protected void doHTTP(Map<String, Object> data) throws HttpRequestFailedException {
        this.baseHTTPService.updateUserProfile(data);
    }

    public boolean canProcess(@NonNull PendingTask task) {
        return task.type == PendingTaskType.API_UPDATE_PROFILE;
    }
}
