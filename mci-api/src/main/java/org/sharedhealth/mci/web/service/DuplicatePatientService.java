package org.sharedhealth.mci.web.service;

import org.sharedhealth.mci.utils.DateUtil;
import org.sharedhealth.mci.web.infrastructure.persistence.DuplicatePatientRepository;
import org.sharedhealth.mci.web.mapper.Catchment;
import org.sharedhealth.mci.web.mapper.DuplicatePatientData;
import org.sharedhealth.mci.web.mapper.DuplicatePatientMergeData;
import org.sharedhealth.mci.web.mapper.PatientData;
import org.sharedhealth.mci.web.model.DuplicatePatient;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static org.sharedhealth.mci.web.utils.MCIConstants.DUPLICATION_ACTION_IGNORE;
import static org.sharedhealth.mci.web.utils.MCIConstants.DUPLICATION_ACTION_MERGE;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class DuplicatePatientService {

    private static final Logger logger = getLogger(DuplicatePatientService.class);

    private PatientService patientService;
    private DuplicatePatientRepository duplicatePatientRepository;

    @Autowired
    public DuplicatePatientService(PatientService patientService,
                                   DuplicatePatientRepository duplicatePatientRepository) {
        this.patientService = patientService;
        this.duplicatePatientRepository = duplicatePatientRepository;
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

        if (DUPLICATION_ACTION_IGNORE.equals(data.getAction())) {
            duplicatePatientRepository.processDuplicates(patient1, patient2, false);

        } else if (DUPLICATION_ACTION_MERGE.equals(data.getAction())) {
            validatePatientMergeRequest(patient1, patient2);
            duplicatePatientRepository.processDuplicates(patient1, patient2, true);
        }
    }

    private void validatePatientMergeRequest(PatientData patient1, PatientData patient2) {
        String healthId1 = patient1.getHealthId();
        String healthId2 = patient2.getHealthId();

        Boolean patient1Active = patient1.isActive();
        if (patient1Active == null || patient1Active) {
            handleIllegalArgument(format("Patient 1 [hid: %s] is not retired. Cannot merge.", healthId1));
        }

        String patient1MergedWith = patient1.getMergedWith();
        if (patient1MergedWith == null || !patient1MergedWith.equals(healthId2)) {
            handleIllegalArgument(
                    format("'merge_with' field of Patient 1 [hid: %s] is not set properly. Cannot merge.", healthId1));
        }

        Boolean patient2Active = patient2.isActive();
        if (patient2Active == null || !patient2Active) {
            handleIllegalArgument(format("Patient 2 [hid: %s] is retired. Cannot merge.", healthId2));
        }
    }

    private void handleIllegalArgument(String message) {
        logger.error(message);
        throw new IllegalArgumentException(message);
    }
}
