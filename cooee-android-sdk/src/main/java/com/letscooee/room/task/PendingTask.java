package com.letscooee.room.task;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

/**
 * Database entity to hold pending tasks which need to be reattempted.
 *
 * @author Ashish Gaikwad on 19/5/21
 * @version 0.3.0
 */
@Entity
public class PendingTask {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "attempts")
    public int attempts;

    @ColumnInfo(name = "date_created")
    public long dateCreated;

    @ColumnInfo(name = "data")
    public String data;

    @ColumnInfo(name = "last_attempted")
    public long lastAttempted;

    @ColumnInfo(name = "type")
    @TypeConverters(PendingTaskTypeConverter.class)
    public PendingTaskType type;

    @NonNull
    @Override
    public String toString() {
        return "PendingTask(id=" + id + ",type=" + type + ")";
    }
}
