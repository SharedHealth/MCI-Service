package org.sharedhealth.mci.utils;

import org.apache.commons.validator.routines.checkdigit.CheckDigitException;
import org.sharedhealth.mci.web.exception.HidGenerationException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import static java.lang.Integer.parseInt;
import static java.lang.String.valueOf;
import static org.apache.commons.validator.routines.checkdigit.LuhnCheckDigit.LUHN_CHECK_DIGIT;
import static org.slf4j.LoggerFactory.getLogger;

@Component
@Qualifier("MciChecksumGenerator")
public class LuhnChecksumGenerator implements ChecksumGenerator {

    private static final Logger logger = getLogger(LuhnChecksumGenerator.class);

    @Override
    public int generate(String code) {
        try {
            return parseInt(LUHN_CHECK_DIGIT.calculate(valueOf(code)));

        } catch (CheckDigitException e) {
            String message = "Cannot create checksum using Luhn algorithm for code " + code;
            logger.debug(message);
            throw new HidGenerationException(message, e);
        }
    }
}
