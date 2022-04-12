package com.letscooee.utils;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

/**
 * DateUtil provides multiple date related utility methods.
 *
 * @author Ashish Gaikwad
 * @since 1.3.9
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class DateUtil {

    /**
     * Generate {@link SimpleDateFormat} with {@link Locale#ENGLISH} and
     * sets time zone to UTC so all dates can be converted to UTC.
     * It uses {@link Constants#DATE_FORMAT_UTC} as date format.
     *
     * @return {@link SimpleDateFormat}
     */
    @NonNull
    public static SimpleDateFormat getSimpleDateFormatForUTC() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT_UTC, Locale.ENGLISH);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat;
    }

    /**
     * Generate {@link SimpleDateFormat} with {@link Locale#ENGLISH} and
     * uses {@link Constants#DATE_FORMAT_DEBUG} as date format.
     *
     * @return {@link SimpleDateFormat}
     */
    @NonNull
    public static SimpleDateFormat getSimpleDateFormatForDebugInfo() {
        return new SimpleDateFormat(Constants.DATE_FORMAT_DEBUG, Locale.ENGLISH);
    }
}
