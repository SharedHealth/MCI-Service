package org.sharedhealth.mci.utils;

import org.junit.Test;
import org.sharedhealth.mci.domain.util.TimeUuidUtil;

import java.util.Date;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

public class TimeUidTest {

    @Test
    public void shouldReturnValidUUIDObject() {
        final UUID uuid = TimeUid.fromString(TimeUuidUtil.uuidForDate(new Date()).toString());
        final UUID uuid2 = TimeUid.fromString(TimeUuidUtil.uuidForDate(new Date()).toString());
        assertNotNull(uuid);
        assertNotNull(uuid2);
        assertNotSame(uuid, uuid2);
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