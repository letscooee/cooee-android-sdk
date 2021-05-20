package com.letscooee.room.postoperations.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.letscooee.room.postoperations.entity.PendingTask;

import java.util.List;

/**
 * @author Ashish Gaikwad on 19/5/21
 * @version 0.1
 * <p>
 * DAO to fetch data from PendingTask table
 */
@Dao
public interface PendingTaskDAO {
    @Query("SELECT * FROM PendingTask")
    List<PendingTask> getAll();

    @Query("SELECT * FROM PendingTask WHERE id IN (:taskId)")
    List<PendingTask> loadAllByIds(int[] taskId);

    @Query("SELECT * FROM PendingTask WHERE date_created < :date and " +
            "attempts < 20")
    List<PendingTask> fetchByDate(long date);

    @Insert
    void insertAll(PendingTask... task);

    @Delete
    void delete(PendingTask task);

    @Query("update PendingTask set attempts = :count and  last_attempted = :time where id = :id")
    void update(int id, int count, long time);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateByObject(PendingTask pendingTask);
}
