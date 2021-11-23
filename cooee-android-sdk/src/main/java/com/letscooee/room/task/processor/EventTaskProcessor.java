package com.letscooee.room.task.processor;

import android.content.Context;

import androidx.annotation.NonNull;

import com.letscooee.exceptions.HttpRequestFailedException;
import com.letscooee.models.Event;
import com.letscooee.room.task.PendingTask;
import com.letscooee.room.task.PendingTaskType;

/**
 * Process a {@link PendingTask} which is related to pushing an {@link Event} to API.
 *
 * @author Shashank Agarwal
 * @since 0.3.0
 */
public class EventTaskProcessor extends HttpTaskProcessor<Event> {

    public EventTaskProcessor(Context context) {
        super(context);
    }

    Event deserialize(PendingTask task) {
        return gson.fromJson(task.data, Event.class);
    }

    protected void doHTTP(Event event) throws HttpRequestFailedException {
        this.baseHTTPService.sendEvent(event);
    }

    public boolean canProcess(@NonNull PendingTask task) {
        return task.type == PendingTaskType.API_SEND_EVENT;
    }
}
