package org.mci.web.infrastructure.persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mci.web.config.EnvironmentMock;
import org.mci.web.config.WebMvcConfig;
import org.mci.web.model.Address;
import org.mci.web.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cassandra.core.CqlOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(initializers = EnvironmentMock.class, classes = WebMvcConfig.class)
public class PatientRepositoryIT {
    @Autowired
    @Qualifier("MCICassandraTemplate")
    private CqlOperations cqlTemplate;

    @Autowired
    private PatientRepository patientRepository;

    private Patient patient;
    private String healthId;
    private String nationalId = "nid-222";

    @Before
    public void setup() throws ExecutionException, InterruptedException {
        patient = new Patient();
        patient.setNationalId(nationalId);
        patient.setFirstName("Scott");
        patient.setLastName("Tiger");
        patient.setDateOfBirth("2014-12-01");
        patient.setGender("1");
        patient.setOccupation("salaried");
        patient.setEducationLevel("BA");
        patient.setPrimaryContact("someone");

        Address address = new Address();
        address.setAddressLine("house-10");
        address.setDivisionId("10");
        address.setDistrictId("1020");
        address.setUpazillaId("102030");
        address.setUnionId("10203040");
        patient.setAddress(address);
        healthId = patientRepository.create(patient).get();
        patient.setHealthId(healthId);
    }

    @Test
    public void shouldFindPatientWithMatchingHealthId() throws ExecutionException, InterruptedException {
        Patient p = patientRepository.findByHealthId(healthId).get();
        assertNotNull(p);
        assertEquals(patient, p);
    }

    @Test
    public void shouldNotFindPatientWithoutMatchingHealthId() throws ExecutionException, InterruptedException {
        assertNull(patientRepository.findByHealthId(healthId + "invalid").get());
    }

    @Test
    public void shouldFindPatientWithMatchingNationalId() throws ExecutionException, InterruptedException {
        final Patient p = patientRepository.findByNationalId(nationalId).get();
        assertNotNull(p);
        assertEquals(patient, p);
    }

    @After
    public void teardown() {
        cqlTemplate.execute("truncate patient");
    }
}