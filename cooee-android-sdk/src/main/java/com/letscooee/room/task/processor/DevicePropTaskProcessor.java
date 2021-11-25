package com.letscooee.room.task.processor;

import android.content.Context;

import androidx.annotation.NonNull;

import com.letscooee.exceptions.HttpRequestFailedException;
import com.letscooee.room.task.PendingTask;
import com.letscooee.room.task.PendingTaskType;

import java.util.Map;

public class DevicePropTaskProcessor extends HttpTaskProcessor<Map<String, Object>> {

    public DevicePropTaskProcessor(Context context) {
        super(context);
    }

    protected void doHTTP(Map<String, Object> data) throws HttpRequestFailedException {
        this.baseHTTPService.updateDeviceProperty(data);
    }

    public boolean canProcess(@NonNull PendingTask task) {
        return task.type == PendingTaskType.API_DEVICE_PROPERTY;
    }
}
