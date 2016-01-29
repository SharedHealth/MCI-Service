package org.sharedhealth.mci.domain.model;

import org.junit.Test;

public class EducationLevelCodeValidatorTest extends BaseCodeValidatorTest<PatientData> {

    @Test
    public void shouldPassForValidValues() throws Exception {
        String[] validStatuses = {"02", "01"};
        assertValidValues(validStatuses, "educationLevel", PatientData.class);
    }

    @Test
    public void shouldFailForInvalidValues() throws Exception {
        String[] inValidRelations = {"some_invalid_code", "4", "20"};
        assertInvalidValues(inValidRelations, "educationLevel", PatientData.class);
    }
}