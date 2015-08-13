package org.sharedhealth.mci.deduplication.rule;

import org.sharedhealth.mci.deduplication.model.DuplicatePatientMapper;
import org.sharedhealth.mci.domain.model.PatientData;
import org.sharedhealth.mci.domain.model.SearchQuery;
import org.sharedhealth.mci.domain.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DuplicatePatientBrnRule extends DuplicatePatientRule {

    private final String reason;

    @Autowired
    public DuplicatePatientBrnRule(PatientRepository patientRepository, DuplicatePatientMapper duplicatePatientMapper) {
        super(patientRepository, duplicatePatientMapper);
        this.reason = DUPLICATE_REASON_BRN;
    }

    @Override
    protected SearchQuery buildSearchQuery(PatientData patient) {
        SearchQuery query = new SearchQuery();
        query.setBin_brn(patient.getBirthRegistrationNumber());
        return query;
    }

    @Override
    protected String getReason() {
        return reason;
    }
}
