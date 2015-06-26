package org.sharedhealth.mci.web.infrastructure.dedup.rule;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.sharedhealth.mci.web.infrastructure.persistence.PatientRepository;
import org.sharedhealth.mci.web.mapper.Address;
import org.sharedhealth.mci.web.mapper.DuplicatePatientData;
import org.sharedhealth.mci.web.mapper.DuplicatePatientMapper;
import org.sharedhealth.mci.web.mapper.PatientData;
import org.sharedhealth.mci.web.mapper.PatientMapper;
import org.sharedhealth.mci.web.mapper.SearchQuery;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static java.util.Arrays.asList;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.sharedhealth.mci.web.infrastructure.dedup.rule.DuplicatePatientRule.DUPLICATE_REASON_BRN;
import static org.sharedhealth.mci.web.infrastructure.dedup.rule.DuplicatePatientRule.DUPLICATE_REASON_NAME_ADDRESS;
import static org.sharedhealth.mci.web.infrastructure.dedup.rule.DuplicatePatientRule.DUPLICATE_REASON_NID;
import static org.sharedhealth.mci.web.infrastructure.dedup.rule.DuplicatePatientRule.DUPLICATE_REASON_UID;

public class DuplicatePatientRuleTest {

    @Mock
    private PatientRepository patientRepository;

    private DuplicatePatientRule duplicatePatientRule;
    private List<PatientData> patients;
    private DuplicatePatientMapper duplicatePatientMapper;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        patients = buildPatients();
        duplicatePatientMapper = new DuplicatePatientMapper(patientRepository, new PatientMapper());
    }

    @Test
    public void shouldFindDuplicatesByNid() {
        duplicatePatientRule = new DuplicatePatientNidRule(patientRepository, duplicatePatientMapper);
        String nationalId = "n100";
        PatientData patient = patients.get(0);
        patient.setNationalId(nationalId);

        SearchQuery query = new SearchQuery();
        query.setNid(nationalId);

        shouldFindDuplicates(patient, query, DUPLICATE_REASON_NID);
    }

    @Test
    public void shouldFindDuplicatesByUid() {
        duplicatePatientRule = new DuplicatePatientUidRule(patientRepository, duplicatePatientMapper);
        String uid = "u100";
        PatientData patient = patients.get(0);
        patient.setUid(uid);

        SearchQuery query = new SearchQuery();
        query.setUid(uid);

        shouldFindDuplicates(patient, query, DUPLICATE_REASON_UID);
    }

    @Test
    public void shouldFindDuplicatesByBrn() {
        duplicatePatientRule = new DuplicatePatientBrnRule(patientRepository, duplicatePatientMapper);
        String brn = "b100";
        PatientData patient = patients.get(0);
        patient.setBirthRegistrationNumber(brn);

        SearchQuery query = new SearchQuery();
        query.setBin_brn(brn);

        shouldFindDuplicates(patient, query, DUPLICATE_REASON_BRN);
    }

    @Test
    public void shouldFindDuplicatesByNameAndAddress() {
        duplicatePatientRule = new DuplicatePatientNameAndAddressRule(patientRepository, duplicatePatientMapper);
        String givenName = "John";
        String surname = "Doe";
        PatientData patient = patients.get(0);
        patient.setGivenName(givenName);
        patient.setSurName(surname);

        SearchQuery query = new SearchQuery();
        query.setGiven_name(givenName);
        query.setSur_name(surname);
        query.setPresent_address("101112");

        shouldFindDuplicates(patient, query, DUPLICATE_REASON_NAME_ADDRESS);
    }

    private void shouldFindDuplicates(PatientData patient1, SearchQuery query, String reason) {
        String healthId1 = "h100";
        patient1.setHealthId(healthId1);
        patient1.setAddress(new Address("10", "11", "12"));
        when(patientRepository.findByHealthId(healthId1)).thenReturn(patient1);

        List<PatientData> patients = buildPatients();
        when(patientRepository.findAllByQuery(query)).thenReturn(patients);

        List<DuplicatePatientData> duplicates = new ArrayList<>();
        duplicatePatientRule.apply(healthId1, duplicates);
        assertTrue(isNotEmpty(duplicates));
        assertEquals(2, duplicates.size());

        assertDuplicateEquals(asList(buildDuplicate(patients.get(0), patients.get(1), reason),
                buildDuplicate(patients.get(0), patients.get(2), reason)), duplicates);
    }

    private void assertDuplicateEquals(List<DuplicatePatientData> duplicates1, List<DuplicatePatientData> duplicates2) {
        assertNotNull(duplicates1);
        assertNotNull(duplicates2);
        int size = duplicates1.size();
        assertEquals(size, duplicates2.size());

        for (int i = 0; i < size - 1; i++) {
            assertEquals(duplicates1.get(i).getPatient1(), duplicates2.get(i).getPatient1());
            assertEquals(duplicates1.get(i).getPatient2(), duplicates2.get(i).getPatient2());
            assertEquals(duplicates1.get(i).getReasons(), duplicates2.get(i).getReasons());
        }
    }

    private DuplicatePatientData buildDuplicate(PatientData patient1, PatientData patient2, String reason) {
        return duplicatePatientMapper.mapToDuplicatePatientData(patient1, patient2, new HashSet<>(asList(reason)));
    }

    private List<PatientData> buildPatients() {
        PatientData patient1 = new PatientData();
        String healthId1 = "h100";
        patient1.setHealthId(healthId1);
        patient1.setAddress(new Address("10", "11", "12"));
        patient1.setNationalId("n100");
        patient1.setUid("u100");
        patient1.setGivenName("John");
        patient1.setSurName("Doe");
        patient1.setBirthRegistrationNumber("b100");

        PatientData patient2 = new PatientData();
        String healthId2 = "h200";
        patient2.setHealthId(healthId2);
        patient2.setAddress(new Address("20", "21", "22"));
        patient2.setNationalId("n100");
        patient2.setBirthRegistrationNumber("b100");
        patient2.setUid("u100");
        patient2.setGivenName("John");
        patient2.setSurName("Doe");
        when(patientRepository.findByHealthId(healthId2)).thenReturn(patient2);

        PatientData patient3 = new PatientData();
        String healthId3 = "h300";
        patient3.setHealthId(healthId3);
        patient3.setAddress(new Address("30", "31", "32"));
        patient3.setNationalId("n100");
        patient3.setBirthRegistrationNumber("b100");
        patient3.setUid("u100");
        patient3.setGivenName("John");
        patient3.setSurName("Doe");
        when(patientRepository.findByHealthId(healthId3)).thenReturn(patient3);

        PatientData patient4 = new PatientData();
        String healthId4 = "h400";
        patient4.setHealthId(healthId4);
        patient4.setAddress(new Address("40", "41", "42"));
        patient4.setNationalId("n100");
        patient4.setBirthRegistrationNumber("b100");
        patient4.setUid("u100");
        patient4.setGivenName("John");
        patient4.setSurName("Doe");
        patient4.setActive(false);
        when(patientRepository.findByHealthId(healthId4)).thenReturn(patient4);

        return asList(patient1, patient2, patient3, patient4);
    }
}