package org.sharedhealth.mci.searchmapping.services;

import org.apache.log4j.Logger;
import org.sharedhealth.mci.domain.config.MCIProperties;
import org.sharedhealth.mci.domain.constant.RepositoryConstants;
import org.sharedhealth.mci.domain.model.FailedEvent;
import org.sharedhealth.mci.domain.model.PatientData;
import org.sharedhealth.mci.domain.model.PatientUpdateLog;
import org.sharedhealth.mci.domain.repository.FailedEventsRepository;
import org.sharedhealth.mci.domain.repository.PatientFeedRepository;
import org.sharedhealth.mci.domain.repository.PatientRepository;
import org.sharedhealth.mci.searchmapping.repository.PatientSearchMappingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.sharedhealth.mci.domain.constant.RepositoryConstants.FAILURE_TYPE_SEARCH_MAPPING;

@Component
public class PatientSearchMappingService {
    private final Logger logger = Logger.getLogger(PatientSearchMappingService.class);

    public static final int SEARCH_MAPPING_RETRY_BLOCK_SIZE = 5;

    private PatientSearchMappingRepository searchMappingRepository;
    private FailedEventsRepository failedEventsRepository;
    private PatientFeedRepository feedRepository;
    private PatientRepository patientRepository;
    private MCIProperties mciProperties;


    @Autowired
    public PatientSearchMappingService(PatientSearchMappingRepository searchMappingRepository, FailedEventsRepository failedEventsRepository, PatientFeedRepository feedRepository, PatientRepository patientRepository, MCIProperties mciProperties) {
        this.searchMappingRepository = searchMappingRepository;
        this.failedEventsRepository = failedEventsRepository;
        this.feedRepository = feedRepository;
        this.patientRepository = patientRepository;
        this.mciProperties = mciProperties;
    }

    public void map() {
        UUID marker = searchMappingRepository.findLatestMarker();
        List<PatientUpdateLog> updateLogs = feedRepository.findPatientsUpdatedSince(marker, mciProperties.getSearchMappingTaskBlockSize());
        List<PatientUpdateLog> createLogs = getCreateLogs(updateLogs);
        if (isEmpty(createLogs)) {
            return;
        }
        for (PatientUpdateLog createLog : createLogs) {
            try {
                logger.debug(String.format("Creating search Mappings for patient %s", createLog.getHealthId()));
                createMappingForPatient(createLog);
            } catch (Exception e) {
                failedEventsRepository.writeToFailedEvents(FAILURE_TYPE_SEARCH_MAPPING, createLog.getEventId(), e.toString());
                logger.error(String.format("Failed to create search Mappings for patient %s", createLog.getHealthId()));
            }
        }
        searchMappingRepository.updateMarkerTable(createLogs.get(createLogs.size() - 1));
    }

    public void mapFailedEvents() {
        List<FailedEvent> failedEvents = failedEventsRepository.getFailedEvents(FAILURE_TYPE_SEARCH_MAPPING, SEARCH_MAPPING_RETRY_BLOCK_SIZE);
        for (FailedEvent failedEvent : failedEvents) {
            PatientUpdateLog patientUpdateLog = feedRepository.findPatientUpdateLog(failedEvent.getEventId());
            try {
                logger.debug(String.format("Creating search Mappings for patient %s from failed events", patientUpdateLog.getHealthId()));
                createMappingForPatient(patientUpdateLog);
                failedEventsRepository.deleteFailedEvent(FAILURE_TYPE_SEARCH_MAPPING, failedEvent.getEventId());
            } catch (Exception e) {
                failedEventsRepository.writeToFailedEvents(FAILURE_TYPE_SEARCH_MAPPING, patientUpdateLog.getEventId(), e.toString());
                logger.error(String.format("Failed to create search Mappings for patient %s", patientUpdateLog.getHealthId()));
            }
        }
    }

    private void createMappingForPatient(PatientUpdateLog createLog) {
        String healthId = createLog.getHealthId();
        PatientData patientData = patientRepository.findByHealthId(healthId);
        searchMappingRepository.saveMappings(patientData);
    }

    private List<PatientUpdateLog> getCreateLogs(List<PatientUpdateLog> updateLogs) {
        List<PatientUpdateLog> createLogs = new ArrayList<>();
        for (PatientUpdateLog updateLog : updateLogs) {
            if (updateLog.getEventType().equals(RepositoryConstants.EVENT_TYPE_CREATED)) {
                createLogs.add(updateLog);
            }
        }
        return createLogs;
    }
}
