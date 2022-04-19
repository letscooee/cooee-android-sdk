package com.letscooee.utils;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * DateUtil provides multiple date related utility methods.
 *
 * @author Ashish Gaikwad
 * @since 1.3.9
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class DateUtils {

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

    /**
     * Converts given <code>date</code> to UTC date.
     *
     * @param date   date to be converted.
     * @param format format of the date.
     * @return Date in UTC string format.
     */
    @NonNull
    public static String getStringDateFromDate(@NonNull Date date, @NonNull String format) {
        return getStringDateFromDate(date, format, false);
    }

    /**
     * Converts given <code>date</code> to UTC date.
     *
     * @param date   date to be converted.
     * @param format format of the date.
     * @param isUTC  if true, time zone will be set to UTC.
     * @return Date in UTC string format.
     */
    @NonNull
    public static String getStringDateFromDate(@NonNull Date date, @NonNull String format, boolean isUTC) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.ENGLISH);

        if (isUTC) {
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        }

        return dateFormat.format(date);
    }

    /**
     * Converts given <code>milliSeconds</code> to UTC date.
     *
     * @param milliSeconds milliseconds to be converted.
     * @param format       format of the date.
     * @return Date in UTC string format.
     */
    @NonNull
    public static String getStringDateFromMS(@NonNull Long milliSeconds, @NonNull String format) {
        return getStringDateFromMS(milliSeconds, format, false);
    }

    /**
     * Converts given <code>milliSeconds</code> to UTC date.
     *
     * @param milliSeconds milliseconds to be converted.
     * @param format       format of the date.
     * @param isUTC        if true, time zone will be set to UTC.
     * @return Date in UTC string format.
     */
    @NonNull
    public static String getStringDateFromMS(@NonNull Long milliSeconds, @NonNull String format, boolean isUTC) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.ENGLISH);

        if (isUTC) {
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        }

        return dateFormat.format(new Date(milliSeconds));
    }

    /**
     * Converts given <code>epochDate</code> to UTC date.
     *
     * @param epochDate epoch date to be converted.
     * @param format    format of the date.
     * @return Date in UTC string format.
     */
    @NonNull
    public static String getStringDateFromEpoch(@NonNull Long epochDate, @NonNull String format) {
        return getStringDateFromEpoch(epochDate, format, false);
    }

    /**
     * Converts given <code>epochDate</code> to UTC date.
     *
     * @param epochDate epoch date to be converted.
     * @param format    format of the date.
     * @param isUTC     if true, time zone will be set to UTC.
     * @return Date in UTC string format.
     */
    @NonNull
    public static String getStringDateFromEpoch(@NonNull Long epochDate, @NonNull String format, boolean isUTC) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.ENGLISH);

        if (isUTC) {
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        }

        return dateFormat.format(new Date(epochDate * 1000));
    }

    /**
     * Converts given <code>date</code> to UTC date.
     * Date should be in {@link Constants#DATE_FORMAT_UTC} format.
     *
     * @param stringDate date to be converted.
     * @param format     format of the date.
     * @return Returns {@link Date} object.
     * @throws ParseException if given date is not in {@link Constants#DATE_FORMAT_UTC} format.
     */
    public static Date getUTCDateFromString(@NonNull String stringDate, @NonNull String format) throws ParseException {
        return getUTCDateFromString(stringDate, format, false);
    }

    /**
     * Converts given <code>date</code> to UTC date.
     * Date should be in {@link Constants#DATE_FORMAT_UTC} format.
     *
     * @param stringDate date to be converted.
     * @param format     format of the date.
     * @param isUTC      if true, time zone will be set to UTC.
     * @return Returns {@link Date} object.
     * @throws ParseException if given date is not in {@link Constants#DATE_FORMAT_UTC} format.
     */
    public static Date getUTCDateFromString(@NonNull String stringDate, @NonNull String format, boolean isUTC) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.ENGLISH);

        if (isUTC) {
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        }

        return dateFormat.parse(stringDate);
    }
}
