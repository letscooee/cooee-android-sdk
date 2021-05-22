package com.letscooee.room.postoperations.converter;

import androidx.room.TypeConverter;

import com.letscooee.room.postoperations.enums.EventType;

/**
 * Manage conversion of enum
 *
 * @author Ashish Gaikwad on 19/5/21
 * @version 0.2.10
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
