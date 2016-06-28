package org.sharedhealth.mci.validation.constraintvalidator;

import net.sf.ehcache.CacheManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.mci.domain.model.Address;
import org.sharedhealth.mci.domain.model.PatientData;
import org.sharedhealth.mci.domain.util.BaseIntegrationTest;
import org.sharedhealth.mci.domain.util.TestUtil;
import org.sharedhealth.mci.web.config.WebMvcConfigTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.sharedhealth.mci.domain.util.TestUtil.setupLocation;

@WebAppConfiguration
@ContextConfiguration(classes = {WebMvcConfigTest.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class LocationValidatorTest extends BaseIntegrationTest {

    @Autowired
    private Validator validator;

    private Address address;

    @Before
    public void setup() {
        initAddressObject();
        setupLocation(cassandraOps);
    }

    @After
    public void tearDown() throws Exception {
        TestUtil.truncateAllColumnFamilies(cassandraOps);
        CacheManager.getInstance().clearAll();
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

    public void shouldFailIfAddressLineIsBlank() {
        address.setAddressLine(null);
        assertInvalidAddressValue();
    }

    private void assertInvalidAddressValue() {
        Set<ConstraintViolation<PatientData>> constraintViolations = validator.validateValue(PatientData.class, "address", address);
        assertEquals(1, constraintViolations.size());
        assertEquals("1004", constraintViolations.iterator().next().getMessage());
    }
}