package com.letscooee.brodcast;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.letscooee.schedular.CooeeJobUtils;

import static com.letscooee.utils.CooeeSDKConstants.PENDING_TASK_JOB_ID;

/**
 * Registers {@link com.letscooee.schedular.job.PendingTaskJob} when device boots.
 *
 * @author Ashish Gaikwad on 19/5/21
 * @version 0.3.0
 */
public class CooeeJobSchedulerBroadcast extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!isJobServiceOn(context)) {
            CooeeJobUtils.schedulePendingTaskJob(context);
        }
    }

    /**
     * Used to check if similar job is already in system queue.
     *
     * @param context will be application context
     * @return true if job is already in queue
     */
    public static boolean isJobServiceOn(Context context) {
        JobScheduler jobScheduler = CooeeJobUtils.getJobScheduler(context);

        boolean hasBeenScheduled = false;

        for (JobInfo jobInfo : jobScheduler.getAllPendingJobs()) {
            if (jobInfo.getId() == PENDING_TASK_JOB_ID) {
                hasBeenScheduled = true;
                break;
            }
        }

        return hasBeenScheduled;
    }
}