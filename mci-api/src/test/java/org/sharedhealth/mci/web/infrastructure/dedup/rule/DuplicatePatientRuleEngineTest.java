package org.sharedhealth.mci.web.infrastructure.dedup.rule;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.sharedhealth.mci.web.infrastructure.persistence.PatientRepository;
import org.sharedhealth.mci.web.mapper.Address;
import org.sharedhealth.mci.web.mapper.DuplicatePatientData;
import org.sharedhealth.mci.web.mapper.PatientData;
import org.sharedhealth.mci.web.mapper.SearchQuery;

import java.util.HashSet;
import java.util.List;

import static java.util.Arrays.asList;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.sharedhealth.mci.web.infrastructure.dedup.rule.DuplicatePatientRule.*;

public class DuplicatePatientRuleEngineTest {

    @Mock
    private PatientRepository patientRepository;

    private DuplicatePatientRuleEngine ruleEngine;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        ruleEngine = new DuplicatePatientRuleEngine(asList(new DuplicatePatientNidRule(patientRepository),
                new DuplicatePatientUidRule(patientRepository), new DuplicatePatientBrnRule(patientRepository),
                new DuplicatePatientNameAndAddressRule(patientRepository)));
    }

    @Test
    public void shouldFindDuplicates() {
        String healthId1 = "h100";
        String nid = "n000";
        String uid = "u000";
        String brn = "b000";
        String givenName = "John";
        String surname = "Doe";
        PatientData patient1 = new PatientData();
        patient1.setNationalId(nid);
        patient1.setHealthId(healthId1);
        patient1.setUid(uid);
        patient1.setBirthRegistrationNumber(brn);
        patient1.setGivenName(givenName);
        patient1.setSurName(surname);
        Address address1 = new Address("10", "11", "12");
        patient1.setAddress(address1);
        when(patientRepository.findByHealthId(healthId1)).thenReturn(patient1);

        String healthId2 = "h200";
        PatientData patient2 = buildPatient(healthId2, address1);
        String healthId3 = "h300";
        PatientData patient3 = buildPatient(healthId3, new Address("30", "31", "32"));
        String healthId4 = "h400";
        PatientData patient4 = buildPatient(healthId4, new Address("40", "41", "42"));
        String healthId5 = "h500";
        PatientData patient5 = buildPatient(healthId5, address1);
        when(patientRepository.findByHealthId(healthId2)).thenReturn(patient2);
        when(patientRepository.findByHealthId(healthId3)).thenReturn(patient3);
        when(patientRepository.findByHealthId(healthId4)).thenReturn(patient4);
        when(patientRepository.findByHealthId(healthId5)).thenReturn(patient5);

        SearchQuery nidQuery = new SearchQuery();
        nidQuery.setNid(nid);
        when(patientRepository.findAllByQuery(nidQuery)).thenReturn(asList(patient2));

        SearchQuery uidQuery = new SearchQuery();
        uidQuery.setUid(uid);
        when(patientRepository.findAllByQuery(uidQuery)).thenReturn(asList(patient3, patient4));

        SearchQuery brnQuery = new SearchQuery();
        brnQuery.setBin_brn(brn);
        when(patientRepository.findAllByQuery(brnQuery)).thenReturn(asList(patient4, patient5));

        SearchQuery nameAddressQuery = new SearchQuery();
        nameAddressQuery.setGiven_name(givenName);
        nameAddressQuery.setSur_name(surname);
        nameAddressQuery.setPresent_address("101112");
        when(patientRepository.findAllByQuery(nameAddressQuery)).thenReturn(asList(patient2, patient4, patient5));

        List<DuplicatePatientData> duplicates = ruleEngine.apply(healthId1);
        assertTrue(isNotEmpty(duplicates));
        assertEquals(4, duplicates.size());

        assertDuplicate(patient1, patient2, asList(DUPLICATE_REASON_NID, DUPLICATE_REASON_NAME_ADDRESS), duplicates.get(0));
        assertDuplicate(patient1, patient3, asList(DUPLICATE_REASON_UID), duplicates.get(1));
        assertDuplicate(patient1, patient4, asList(DUPLICATE_REASON_UID, DUPLICATE_REASON_BRN, DUPLICATE_REASON_NAME_ADDRESS), duplicates.get(2));
        assertDuplicate(patient1, patient5, asList(DUPLICATE_REASON_BRN, DUPLICATE_REASON_NAME_ADDRESS), duplicates.get(3));
    }

    private void assertDuplicate(PatientData patient1, PatientData patient2, List<String> reasons, DuplicatePatientData duplicate) {
        assertEquals(patient1.getHealthId(), duplicate.getHealthId1());
        assertEquals(patient2.getHealthId(), duplicate.getHealthId2());
        assertEquals(patient1.getCatchment(), duplicate.getCatchment1());
        assertEquals(patient2.getCatchment(), duplicate.getCatchment2());
        assertEquals(new HashSet<>(reasons), duplicate.getReasons());
    }

    private PatientData buildPatient(String healthId, Address address) {
        PatientData patient = new PatientData();
        patient.setHealthId(healthId);
        patient.setAddress(address);
        return patient;
    }
}