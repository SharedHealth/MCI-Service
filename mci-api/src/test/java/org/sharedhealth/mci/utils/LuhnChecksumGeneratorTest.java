package org.sharedhealth.mci.utils;

import org.junit.Before;
import org.junit.Test;
import org.sharedhealth.mci.web.exception.HidGenerationException;

import static org.junit.Assert.assertEquals;

public class LuhnChecksumGeneratorTest {

    private ChecksumGenerator checksumGenerator;

    @Before
    public void setUp() throws Exception {
        checksumGenerator = new LuhnChecksumGenerator();
    }

    @Test
    public void shouldGenerateChecksumUsingLuhnAlgorithm() {
        assertEquals(9, checksumGenerator.generate(7892402363L));
        assertEquals(7, checksumGenerator.generate(6894402763L));
        assertEquals(5, checksumGenerator.generate(3392244354L));
    }

    @Test(expected = HidGenerationException.class)
    public void shouldThrowExceptionIfChecksumGenerationFails() {
        checksumGenerator.generate(-1);
    }
}