package com.letscooee.room.task.processor;

import com.letscooee.room.task.PendingTask;
import org.jetbrains.annotations.NotNull;

/**
 * Skeleton of a {@link PendingTask} processor.
 *
 * @author Shashank Agrawal
 * @since 0.3.0
 */
public interface PendingTaskProcessor {

    void process(@NotNull PendingTask task);

    boolean canProcess(@NotNull PendingTask task);
}
