package org.sharedhealth.mci.web.infrastructure.dedup.rule;

import org.sharedhealth.mci.web.infrastructure.persistence.PatientRepository;
import org.sharedhealth.mci.web.mapper.DuplicatePatientMapper;
import org.sharedhealth.mci.web.mapper.PatientData;
import org.sharedhealth.mci.web.mapper.SearchQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DuplicatePatientNidRule extends DuplicatePatientRule {

    private final String reason;

    @Autowired
    public DuplicatePatientNidRule(PatientRepository patientRepository, DuplicatePatientMapper duplicatePatientMapper) {
        super(patientRepository, duplicatePatientMapper);
        this.reason = DUPLICATE_REASON_NID;
    }

    @Override
    protected SearchQuery buildSearchQuery(PatientData patient) {
        SearchQuery query = new SearchQuery();
        query.setNid(patient.getNationalId());
        return query;
    }

    @Override
    protected String getReason() {
        return reason;
    }
}
