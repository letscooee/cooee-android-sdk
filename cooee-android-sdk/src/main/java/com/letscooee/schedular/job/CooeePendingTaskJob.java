package com.letscooee.schedular.job;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.letscooee.models.Event;
import com.letscooee.retrofit.APIClient;
import com.letscooee.retrofit.HttpCallsHelper;
import com.letscooee.room.CooeeDatabase;
import com.letscooee.room.postoperations.entity.PendingTask;
import com.letscooee.room.postoperations.enums.EventType;
import com.letscooee.schedular.jobschedular.CooeeScheduleJob;
import com.letscooee.network.ConnectionManager;
import com.letscooee.utils.CooeeSDKConstants;
import com.letscooee.utils.LocalStorageHelper;
import com.letscooee.user.SessionManager;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * CooeePendingTaskJob is Job which will run every 2 minute to perform pending task
 *
 * @author Ashish Gaikwad on 19/5/21
 * @version 0.2.10
 */
public class CooeePendingTaskJob extends JobService {
    private final Gson gson = new Gson();

    @Override
    public boolean onStartJob(JobParameters params) {

        if (ConnectionManager.isNetworkAvailable(getApplicationContext())) {

            CooeeDatabase appDatabase = CooeeDatabase.getInstance(getApplicationContext());

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, -2);

            List<PendingTask> taskList = appDatabase.pendingTaskDAO().fetchByDate(calendar.getTimeInMillis());
            SessionManager sessionManager = SessionManager.getInstance(getApplicationContext());

            String sdkToken = LocalStorageHelper.getString(getApplicationContext(), CooeeSDKConstants.STORAGE_SDK_TOKEN, null);
            String sessionId = sessionManager.getCurrentSessionID();

            if (!TextUtils.isEmpty(sdkToken)) {
                if (taskList != null && !taskList.isEmpty()) {
                    APIClient.setAPIToken(sdkToken);
                    readTaskAndHandle(taskList, sessionId, appDatabase);
                }

            }
        }

        //re-schedule job
        CooeeScheduleJob.scheduleJob(getApplicationContext());
        return true;
    }

    /**
     * Read task list and start sending tasks
     */
    private void readTaskAndHandle(List<PendingTask> taskList, String sessionId, CooeeDatabase appDatabase) {
        for (PendingTask pendingTask : taskList) {
            if (pendingTask.type == EventType.EVENT) {

                Event event = gson.fromJson(pendingTask.data, Event.class);
                if (TextUtils.isEmpty(event.getSessionID())) {
                    event.setSessionID(sessionId);
                }
                HttpCallsHelper.pushEvent(getApplicationContext(), event, null, appDatabase, pendingTask);

            } else if (pendingTask.type == EventType.PROFILE) {

                Map<String, Object> userMap = convertToMapAndCheckSessionID(pendingTask.data, sessionId);

                HttpCallsHelper.pushUserProfile(userMap, "", null, appDatabase, pendingTask);

            } else if (pendingTask.type == EventType.SESSION_CONCLUDED) {

                Map<String, Object> sessionConcludedRequest = convertToMapAndCheckSessionID(pendingTask.data, sessionId);

                HttpCallsHelper.pushSessionConcluded(sessionConcludedRequest, appDatabase, pendingTask);

            } else if (pendingTask.type == EventType.KEEP_ALIVE) {

                Map<String, Object> keepAliveRequest = convertToMapAndCheckSessionID(pendingTask.data, sessionId);

                HttpCallsHelper.pushKeepAlive(keepAliveRequest, appDatabase, pendingTask);

            } else if (pendingTask.type == EventType.FB_TOKEN) {

                Map<String, Object> fbTokenRequest = convertToMapAndCheckSessionID(pendingTask.data, sessionId);

                HttpCallsHelper.pushFirebaseToken(fbTokenRequest, appDatabase, pendingTask);

            }
        }
    }

    /**
     * Convert string to Map and check if it has sessionID
     */
    private Map<String, Object> convertToMapAndCheckSessionID(String data, String sessionId) {
        Map<String, Object> dataMap = gson.fromJson(data, new TypeToken<Map<String, Object>>() {
        }.getType());

        if (TextUtils.isEmpty(dataMap.get("sessionID").toString())) {
            dataMap.put("sessionID", sessionId);
        }
        return dataMap;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        CooeeScheduleJob.scheduleJob(getApplicationContext());
        return true;
    }
}
