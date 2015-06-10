package org.sharedhealth.mci.web.infrastructure.dedup;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.sharedhealth.mci.web.infrastructure.dedup.DedupEventProcessorFactory.getEventProcessor;
import static org.sharedhealth.mci.web.infrastructure.dedup.DedupEventProcessorFactory.isRetired;
import static org.sharedhealth.mci.web.infrastructure.persistence.RepositoryConstants.EVENT_TYPE_CREATED;
import static org.sharedhealth.mci.web.infrastructure.persistence.RepositoryConstants.EVENT_TYPE_UPDATED;
import static org.sharedhealth.mci.web.utils.JsonConstants.NEW_VALUE;
import static org.sharedhealth.mci.web.utils.JsonConstants.OLD_VALUE;

public class DedupEventProcessorFactoryTest {

    @Test
    public void testGetEventProcessor() throws Exception {
        DedupEventProcessor eventProcessor = getEventProcessor(EVENT_TYPE_CREATED, null);
        assertTrue(eventProcessor instanceof DedupCreateEventProcessor);

        eventProcessor = getEventProcessor(EVENT_TYPE_UPDATED,
                "{\"active1\":{\"new_value\":false,\"old_value\":true}}");
        assertTrue(eventProcessor instanceof DedupUpdateEventProcessor);

        eventProcessor = getEventProcessor(EVENT_TYPE_UPDATED,
                "{\"active\":{\"new_value\":false,\"old_value\":true}}");
        assertTrue(eventProcessor instanceof DedupRetireEventProcessor);
    }

    @Test
    public void shouldCheckIfRetired() throws Exception {
        assertFalse(isRetired(null));

        Map<String, Object> activeFieldDetails = new HashMap<>();
        assertFalse(isRetired(activeFieldDetails));

        activeFieldDetails = new HashMap<>();
        activeFieldDetails.put(NEW_VALUE, true);
        activeFieldDetails.put(OLD_VALUE, false);
        assertFalse(isRetired(activeFieldDetails));

        activeFieldDetails = new HashMap<>();
        activeFieldDetails.put(NEW_VALUE, false);
        activeFieldDetails.put(OLD_VALUE, true);
        assertTrue(isRetired(activeFieldDetails));
    }
}