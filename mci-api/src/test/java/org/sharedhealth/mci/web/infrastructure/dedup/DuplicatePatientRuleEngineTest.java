package org.sharedhealth.mci.web.infrastructure.dedup;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.sharedhealth.mci.web.infrastructure.persistence.PatientRepository;
import org.sharedhealth.mci.web.mapper.Address;
import org.sharedhealth.mci.web.mapper.DuplicatePatientData;
import org.sharedhealth.mci.web.mapper.PatientData;
import org.sharedhealth.mci.web.mapper.SearchQuery;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertTrue;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class DuplicatePatientRuleEngineTest {

    @Mock
    private PatientRepository patientRepository;

    private DuplicatePatientRuleEngine ruleEngine;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        ruleEngine = new DuplicatePatientRuleEngine();
        ruleEngine.setRules(new DuplicatePatientNidRule(patientRepository),
                new DuplicatePatientUidRule(patientRepository), new DuplicatePatientBrnRule(patientRepository),
                new DuplicatePatientNameAndAddressRule(patientRepository));
    }

    @Test
    public void shouldFindDuplicates() {
        String hid = "h100";
        String nid = "n000";
        String uid = "u000";
        String brn = "b000";
        String givenName = "John";
        String surname = "Doe";
        String address = "102030";
        PatientData patient = new PatientData();
        patient.setNationalId(nid);
        patient.setHealthId(hid);
        patient.setUid(uid);
        patient.setBirthRegistrationNumber(brn);
        patient.setGivenName(givenName);
        patient.setSurName(surname);
        patient.setAddress(new Address("10", "20", "30"));
        when(patientRepository.findByHealthId(hid)).thenReturn(patient);

        SearchQuery nidQuery = new SearchQuery();
        nidQuery.setNid(nid);
        when(patientRepository.findAllByQuery(nidQuery)).thenReturn(buildPatients("h200"));

        SearchQuery uidQuery = new SearchQuery();
        uidQuery.setUid(uid);
        when(patientRepository.findAllByQuery(uidQuery)).thenReturn(buildPatients("h300", "h400"));

        SearchQuery brnQuery = new SearchQuery();
        brnQuery.setBin_brn(brn);
        when(patientRepository.findAllByQuery(brnQuery)).thenReturn(buildPatients("h400", "h500"));

        SearchQuery nameAddressQuery = new SearchQuery();
        nameAddressQuery.setGiven_name(givenName);
        nameAddressQuery.setSur_name(surname);
        nameAddressQuery.setPresent_address(address);
        when(patientRepository.findAllByQuery(nameAddressQuery)).thenReturn(buildPatients("h200", "h400", "h500"));

        List<DuplicatePatientData> duplicates = ruleEngine.apply(hid);
        assertTrue(isNotEmpty(duplicates));
    }

    private List<PatientData> buildPatients(String... healthIds) {
        List<PatientData> patients = new ArrayList<>();
        for (String healthId : healthIds) {
            PatientData patient = new PatientData();
            patient.setHealthId(healthId);
            patients.add(patient);
        }
        return patients;
    }
}