package com.letscooee.brodcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.letscooee.schedular.CooeeJobUtils;

/**
 * Registers {@link com.letscooee.schedular.job.PendingTaskJob} when device boots.
 *
 * @author Ashish Gaikwad on 19/5/21
 * @version 0.3.0
 */
public class CooeeJobSchedulerBroadcast extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        CooeeJobUtils.schedulePendingTaskJob(context);
    }
}