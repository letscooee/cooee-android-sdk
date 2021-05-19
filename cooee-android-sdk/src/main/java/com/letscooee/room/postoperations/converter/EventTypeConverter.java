package com.letscooee.room.postoperations.converter;

import androidx.room.TypeConverter;

import com.letscooee.room.postoperations.enums.EventType;

/**
 * @author Ashish Gaikwad on 19/5/21
 * @version 0.1
 * <p>
 * Manage conversion of enum
 */
public class EventTypeConverter {
    @TypeConverter
    public String fromEventType(EventType value) {
        return value.name();
    }

    @TypeConverter
    public EventType toEventType(String value) {
        return EventType.valueOf(value);
    }
}
