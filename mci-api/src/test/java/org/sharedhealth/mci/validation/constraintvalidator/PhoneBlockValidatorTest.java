package org.sharedhealth.mci.validation.constraintvalidator;

import org.junit.Before;
import org.junit.Test;
import org.sharedhealth.mci.domain.model.PatientData;
import org.sharedhealth.mci.domain.model.PhoneNumber;
import org.sharedhealth.mci.domain.model.ValidationAwareMapper;

import javax.validation.ConstraintViolation;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class PhoneBlockValidatorTest extends ValidationAwareMapper {

    private PhoneNumber phoneNumber;

    @Before
    public void init(){
        phoneNumber = new PhoneNumber();
    }

    @Test
    public void shouldPassForEmptyBlock() throws Exception {
        assertValidBlock(phoneNumber);
    }

    @Test
    public void shouldPassIfOnlyNumberGiven() throws Exception {
        phoneNumber.setNumber("123456789");
        assertValidBlock(phoneNumber);
    }

    @Test
    public void shouldFailIfNumberIsNullForNoEmptyBlock() throws Exception {
        phoneNumber.setExtension("123");
        Set<ConstraintViolation<PatientData>> constraintViolations = validator.validateValue(PatientData.class, "phoneNumber", phoneNumber);
        assertEquals(1, constraintViolations.size());
        assertEquals("1001", constraintViolations.iterator().next().getMessage());
    }

    private void assertValidBlock(PhoneNumber phoneNumber) {
        Set<ConstraintViolation<PatientData>> constraintViolations = validator.validateValue(PatientData.class, "phoneNumber", phoneNumber);
        assertEquals(0, constraintViolations.size());
    }
}