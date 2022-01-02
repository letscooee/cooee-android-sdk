package com.letscooee.room.task.processor;

import androidx.annotation.NonNull;

import com.letscooee.room.task.PendingTask;

/**
 * Skeleton of a {@link PendingTask} processor.
 *
 * @author Shashank Agrawal
 * @since 0.3.0
 */
public interface PendingTaskProcessor {

    void process(@NonNull PendingTask task);

    boolean canProcess(@NonNull PendingTask task);
}
