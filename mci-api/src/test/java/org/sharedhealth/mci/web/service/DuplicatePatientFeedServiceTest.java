package org.sharedhealth.mci.web.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.sharedhealth.mci.deduplication.config.event.DuplicatePatientEventProcessor;
import org.sharedhealth.mci.deduplication.config.event.DuplicatePatientEventProcessorFactory;
import org.sharedhealth.mci.deduplication.config.service.DuplicatePatientFeedService;
import org.sharedhealth.mci.domain.model.PatientUpdateLog;
import org.sharedhealth.mci.domain.model.PatientUpdateLogMapper;
import org.sharedhealth.mci.domain.repository.MarkerRepository;
import org.sharedhealth.mci.domain.repository.PatientFeedRepository;

import java.util.UUID;

import static com.datastax.driver.core.utils.UUIDs.timeBased;
import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.sharedhealth.mci.domain.constant.RepositoryConstants.DUPLICATE_PATIENT_MARKER;
import static org.sharedhealth.mci.domain.constant.RepositoryConstants.EVENT_TYPE_CREATED;

public class DuplicatePatientFeedServiceTest {

    @Mock
    private PatientFeedRepository feedRepository;
    @Mock
    private MarkerRepository markerRepository;
    @Mock
    private DuplicatePatientEventProcessorFactory eventProcessorFactory;
    @Mock
    private DuplicatePatientEventProcessor eventProcessor;
    private PatientUpdateLogMapper patientUpdateLogMapper;

    private DuplicatePatientFeedService feedService;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        patientUpdateLogMapper = new PatientUpdateLogMapper();
        feedService = new DuplicatePatientFeedService(feedRepository, markerRepository, eventProcessorFactory, patientUpdateLogMapper);
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
        verify(eventProcessor).process(patientUpdateLogMapper.map(log), log.getEventId());
    }
}