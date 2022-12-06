package com.letscooee.room.task;

import android.content.Context;
import androidx.annotation.RestrictTo;
import com.google.gson.Gson;
import com.letscooee.BuildConfig;
import com.letscooee.ContextAware;
import com.letscooee.CooeeFactory;
import com.letscooee.models.Event;
import com.letscooee.room.CooeeDatabase;
import com.letscooee.room.task.processor.DevicePropTaskProcessor;
import com.letscooee.room.task.processor.EventTaskProcessor;
import com.letscooee.room.task.processor.LogoutTaskProcessor;
import com.letscooee.room.task.processor.PendingTaskProcessor;
import com.letscooee.room.task.processor.ProfileTaskProcessor;
import com.letscooee.room.task.processor.PushTokenTaskProcessor;
import com.letscooee.room.task.processor.SessionConcludeTaskProcessor;
import com.letscooee.schedular.CooeeJobUtils;
import com.letscooee.schedular.job.PendingTaskJob;
import com.letscooee.utils.Logger;
import com.letscooee.utils.SentryHelper;
import com.letscooee.utils.Timer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A singleton service for utility over {@link PendingTask}.
 *
 * @author Shashank Agrawal
 * @since 0.3.0
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class PendingTaskService extends ContextAware {

    private static final ArrayList<PendingTaskProcessor> PROCESSORS = new ArrayList<>();
    private static final Set<Long> CURRENT_PROCESSING_TASKS = Collections.synchronizedSet(new HashSet<>());

    private final SentryHelper sentryHelper;
    private final CooeeDatabase database;
    private final Gson gson = new Gson();
    private final Logger logger;

    public PendingTaskService(Context context, SentryHelper sentryHelper) {
        super(context);
        this.database = CooeeDatabase.getInstance(this.context);
        this.sentryHelper = sentryHelper;
        this.logger = CooeeFactory.getLogger();
        this.instantiateProcessors(context);
    }

    private void instantiateProcessors(Context context) {
        PROCESSORS.add(new EventTaskProcessor(context));
        PROCESSORS.add(new ProfileTaskProcessor(context));
        PROCESSORS.add(new PushTokenTaskProcessor(context));
        PROCESSORS.add(new SessionConcludeTaskProcessor(context));
        PROCESSORS.add(new DevicePropTaskProcessor(context));
        PROCESSORS.add(new LogoutTaskProcessor(context));
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
        task.sdkCode = BuildConfig.VERSION_CODE;

        task.id = this.database.pendingTaskDAO().insert(task);

        logger.verbose("Created " + task);
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
        logger.debug("Attempt processing " + pendingTask);

        if (pendingTask == null) {
            throw new IllegalArgumentException("pendingTask can't be null");
        }

        if (CURRENT_PROCESSING_TASKS.contains(pendingTask.id)) {
            logger.debug("Already processing " + pendingTask);
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
