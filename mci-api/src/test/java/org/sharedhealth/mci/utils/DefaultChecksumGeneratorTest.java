package org.sharedhealth.mci.utils;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DefaultChecksumGeneratorTest {

    private ChecksumGenerator checksumGenerator;

    @Before
    public void setUp() throws Exception {
        checksumGenerator = new DefaultChecksumGenerator();
    }

    @Test
    public void shouldGenerateChecksum() {
        assertEquals(6, checksumGenerator.generate(12345));
        assertEquals(3, checksumGenerator.generate(123456));
        assertEquals(1, checksumGenerator.generate(1234567));
    }
}