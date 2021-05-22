package com.letscooee.room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.letscooee.room.postoperations.dao.PendingTaskDAO;
import com.letscooee.room.postoperations.entity.PendingTask;

/**
 * Create a instance of the database
 *
 * @author Ashish Gaikwad on 19/5/21
 * @version 0.2.10
 */
@Database(entities = {PendingTask.class}, exportSchema = false, version = 1)
public abstract class CooeeDatabase extends RoomDatabase {

    private static final String DB_NAME = "letscooee";
    private static CooeeDatabase instance;

    /**
     * Will create instance of database
     *
     * @param context will be application context
     * @return CooeeDatabase instance
     */
    public static synchronized CooeeDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), CooeeDatabase.class, DB_NAME)
                    //.fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }

    public abstract PendingTaskDAO pendingTaskDAO();
}
