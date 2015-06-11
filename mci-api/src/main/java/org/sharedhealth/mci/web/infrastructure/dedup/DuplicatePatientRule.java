package org.sharedhealth.mci.web.infrastructure.dedup;

import org.apache.commons.collections4.Predicate;
import org.sharedhealth.mci.web.infrastructure.persistence.PatientRepository;
import org.sharedhealth.mci.web.mapper.Catchment;
import org.sharedhealth.mci.web.mapper.DuplicatePatientData;
import org.sharedhealth.mci.web.mapper.PatientData;
import org.sharedhealth.mci.web.mapper.SearchQuery;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static java.util.Arrays.asList;
import static org.apache.commons.collections4.CollectionUtils.find;

public abstract class DuplicatePatientRule {

    protected static final String DUPLICATE_REASON_NID = "DUPLICATE_REASON_NID";
    protected static final String DUPLICATE_REASON_UID = "DUPLICATE_REASON_UID";
    protected static final String DUPLICATE_REASON_BRN = "DUPLICATE_REASON_BRN";
    protected static final String DUPLICATE_REASON_NAME_ADDRESS = "DUPLICATE_REASON_NAME_ADDRESS";

    private PatientRepository patientRepository;

    protected DuplicatePatientRule(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public void apply(String healthId, List<DuplicatePatientData> duplicates) {
        PatientData patient = patientRepository.findByHealthId(healthId);
        SearchQuery query = buildSearchQuery(patient);
        List<String> healthIds = findDuplicatesBySearchQuery(healthId, query);
        buildDuplicates(healthId, patient.getCatchment(), healthIds, getReason(), duplicates);
    }

    protected abstract SearchQuery buildSearchQuery(PatientData patient);

    protected abstract String getReason();

    protected List<String> findDuplicatesBySearchQuery(String healthId, SearchQuery query) {
        List<PatientData> duplicatePatients = patientRepository.findAllByQuery(query);
        List<String> duplicateHealthIds = new ArrayList<>();
        for (PatientData patient : duplicatePatients) {
            if (!healthId.equals(patient.getHealthId())) {
                duplicateHealthIds.add(patient.getHealthId());
            }
        }
        return duplicateHealthIds;
    }

    protected void buildDuplicates(final String healthId1, Catchment catchment1, List<String> healthIds, String reason,
                                   List<DuplicatePatientData> duplicates) {
        for (final String healthId2 : healthIds) {
            DuplicatePatientData duplicate = find(duplicates, new Predicate<DuplicatePatientData>() {
                @Override
                public boolean evaluate(DuplicatePatientData d) {
                    return healthId1.equals(d.getHealthId1()) && healthId2.equals(d.getHealthId2());
                }
            });

            if (duplicate != null) {
                duplicate.addReason(reason);
            } else {
                HashSet<String> reasons = new HashSet<>(asList(reason));
                DuplicatePatientData patient = new DuplicatePatientData(healthId1, healthId2, reasons, null);
                patient.setCatchment1(catchment1);
                PatientData patient2 = patientRepository.findByHealthId(healthId2);
                patient.setCatchment2(patient2.getCatchment());
                duplicates.add(patient);
            }
        }
    }
}
