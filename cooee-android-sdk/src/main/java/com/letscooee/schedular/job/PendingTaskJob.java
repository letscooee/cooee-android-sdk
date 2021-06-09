package com.letscooee.schedular.job;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import com.letscooee.room.CooeeDatabase;
import com.letscooee.room.task.PendingTask;
import com.letscooee.room.task.PendingTaskService;
import com.letscooee.schedular.CooeeJobUtils;

import java.util.Calendar;
import java.util.List;

/**
 * This will run every 2 minute to execute pending tasks.
 *
 * @author Ashish Gaikwad on 19/5/21
 * @version 0.3.0
 */
public class PendingTaskJob extends JobService {

    @Override
    public boolean onStartJob(JobParameters params) {
        Context context = getApplicationContext();

        List<PendingTask> taskList = CooeeDatabase.getInstance(context)
                .pendingTaskDAO()
                .fetchBeforeTime(this.getTMinusTwoMinutes());

        PendingTaskService.getInstance(context).processTasks(taskList);

        CooeeJobUtils.schedulePendingTaskJob(context);
        return true;
    }

    private long getTMinusTwoMinutes() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -2);
        return calendar.getTimeInMillis();
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        // TODO: 03/06/21 Why we are again re-scheduling here?
        CooeeJobUtils.schedulePendingTaskJob(getApplicationContext());
        return true;
    }
}
