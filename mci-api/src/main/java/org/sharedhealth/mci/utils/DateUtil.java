package org.sharedhealth.mci.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import static com.datastax.driver.core.utils.UUIDs.unixTimestamp;
import static java.util.TimeZone.getTimeZone;

public class DateUtil {

    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    private static final String ISO_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final String UTC = "UTC";

    public static Date string2Date(String value, String format) {

        if (value == null || value.trim().equals("")) {
            return null;
        }

        SimpleDateFormat sdf = new SimpleDateFormat(format);

        try {
            return new java.util.Date(sdf.parse(value).getTime());
        } catch (Exception e) {
            return null;
        }
    }

    public static Date string2Date(String value) {
        return string2Date(value, DEFAULT_DATE_FORMAT);
    }

    public static String toIsoFormat(long date) {
        return buildIsoDateFormat().format(date);
    }

    public static String toIsoFormat(UUID uuid) {
        return toIsoFormat(unixTimestamp(uuid));
    }

    public static Date fromIsoFormat(String date) {
        try {
            return buildIsoDateFormat().parse(date);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static int getYear(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        return cal.get(Calendar.YEAR);
    }

    public static int getYear(UUID uuid) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(unixTimestamp(uuid));

        return cal.get(Calendar.YEAR);
    }

    private static DateFormat buildIsoDateFormat() {
        DateFormat dateFormat = new SimpleDateFormat(ISO_DATE_TIME_FORMAT);
        dateFormat.setTimeZone(getTimeZone(UTC));
        return dateFormat;
    }
}
