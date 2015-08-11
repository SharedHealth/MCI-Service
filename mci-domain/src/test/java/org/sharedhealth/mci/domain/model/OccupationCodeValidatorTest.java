package org.sharedhealth.mci.domain.model;

import org.junit.Test;

public class OccupationCodeValidatorTest extends BaseCodeValidatorTest<PatientData> {

    @Test
    public void shouldPassForValidValues() throws Exception {
        String[] validStatuses = {"01", "02", "03", "92"};
        assertValidValues(validStatuses, "occupation", PatientData.class);
    }

    @Test
    public void shouldFailForInvalidValues() throws Exception {
        String[] inValidRelations = {"some_invalid_code", "4", "93"};
        assertInvalidValues(inValidRelations, "occupation", PatientData.class);
    }
}