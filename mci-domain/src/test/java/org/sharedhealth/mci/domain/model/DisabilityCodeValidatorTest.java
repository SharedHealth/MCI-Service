package org.sharedhealth.mci.domain.model;

import org.junit.Test;

public class DisabilityCodeValidatorTest extends BaseCodeValidatorTest<PatientData> {

    @Test
    public void shouldPassForValidValues() throws Exception {
        String[] validStatuses = {"0", "1", "2", "3"};
        assertValidValues(validStatuses, "disability", PatientData.class);
    }

    @Test
    public void shouldFailForInvalidValues() throws Exception {
        String[] inValidRelations = {"", "some_invalid_code", "6", "01"};
        assertInvalidValues(inValidRelations, "disability", PatientData.class);
    }
}