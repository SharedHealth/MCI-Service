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
import java.util.HashSet;
import java.util.List;

import static java.util.Arrays.asList;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.sharedhealth.mci.web.infrastructure.dedup.DuplicatePatientRule.*;

public class DuplicatePatientRuleTest {

    @Mock
    private PatientRepository patientRepository;

    private DuplicatePatientRule duplicatePatientRule;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void shouldFindDuplicatesByNid() {
        duplicatePatientRule = new DuplicatePatientNidRule(patientRepository);
        String nationalId = "n100";
        PatientData patient = new PatientData();
        patient.setNationalId(nationalId);

        SearchQuery query = new SearchQuery();
        query.setNid(nationalId);

        shouldFindDuplicates(patient, query, DUPLICATE_REASON_NID);
    }

    @Test
    public void shouldFindDuplicatesByUid() {
        duplicatePatientRule = new DuplicatePatientUidRule(patientRepository);
        String uid = "u100";
        PatientData patient = new PatientData();
        patient.setUid(uid);

        SearchQuery query = new SearchQuery();
        query.setUid(uid);

        shouldFindDuplicates(patient, query, DUPLICATE_REASON_UID);
    }

    @Test
    public void shouldFindDuplicatesByBrn() {
        duplicatePatientRule = new DuplicatePatientBrnRule(patientRepository);
        String brn = "b100";
        PatientData patient = new PatientData();
        patient.setBirthRegistrationNumber(brn);

        SearchQuery query = new SearchQuery();
        query.setBin_brn(brn);

        shouldFindDuplicates(patient, query, DUPLICATE_REASON_BRN);
    }

    @Test
    public void shouldFindDuplicatesByNameAndAddress() {
        duplicatePatientRule = new DuplicatePatientNameAndAddressRule(patientRepository);
        String givenName = "John";
        String surname = "Doe";
        String address = "102030";
        PatientData patient = new PatientData();
        patient.setGivenName(givenName);
        patient.setSurName(surname);
        patient.setAddress(new Address("10", "20", "30"));

        SearchQuery query = new SearchQuery();
        query.setGiven_name(givenName);
        query.setSur_name(surname);
        query.setPresent_address(address);

        shouldFindDuplicates(patient, query, DUPLICATE_REASON_NAME_ADDRESS);
    }

    private void shouldFindDuplicates(PatientData patient, SearchQuery query, String reason) {
        String healthId = "h100";
        patient.setHealthId(healthId);

        when(patientRepository.findByHealthId(healthId)).thenReturn(patient);
        when(patientRepository.findAllByQuery(query)).thenReturn(buildPatients());

        List<DuplicatePatientData> duplicates = new ArrayList<>();
        duplicatePatientRule.apply(healthId, duplicates);
        assertTrue(isNotEmpty(duplicates));
        assertEquals(3, duplicates.size());

        assertEquals(asList(buildDuplicate(healthId, "h200", reason), buildDuplicate(healthId, "h300", reason),
                buildDuplicate(healthId, "h400", reason)), duplicates);
    }

    private DuplicatePatientData buildDuplicate(String healthId1, String healthId2, String reason) {
        return new DuplicatePatientData(healthId1, healthId2, new HashSet<>(asList(reason)), null);
    }

    private List<PatientData> buildPatients() {
        PatientData patient1 = new PatientData();
        patient1.setHealthId("h100");
        PatientData patient2 = new PatientData();
        patient2.setHealthId("h200");
        PatientData patient3 = new PatientData();
        patient3.setHealthId("h300");
        PatientData patient4 = new PatientData();
        patient4.setHealthId("h400");
        return asList(patient1, patient2, patient3, patient4);
    }
}