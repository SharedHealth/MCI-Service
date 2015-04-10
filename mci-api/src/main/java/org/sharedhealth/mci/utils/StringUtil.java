package org.sharedhealth.mci.utils;

import static org.apache.commons.lang3.StringUtils.defaultString;

public class StringUtil {

    public static String ensureSuffix(String value, String pattern) {
        String trimmedValue = defaultString(value).trim();
        if (trimmedValue.endsWith(pattern)) {
            return trimmedValue;
        } else {
            return trimmedValue + pattern;
        }
    }
}
