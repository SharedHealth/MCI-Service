package org.sharedhealth.mci.utils;

import org.junit.Before;
import org.junit.Test;
import org.sharedhealth.mci.web.exception.HidGenerationException;

import static java.lang.String.valueOf;
import static org.junit.Assert.assertEquals;

public class LuhnChecksumGeneratorTest {

    private ChecksumGenerator checksumGenerator;

    @Before
    public void setUp() throws Exception {
        checksumGenerator = new LuhnChecksumGenerator();
    }

    @Test
    public void shouldGenerateChecksumUsingLuhnAlgorithm() {
        assertEquals(9, checksumGenerator.generate(valueOf(7892402363L)));
        assertEquals(7, checksumGenerator.generate(valueOf(6894402763L)));
        assertEquals(5, checksumGenerator.generate(valueOf(3392244354L)));
    }

    @Test(expected = HidGenerationException.class)
    public void shouldThrowExceptionIfChecksumGenerationFails() {
        checksumGenerator.generate(valueOf(-1));
    }
}