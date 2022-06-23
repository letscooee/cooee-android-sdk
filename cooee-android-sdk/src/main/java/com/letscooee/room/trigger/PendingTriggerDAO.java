package com.letscooee.room.trigger;

import androidx.room.*;

import java.util.List;

/**
 * DAO for PendingTrigger to fetch, update, delete and insert data.
 *
 * @author Ashish Gaikwad 02/06/22
 * @since 1.3.12
 */
@Dao
public interface PendingTriggerDAO {

    @Query("SELECT * FROM PendingTrigger where trigger_id = :triggerId limit 1")
    PendingTrigger getByID(String triggerId);

    @Query("SELECT * FROM PendingTrigger order by date_created desc")
    List<PendingTrigger> getAll();

    @Query("SELECT * FROM PendingTrigger order by date_created desc limit 1")
    PendingTrigger getFirst();

    @Insert
    long insert(PendingTrigger pendingTrigger);

    @Delete
    void delete(PendingTrigger pendingTrigger);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(PendingTrigger pendingTrigger);

    @Query("DELETE FROM PendingTrigger where trigger_id = :triggerId")
    void deleteByID(String triggerId);

    @Query("DELETE FROM PendingTrigger")
    void deleteAll();

}
