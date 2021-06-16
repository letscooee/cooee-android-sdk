package com.letscooee.room.task;

import android.content.Context;
import android.util.Log;
import androidx.annotation.RestrictTo;
import com.google.gson.Gson;
import com.letscooee.ContextAware;
import com.letscooee.CooeeFactory;
import com.letscooee.models.Event;
import com.letscooee.room.CooeeDatabase;
import com.letscooee.room.task.processor.*;
import com.letscooee.schedular.CooeeJobUtils;
import com.letscooee.schedular.job.PendingTaskJob;
import com.letscooee.utils.Constants;
import com.letscooee.utils.SentryHelper;
import com.letscooee.utils.Timer;

import java.util.*;

/**
 * A singleton service for utility over {@link PendingTask}.
 *
 * @author Shashank Agrawal
 * @since 0.3.0
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class PendingTaskService extends ContextAware {

    private static PendingTaskService INSTANCE;

    private static final ArrayList<PendingTaskProcessor> PROCESSORS = new ArrayList<>();
    private static final Set<Long> CURRENT_PROCESSING_TASKS = Collections.synchronizedSet(new HashSet<>());

    private final SentryHelper sentryHelper;
    private final CooeeDatabase database;
    private final Gson gson = new Gson();

    public static PendingTaskService getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (PendingTaskService.class) {
                if (INSTANCE == null) {
                    INSTANCE = new PendingTaskService(context);
                }
            }
        }

        return INSTANCE;
    }

    private PendingTaskService(Context context) {
        super(context);
        this.database = CooeeDatabase.getInstance(this.context);
        this.sentryHelper = CooeeFactory.getSentryHelper();
        this.instantiateProcessors(context);
    }

    private void instantiateProcessors(Context context) {
        PROCESSORS.add(new EventTaskProcessor(context));
        PROCESSORS.add(new ProfileTaskProcessor(context));
        PROCESSORS.add(new PushTokenTaskProcessor(context));
        PROCESSORS.add(new SessionConcludeTaskProcessor(context));
    }

    public PendingTask newTask(Event event) {
        return this.newTask(gson.toJson(event), PendingTaskType.API_SEND_EVENT);
    }

    public PendingTask newTask(Map<String, Object> data, PendingTaskType taskType) {
        String jsonData = gson.toJson(data);
        return this.newTask(jsonData, taskType);
    }

    /**
     * Create a new pending task to be processed later by {@link PendingTaskJob}
     * and {@link PendingTaskProcessor}.
     *
     * @param data     The raw JSON data to be stored for later processing.
     * @param taskType The type of pending task which can be processed by {@link PendingTaskProcessor}.
     * @return The created pending task.
     */
    public PendingTask newTask(String data, PendingTaskType taskType) {
        PendingTask task = new PendingTask();
        task.attempts = 0;
        task.data = data;
        task.type = taskType;
        task.dateCreated = new Date().getTime();

        task.id = this.database.pendingTaskDAO().insert(task);

        Log.v(Constants.LOG_PREFIX, "Created " + task);
        return task;
    }

    /**
     * Process the given list of {@link PendingTask} via {@link PendingTaskProcessor}.
     *
     * @param pendingTasks   The list of tasks.
     * @param pendingTaskJob instance of {@link PendingTaskJob}
     */
    public void processTasks(List<PendingTask> pendingTasks, PendingTaskJob pendingTaskJob) {
        if (pendingTasks == null || pendingTasks.isEmpty()) {
            return;
        }

        for (PendingTask pendingTask : pendingTasks) {
            this.processTask(pendingTask);
        }
        reScheduleJob(pendingTaskJob);
    }

    /**
     * Stops the current running job and reschedule job with the help of
     * {@link CooeeJobUtils}
     *
     * @param pendingTaskJob is instance of {@link PendingTaskJob}
     */
    private void reScheduleJob(PendingTaskJob pendingTaskJob) {
        pendingTaskJob.jobFinished(pendingTaskJob.getJobParameters(), false);

        // Add delay to let previous job get fully finished
        new Timer().schedule(() -> CooeeJobUtils.schedulePendingTaskJob(context), 2000);
    }

    /**
     * Process an individual {@link PendingTask}.
     *
     * @param pendingTask The task to process.
     */
    public void processTask(PendingTask pendingTask) {
        Log.d(Constants.LOG_PREFIX, "Attempt processing " + pendingTask);

        if (pendingTask == null) {
            throw new IllegalArgumentException("pendingTask can't be null");
        }

        if (CURRENT_PROCESSING_TASKS.contains(pendingTask.id)) {
            Log.d(Constants.LOG_PREFIX, "Already processing " + pendingTask);
            return;
        }

        CURRENT_PROCESSING_TASKS.add(pendingTask.id);

        for (PendingTaskProcessor taskProcessor : PROCESSORS) {
            if (taskProcessor.canProcess(pendingTask)) {

                try {
                    taskProcessor.process(pendingTask);
                } catch (Throwable t) {
                    this.sentryHelper.captureException(t);
                    // Suppress the exception to prevent app crash. It's already logged to Sentry
                } finally {
                    CURRENT_PROCESSING_TASKS.remove(pendingTask.id);
                }
            }
        }
    }
}
