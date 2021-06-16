package com.letscooee.room.task;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * DAO to fetch data from {@link PendingTask} table.
 *
 * @author Ashish Gaikwad on 19/5/21
 * @version 0.3.0
 */
@Dao
public interface PendingTaskDAO {

    @Query("SELECT * FROM PendingTask WHERE attempts < 20")
    List<PendingTask> fetchPending();

    @Insert
    long insert(PendingTask task);

    @Delete
    void delete(PendingTask task);

    @Query("update PendingTask set attempts = :count and  last_attempted = :time where id = :id")
    void update(int id, int count, long time);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateByObject(PendingTask pendingTask);
}
