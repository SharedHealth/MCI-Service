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

import static java.util.Arrays.asList;
import static org.junit.Assert.assertTrue;
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

        assertTrue(duplicatePatientRepository.duplicatePatientExists(patient1, patient2));
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
}