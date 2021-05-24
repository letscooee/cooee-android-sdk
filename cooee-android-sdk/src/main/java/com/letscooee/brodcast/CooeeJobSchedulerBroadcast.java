package com.letscooee.brodcast;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.letscooee.schedular.jobschedular.CooeeScheduleJob;

import static com.letscooee.utils.CooeeSDKConstants.JOB_ID;

/**
 * @author Ashish Gaikwad on 19/5/21
 * @version 0.1
 * <p>
 * Registers PendingTask Job when device boots
 */
public class CooeeJobSchedulerBroadcast extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (!isJobServiceOn(context)) {
            CooeeScheduleJob.scheduleJob(context);
        }
    }

    /**
     * Used to check if similar job is already in system queue
     * will return true if job is already in queue
     *
     * @param context will be application context
     * @return true or false
     */
    public static boolean isJobServiceOn(Context context) {
        JobScheduler jobScheduler = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            jobScheduler = context.getSystemService(JobScheduler.class);
        } else {
            jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        }

        boolean hasBeenScheduled = false;

        for (JobInfo jobInfo : jobScheduler.getAllPendingJobs()) {
            if (jobInfo.getId() == JOB_ID) {
                hasBeenScheduled = true;
                break;
            }
        }

        return hasBeenScheduled;
    }
}