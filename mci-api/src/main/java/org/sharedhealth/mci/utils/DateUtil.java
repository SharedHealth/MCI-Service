package org.sharedhealth.mci.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static java.util.TimeZone.getTimeZone;

public class DateUtil {

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
        return string2Date(value, "yyyy-MM-dd");
    }

    public static String toIsoFormat(long date) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        dateFormat.setTimeZone(getTimeZone("UTC"));
        return dateFormat.format(date);
    }
}
