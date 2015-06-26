package org.sharedhealth.mci.web.infrastructure.dedup.rule;

import org.sharedhealth.mci.web.infrastructure.persistence.PatientRepository;
import org.sharedhealth.mci.web.mapper.DuplicatePatientMapper;
import org.sharedhealth.mci.web.mapper.PatientData;
import org.sharedhealth.mci.web.mapper.SearchQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DuplicatePatientBrnRule extends DuplicatePatientRule {

    private final String reason;

    @Autowired
    public DuplicatePatientBrnRule(PatientRepository patientRepository, DuplicatePatientMapper  duplicatePatientMapper) {
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
