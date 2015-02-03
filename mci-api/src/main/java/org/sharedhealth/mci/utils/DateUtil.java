package org.sharedhealth.mci.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import static com.datastax.driver.core.utils.UUIDs.unixTimestamp;

public class DateUtil {

    private static final String UTC = "UTC";

    public static final String UTC_DATE_TILL_MILLIS_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static final String UTC_DATE_TILL_SECS_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    public static final String ISO_DATE_TILL_MILLIS_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    public static final String ISO_DATE_TILL_SECS_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
    public static final String ISO_DATE_TILL_MINS_FORMAT = "yyyy-MM-dd'T'HH:mmZ";
    public static final String RFC_DATE_TILL_MILLIS_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
    public static final String RFC_DATE_TILL_SECS_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";
    public static final String RFC_DATE_TILL_MINS_FORMAT = "yyyy-MM-dd'T'HH:mmXXX";
    public static final String SIMPLE_DATE_WITH_SECS_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String SIMPLE_DATE_FORMAT = "yyyy-MM-dd";
    public static final String SIMPLE_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final String DEFAULT_DATE_FORMAT = SIMPLE_DATE_FORMAT;

    public static final String[] DATE_FORMATS = new String[]{
            ISO_DATE_TILL_MILLIS_FORMAT, ISO_DATE_TILL_SECS_FORMAT,
            ISO_DATE_TILL_MINS_FORMAT, UTC_DATE_TILL_MILLIS_FORMAT,
            UTC_DATE_TILL_SECS_FORMAT, SIMPLE_DATE_WITH_SECS_FORMAT,
            SIMPLE_DATE_FORMAT, SIMPLE_DATE_TIME_FORMAT,
            RFC_DATE_TILL_MINS_FORMAT, RFC_DATE_TILL_SECS_FORMAT,
            RFC_DATE_TILL_MILLIS_FORMAT};

    public static int getCurrentYear() {
        return Calendar.getInstance().get(Calendar.YEAR);
    }

    public static Date parseDate(String date, String... formats) throws ParseException {
        return org.apache.commons.lang3.time.DateUtils.parseDate(date, formats);
    }

    public static Date parseDate(String date) {
        try {
            return parseDate(date, DateUtil.DATE_FORMATS);
        } catch (ParseException e) {
            throw new RuntimeException("invalid date:" + date);
        }
    }

    public static String toISOString(Date date) {
        return toDateString(date, ISO_DATE_TILL_MILLIS_FORMAT);
    }

    public static String toDateString(Date date, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(date);
    }

    public static Date string2Date(String value, String format) {

        if (value == null || value.trim().equals("")) {
            return null;
        }

        try {
            return parseDate(value, format);
        } catch (Exception e) {
            return null;
        }
    }

    public static Date string2Date(String value) {
        return string2Date(value, DEFAULT_DATE_FORMAT);
    }

    public static String toIsoFormat(long date) {
        return toIsoFormat(new Date(date));
    }

    public static String toIsoFormat(UUID uuid) {
        return toIsoFormat(unixTimestamp(uuid));
    }

    public static String toIsoFormat(Date date) {
        return toISOString(date);
    }

    public static Date fromIsoFormat(String date) {
        return parseDate(date);
    }

    public static int getYearOf(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        return cal.get(Calendar.YEAR);
    }

    public static int getYearOf(UUID uuid) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(unixTimestamp(uuid));

        return cal.get(Calendar.YEAR);
    }
}
