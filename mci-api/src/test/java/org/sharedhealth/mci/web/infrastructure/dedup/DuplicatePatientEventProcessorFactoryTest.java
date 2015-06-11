package org.sharedhealth.mci.web.infrastructure.dedup;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.sharedhealth.mci.web.infrastructure.persistence.RepositoryConstants.EVENT_TYPE_CREATED;
import static org.sharedhealth.mci.web.infrastructure.persistence.RepositoryConstants.EVENT_TYPE_UPDATED;
import static org.sharedhealth.mci.web.utils.JsonConstants.NEW_VALUE;
import static org.sharedhealth.mci.web.utils.JsonConstants.OLD_VALUE;

public class DuplicatePatientEventProcessorFactoryTest {

    private DuplicatePatientEventProcessorFactory factory;

    @Mock
    private DuplicatePatientRuleEngine ruleEngine;

    @Before
    public void setup() throws Exception {
        factory = new DuplicatePatientEventProcessorFactory(ruleEngine);
    }

    @Test
    public void testGetEventProcessor() throws Exception {
        DuplicatePatientEventProcessor eventProcessor = factory.getEventProcessor(EVENT_TYPE_CREATED, null);
        assertTrue(eventProcessor instanceof DuplicatePatientCreateEventProcessor);

        eventProcessor = factory.getEventProcessor(EVENT_TYPE_UPDATED,
                "{\"active1\":{\"new_value\":false,\"old_value\":true}}");
        assertTrue(eventProcessor instanceof DuplicatePatientUpdateEventProcessor);

        eventProcessor = factory.getEventProcessor(EVENT_TYPE_UPDATED,
                "{\"active\":{\"new_value\":false,\"old_value\":true}}");
        assertTrue(eventProcessor instanceof DuplicatePatientRetireEventProcessor);
    }

    @Test
    public void shouldCheckIfRetired() throws Exception {
        assertFalse(factory.isRetired(null));

        Map<String, Object> activeFieldDetails = new HashMap<>();
        assertFalse(factory.isRetired(activeFieldDetails));

        activeFieldDetails = new HashMap<>();
        activeFieldDetails.put(NEW_VALUE, true);
        activeFieldDetails.put(OLD_VALUE, false);
        assertFalse(factory.isRetired(activeFieldDetails));

        activeFieldDetails = new HashMap<>();
        activeFieldDetails.put(NEW_VALUE, false);
        activeFieldDetails.put(OLD_VALUE, true);
        assertTrue(factory.isRetired(activeFieldDetails));
    }
}