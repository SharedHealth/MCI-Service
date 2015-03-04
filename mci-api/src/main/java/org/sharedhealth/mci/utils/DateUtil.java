package org.sharedhealth.mci.utils;

import org.slf4j.Logger;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.datastax.driver.core.utils.UUIDs.unixTimestamp;
import static java.util.Calendar.YEAR;
import static java.util.TimeZone.getTimeZone;
import static org.slf4j.LoggerFactory.getLogger;

public class DateUtil {

    private static final Logger logger = getLogger(DateUtil.class);

    private static final String UTC_TIMEZONE = "UTC";

    public static final String ISO_DATE_FORMAT = "yyyy-MM-dd";

    public static final String ISO_DATE_TIME_TILL_MINS_FORMAT1 = "yyyy-MM-dd'T'HH:mmX"; // Z for UTC
    public static final String ISO_DATE_TIME_TILL_MINS_FORMAT2 = "yyyy-MM-dd'T'HH:mmXX"; // +0530
    public static final String ISO_DATE_TIME_TILL_MINS_FORMAT3 = "yyyy-MM-dd'T'HH:mmXXX"; // +05:30

    public static final String ISO_DATE_TIME_TILL_SECS_FORMAT1 = "yyyy-MM-dd'T'HH:mm:ssX";
    public static final String ISO_DATE_TIME_TILL_SECS_FORMAT2 = "yyyy-MM-dd'T'HH:mm:ssXX";
    public static final String ISO_DATE_TIME_TILL_SECS_FORMAT3 = "yyyy-MM-dd'T'HH:mm:ssXXX";

    public static final String ISO_DATE_TIME_TILL_MILLIS_FORMAT1 = "yyyy-MM-dd'T'HH:mm:ss.SSSX";
    public static final String ISO_DATE_TIME_TILL_MILLIS_FORMAT2 = "yyyy-MM-dd'T'HH:mm:ss.SSSXX";
    public static final String ISO_DATE_TIME_TILL_MILLIS_FORMAT3 = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    public static final String[] DATE_FORMATS = new String[]{
            ISO_DATE_FORMAT,

            ISO_DATE_TIME_TILL_MINS_FORMAT1,
            ISO_DATE_TIME_TILL_MINS_FORMAT2,
            ISO_DATE_TIME_TILL_MINS_FORMAT3,

            ISO_DATE_TIME_TILL_SECS_FORMAT1,
            ISO_DATE_TIME_TILL_SECS_FORMAT2,
            ISO_DATE_TIME_TILL_SECS_FORMAT3,

            ISO_DATE_TIME_TILL_MILLIS_FORMAT1,
            ISO_DATE_TIME_TILL_MILLIS_FORMAT2,
            ISO_DATE_TIME_TILL_MILLIS_FORMAT3
    };

    public static Date parseDate(String date, String... formats) {
        formats = formats == null || formats.length == 0 ? DATE_FORMATS : formats;
        try {
            return org.apache.commons.lang3.time.DateUtils.parseDate(date, formats);
        } catch (IllegalArgumentException | ParseException e) {
            logger.error("Invalid date:" + date, e);
        }
        return null;
    }

    public static String toIsoFormat(UUID uuid) {
        return toIsoFormat(unixTimestamp(uuid));
    }

    public static String toIsoFormat(long date) {
        return toIsoFormat(new Date(date));
    }

    public static String toIsoFormat(Date date) {
        DateFormat dateFormat = new SimpleDateFormat(ISO_DATE_TIME_TILL_MILLIS_FORMAT1);
        dateFormat.setTimeZone(getTimeZone(UTC_TIMEZONE));
        return dateFormat.format(date);
    }

    public static int getCurrentYear() {
        return Calendar.getInstance().get(YEAR);
    }

    public static int getYearOf(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(YEAR);
    }

    public static int getYearOf(UUID uuid) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(unixTimestamp(uuid));
        return cal.get(YEAR);
    }

    public static List<Integer> getYearsSince(int year) {
        List<Integer> years = new ArrayList<>();

        for (int i = year; i <= getCurrentYear(); i++) {
            years.add(i);
        }
        return years;
    }
}
