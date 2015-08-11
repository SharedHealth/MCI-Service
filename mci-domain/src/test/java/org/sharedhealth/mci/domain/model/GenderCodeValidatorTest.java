package org.sharedhealth.mci.domain.model;

import org.junit.Test;

public class GenderCodeValidatorTest extends BaseCodeValidatorTest<PatientData> {

    @Test
    public void shouldPassForValidValues() throws Exception {
        String[] validStatuses = {"M", "F", "O"};
        assertValidValues(validStatuses, "gender", PatientData.class);
    }

    @Test
    public void shouldFailForInvalidValues() throws Exception {
        String[] inValidRelations = {"some_invalid_code", "X", "1"};
        assertInvalidValues(inValidRelations, "gender", PatientData.class);
    }
}