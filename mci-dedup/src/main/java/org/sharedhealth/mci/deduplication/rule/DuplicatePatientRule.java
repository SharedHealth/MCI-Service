package org.sharedhealth.mci.deduplication.rule;

import org.apache.commons.collections4.Predicate;
import org.sharedhealth.mci.deduplication.model.DuplicatePatientData;
import org.sharedhealth.mci.deduplication.model.DuplicatePatientMapper;
import org.sharedhealth.mci.domain.model.PatientData;
import org.sharedhealth.mci.domain.model.SearchQuery;
import org.sharedhealth.mci.domain.repository.PatientRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static java.util.Arrays.asList;
import static org.apache.commons.collections4.CollectionUtils.find;

public abstract class DuplicatePatientRule {

    public static final String DUPLICATE_REASON_NID = "DUPLICATE_REASON_NID";
    public static final String DUPLICATE_REASON_UID = "DUPLICATE_REASON_UID";
    public static final String DUPLICATE_REASON_BRN = "DUPLICATE_REASON_BRN";
    public static final String DUPLICATE_REASON_NAME_ADDRESS = "DUPLICATE_REASON_NAME_ADDRESS";

    private PatientRepository patientRepository;
    private DuplicatePatientMapper duplicatePatientMapper;

    protected DuplicatePatientRule(PatientRepository patientRepository,
                                   DuplicatePatientMapper duplicatePatientMapper) {
        this.patientRepository = patientRepository;
        this.duplicatePatientMapper = duplicatePatientMapper;
    }

    public void apply(String healthId, List<DuplicatePatientData> duplicates) {
        PatientData patient = patientRepository.findByHealthId(healthId);
        SearchQuery query = buildSearchQuery(patient);
        List<String> healthIds = findDuplicatesBySearchQuery(healthId, query);
        buildDuplicates(patient, healthIds, getReason(), duplicates);
    }

    protected abstract SearchQuery buildSearchQuery(PatientData patient);

    protected abstract String getReason();

    protected List<String> findDuplicatesBySearchQuery(String healthId, SearchQuery query) {
        List<PatientData> duplicatePatients = patientRepository.findAllByQuery(query);
        List<String> duplicateHealthIds = new ArrayList<>();
        for (PatientData patient : duplicatePatients) {
            if (!healthId.equals(patient.getHealthId()) && !patient.isRetired()) {
                duplicateHealthIds.add(patient.getHealthId());
            }
        }
        return duplicateHealthIds;
    }

    protected void buildDuplicates(final PatientData patient1, List<String> healthIds, String reason,
                                   List<DuplicatePatientData> duplicates) {
        for (final String healthId2 : healthIds) {
            DuplicatePatientData duplicate = find(duplicates, new Predicate<DuplicatePatientData>() {
                @Override
                public boolean evaluate(DuplicatePatientData d) {
                    return patient1.getHealthId().equals(d.getPatient1().getHealthId())
                            && healthId2.equals(d.getPatient2().getHealthId());
                }
            });

            if (duplicate != null) {
                duplicate.addReason(reason);
            } else {
                PatientData patient2 = patientRepository.findByHealthId(healthId2);
                HashSet<String> reasons = new HashSet<>(asList(reason));
                duplicates.add(duplicatePatientMapper.mapToDuplicatePatientData(patient1, patient2, reasons));
            }
        }
    }
}
