package org.sharedhealth.mci.utils;

import org.junit.Test;

import static junit.framework.Assert.*;
import static org.sharedhealth.mci.domain.util.StringUtil.*;

public class StringUtilTest {

    @Test
    public void shouldEnsureSuffix() throws Exception {
        assertEquals("abc/", ensureSuffix("abc/", "/"));
        assertEquals("abc/", ensureSuffix("abc", "/"));
    }

    @Test
    public void shouldFindWhetherInputContainsRepeatingDigits() {
        assertFalse(containsRepeatingDigits(1234567890L, 3));
        assertFalse(containsRepeatingDigits(112113451167890L, 3));

        assertTrue(containsRepeatingDigits(12131444511167890L, 3));
        assertTrue(containsRepeatingDigits(1213145678900000L, 4));
        assertTrue(containsRepeatingDigits(12131455556789L, 4));
    }

    @Test
    public void shouldFindWhetherInputContainsMultipleGroupsOfRepeatingDigits() {
        assertFalse(containsMultipleGroupsOfRepeatingDigits(1234567890L, 3));
        assertFalse(containsMultipleGroupsOfRepeatingDigits(123456789000L, 3));
        assertFalse(containsMultipleGroupsOfRepeatingDigits(12789999990L, 4));

        assertTrue(containsMultipleGroupsOfRepeatingDigits(1127899999088776668L, 3));
        assertTrue(containsMultipleGroupsOfRepeatingDigits(1111278999990000L, 4));
    }
}