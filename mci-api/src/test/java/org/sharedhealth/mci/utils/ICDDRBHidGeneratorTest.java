package org.sharedhealth.mci.utils;

import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.*;

public class ICDDRBHidGeneratorTest {

    @Test
    public void shouldGenerateValidIds() throws Exception {
        HidValidator hidValidator = new ICDDRBHidValidator();
        ChecksumGenerator checksumGenerator = new LuhnChecksumGenerator();
        ICDDRBHidGenerator hidGenerator = new ICDDRBHidGenerator(hidValidator, checksumGenerator);

        int count = 100;
        Set<String> ids = hidGenerator.generate(count);
        assertTrue(ids != null);
        assertEquals(count, ids.size());

        for (String id : ids) {
            assertTrue(hidValidator.isValid(Long.valueOf(id.substring(0, 10))));
            assertEquals(Integer.valueOf(checksumGenerator.generate(id.substring(1, 10))),
                    Integer.valueOf(id.substring(10)));
        }
    }
}