package org.sharedhealth.mci.searchmapping.services;

import org.apache.log4j.Logger;
import org.sharedhealth.mci.domain.config.MCIProperties;
import org.sharedhealth.mci.domain.constant.RepositoryConstants;
import org.sharedhealth.mci.domain.model.PatientData;
import org.sharedhealth.mci.domain.model.PatientUpdateLog;
import org.sharedhealth.mci.domain.repository.PatientFeedRepository;
import org.sharedhealth.mci.domain.repository.PatientRepository;
import org.sharedhealth.mci.searchmapping.repository.PatientSearchMappingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

@Component
public class PatientSearchMappingService {
    private final Logger logger = Logger.getLogger(PatientSearchMappingService.class);

    private PatientSearchMappingRepository searchMappingRepository;
    private PatientFeedRepository feedRepository;
    private PatientRepository patientRepository;
    private MCIProperties mciProperties;


    @Autowired
    public PatientSearchMappingService(PatientSearchMappingRepository searchMappingRepository, PatientFeedRepository feedRepository, PatientRepository patientRepository, MCIProperties mciProperties) {
        this.searchMappingRepository = searchMappingRepository;
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
            logger.debug(String.format("Creating search Mappings for patient %s", createLog.getHealthId()));
            PatientData patientData = patientRepository.findByHealthId(createLog.getHealthId());
            searchMappingRepository.saveMappings(patientData);
        }
        searchMappingRepository.updateMarkerTable(createLogs.get(createLogs.size() - 1));
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
