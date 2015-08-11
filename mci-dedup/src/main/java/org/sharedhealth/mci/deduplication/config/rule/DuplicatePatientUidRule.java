package org.sharedhealth.mci.deduplication.config.rule;

import org.sharedhealth.mci.deduplication.config.model.DuplicatePatientMapper;
import org.sharedhealth.mci.domain.model.PatientData;
import org.sharedhealth.mci.domain.model.SearchQuery;
import org.sharedhealth.mci.domain.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DuplicatePatientUidRule extends DuplicatePatientRule {

    private final String reason;

    @Autowired
    public DuplicatePatientUidRule(PatientRepository patientRepository, DuplicatePatientMapper duplicatePatientMapper) {
        super(patientRepository, duplicatePatientMapper);
        this.reason = DUPLICATE_REASON_UID;
    }

    @Override
    protected SearchQuery buildSearchQuery(PatientData patient) {
        SearchQuery query = new SearchQuery();
        query.setUid(patient.getUid());
        return query;
    }

    @Override
    protected String getReason() {
        return reason;
    }
}
