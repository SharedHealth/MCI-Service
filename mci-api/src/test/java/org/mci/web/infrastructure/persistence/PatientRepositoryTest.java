package org.mci.web.infrastructure.persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mci.web.config.EnvironmentMock;
import org.mci.web.config.WebMvcConfig;
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
public class PatientRepositoryTest {
    private final String healthId = "testHealthId";

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    @Qualifier("MCICassandraTemplate")
    private CqlOperations cqlTemplate;

    @Before
    public void setup() {
        cqlTemplate.execute("INSERT into patient (health_id) VALUES ('" + healthId + "');");
    }

    @Test
    public void shouldFindPatientWithMatchingHealthId() throws ExecutionException, InterruptedException {
        assertNotNull(patientRepository.find(healthId).get());
    }

    @Test
    public void shouldNotFindPatientWithoutMatchingHealthId() throws ExecutionException, InterruptedException {
        assertNull(patientRepository.find(healthId + "invalid").get());
    }

    @After
    public void teardown() {
        cqlTemplate.execute("truncate patient");
    }

}