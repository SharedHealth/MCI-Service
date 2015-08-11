package org.sharedhealth.mci.domain.model;

import org.junit.Test;

import javax.validation.ConstraintViolation;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class PatientStatusCodeValidatorTest extends BaseCodeValidatorTest<PatientStatus> {

    @Test
    public void shouldPassForValidValues() throws Exception {
        String[] validStatus = {"1", "2", "3"};
        assertValidValues(validStatus, "type", PatientStatus.class);
    }

    @Test
    public void shouldFailForInvalidValues() throws Exception {
        PatientStatus patientStatus = new PatientStatus();
        patientStatus.setType("4");
        Set<ConstraintViolation<PatientStatus>> constraintViolations = getValidator().validate(patientStatus);
        assertEquals(1, constraintViolations.size());
        assertEquals("1004", constraintViolations.iterator().next().getMessage());
    }
}