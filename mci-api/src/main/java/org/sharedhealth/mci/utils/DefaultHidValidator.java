package org.sharedhealth.mci.utils;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import static org.sharedhealth.mci.utils.NumberUtil.is10DigitNumber;

@Component
@Qualifier("MciHidValidator")
public class DefaultHidValidator implements HidValidator {

    @Override
    public boolean isValid(long id) {
        return is10DigitNumber(id);
    }
}
