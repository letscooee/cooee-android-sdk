package com.letscooee.room.task;

import android.content.Context;
import androidx.annotation.RestrictTo;
import com.google.gson.Gson;
import com.letscooee.ContextAware;
import com.letscooee.models.Event;
import com.letscooee.room.CooeeDatabase;
import com.letscooee.utils.SentryHelper;

import java.util.Date;
import java.util.Map;

/**
 * A singleton service for utility over {@link PendingTask}.
 *
 * @author Shashank Agrawal
 * @since 0.3.0
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class PendingTaskService extends ContextAware<PendingTaskService> {

    private static PendingTaskService INSTANCE;

    private final CooeeDatabase database;
    private final Gson gson = new Gson();

    public static PendingTaskService getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (SentryHelper.class) {
                if (INSTANCE == null) {
                    INSTANCE = new PendingTaskService(context);
                }
            }
        }

        return INSTANCE;
    }

    private PendingTaskService(Context context) {
        super(context);
        this.database = CooeeDatabase.getInstance(context);
    }

    public PendingTask newTask(Event event) {
        return this.newTask(gson.toJson(event), PendingTaskType.API_PUSH_EVENT);
    }

    public PendingTask newTask(String data, PendingTaskType taskType) {
        PendingTask task = new PendingTask();
        task.attempts = 0;
        task.data = gson.toJson(data);
        task.type = taskType;
        task.dateCreated = new Date().getTime();

        this.database.pendingTaskDAO().insertAll(task);

        return task;
    }
}
