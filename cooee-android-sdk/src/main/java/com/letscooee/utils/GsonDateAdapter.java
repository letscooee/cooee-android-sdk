package com.letscooee.utils;

import androidx.annotation.RestrictTo;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.text.*;
import java.util.Date;

/**
 * Adapter to proccess {@link Date} while serializing and deserializing {@link Date}.
 * It Converts {@link Date} in UTC date format.
 *
 * @author Ashish Gaikwad on 09/06/21
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class GsonDateAdapter implements JsonSerializer<Date>, JsonDeserializer<Date> {

    private final DateFormat dateFormat;

    public GsonDateAdapter() {
        dateFormat = DateUtil.getSimpleDateFormatForUTC();
    }

    @Override
    public synchronized JsonElement serialize(Date date, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(dateFormat.format(date));
    }

    @Override
    public synchronized Date deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) {
        try {
            return dateFormat.parse(jsonElement.getAsString());
        } catch (ParseException e) {
            throw new JsonParseException(e);
        }
    }
}