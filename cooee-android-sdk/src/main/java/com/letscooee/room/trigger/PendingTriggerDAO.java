package com.letscooee.room.trigger;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

/**
 * DAO for PendingTrigger to fetch, update, delete and insert data.
 *
 * @author Ashish Gaikwad 02/06/22
 * @since 1.3.12
 */
@Dao
public interface PendingTriggerDAO {
    @Query("SELECT * FROM PendingTrigger where trigger_id = :triggerId")
    PendingTrigger getPendingTriggerWithTriggerId(int triggerId);

    @Query("SELECT * FROM PendingTrigger order by trigger_time desc")
    List<PendingTrigger> getAllPendingTriggers();

    @Insert
    long insertPendingTrigger(PendingTrigger pendingTrigger);

    @Delete
    void deletePendingTrigger(PendingTrigger pendingTrigger);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updatePendingTrigger(PendingTrigger pendingTrigger);
}
