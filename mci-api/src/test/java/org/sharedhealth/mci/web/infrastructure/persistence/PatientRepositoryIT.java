package org.sharedhealth.mci.web.infrastructure.persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.mci.web.config.EnvironmentMock;
import org.sharedhealth.mci.web.config.WebMvcConfig;
import org.sharedhealth.mci.web.model.Address;
import org.sharedhealth.mci.web.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.data.cassandra.core.CassandraOperations;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(initializers = EnvironmentMock.class, classes = WebMvcConfig.class)
public class PatientRepositoryIT {
    @Autowired
    @Qualifier("MCICassandraTemplate")
    private CassandraOperations cqlTemplate;

    @Autowired
    private PatientRepository patientRepository;

    private Patient patient;
    private String nationalId = "1234567890123";
    private String birthRegistrationNumber = "12345678901234567";
    private String uid = "12345678901";

    @Before
    public void setup() throws ExecutionException, InterruptedException {
        patient = new Patient();
        patient.setNationalId(nationalId);
        patient.setBirthRegistrationNumber(birthRegistrationNumber);
        patient.setUid(uid);
        patient.setFirstName("Scott");
        patient.setLastName("Tiger");
        patient.setDateOfBirth("2014-12-01");
        patient.setGender("1");
        patient.setOccupation("salaried");
        patient.setEducationLevel("BA");

        Address address = new Address();
        address.setAddressLine("house-10");
        address.setDivisionId("10");
        address.setDistrictId("1020");
        address.setUpazillaId("102030");
        address.setUnionId("10203040");
        patient.setAddress(address);
    }

    @Test
    public void shouldFindPatientWithMatchingGeneratedHealthId() throws ExecutionException, InterruptedException {
        String healthId = patientRepository.create(patient).get();
        Patient p = patientRepository.findByHealthId(healthId).get();
        assertNotNull(p);
        patient.setHealthId(healthId);
        assertEquals(patient, p);
    }

    @Test(expected = ExecutionException.class)
    public void shouldThrowException_IfPatientDoesNotExistForGivenHealthId() throws ExecutionException, InterruptedException {
        patientRepository.findByHealthId(UUID.randomUUID().toString()).get();
    }

    @Test(expected = ExecutionException.class)
    public void shouldThrowException_IfInvalidHealthIdProvidedForCreate() throws ExecutionException, InterruptedException {
        patient.setHealthId("12");
        patientRepository.create(patient).get();
    }

    @Test(expected = ExecutionException.class)
    public void shouldThrowException_IfPatientExistWithProvidedHealthIdOnCreate() throws ExecutionException, InterruptedException {
        patientRepository.create(patient).get();
        patient.setUid("12345678123");
        patient.setBirthRegistrationNumber("12345678901234568");
        patientRepository.create(patient).get();
    }

    @Test
    public void shouldFindPatientWithMatchingNationalId() throws ExecutionException, InterruptedException {
        String hid = patientRepository.create(patient).get();
        final Patient p = patientRepository.findByNationalId(nationalId).get();
        assertNotNull(p);
        patient.setHealthId(hid);
        assertEquals(patient, p);
    }

    @Test(expected = ExecutionException.class)
    public void shouldThrowException_IfPatientDoesNotExistGivenNationalId() throws ExecutionException, InterruptedException {
        patientRepository.findByNationalId(UUID.randomUUID().toString()).get();
    }

    @Test
    public void shouldFindPatientWithMatchingBirthRegistrationNumber() throws ExecutionException, InterruptedException {
        String hid = patientRepository.create(patient).get();
        final Patient p = patientRepository.findByBirthRegistrationNumber(birthRegistrationNumber).get();
        assertNotNull(p);
        patient.setHealthId(hid);
        assertEquals(patient, p);
    }

    @Test(expected = ExecutionException.class)
    public void shouldThrowException_IfPatientDoesNotExistGivenBirthRegistrationNumber() throws ExecutionException, InterruptedException {
        patientRepository.findByBirthRegistrationNumber(UUID.randomUUID().toString()).get();
    }

    @Test
    public void shouldFindPatientWithMatchingUid() throws ExecutionException, InterruptedException {
        String hid = patientRepository.create(patient).get();

        final Patient p = patientRepository.findByUid(uid).get();
        assertNotNull(p);
        patient.setHealthId(hid);
        assertEquals(patient, p);
    }

    @Test(expected = ExecutionException.class)
    public void shouldThrowException_IfPatientDoesNotExistGivenUid() throws ExecutionException, InterruptedException {
        patientRepository.findByUid(UUID.randomUUID().toString()).get();
    }

    @After
    public void teardown() {
        cqlTemplate.execute("truncate patient");
    }
}