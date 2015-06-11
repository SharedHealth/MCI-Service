package org.sharedhealth.mci.web.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.sharedhealth.mci.web.infrastructure.dedup.DuplicatePatientEventProcessor;
import org.sharedhealth.mci.web.infrastructure.dedup.DuplicatePatientEventProcessorFactory;
import org.sharedhealth.mci.web.infrastructure.persistence.MarkerRepository;
import org.sharedhealth.mci.web.infrastructure.persistence.PatientFeedRepository;
import org.sharedhealth.mci.web.model.PatientUpdateLog;

import java.util.UUID;

import static com.datastax.driver.core.utils.UUIDs.timeBased;
import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.sharedhealth.mci.web.infrastructure.persistence.RepositoryConstants.EVENT_TYPE_CREATED;
import static org.sharedhealth.mci.web.service.DuplicatePatientFeedService.DUPLICATE_PATIENT_MARKER;

public class DuplicatePatientFeedServiceTest {

    @Mock
    private PatientFeedRepository feedRepository;
    @Mock
    private MarkerRepository markerRepository;
    @Mock
    private DuplicatePatientEventProcessorFactory eventProcessorFactory;
    @Mock
    DuplicatePatientEventProcessor eventProcessor;

    private DuplicatePatientFeedService feedService;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        feedService = new DuplicatePatientFeedService(feedRepository, markerRepository, eventProcessorFactory);
    }

    @Test
    public void shouldProcessCreateEventInFeed() {
        PatientUpdateLog log = new PatientUpdateLog();
        log.setHealthId("h100");
        log.setEventType(EVENT_TYPE_CREATED);
        log.setEventId(timeBased());
        UUID marker = randomUUID();
        when(markerRepository.find(DUPLICATE_PATIENT_MARKER)).thenReturn(marker.toString());
        when(feedRepository.findPatientUpdateLog(marker)).thenReturn(log);
        when(eventProcessorFactory.getEventProcessor(log.getEventType(), log.getChangeSet())).thenReturn(eventProcessor);

        feedService.processDuplicatePatients();
        verify(eventProcessor).process(log.getHealthId(), log.getEventId());
    }
}