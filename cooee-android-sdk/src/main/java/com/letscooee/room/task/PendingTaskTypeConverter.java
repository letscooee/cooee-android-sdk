package com.letscooee.room.task;

import androidx.room.TypeConverter;

/**
 * Manage conversion of enum
 *
 * @author Ashish Gaikwad on 19/5/21
 * @version 0.2.10
 */
public class PendingTaskTypeConverter {

    @TypeConverter
    public String fromEventType(PendingTaskType value) {
        return value.name();
    }

    @TypeConverter
    public PendingTaskType toEventType(String value) {
        return PendingTaskType.valueOf(value);
    }
}
