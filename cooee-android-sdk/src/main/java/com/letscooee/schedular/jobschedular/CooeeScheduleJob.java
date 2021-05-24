package com.letscooee.schedular.jobschedular;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;

import com.letscooee.schedular.job.CooeePendingTaskJob;

import static com.letscooee.utils.CooeeSDKConstants.JOB_ID;

/**
 * Schedules a [@link CooeeJobService]
 *
 * @author Ashish Gaikwad on 19/5/21
 * @version 0.2.10
 */
public class CooeeScheduleJob {

    /**
     * Used to schedule a job with android service
     *
     * @param context will be application context
     */
    public static void scheduleJob(Context context) {

        ComponentName serviceComponent = new ComponentName(context, CooeePendingTaskJob.class);
        JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, serviceComponent);
        builder.setMinimumLatency(120 * 1000);

        JobScheduler jobScheduler = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            jobScheduler = context.getSystemService(JobScheduler.class);
        } else {
            jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        }
        jobScheduler.schedule(builder.build());
    }
}
