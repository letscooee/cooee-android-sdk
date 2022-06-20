package com.letscooee.utils;

import androidx.annotation.RestrictTo;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.text.*;
import java.util.Date;

/**
 * Adapter to process {@link Date} while serializing and deserializing {@link Date}.
 * It Converts {@link Date} in UTC date format.
 *
 * @author Ashish Gaikwad on 09/06/21
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class GsonDateAdapter implements JsonSerializer<Date>, JsonDeserializer<Date> {

    public GsonDateAdapter() {
    }

    @Override
    public synchronized JsonElement serialize(Date date, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(DateUtils.getStringDateFromDate(date, Constants.DATE_FORMAT_UTC, true));
    }

    @Override
    public synchronized Date deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) {
        try {
            return DateUtils.getUTCDateFromString(jsonElement.getAsString(), Constants.DATE_FORMAT_UTC, true);
        } catch (ParseException e) {
            throw new JsonParseException(e);
        }
    }
}