package com.letscooee.room.trigger;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entity class for pending trigger which need to displayed.
 *
 * @author Ashish Gaikwad 02/06/22
 * @since 1.3.12
 */
@Entity
public class PendingTrigger {

    @PrimaryKey(autoGenerate = true)
    public long id;

    /**
     * This is the trigger id of Cooee's Trigger.
     */
    @ColumnInfo(name = "trigger_id")
    public String triggerId;

    @ColumnInfo(name = "date_created")
    public long dateCreated;

    @ColumnInfo(name = "data")
    public String data;

    @ColumnInfo(name = "loaded_lazy_data")
    public boolean loadedLazyData;

    @ColumnInfo(name = "schedule_at")
    public long scheduleAt;

    @ColumnInfo(name = "sdk_code")
    public long sdkCode;

    /**
     * The unique id assigned to the {@link android.app.Notification};
     */
    @ColumnInfo(name = "notification_id")
    public long notificationId;

    @NonNull
    @Override
    public String toString() {
        return "PendingTrigger(id=" + id + ", triggerId=" + triggerId + ", sdkCode=" + sdkCode + ")";
    }
}
