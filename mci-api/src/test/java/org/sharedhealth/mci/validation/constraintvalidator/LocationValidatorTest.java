package org.sharedhealth.mci.validation.constraintvalidator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.mci.web.config.EnvironmentMock;
import org.sharedhealth.mci.web.config.WebMvcConfigTest;
import org.sharedhealth.mci.web.mapper.Address;
import org.sharedhealth.mci.web.mapper.PatientData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.sharedhealth.mci.web.infrastructure.persistence.TestUtil.setupLocation;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(initializers = EnvironmentMock.class, classes = {WebMvcConfigTest.class})
public class LocationValidatorTest {

    @Autowired
    @Qualifier("MCICassandraTemplate")
    private CassandraOperations cassandraOps;

    @Autowired
    private Validator validator;

    private Address address;

    @Before
    public void setup() {
        initAddressObject();
        setupLocation(cassandraOps);
    }

    private void initAddressObject() {
        address = new Address();
        address.setAddressLine("house-10");
        address.setDivisionId("10");
        address.setDistrictId("04");
        address.setUpazilaId("09");
        address.setCityCorporationId("20");
        address.setUnionOrUrbanWardId("01");
        address.setCountryCode("050");
    }

    @Test
    public void shouldPassForValidAddress() throws Exception {
        Set<ConstraintViolation<PatientData>> constraintViolations = validator.validateValue(PatientData.class, "address", address);
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void ruralWardIdIsOptional() throws Exception {
        address.setRuralWardId(null);
        Set<ConstraintViolation<PatientData>> constraintViolations = validator.validateValue(PatientData.class, "address", address);
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldFailForInvalidAddress() throws Exception {
        address.setDivisionId("00");
        assertInvalidAddressValue();
    }

    @Test
    public void shouldFailForInvalidAddressHierarchy() throws Exception {
        initAddressObject();
        address.setCityCorporationId(null);
        assertInvalidAddressValue();
    }

    @Test
    public void shouldFailForInvalidCountryForPresentAddress() throws Exception {
        address.setCountryCode("051");
        assertInvalidAddressValue();
    }

    private void assertInvalidAddressValue() {
        Set<ConstraintViolation<PatientData>> constraintViolations = validator.validateValue(PatientData.class, "address", address);
        assertEquals(1, constraintViolations.size());
        assertEquals("1004", constraintViolations.iterator().next().getMessage());
    }
}