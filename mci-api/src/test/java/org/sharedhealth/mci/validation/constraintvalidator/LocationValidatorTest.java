package org.sharedhealth.mci.validation.constraintvalidator;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.mci.web.config.EnvironmentMock;
import org.sharedhealth.mci.web.config.WebMvcConfigTest;
import org.sharedhealth.mci.web.mapper.Address;
import org.sharedhealth.mci.web.mapper.PatientMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(initializers = EnvironmentMock.class, classes = WebMvcConfigTest.class)
public class LocationValidatorTest {

    @Autowired
    private Validator validator;

    private Address address;

    @Before
    public void setup() {
        address = new Address();
        address.setAddressLine("house-10");
        address.setDivisionId("10");
        address.setDistrictId("04");
        address.setUpazillaId("09");
        address.setCityCorporationId("20");
        address.setVillage("10");
        address.setWardId("01");
        address.setCountryCode("050");
    }

    @Test
    public void shouldPassForValidAddress() throws Exception {
        Set<ConstraintViolation<PatientMapper>> constraintViolations = validator.validateValue(PatientMapper.class, "address", address);
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldFailForInvalidAddress() throws Exception {
        address.setDivisionId("00");
        Set<ConstraintViolation<PatientMapper>> constraintViolations = validator.validateValue(PatientMapper.class, "address", address);
        assertEquals(1, constraintViolations.size());
        assertEquals("1004", constraintViolations.iterator().next().getMessage());
    }
}