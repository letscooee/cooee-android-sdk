package com.letscooee.room.postoperations.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.letscooee.room.postoperations.converter.EventTypeConverter;
import com.letscooee.room.postoperations.enums.EventType;


/**
 * Database Entity
 *
 * @author Ashish Gaikwad on 19/5/21
 * @version 0.2.10
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
    @TypeConverters(EventTypeConverter.class)
    public EventType type;


}
