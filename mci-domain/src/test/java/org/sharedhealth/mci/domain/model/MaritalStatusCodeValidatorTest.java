package org.sharedhealth.mci.domain.model;

import org.junit.Test;

public class MaritalStatusCodeValidatorTest extends BaseCodeValidatorTest<PatientData> {

    @Test
    public void shouldPassForValidValues() throws Exception {
        String[] validStatuses = {"1", "2"};
        assertValidValues(validStatuses, "maritalStatus", PatientData.class);
    }

    @Test
    public void shouldFailForInvalidValues() throws Exception {
        String[] inValidRelations = {"", "some_invalid_code", "6", "0"};
        assertInvalidValues(inValidRelations, "maritalStatus", PatientData.class);
    }
}