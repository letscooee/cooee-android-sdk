package com.letscooee.room;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import com.letscooee.room.task.PendingTask;
import com.letscooee.room.task.PendingTaskDAO;
import com.letscooee.room.trigger.PendingTrigger;
import com.letscooee.room.trigger.PendingTriggerDAO;

/**
 * Create a instance of the database
 *
 * @author Ashish Gaikwad on 19/5/21
 * @version 0.3.0
 */
@Database(
        entities = {PendingTask.class, PendingTrigger.class},
        exportSchema = false,
        version = 2
)
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
                    .addMigrations(new Migration(1, 2) {
                        @Override
                        public void migrate(@NonNull SupportSQLiteDatabase database) {
                            database.execSQL("CREATE TABLE PendingTrigger(id INTEGER NOT NULL, trigger_id TEXT, " +
                                    "trigger_time INTEGER NOT NULL, trigger_data TEXT, loaded_lazy_data INTEGER NOT NULL," +
                                    " schedule_at INTEGER NOT NULL, sdk_code INTEGER NOT NULL, notification_id INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(id))");
                            database.execSQL("ALTER TABLE PendingTask ADD COLUMN sdk_code INTEGER NOT NULL DEFAULT 0");
                        }
                    })
                    .build();
        }
        return instance;
    }

    public abstract PendingTaskDAO pendingTaskDAO();

    public abstract PendingTriggerDAO pendingTriggerDAO();
}
