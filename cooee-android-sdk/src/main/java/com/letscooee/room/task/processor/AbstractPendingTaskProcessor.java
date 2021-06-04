package com.letscooee.room.task.processor;

import android.content.Context;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.letscooee.room.CooeeDatabase;
import com.letscooee.room.task.PendingTask;
import com.letscooee.utils.Constants;

import java.util.Date;

/**
 * An abstract layer to process the {@link PendingTask} with some common useful methods.
 *
 * @author Shashank Agrawal
 * @since 0.3.0
 */
public abstract class AbstractPendingTaskProcessor<T> implements PendingTaskProcessor {

    private final CooeeDatabase appDatabase;

    protected final Gson gson = new Gson();
    protected final Context context;

    protected AbstractPendingTaskProcessor(Context context) {
        this.context = context;
        this.appDatabase = CooeeDatabase.getInstance(context);
    }

    /**
     * Deserialize the {@link PendingTask#data} to the Java Object of Type {@link T}
     *
     * @param task The pending task to deserialize.
     * @return Deserialized Java object of given type {@link T}.
     */
    T deserialize(PendingTask task) throws JsonParseException {
        return gson.fromJson(task.data, new TypeToken<T>() {
        }.getType());
    }

    /**
     * Delete the given task which was successfully executed/completed.
     *
     * @param task Task to delete.
     */
    void deleteTask(PendingTask task) {
        Log.v(Constants.LOG_PREFIX, "Deleting " + task);
        this.appDatabase.pendingTaskDAO().delete(task);
    }

    /**
     * If a task execution fails, update it {@link PendingTask#attempts} & {@link PendingTask#lastAttempted}.
     *
     * @param task Task to update.
     */
    void updateAttempted(PendingTask task) {
        task.attempts = task.attempts + 1;
        task.lastAttempted = new Date().getTime();
        appDatabase.pendingTaskDAO().updateByObject(task);

        Log.v(Constants.LOG_PREFIX, "" + task + " attempted " + task.attempts);
    }
}
