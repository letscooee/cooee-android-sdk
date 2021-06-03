package com.letscooee.schedular;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import com.letscooee.schedular.job.PendingTaskJob;
import com.letscooee.utils.CooeeSDKConstants;

/**
 * Schedules {@link PendingTaskJob}.
 *
 * @author Ashish Gaikwad on 19/5/21
 * @version 0.2.10
 */
public class CooeeJobUtils {

    public static JobScheduler getJobScheduler(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            return context.getSystemService(JobScheduler.class);
        } else {
            return (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        }
    }

    /**
     * Used to check if similar job is already in system queue.
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
     * @param context will be application context
     */
    public static void scheduleJob(Context context, Class<? extends JobService> clazz, int jobID) {
        ComponentName serviceComponent = new ComponentName(context, clazz);
        JobInfo.Builder builder = new JobInfo.Builder(jobID, serviceComponent);
        builder.setMinimumLatency(120 * 1000);

        getJobScheduler(context).schedule(builder.build());
    }

    public static void schedulePendingTaskJob(Context context) {
        // TODO: 03/06/21 Do we really need to check if the job is running or not
        if (isJobServiceOn(context, CooeeSDKConstants.PENDING_TASK_JOB_ID)) {
            return;
        }

        scheduleJob(context, PendingTaskJob.class, CooeeSDKConstants.PENDING_TASK_JOB_ID);
    }
}
