package org.sharedhealth.mci.domain.model;

import org.junit.Test;

public class ReligionCodeValidatorTest extends BaseCodeValidatorTest<PatientData> {

    @Test
    public void shouldPassForValidValues() throws Exception {
        String[] validStatuses = {"0", "1", "2", "9"};
        assertValidValues(validStatuses, "religion", PatientData.class);
    }

    @Test
    public void shouldFailForInvalidValues() throws Exception {
        String[] inValidRelations = {"", "some_invalid_code", "04", "10"};
        assertInvalidValues(inValidRelations, "religion", PatientData.class);
    }
}