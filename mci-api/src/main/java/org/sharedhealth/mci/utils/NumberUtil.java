package org.sharedhealth.mci.utils;

import static java.lang.Integer.parseInt;
import static java.lang.Math.pow;
import static java.lang.Math.round;
import static java.lang.String.valueOf;

public class NumberUtil {

    public static boolean is10DigitNumber(long id) {
        return getMin10DigitNumber() <= id && id < round(pow(10, 10));
    }

    public static long getMin10DigitNumber() {
        return round(pow(10, 9));
    }

    public static int getDigitAt(long value, int position) {
        return parseInt(valueOf(valueOf(value).charAt(position)));
    }
}
