package com.letscooee.schedular.job;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.letscooee.models.Event;
import com.letscooee.retrofit.HttpCallsHelper;
import com.letscooee.room.CooeeDatabase;
import com.letscooee.room.postoperations.entity.PendingTask;
import com.letscooee.room.postoperations.enums.EventType;
import com.letscooee.schedular.jobschedular.CooeeScheduleJob;
import com.letscooee.utils.CooeeConnectivityManager;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * @author Ashish Gaikwad on 19/5/21
 * @version 0.1
 * <p>
 * CooeePendingTaskJob is Job which will run every 2 minute to perform pending task
 */
public class CooeePendingTaskJob extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {

        if (CooeeConnectivityManager.isNetworkAvailable(getApplicationContext())) {

            CooeeDatabase appDatabase = CooeeDatabase.getInstance(getApplicationContext());

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, -2);

            List<PendingTask> taskList = appDatabase.pendingTaskDAO().fetchByDate(calendar.getTimeInMillis());

            if (taskList != null && !taskList.isEmpty()) {

                Gson gson = new Gson();

                for (PendingTask pendingTask : taskList) {
                    if (pendingTask.type == EventType.EVENT) {

                        Event event = gson.fromJson(pendingTask.data, Event.class);
                        HttpCallsHelper.pushEvent(event, null, appDatabase, pendingTask);

                    } else if (pendingTask.type == EventType.PROFILE) {

                        Map<String, Object> userMap = gson.fromJson(pendingTask.data, new TypeToken<Map<String, Object>>() {
                        }.getType());
                        HttpCallsHelper.pushUserProfile(userMap, "", null, appDatabase, pendingTask);

                    } else if (pendingTask.type == EventType.SESSION_CONCLUDED) {

                        Map<String, Object> sessionConcludedRequest = gson.fromJson(pendingTask.data, new TypeToken<Map<String, Object>>() {
                        }.getType());
                        HttpCallsHelper.pushSessionConcluded(sessionConcludedRequest, appDatabase, pendingTask);

                    } else if (pendingTask.type == EventType.KEEP_ALIVE) {

                        Map<String, String> keepAliveRequest = gson.fromJson(pendingTask.data, new TypeToken<Map<String, String>>() {
                        }.getType());
                        HttpCallsHelper.pushKeepAlive(keepAliveRequest, appDatabase, pendingTask);

                    } else if (pendingTask.type == EventType.FB_TOKEN) {

                        Map<String, String> keepAliveRequest = gson.fromJson(pendingTask.data, new TypeToken<Map<String, String>>() {
                        }.getType());
                        HttpCallsHelper.pushFirebaseToken(keepAliveRequest, appDatabase, pendingTask);

                    }
                }
            }
        }
        CooeeScheduleJob.scheduleJob(getApplicationContext());
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        CooeeScheduleJob.scheduleJob(getApplicationContext());
        return true;
    }
}
