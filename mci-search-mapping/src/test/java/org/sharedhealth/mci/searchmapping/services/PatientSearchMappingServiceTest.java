package org.sharedhealth.mci.searchmapping.services;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.sharedhealth.mci.domain.config.MCIProperties;
import org.sharedhealth.mci.domain.model.FailedEvent;
import org.sharedhealth.mci.domain.model.PatientData;
import org.sharedhealth.mci.domain.model.PatientUpdateLog;
import org.sharedhealth.mci.domain.repository.FailedEventsRepository;
import org.sharedhealth.mci.domain.repository.PatientFeedRepository;
import org.sharedhealth.mci.domain.repository.PatientRepository;
import org.sharedhealth.mci.searchmapping.repository.PatientSearchMappingRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.datastax.driver.core.utils.UUIDs.timeBased;
import static java.util.Arrays.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.sharedhealth.mci.domain.constant.RepositoryConstants.*;
import static org.sharedhealth.mci.searchmapping.services.PatientSearchMappingService.SEARCH_MAPPING_RETRY_BLOCK_SIZE;

public class PatientSearchMappingServiceTest {
    @Mock
    private PatientSearchMappingRepository searchMappingRepository;
    @Mock
    private PatientFeedRepository feedRepository;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private FailedEventsRepository failedEventsRepository;
    @Mock
    private MCIProperties mciProperties;

    private PatientSearchMappingService searchMappingService;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        searchMappingService = new PatientSearchMappingService(searchMappingRepository, failedEventsRepository, feedRepository, patientRepository, mciProperties);
    }

    @Test
    public void shouldCreateSearchMappings() throws Exception {
        String healthId = "h100";
        UUID marker = UUID.randomUUID();
        PatientUpdateLog patientUpdateLog = getPatientUpdateLog(healthId, EVENT_TYPE_CREATED, timeBased());
        List<PatientUpdateLog> updateLogs = asList(patientUpdateLog);
        PatientData patientData = new PatientData();

        when(searchMappingRepository.findLatestMarker()).thenReturn(marker);
        when(feedRepository.findPatientsUpdatedSince(marker, mciProperties.getSearchMappingTaskBlockSize())).thenReturn(updateLogs);
        when(patientRepository.findByHealthId(healthId)).thenReturn(patientData);

        searchMappingService.map();

        verify(searchMappingRepository, times(1)).findLatestMarker();
        verify(feedRepository, times(1)).findPatientsUpdatedSince(marker, mciProperties.getSearchMappingTaskBlockSize());
        verify(patientRepository, times(1)).findByHealthId(healthId);
        verify(searchMappingRepository, times(1)).saveMappings(patientData);
        verify(searchMappingRepository, times(1)).updateMarkerTable(any(PatientUpdateLog.class));
    }

    @Test
    public void shouldNotMapWhenThereAreNoLogs() throws Exception {
        UUID marker = UUID.randomUUID();

        when(searchMappingRepository.findLatestMarker()).thenReturn(marker);
        when(feedRepository.findPatientsUpdatedSince(marker, mciProperties.getSearchMappingTaskBlockSize())).thenReturn(new ArrayList<PatientUpdateLog>());

        searchMappingService.map();

        verify(searchMappingRepository, times(1)).findLatestMarker();
        verify(feedRepository, times(1)).findPatientsUpdatedSince(marker, mciProperties.getSearchMappingTaskBlockSize());

        verify(patientRepository, never()).findByHealthId(anyString());
        verify(searchMappingRepository, never()).saveMappings(any(PatientData.class));
        verify(searchMappingRepository, never()).updateMarkerTable(any(PatientUpdateLog.class));
    }

    @Test
    public void shouldCreateMappingsForAllFeeds() throws Exception {
        String healthId1 = "h100";
        String healthId2 = "h101";
        UUID marker = UUID.randomUUID();
        List<PatientUpdateLog> updateLogs = asList(getPatientUpdateLog(healthId1, EVENT_TYPE_CREATED, timeBased()), getPatientUpdateLog(healthId2, EVENT_TYPE_CREATED, timeBased()));

        when(searchMappingRepository.findLatestMarker()).thenReturn(marker);
        when(feedRepository.findPatientsUpdatedSince(marker, mciProperties.getSearchMappingTaskBlockSize())).thenReturn(updateLogs);
        when(patientRepository.findByHealthId(healthId1)).thenReturn(new PatientData());

        searchMappingService.map();

        verify(searchMappingRepository, times(1)).findLatestMarker();
        verify(feedRepository, times(1)).findPatientsUpdatedSince(marker, mciProperties.getSearchMappingTaskBlockSize());
        verify(patientRepository, times(2)).findByHealthId(anyString());
        verify(searchMappingRepository, times(2)).saveMappings(any(PatientData.class));
        verify(searchMappingRepository, times(1)).updateMarkerTable(any(PatientUpdateLog.class));
    }

    @Test
    public void shouldCreateMappingsForJustNewCreatedPatients() throws Exception {
        String healthId1 = "h101";
        String healthId2 = "h102";
        UUID marker = UUID.randomUUID();
        List<PatientUpdateLog> updateLogs = asList(getPatientUpdateLog(healthId1, EVENT_TYPE_UPDATED, timeBased()), getPatientUpdateLog(healthId2, EVENT_TYPE_CREATED, timeBased()));
        PatientData patientData = new PatientData();

        when(searchMappingRepository.findLatestMarker()).thenReturn(marker);
        when(feedRepository.findPatientsUpdatedSince(marker, mciProperties.getSearchMappingTaskBlockSize())).thenReturn(updateLogs);
        when(patientRepository.findByHealthId(healthId2)).thenReturn(patientData);

        searchMappingService.map();

        verify(searchMappingRepository, times(1)).findLatestMarker();
        verify(feedRepository, times(1)).findPatientsUpdatedSince(marker, mciProperties.getSearchMappingTaskBlockSize());
        verify(patientRepository, times(1)).findByHealthId(healthId2);
        verify(searchMappingRepository, times(1)).saveMappings(patientData);

        verify(patientRepository, never()).findByHealthId(healthId1);
        verify(searchMappingRepository, times(1)).updateMarkerTable(any(PatientUpdateLog.class));
    }

    @Test
    public void shouldWriteToFailedEventsIfFailsToMap() throws Exception {
        String healthId = "h100";
        UUID marker = UUID.randomUUID();
        UUID eventId = timeBased();
        PatientUpdateLog patientUpdateLog = getPatientUpdateLog(healthId, EVENT_TYPE_CREATED, eventId);
        List<PatientUpdateLog> updateLogs = asList(patientUpdateLog);
        PatientData patientData = new PatientData();

        when(searchMappingRepository.findLatestMarker()).thenReturn(marker);
        when(feedRepository.findPatientsUpdatedSince(marker, mciProperties.getSearchMappingTaskBlockSize())).thenReturn(updateLogs);
        when(patientRepository.findByHealthId(healthId)).thenReturn(patientData);
        doThrow(new RuntimeException()).when(searchMappingRepository).saveMappings(patientData);

        searchMappingService.map();

        verify(searchMappingRepository, times(1)).findLatestMarker();
        verify(feedRepository, times(1)).findPatientsUpdatedSince(marker, mciProperties.getSearchMappingTaskBlockSize());
        verify(patientRepository, times(1)).findByHealthId(healthId);
        verify(searchMappingRepository, times(1)).saveMappings(patientData);
        verify(searchMappingRepository, times(1)).updateMarkerTable(any(PatientUpdateLog.class));
        verify(failedEventsRepository, times(1)).writeToFailedEvents(eq(FAILURE_TYPE_SEARCH_MAPPING), eq(eventId), anyString());
    }

    @Test
    public void shouldMapAFailedEvent() throws Exception {
        String healthId = "h100";
        UUID eventId = timeBased();
        FailedEvent failedEvent = new FailedEvent(FAILURE_TYPE_SEARCH_MAPPING, eventId, "Some Error");
        PatientData patientData = new PatientData();

        when(failedEventsRepository.getFailedEvents(FAILURE_TYPE_SEARCH_MAPPING, SEARCH_MAPPING_RETRY_BLOCK_SIZE)).thenReturn(asList(failedEvent));
        when(feedRepository.findPatientUpdateLog(eventId)).thenReturn(getPatientUpdateLog(healthId, EVENT_TYPE_CREATED, eventId));
        when(patientRepository.findByHealthId(healthId)).thenReturn(patientData);

        searchMappingService.mapFailedEvents();

        verify(failedEventsRepository, times(1)).getFailedEvents(FAILURE_TYPE_SEARCH_MAPPING, SEARCH_MAPPING_RETRY_BLOCK_SIZE);
        verify(feedRepository, times(1)).findPatientUpdateLog(eventId);
        verify(patientRepository, times(1)).findByHealthId(healthId);
        verify(searchMappingRepository, times(1)).saveMappings(patientData);
        verify(failedEventsRepository, times(1)).deleteFailedEvent(FAILURE_TYPE_SEARCH_MAPPING, eventId);
    }

    @Test
    public void shouldMapAllFailedEvent() throws Exception {
        String healthId1 = "h100";
        String healthId2 = "h101";
        UUID eventId1 = timeBased();
        UUID eventId2 = timeBased();
        FailedEvent failedEvent1 = new FailedEvent(FAILURE_TYPE_SEARCH_MAPPING, eventId1, "Some Error");
        FailedEvent failedEvent2 = new FailedEvent(FAILURE_TYPE_SEARCH_MAPPING, eventId2, "Some Error");

        when(failedEventsRepository.getFailedEvents(FAILURE_TYPE_SEARCH_MAPPING, SEARCH_MAPPING_RETRY_BLOCK_SIZE)).thenReturn(asList(failedEvent1, failedEvent2));
        when(feedRepository.findPatientUpdateLog(eventId1)).thenReturn(getPatientUpdateLog(healthId1, EVENT_TYPE_CREATED, eventId1));
        when(feedRepository.findPatientUpdateLog(eventId2)).thenReturn(getPatientUpdateLog(healthId2, EVENT_TYPE_CREATED, eventId2));
        when(patientRepository.findByHealthId(anyString())).thenReturn(new PatientData());

        searchMappingService.mapFailedEvents();

        verify(failedEventsRepository, times(1)).getFailedEvents(FAILURE_TYPE_SEARCH_MAPPING, SEARCH_MAPPING_RETRY_BLOCK_SIZE);
        verify(feedRepository, times(2)).findPatientUpdateLog(any(UUID.class));
        verify(patientRepository, times(2)).findByHealthId(anyString());
        verify(searchMappingRepository, times(2)).saveMappings(any(PatientData.class));
        verify(failedEventsRepository, times(2)).deleteFailedEvent(anyString(), any(UUID.class));
    }

    @Test
    public void shouldReWriteToFailedEventsIfFailsToMapAFailedEvent() throws Exception {
        String healthId = "h100";
        UUID eventId = timeBased();
        FailedEvent failedEvent = new FailedEvent(FAILURE_TYPE_SEARCH_MAPPING, eventId, "Some Error");
        PatientData patientData = new PatientData();

        when(failedEventsRepository.getFailedEvents(FAILURE_TYPE_SEARCH_MAPPING, SEARCH_MAPPING_RETRY_BLOCK_SIZE)).thenReturn(asList(failedEvent));
        when(feedRepository.findPatientUpdateLog(eventId)).thenReturn(getPatientUpdateLog(healthId, EVENT_TYPE_CREATED, eventId));
        when(patientRepository.findByHealthId(healthId)).thenReturn(patientData);
        doThrow(new RuntimeException()).when(searchMappingRepository).saveMappings(patientData);

        searchMappingService.mapFailedEvents();

        verify(failedEventsRepository, times(1)).getFailedEvents(FAILURE_TYPE_SEARCH_MAPPING, SEARCH_MAPPING_RETRY_BLOCK_SIZE);
        verify(feedRepository, times(1)).findPatientUpdateLog(eventId);
        verify(patientRepository, times(1)).findByHealthId(healthId);
        verify(searchMappingRepository, times(1)).saveMappings(patientData);
        verify(failedEventsRepository, times(1)).writeToFailedEvents(eq(FAILURE_TYPE_SEARCH_MAPPING), eq(eventId), anyString());
    }

    private PatientUpdateLog getPatientUpdateLog(String healthId, String eventType, UUID eventId) {
        PatientUpdateLog patientUpdateLog = new PatientUpdateLog();
        patientUpdateLog.setEventType(eventType);
        patientUpdateLog.setEventId(eventId);
        patientUpdateLog.setHealthId(healthId);
        return patientUpdateLog;
    }
}