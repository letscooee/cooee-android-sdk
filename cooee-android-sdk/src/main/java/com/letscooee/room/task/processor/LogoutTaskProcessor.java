package com.letscooee.room.task.processor;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import com.letscooee.exceptions.HttpRequestFailedException;
import com.letscooee.room.task.PendingTask;
import com.letscooee.room.task.PendingTaskType;

/**
 * An layer to process the {@link PendingTask} related {@link PendingTaskType#API_LOGOUT}
 * with some common useful methods.
 *
 * @author Ashish Gaikwad
 * @since 1.4.2
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class LogoutTaskProcessor extends HttpTaskProcessor<String> {


    public LogoutTaskProcessor(Context context) {
        super(context);
    }

    @Override
    protected void doHTTP(String data) throws HttpRequestFailedException {
        this.baseHTTPService.logout();
    }

    @Override
    public boolean canProcess(@NonNull PendingTask task) {
        return task.type == PendingTaskType.API_LOGOUT;
    }

}
