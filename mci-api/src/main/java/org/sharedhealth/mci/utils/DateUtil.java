package org.sharedhealth.mci.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

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
}
