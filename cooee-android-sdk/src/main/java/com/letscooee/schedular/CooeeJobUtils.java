package com.letscooee.schedular;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import androidx.annotation.RestrictTo;
import com.letscooee.CooeeFactory;
import com.letscooee.schedular.job.PendingTaskJob;
import com.letscooee.utils.Constants;

/**
 * Schedules {@link PendingTaskJob}.
 *
 * @author Ashish Gaikwad on 19/5/21
 * @version 0.2.10
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class CooeeJobUtils {

    private CooeeJobUtils() {
    }

    private static final Long PENDING_JOB_INTERVAL_MILLIS = (long) (2 * 60 * 1000);

    public static JobScheduler getJobScheduler(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            return context.getSystemService(JobScheduler.class);
        } else {
            return (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        }
    }

    /**
     * Used to check if a jobs with given <code>jobID</code> is already in queue or
     * in running state.
     *
     * @param context will be application context
     * @return true if job is already in queue
     */
    public static boolean isJobServiceOn(Context context, int jobID) {
        JobScheduler jobScheduler = CooeeJobUtils.getJobScheduler(context);

        boolean hasBeenScheduled = false;

        for (JobInfo jobInfo : jobScheduler.getAllPendingJobs()) {
            if (jobInfo.getId() == jobID) {
                hasBeenScheduled = true;
                break;
            }
        }

        return hasBeenScheduled;
    }

    /**
     * Schedule a job with Android Service.
     *
     * @param context       will be application context
     * @param clazz         The {@link JobService} class.
     * @param jobID         Unique id of the job.
     * @param latencyMillis Optional latency/delay for the job execution.
     */
    public static void scheduleJob(Context context, Class<? extends JobService> clazz, int jobID,
                                   Long latencyMillis) {
        ComponentName serviceComponent = new ComponentName(context, clazz);
        JobInfo.Builder builder = new JobInfo.Builder(jobID, serviceComponent);

        builder.setMinimumLatency(latencyMillis);

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N) {
            builder.setOverrideDeadline(latencyMillis);
        }

        getJobScheduler(context).schedule(builder.build());
    }

    /**
     * Run the {@link PendingTaskJob} immediately. This will also make sure two points-
     *
     * <ol>
     *     <li>This will update any scheduled same job to run immediately.</li>
     *     <li>If any instance of the same job is running, it will stop that.</li>
     * </ol>
     *
     * @param context the application context.
     */
    public static void triggerPendingTaskJobImmediately(Context context) {
        CooeeFactory.getLogger().verbose("Run PendingTaskJob immediately");
        scheduleJob(context, PendingTaskJob.class, Constants.PENDING_TASK_JOB_ID, 1L);
    }

    public static void schedulePendingTaskJob(Context context) {
        if (isJobServiceOn(context, Constants.PENDING_TASK_JOB_ID)) {
            return;
        }

        scheduleJob(context, PendingTaskJob.class, Constants.PENDING_TASK_JOB_ID,
                PENDING_JOB_INTERVAL_MILLIS);
    }

}
