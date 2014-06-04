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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(initializers = EnvironmentMock.class, classes = WebMvcConfig.class)
public class PatientRepositoryIT {
    @Autowired
    @Qualifier("MCICassandraTemplate")
    private CqlOperations cqlTemplate;

    @Autowired
    private PatientRepository patientRepository;
    private String healthId;
    private String nationalId = "nid-222";

    @Before
    public void setup() throws ExecutionException, InterruptedException {
        Patient patient = new Patient();
        patient.setNationalId(nationalId);
        patient.setFirstName("Scott");
        patient.setLastName("Tiger");
        patient.setGender("1");
        Address address = new Address();
        address.setDivisionId("10");
        address.setDistrictId("1020");
        address.setUpazillaId("102030");
        address.setUnionId("10203040");
        patient.setAddress(address);
        healthId = patientRepository.create(patient).get();
    }

    @Test
    public void shouldFindPatientWithMatchingHealthId() throws ExecutionException, InterruptedException {
        assertNotNull(patientRepository.findByHealthId(healthId).get());
    }

    @Test
    public void shouldNotFindPatientWithoutMatchingHealthId() throws ExecutionException, InterruptedException {
        assertNull(patientRepository.findByHealthId(healthId + "invalid").get());
    }

    @Test
    public void shouldFindPatientWithMatchingNationalId() throws ExecutionException, InterruptedException {
        assertNotNull(patientRepository.findByNationalId(nationalId).get());
    }

    @After
    public void teardown() {
        cqlTemplate.execute("truncate patient");
    }

}