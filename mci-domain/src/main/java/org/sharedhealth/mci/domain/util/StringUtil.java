package org.sharedhealth.mci.domain.util;

import java.util.regex.Matcher;

import static java.lang.String.format;
import static java.lang.String.valueOf;
import static java.util.regex.Pattern.compile;
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

    public static boolean containsRepeatingDigits(long input, int times) {
        Matcher matcher = buildMatcherForRepeatingDigits(input, times);
        return matcher.find();
    }

    public static boolean containsMultipleGroupsOfRepeatingDigits(long input, int times) {
        Matcher matcher = buildMatcherForRepeatingDigits(input, times);
        return matcher.find() && matcher.find();
    }

    private static Matcher buildMatcherForRepeatingDigits(long input, int times) {
        String regex = format("(\\d)\\1{%s}", times - 1);
        return compile(regex).matcher(valueOf(input));
    }
}
