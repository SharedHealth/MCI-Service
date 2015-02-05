package org.sharedhealth.mci.utils;

import org.slf4j.Logger;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import static com.datastax.driver.core.utils.UUIDs.unixTimestamp;
import static java.util.Calendar.YEAR;
import static java.util.TimeZone.getTimeZone;
import static org.slf4j.LoggerFactory.getLogger;

public class DateUtil {

    private static final Logger logger = getLogger(DateUtil.class);

    private static final String UTC = "UTC";

    public static final String UTC_DATE_TILL_SECS_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    public static final String UTC_DATE_TILL_MILLIS_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    public static final String ISO_DATE_TILL_MINS_FORMAT = "yyyy-MM-dd'T'HH:mmZ";
    public static final String ISO_DATE_TILL_SECS_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
    public static final String ISO_DATE_TILL_MILLIS_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    public static final String RFC_DATE_TILL_MINS_FORMAT = "yyyy-MM-dd'T'HH:mmXXX";
    public static final String RFC_DATE_TILL_SECS_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";
    public static final String RFC_DATE_TILL_MILLIS_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    public static final String SIMPLE_DATE_FORMAT = "yyyy-MM-dd";
    public static final String SIMPLE_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final String[] DATE_FORMATS = new String[]{
            UTC_DATE_TILL_MILLIS_FORMAT,
            UTC_DATE_TILL_SECS_FORMAT,

            ISO_DATE_TILL_MILLIS_FORMAT,
            ISO_DATE_TILL_SECS_FORMAT,
            ISO_DATE_TILL_MINS_FORMAT,

            RFC_DATE_TILL_MILLIS_FORMAT,
            RFC_DATE_TILL_SECS_FORMAT,
            RFC_DATE_TILL_MINS_FORMAT,

            SIMPLE_DATE_FORMAT,
            SIMPLE_DATE_TIME_FORMAT
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
        DateFormat dateFormat = new SimpleDateFormat(UTC_DATE_TILL_MILLIS_FORMAT);
        dateFormat.setTimeZone(getTimeZone(UTC));
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
}
