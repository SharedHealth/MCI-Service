package org.sharedhealth.mci.deduplication.service;

import org.sharedhealth.mci.deduplication.mapper.DuplicatePatientMapper;
import org.sharedhealth.mci.deduplication.model.DuplicatePatient;
import org.sharedhealth.mci.deduplication.model.DuplicatePatientData;
import org.sharedhealth.mci.deduplication.model.DuplicatePatientMergeData;
import org.sharedhealth.mci.deduplication.repository.DuplicatePatientRepository;
import org.sharedhealth.mci.domain.constant.MCIConstants;
import org.sharedhealth.mci.domain.model.Catchment;
import org.sharedhealth.mci.domain.model.PatientData;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

import static java.lang.String.format;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class DuplicatePatientService {

    private static final Logger logger = getLogger(DuplicatePatientService.class);

    private DuplicatePatientRepository duplicatePatientRepository;
    private DuplicatePatientMapper duplicatePatientMapper;

    @Autowired
    public DuplicatePatientService(DuplicatePatientRepository duplicatePatientRepository,
                                   DuplicatePatientMapper duplicatePatientMapper) {
        this.duplicatePatientMapper = duplicatePatientMapper;
        this.duplicatePatientRepository = duplicatePatientRepository;
    }

    public List<DuplicatePatientData> findAllByCatchment(Catchment catchment, UUID after, UUID before, int limit) {
        List<DuplicatePatient> duplicatePatients = duplicatePatientRepository.findByCatchment(catchment, after, before, limit);
        return duplicatePatientMapper.mapToDuplicatePatientDataList(duplicatePatients);
    }

    public void processDuplicates(DuplicatePatientMergeData data) {
        PatientData patient1 = data.getPatient1();
        PatientData patient2 = data.getPatient2();

        if (MCIConstants.DUPLICATION_ACTION_RETAIN_ALL.equals(data.getAction())) {
            validatePatientRetainRequest(patient1, patient2);
            duplicatePatientRepository.processDuplicates(patient1, patient2, false);

        } else if (MCIConstants.DUPLICATION_ACTION_MERGE.equals(data.getAction())) {
            validatePatientMergeRequest(patient1, patient2);
            duplicatePatientRepository.processDuplicates(patient1, patient2, true);
        }
    }

    private void validatePatientRetainRequest(PatientData patient1, PatientData patient2) {
        String healthId1 = patient1.getHealthId();
        String healthId2 = patient2.getHealthId();

        if (patient1.isRetired() || patient2.isRetired()) {
            handleIllegalArgument(format("Patient 1 [hid: %s] or patient 2 [hid: %s] are retired. Cannot retain.",
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
}
