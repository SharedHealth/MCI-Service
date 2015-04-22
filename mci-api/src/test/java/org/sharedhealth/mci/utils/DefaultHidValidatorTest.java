package org.sharedhealth.mci.utils;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DefaultHidValidatorTest {

    private DefaultHidValidator defaultHidValidator;

    @Before
    public void setUp() {
        defaultHidValidator = new DefaultHidValidator();
    }

    @Test
    public void shouldValidateHid() {
        assertFalse(defaultHidValidator.isValid(123456789L));
        assertFalse(defaultHidValidator.isValid(1234567890L));
        assertFalse(defaultHidValidator.isValid(9034567890L));
        assertFalse(defaultHidValidator.isValid(9833277770L));
        assertFalse(defaultHidValidator.isValid(9877743330L));
        assertFalse(defaultHidValidator.isValid(9677743320L));

        assertTrue(defaultHidValidator.isValid(9977743320L));
        assertTrue(defaultHidValidator.isValid(9877743320L));
    }
}