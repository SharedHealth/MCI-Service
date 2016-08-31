package org.sharedhealth.mci.domain.util;

import static org.apache.commons.lang3.StringUtils.defaultString;

public class StringUtil {

    public static String ensureSuffix(String input, String pattern) {
        String trimmedValue = defaultString(input).trim();
        if (trimmedValue.endsWith(pattern)) {
            return trimmedValue;
        } else {
            return trimmedValue + pattern;
        }
    }

    public static String removeSuffix(String value, String pattern) {
        String trimmedValue = value.trim();
        if (trimmedValue.endsWith(pattern)) {
            return trimmedValue.substring(0, trimmedValue.lastIndexOf(pattern));
        } else {
            return trimmedValue;
        }
    }

    public static String removePrefix(String value, String prefix) {
        String trimmedValue = value.trim();
        if (trimmedValue.startsWith(prefix)) {
            return trimmedValue.substring(prefix.length());
        } else {
            return trimmedValue;
        }
    }
}
