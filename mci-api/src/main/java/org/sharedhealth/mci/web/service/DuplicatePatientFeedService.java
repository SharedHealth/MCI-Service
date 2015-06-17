package org.sharedhealth.mci.web.service;

import org.sharedhealth.mci.web.infrastructure.dedup.event.DuplicatePatientEventProcessor;
import org.sharedhealth.mci.web.infrastructure.dedup.event.DuplicatePatientEventProcessorFactory;
import org.sharedhealth.mci.web.infrastructure.persistence.MarkerRepository;
import org.sharedhealth.mci.web.infrastructure.persistence.PatientFeedRepository;
import org.sharedhealth.mci.web.infrastructure.persistence.RepositoryConstants;
import org.sharedhealth.mci.web.mapper.PatientUpdateLogMapper;
import org.sharedhealth.mci.web.model.PatientUpdateLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static java.util.UUID.fromString;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component
public class DuplicatePatientFeedService {

    private PatientFeedRepository feedRepository;
    private MarkerRepository markerRepository;
    private DuplicatePatientEventProcessorFactory eventProcessorFactory;
    private PatientUpdateLogMapper patientUpdateLogMapper;

    @Autowired
    public DuplicatePatientFeedService(PatientFeedRepository feedRepository, MarkerRepository markerRepository,
                                       DuplicatePatientEventProcessorFactory eventProcessorFactory,
                                       PatientUpdateLogMapper patientUpdateLogMapper) {
        this.feedRepository = feedRepository;
        this.markerRepository = markerRepository;
        this.eventProcessorFactory = eventProcessorFactory;
        this.patientUpdateLogMapper = patientUpdateLogMapper;
    }

    public void processDuplicatePatients() {
        String markerString = markerRepository.find(RepositoryConstants.DUPLICATE_PATIENT_MARKER);
        UUID marker = isNotBlank(markerString) ? fromString(markerString) : null;
        PatientUpdateLog log = feedRepository.findPatientUpdateLog(marker);
        if (log == null) {
            return;
        }
        DuplicatePatientEventProcessor eventProcessor = eventProcessorFactory
                .getEventProcessor(log.getEventType(), log.getChangeSet());
        eventProcessor.process(patientUpdateLogMapper.map(log), log.getEventId());
    }
}
