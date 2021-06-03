package com.letscooee.room.task.processor;

import android.content.Context;
import android.util.Log;
import com.letscooee.CooeeFactory;
import com.letscooee.exceptions.HttpRequestFailedException;
import com.letscooee.network.BaseHTTPService;
import com.letscooee.room.task.PendingTask;
import com.letscooee.utils.CooeeSDKConstants;
import org.jetbrains.annotations.NotNull;

/**
 * An abstract layer to process all {@link PendingTask} which is related to
 * HTTP API interaction.
 *
 * @author Shashank Agrawal
 * @since 0.3.0
 */
public abstract class HttpTaskProcessor<T> extends AbstractPendingTaskProcessor<T> {

    protected final BaseHTTPService baseHTTPService;

    protected HttpTaskProcessor(Context context) {
        super(context);
        this.baseHTTPService = CooeeFactory.getBaseHTTPService();
    }

    /**
     * Make the <strong>synchronous</strong> HTTP call to the service via {@link BaseHTTPService}.
     *
     * @param data The data to pass to the HTTP API.
     * @throws HttpRequestFailedException
     */
    protected abstract void doHTTP(T data) throws HttpRequestFailedException;

    /**
     * This method will deserialize the {@link PendingTask#data} and send it to HTTP API via {@link #doHTTP(Object)}
     * which is a synchronous call. If that call succeeds, delete the given {@link PendingTask}, otherwise, it will
     * just update the {@link PendingTask} as attempted via {@link #updateAttempted(PendingTask)}
     *
     * @param task Task to process.
     */
    public void process(@NotNull PendingTask task) {
        Log.d(CooeeSDKConstants.LOG_PREFIX, "Processing " + task);
        T data = deserialize(task);

        try {
            this.doHTTP(data);
            this.deleteTask(task);

        } catch (HttpRequestFailedException e) {
            this.updateAttempted(task);
        }
    }
}
