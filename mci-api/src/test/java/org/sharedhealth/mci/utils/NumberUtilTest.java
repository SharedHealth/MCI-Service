package org.sharedhealth.mci.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.sharedhealth.mci.utils.NumberUtil.getMin10DigitNumber;
import static org.sharedhealth.mci.utils.NumberUtil.is10DigitNumber;

public class NumberUtilTest {

    @Test
    public void shouldValidateWhether10DigitNumber() {
        assertFalse(is10DigitNumber(1));
        assertFalse(is10DigitNumber(12345678901L));
        assertTrue(is10DigitNumber(1234567890L));
    }

    @Test
    public void shouldGetMin10DigitNumber() {
        assertEquals(1000000000, getMin10DigitNumber());
    }
}