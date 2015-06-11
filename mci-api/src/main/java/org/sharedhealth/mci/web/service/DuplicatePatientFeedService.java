package org.sharedhealth.mci.web.service;

import org.sharedhealth.mci.web.infrastructure.dedup.DuplicatePatientEventProcessor;
import org.sharedhealth.mci.web.infrastructure.dedup.DuplicatePatientEventProcessorFactory;
import org.sharedhealth.mci.web.infrastructure.persistence.MarkerRepository;
import org.sharedhealth.mci.web.infrastructure.persistence.PatientFeedRepository;
import org.sharedhealth.mci.web.model.PatientUpdateLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static java.util.UUID.fromString;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component
public class DuplicatePatientFeedService {

    public static final String DUPLICATE_PATIENT_MARKER = "duplicate_patient_marker";

    private PatientFeedRepository feedRepository;
    private MarkerRepository markerRepository;
    private DuplicatePatientEventProcessorFactory eventProcessorFactory;

    @Autowired
    public DuplicatePatientFeedService(PatientFeedRepository feedRepository, MarkerRepository markerRepository,
                                       DuplicatePatientEventProcessorFactory eventProcessorFactory) {
        this.feedRepository = feedRepository;
        this.markerRepository = markerRepository;
        this.eventProcessorFactory = eventProcessorFactory;
    }

    public void processDuplicatePatients() {
        String markerString = markerRepository.find(DUPLICATE_PATIENT_MARKER);
        UUID marker = isNotBlank(markerString) ? fromString(markerString) : null;
        PatientUpdateLog log = feedRepository.findPatientUpdateLog(marker);
        if (log == null) {
            return;
        }
        DuplicatePatientEventProcessor eventProcessor = eventProcessorFactory
                .getEventProcessor(log.getEventType(), log.getChangeSet());
        eventProcessor.process(log.getHealthId(), log.getEventId());
    }
}
