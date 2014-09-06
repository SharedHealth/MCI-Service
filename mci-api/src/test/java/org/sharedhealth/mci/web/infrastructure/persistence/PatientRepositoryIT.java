package org.sharedhealth.mci.web.infrastructure.persistence;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.mci.web.config.EnvironmentMock;
import org.sharedhealth.mci.web.config.WebMvcConfig;
import org.sharedhealth.mci.web.mapper.Address;
import org.sharedhealth.mci.web.mapper.PatientMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

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

    private PatientMapper patientMapper;
    private String nationalId = "1234567890123";
    private String birthRegistrationNumber = "12345678901234567";
    private String uid = "12345678901";


    @Before
    public void setup() throws ExecutionException, InterruptedException {
        patientMapper = new PatientMapper();
        patientMapper.setNationalId(nationalId);
        patientMapper.setBirthRegistrationNumber(birthRegistrationNumber);
        patientMapper.setUid(uid);
        patientMapper.setGivenName("Scott");
        patientMapper.setSurName("Tiger");
        patientMapper.setDateOfBirth("2014-12-01");
        patientMapper.setGender("M");
        patientMapper.setOccupation("salaried");
        patientMapper.setEducationLevel("BA");

        Address address = new Address();
        address.setAddressLine("house-10");
        address.setDivisionId("10");
        address.setDistrictId("1020");
        address.setUpazillaId("102030");
        address.setUnionId("10203040");
        patientMapper.setAddress(address);

    }

    @Test
    public void shouldFindPatientWithMatchingGeneratedHealthId() throws ExecutionException, InterruptedException {
        String healthId = patientRepository.create(patientMapper).get();
        PatientMapper p = patientRepository.findByHealthId(healthId).get();
        assertNotNull(p);
        patientMapper.setHealthId(healthId);
        assertEquals(patientMapper, p);
    }

    @Test(expected = ExecutionException.class)
    public void shouldThrowException_IfPatientDoesNotExistForGivenHealthId() throws ExecutionException, InterruptedException {
        patientRepository.findByHealthId(UUID.randomUUID().toString()).get();
    }

    @Test(expected = ExecutionException.class)
    public void shouldThrowException_IfInvalidHealthIdProvidedForCreate() throws ExecutionException, InterruptedException {
        patientMapper.setHealthId("12");
        patientRepository.create(patientMapper).get();
    }

    @Test(expected = ExecutionException.class)
    public void shouldThrowException_IfPatientExistWithProvidedHealthIdOnCreate() throws ExecutionException, InterruptedException {
        patientRepository.create(patientMapper).get();
        patientMapper.setUid("12345678123");
        patientMapper.setBirthRegistrationNumber("12345678901234568");
        patientRepository.create(patientMapper).get();
    }

    @Test
    public void shouldFindPatientWithMatchingNationalId() throws ExecutionException, InterruptedException {
        String hid = patientRepository.create(patientMapper).get();
        final PatientMapper p = patientRepository.findByNationalId(nationalId).get();
        assertNotNull(p);
        patientMapper.setHealthId(hid);
        assertEquals(patientMapper, p);
    }

    @Test(expected = ExecutionException.class)
    public void shouldThrowException_IfPatientDoesNotExistGivenNationalId() throws ExecutionException, InterruptedException {
        patientRepository.findByNationalId(UUID.randomUUID().toString()).get();
    }

    @Test
    public void shouldFindPatientWithMatchingBirthRegistrationNumber() throws ExecutionException, InterruptedException {
        String hid = patientRepository.create(patientMapper).get();
        final PatientMapper p = patientRepository.findByBirthRegistrationNumber(birthRegistrationNumber).get();
        assertNotNull(p);
        patientMapper.setHealthId(hid);
        assertEquals(patientMapper, p);
    }

    @Test(expected = ExecutionException.class)
    public void shouldThrowException_IfPatientDoesNotExistGivenBirthRegistrationNumber() throws ExecutionException, InterruptedException {
        patientRepository.findByBirthRegistrationNumber(UUID.randomUUID().toString()).get();
    }

    @Test
    public void shouldFindPatientWithMatchingUid() throws ExecutionException, InterruptedException {
        String hid = patientRepository.create(patientMapper).get();

        final PatientMapper p = patientRepository.findByUid(uid).get();
        assertNotNull(p);
        patientMapper.setHealthId(hid);
        assertEquals(patientMapper, p);
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