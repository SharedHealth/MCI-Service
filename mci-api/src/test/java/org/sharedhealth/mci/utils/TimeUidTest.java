package org.sharedhealth.mci.utils;

import com.datastax.driver.core.utils.UUIDs;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class TimeUidTest {

    @Test
    public void shouldReturnValidUUIDObject() {
        final UUID uuid = TimeUid.fromString(UUIDs.timeBased().toString());
        assertNotNull(uuid);
    }

    @Test
    public void shouldReturnNullForInvalidUUID() {
        assertNull(TimeUid.fromString("Invalid UUID"));
        assertNull(TimeUid.fromString("1-2-3-4-5"));
    }

    @Test
    public void shouldReturnNullIfNotTimeBasedUUID() {
        assertNull(TimeUid.fromString(UUID.randomUUID().toString()));
    }
}