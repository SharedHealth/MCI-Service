package org.sharedhealth.mci.utils;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.sharedhealth.mci.utils.NumberUtil.*;

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

    @Test
    public void shouldGetDigitAtAGivenPosition() {
        assertEquals(7, getDigitAt(7, 0));
        assertEquals(4, getDigitAt(456, 0));
        assertEquals(2, getDigitAt(5238428094L, 1));
    }
}