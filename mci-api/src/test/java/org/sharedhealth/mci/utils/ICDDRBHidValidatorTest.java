package org.sharedhealth.mci.utils;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ICDDRBHidValidatorTest {

    private HidValidator hidValidator;

    @Before
    public void setUp() {
        hidValidator = new ICDDRBHidValidator();
    }

    @Test
    public void shouldValidateHid() {
        assertFalse(hidValidator.isValid(123456789L));

        assertFalse(hidValidator.isValid(1234567890L));
        assertFalse(hidValidator.isValid(9012345678L));
        assertFalse(hidValidator.isValid(9812345678L));
        assertFalse(hidValidator.isValid(9912345678L));

        assertFalse(hidValidator.isValid(9233277770L));
        assertFalse(hidValidator.isValid(9277743330L));

        assertTrue(hidValidator.isValid(9112345678L));
        assertTrue(hidValidator.isValid(9212345678L));
        assertTrue(hidValidator.isValid(9312345678L));
        assertTrue(hidValidator.isValid(9412345678L));
        assertTrue(hidValidator.isValid(9512345678L));
        assertTrue(hidValidator.isValid(9612345678L));
        assertTrue(hidValidator.isValid(9712345678L));
    }
}