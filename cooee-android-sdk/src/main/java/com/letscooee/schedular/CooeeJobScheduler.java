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
public class CooeeJobScheduler {

    /**
     * Schedule a job with Android Service.
     *
     * @param context will be application context
     */
    public static void scheduleJob(Context context, Class<? extends JobService> clazz, int jobID) {
        ComponentName serviceComponent = new ComponentName(context, clazz);
        JobInfo.Builder builder = new JobInfo.Builder(jobID, serviceComponent);
        builder.setMinimumLatency(120 * 1000);

        JobScheduler jobScheduler;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            jobScheduler = context.getSystemService(JobScheduler.class);
        } else {
            jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        }

        jobScheduler.schedule(builder.build());
    }

    public static void schedulePendingTaskJob(Context context) {
        scheduleJob(context, PendingTaskJob.class, CooeeSDKConstants.PENDING_TASK_JOB_ID);
    }
}
