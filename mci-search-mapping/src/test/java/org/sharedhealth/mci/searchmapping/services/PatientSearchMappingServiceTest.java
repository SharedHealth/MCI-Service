package org.sharedhealth.mci.searchmapping.services;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.sharedhealth.mci.domain.config.MCIProperties;
import org.sharedhealth.mci.domain.model.PatientData;
import org.sharedhealth.mci.domain.model.PatientUpdateLog;
import org.sharedhealth.mci.domain.repository.PatientFeedRepository;
import org.sharedhealth.mci.domain.repository.PatientRepository;
import org.sharedhealth.mci.searchmapping.repository.PatientSearchMappingRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.sharedhealth.mci.domain.constant.RepositoryConstants.EVENT_TYPE_CREATED;
import static org.sharedhealth.mci.domain.constant.RepositoryConstants.EVENT_TYPE_UPDATED;

public class PatientSearchMappingServiceTest {
    @Mock
    private PatientSearchMappingRepository searchMappingRepository;
    @Mock
    private PatientFeedRepository feedRepository;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private MCIProperties mciProperties;

    private PatientSearchMappingService searchMappingService;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        searchMappingService = new PatientSearchMappingService(searchMappingRepository, feedRepository, patientRepository, mciProperties);
    }

    @Test
    public void shouldCreateSearchMappings() throws Exception {
        String healthId = "h100";
        UUID marker = UUID.randomUUID();
        PatientUpdateLog patientUpdateLog = getPatientUpdateLog(healthId, EVENT_TYPE_CREATED);
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
        List<PatientUpdateLog> updateLogs = asList(getPatientUpdateLog(healthId1, EVENT_TYPE_CREATED), getPatientUpdateLog(healthId2, EVENT_TYPE_CREATED));

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
        List<PatientUpdateLog> updateLogs = asList(getPatientUpdateLog(healthId1, EVENT_TYPE_UPDATED), getPatientUpdateLog(healthId2, EVENT_TYPE_CREATED));
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

    private PatientUpdateLog getPatientUpdateLog(String healthId, String eventType) {
        PatientUpdateLog patientUpdateLog = new PatientUpdateLog();
        patientUpdateLog.setEventType(eventType);
        patientUpdateLog.setHealthId(healthId);
        return patientUpdateLog;
    }
}