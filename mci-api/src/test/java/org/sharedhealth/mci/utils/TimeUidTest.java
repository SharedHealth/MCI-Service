package org.sharedhealth.mci.utils;

import java.util.UUID;

import org.junit.Test;

import static org.junit.Assert.*;

public class TimeUidTest {

    @Test
    public void shouldReturnValidUUIDObject() {
        final UUID uuid = TimeUid.fromString("123e4567-e89b-12d3-a456-426655440000");
        assertNotNull(uuid);
    }

    @Test
    public void shouldReturnNullForInvalidUUID() {
        assertNull(TimeUid.fromString("Invalid UUID"));
        assertNull(TimeUid.fromString("1-2-3-4-5"));
    }

    @Test
    public void shouldReturnNullIfNotTimeBasedUUID() {
        assertNull(TimeUid.fromString("16498faf-b3ea-4576-bfd3-f9ab0388ef2c"));
    }
}