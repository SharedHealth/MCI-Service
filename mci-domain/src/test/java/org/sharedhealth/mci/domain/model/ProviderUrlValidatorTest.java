package org.sharedhealth.mci.domain.model;

import org.junit.Test;

public class ProviderUrlValidatorTest extends BaseCodeValidatorTest<PatientData> {

    @Test
    public void shouldPassForValidValues() throws Exception {
        String[] values = {"http://localhost:9997/api/1.0/providers/100123.json"};
        assertValidValues(values, "provider", PatientData.class);
    }

    @Test
    public void shouldFailForInvalidValues() throws Exception {
        String[] values = {"https://localhost:9997/api/1.0/providers/100123.json"};
        assertInvalidValues(values, "provider", PatientData.class);
    }
}