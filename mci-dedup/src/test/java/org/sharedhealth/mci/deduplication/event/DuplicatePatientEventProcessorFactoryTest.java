package org.sharedhealth.mci.deduplication.event;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.sharedhealth.mci.deduplication.config.event.*;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.sharedhealth.mci.domain.constant.JsonConstants.NEW_VALUE;
import static org.sharedhealth.mci.domain.constant.JsonConstants.OLD_VALUE;
import static org.sharedhealth.mci.domain.constant.RepositoryConstants.EVENT_TYPE_CREATED;
import static org.sharedhealth.mci.domain.constant.RepositoryConstants.EVENT_TYPE_UPDATED;

public class DuplicatePatientEventProcessorFactoryTest {

    private DuplicatePatientEventProcessorFactory factory;

    @Mock
    private DuplicatePatientCreateEventProcessor createEventProcessor;
    @Mock
    private DuplicatePatientUpdateEventProcessor updateEventProcessor;
    @Mock
    private DuplicatePatientRetireEventProcessor retireEventProcessor;


    @Before
    public void setup() {
        initMocks(this);
        factory = new DuplicatePatientEventProcessorFactory(createEventProcessor, updateEventProcessor, retireEventProcessor);
    }

    @Test
    public void testGetEventProcessor() {
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
    public void shouldCheckIfActiveFieldRetired() {
        assertFalse(factory.isActiveFieldRetired(null));

        Map<String, Object> activeFieldDetails = new HashMap<>();
        assertFalse(factory.isActiveFieldRetired(activeFieldDetails));

        activeFieldDetails = new HashMap<>();
        activeFieldDetails.put(NEW_VALUE, true);
        activeFieldDetails.put(OLD_VALUE, false);
        assertFalse(factory.isActiveFieldRetired(activeFieldDetails));

        activeFieldDetails = new HashMap<>();
        activeFieldDetails.put(NEW_VALUE, false);
        activeFieldDetails.put(OLD_VALUE, true);
        assertTrue(factory.isActiveFieldRetired(activeFieldDetails));
    }
}