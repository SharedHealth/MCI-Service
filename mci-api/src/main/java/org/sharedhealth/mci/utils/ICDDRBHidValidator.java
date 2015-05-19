package org.sharedhealth.mci.utils;

import org.slf4j.Logger;

import static java.lang.String.format;
import static java.lang.String.valueOf;
import static org.sharedhealth.mci.utils.NumberUtil.is10DigitNumber;
import static org.sharedhealth.mci.utils.StringUtil.containsMultipleGroupsOfRepeatingDigits;
import static org.sharedhealth.mci.utils.StringUtil.containsRepeatingDigits;
import static org.slf4j.LoggerFactory.getLogger;

public class ICDDRBHidValidator implements HidValidator {

    private static final Logger logger = getLogger(ICDDRBHidValidator.class);

    @Override
    public boolean isValid(long id) {
        if (!is10DigitNumber(id)) {
            logger.debug(format("Invalid hid %s. Should be 10 digits long.", id));
            return false;
        }

        if (!valueOf(id).startsWith("9") || valueOf(id).startsWith("90") || valueOf(id).startsWith("98")
                || valueOf(id).startsWith("99")) {
            logger.debug(format("Invalid hid %s. Should start with 9, but not with 90, 98 or 99.", id));
            return false;
        }

        if (containsRepeatingDigits(id, 4)) {
            logger.debug(format("Invalid hid %s. A particular digit cannot repeat for more than 3 times.", id));
            return false;
        }

        if (containsMultipleGroupsOfRepeatingDigits(id, 3)) {
            logger.debug(format("Invalid hid %s. Repeating groups of 3 digits cannot occur multiple times.", id));
            return false;
        }

        return true;
    }
}
