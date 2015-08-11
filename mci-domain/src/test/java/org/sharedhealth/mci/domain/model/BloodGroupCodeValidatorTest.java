package org.sharedhealth.mci.domain.model;

import org.junit.Test;

public class BloodGroupCodeValidatorTest extends BaseCodeValidatorTest<PatientData> {

    @Test
    public void shouldPassForValidValues() throws Exception {
        String[] validStatuses = {"1", "8"};
        assertValidValues(validStatuses, "bloodGroup", PatientData.class);
    }

    @Test
    public void shouldFailForInvalidValues() throws Exception {
        String[] inValidRelations = {"", "some_invalid_code", "00", "9"};
        assertInvalidValues(inValidRelations, "bloodGroup", PatientData.class);
    }
}