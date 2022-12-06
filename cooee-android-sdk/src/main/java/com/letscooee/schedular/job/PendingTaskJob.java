package com.letscooee.schedular.job;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import com.letscooee.CooeeFactory;
import com.letscooee.retrofit.DeviceAuthService;
import com.letscooee.room.CooeeDatabase;
import com.letscooee.room.task.PendingTask;
import com.letscooee.task.CooeeExecutors;
import com.letscooee.utils.Logger;
import java.util.List;

/**
 * This will run every 2 minute to execute pending tasks.
 *
 * @author Ashish Gaikwad on 19/5/21
 * @version 0.3.0
 */
public class PendingTaskJob extends JobService {

    private JobParameters jobParameters;
    private final Logger logger = CooeeFactory.getLogger();

    @Override
    public boolean onStartJob(JobParameters params) {
        jobParameters = params;
        Context context = getApplicationContext();
        logger.verbose("PendingTaskJob running");

        DeviceAuthService deviceAuthService = CooeeFactory.getDeviceAuthService();
        if (!deviceAuthService.hasToken()) {
            logger.debug("Abort PendingTaskJob. Do not have the SDK token");
            return false;       // Job is finished
        }

        List<PendingTask> taskList = CooeeDatabase.getInstance(context)
                .pendingTaskDAO()
                .fetchPending();

        // As job was running on main thread so all the network call were getting break
        CooeeExecutors.getInstance().singleThreadExecutor().execute(() ->
                CooeeFactory.getPendingTaskService().processTasks(taskList, this)
        );

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        // Returning false to let job get finish
        return false;
    }

    public JobParameters getJobParameters() {
        return jobParameters;
    }

}
