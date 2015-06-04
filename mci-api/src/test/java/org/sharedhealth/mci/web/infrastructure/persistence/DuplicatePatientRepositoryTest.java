package org.sharedhealth.mci.web.infrastructure.persistence;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.sharedhealth.mci.web.mapper.Address;
import org.sharedhealth.mci.web.mapper.PatientData;
import org.sharedhealth.mci.web.model.DuplicatePatient;
import org.springframework.data.cassandra.core.CassandraOperations;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.junit.Assert.*;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class DuplicatePatientRepositoryTest {

    @Rule
    public ExpectedException expectedEx = none();

    @Mock
    private CassandraOperations cassandraOps;
    @Mock
    private PatientRepository patientRepository;

    private DuplicatePatientRepository duplicatePatientRepository;

    @Before
    public void setUp() {
        initMocks(this);
        duplicatePatientRepository = new DuplicatePatientRepository(patientRepository, cassandraOps);
    }

    @Test
    public void shouldVerifyWhetherDuplicatePatientsExist() {
        String healthId1 = "h100";
        PatientData patient1 = new PatientData();
        patient1.setHealthId(healthId1);
        patient1.setAddress(new Address("10", "20", "30"));
        String healthId2 = "h200";
        PatientData patient2 = new PatientData();
        patient2.setHealthId(healthId2);
        when(cassandraOps.select(anyString(), eq(DuplicatePatient.class))).thenReturn(asList(new DuplicatePatient()));

        assertTrue(isNotEmpty(duplicatePatientRepository.findDuplicatePatients(patient1, patient2)));
    }

    @Test
    public void shouldNotMergePatientsIfNoDuplicateInfoInDb() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Duplicates don't exist for health IDs 100 & 200 in db. Cannot merge.");

        String healthId1 = "100";
        String healthId2 = "200";

        PatientData patient1 = new PatientData();
        patient1.setHealthId(healthId1);
        patient1.setMergedWith(healthId2);
        patient1.setAddress(new Address("10", "20", "30"));
        patient1.setActive(false);

        PatientData patient2 = new PatientData();
        patient2.setHealthId(healthId2);
        patient2.setAddress(new Address("10", "20", "30"));
        patient2.setActive(true);

        when(patientRepository.findByHealthId(healthId1)).thenReturn(patient1);
        when(patientRepository.findByHealthId(healthId2)).thenReturn(patient2);
        when(cassandraOps.selectOne(anyString(), eq(DuplicatePatient.class))).thenReturn(null);

        duplicatePatientRepository.processDuplicates(patient1, patient2, true);
    }

    @Test
    public void shouldNotMergePatientsIfPatient2NotMergedFromPatient1() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Patient 2 [hid: 200] not merged from patient 1 [hid: 100]");

        String healthId1 = "100";
        String healthId2 = "200";

        PatientData patient1 = new PatientData();
        patient1.setHealthId(healthId1);
        patient1.setMergedWith(healthId2);
        patient1.setEducationLevel("01");
        patient1.setAddress(new Address("10", "20", "30"));
        patient1.setActive(true);

        PatientData patient2 = new PatientData();
        patient2.setHealthId(healthId2);
        patient2.setEducationLevel("02");
        patient2.setAddress(new Address("10", "20", "30"));
        patient2.setActive(true);

        when(patientRepository.findByHealthId(healthId1)).thenReturn(patient1);
        when(patientRepository.findByHealthId(healthId2)).thenReturn(patient2);
        when(cassandraOps.select(anyString(), eq(DuplicatePatient.class))).thenReturn(asList(new DuplicatePatient()));

        PatientData requestData2 = new PatientData();
        requestData2.setHealthId(healthId2);
        requestData2.setEducationLevel("03");
        requestData2.setAddress(new Address("10", "20", "30"));
        requestData2.setActive(true);
        duplicatePatientRepository.processDuplicates(patient1, requestData2, true);
    }

    @Test
    public void shouldNotMergePatientsIfPatient2NotMergedFromPatient1_WhenBlockDataDiffer() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Patient 2 [hid: 200] not merged from patient 1 [hid: 100]");

        String healthId1 = "100";
        String healthId2 = "200";

        PatientData patient1 = new PatientData();
        patient1.setHealthId(healthId1);
        patient1.setMergedWith(healthId2);
        patient1.setAddress(new Address("10", "20", "30"));
        patient1.setActive(true);

        PatientData patient2 = new PatientData();
        patient2.setHealthId(healthId2);
        patient2.setAddress(new Address("11", "22", "33"));
        patient2.setActive(true);

        when(patientRepository.findByHealthId(healthId1)).thenReturn(patient1);
        when(patientRepository.findByHealthId(healthId2)).thenReturn(patient2);
        when(cassandraOps.select(anyString(), eq(DuplicatePatient.class))).thenReturn(asList(new DuplicatePatient()));

        PatientData requestData2 = new PatientData();
        requestData2.setHealthId(healthId2);
        requestData2.setAddress(new Address("99", "88", "77"));
        requestData2.setActive(true);
        duplicatePatientRepository.processDuplicates(patient1, requestData2, true);
    }

    @Test
    public void shouldFindReasonsForDuplicates() {
        DuplicatePatient duplicatePatient = new DuplicatePatient();
        duplicatePatient.setReasons(new HashSet<>(asList("nid", "urn", "phone")));
        List<DuplicatePatient> duplicatePatients = asList(duplicatePatient, null, new DuplicatePatient());
        Set<String> reasons = duplicatePatientRepository.findReasonsForDuplicates(duplicatePatients);
        assertNotNull(reasons);
        assertEquals(3, reasons.size());
    }
}