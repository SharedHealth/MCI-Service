package org.sharedhealth.mci.web.service;

import org.sharedhealth.mci.utils.DateUtil;
import org.sharedhealth.mci.web.infrastructure.dedup.DedupEventProcessor;
import org.sharedhealth.mci.web.infrastructure.persistence.DuplicatePatientRepository;
import org.sharedhealth.mci.web.infrastructure.persistence.MarkerRepository;
import org.sharedhealth.mci.web.infrastructure.persistence.PatientFeedRepository;
import org.sharedhealth.mci.web.mapper.Catchment;
import org.sharedhealth.mci.web.mapper.DuplicatePatientData;
import org.sharedhealth.mci.web.mapper.DuplicatePatientMergeData;
import org.sharedhealth.mci.web.mapper.PatientData;
import org.sharedhealth.mci.web.model.DuplicatePatient;
import org.sharedhealth.mci.web.model.PatientUpdateLog;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.lang.String.format;
import static java.util.UUID.fromString;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.sharedhealth.mci.web.infrastructure.dedup.DedupEventProcessorFactory.getEventProcessor;
import static org.sharedhealth.mci.web.utils.MCIConstants.DUPLICATION_ACTION_MERGE;
import static org.sharedhealth.mci.web.utils.MCIConstants.DUPLICATION_ACTION_RETAIN_ALL;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class DuplicatePatientService {

    private static final Logger logger = getLogger(DuplicatePatientService.class);
    private static final String DUPLICATE_PATIENT_MARKER = "duplicate_patient_marker";

    private DuplicatePatientRepository duplicatePatientRepository;
    private PatientFeedRepository feedRepository;
    private MarkerRepository markerRepository;

    @Autowired
    public DuplicatePatientService(DuplicatePatientRepository duplicatePatientRepository,
                                   PatientFeedRepository feedRepository, MarkerRepository markerRepository) {
        this.duplicatePatientRepository = duplicatePatientRepository;
        this.feedRepository = feedRepository;
        this.markerRepository = markerRepository;
    }

    public List<DuplicatePatientData> findAllByCatchment(Catchment catchment) {
        List<DuplicatePatient> duplicatePatients = duplicatePatientRepository.findAllByCatchment(catchment);
        return buildDuplicatePatientData(duplicatePatients);
    }

    private List<DuplicatePatientData> buildDuplicatePatientData(List<DuplicatePatient> duplicatePatients) {
        List<DuplicatePatientData> duplicatePatientDataList = new ArrayList<>();
        for (DuplicatePatient duplicatePatient : duplicatePatients) {
            duplicatePatientDataList.add(buildDuplicatePatientData(duplicatePatient));
        }
        return duplicatePatientDataList;
    }

    private DuplicatePatientData buildDuplicatePatientData(DuplicatePatient duplicatePatient) {
        DuplicatePatientData duplicatePatientData = new DuplicatePatientData();
        duplicatePatientData.setHealthId1(duplicatePatient.getHealth_id1());
        duplicatePatientData.setHealthId2(duplicatePatient.getHealth_id2());
        duplicatePatientData.setReasons(duplicatePatient.getReasons());
        duplicatePatientData.setCreatedAt(DateUtil.toIsoFormat(duplicatePatient.getCreated_at()));
        return duplicatePatientData;
    }

    public void processDuplicates(DuplicatePatientMergeData data) {
        PatientData patient1 = data.getPatient1();
        PatientData patient2 = data.getPatient2();

        if (DUPLICATION_ACTION_RETAIN_ALL.equals(data.getAction())) {
            validatePatientRetainRequest(patient1, patient2);
            duplicatePatientRepository.processDuplicates(patient1, patient2, false);

        } else if (DUPLICATION_ACTION_MERGE.equals(data.getAction())) {
            validatePatientMergeRequest(patient1, patient2);
            duplicatePatientRepository.processDuplicates(patient1, patient2, true);
        }
    }

    private void validatePatientRetainRequest(PatientData patient1, PatientData patient2) {
        String healthId1 = patient1.getHealthId();
        String healthId2 = patient2.getHealthId();

        if (patient1.isRetired() || patient2.isRetired()) {
            handleIllegalArgument(format("Patient 1 [hid: %s] and/or patient 2 [hid: %s] are/is retired. Cannot retain.",
                    healthId1, healthId2));
        }
    }

    private void validatePatientMergeRequest(PatientData patient1, PatientData patient2) {
        String healthId1 = patient1.getHealthId();
        String healthId2 = patient2.getHealthId();

        if (!patient1.isRetired()) {
            handleIllegalArgument(format("Patient 1 [hid: %s] is not retired. Cannot merge.", healthId1));
        }

        String patient1MergedWith = patient1.getMergedWith();
        if (patient1MergedWith == null || !patient1MergedWith.equals(healthId2)) {
            handleIllegalArgument(
                    format("'merge_with' field of Patient 1 [hid: %s] is not set properly. Cannot merge.", healthId1));
        }

        if (patient2.isRetired()) {
            handleIllegalArgument(format("Patient 2 [hid: %s] is retired. Cannot merge.", healthId2));
        }
    }

    private void handleIllegalArgument(String message) {
        logger.error(message);
        throw new IllegalArgumentException(message);
    }

    public void processDuplicatePatients() {
        String markerString = markerRepository.find(DUPLICATE_PATIENT_MARKER);
        UUID marker = isNotBlank(markerString) ? fromString(markerString) : null;
        PatientUpdateLog log = feedRepository.findPatientUpdateLog(marker);
        if (log == null) {
            return;
        }
        DedupEventProcessor eventProcessor = getEventProcessor(log.getEventType(), log.getChangeSet());
        eventProcessor.process(log.getHealthId(), log.getEventId());
    }
}
