package org.sharedhealth.mci.web.infrastructure.dedup.rule;

import org.sharedhealth.mci.web.infrastructure.persistence.PatientRepository;
import org.sharedhealth.mci.web.mapper.Address;
import org.sharedhealth.mci.web.mapper.DuplicatePatientMapper;
import org.sharedhealth.mci.web.mapper.PatientData;
import org.sharedhealth.mci.web.mapper.SearchQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.apache.commons.lang3.StringUtils.defaultString;

@Component
public class DuplicatePatientNameAndAddressRule extends DuplicatePatientRule {

    private final String reason;

    @Autowired
    public DuplicatePatientNameAndAddressRule(PatientRepository patientRepository, DuplicatePatientMapper duplicatePatientMapper) {
        super(patientRepository, duplicatePatientMapper);
        this.reason = DUPLICATE_REASON_NAME_ADDRESS;
    }

    @Override
    protected SearchQuery buildSearchQuery(PatientData patient) {
        SearchQuery query = new SearchQuery();
        query.setGiven_name(patient.getGivenName());
        query.setSur_name(patient.getSurName());
        Address address = patient.getAddress();
        query.setPresent_address(defaultString(address.getDivisionId()) + defaultString(address.getDistrictId())
                + defaultString(address.getUpazilaId()) + defaultString(address.getUnionOrUrbanWardId())
                + defaultString(address.getRuralWardId()));
        return query;
    }

    @Override
    protected String getReason() {
        return reason;
    }
}
