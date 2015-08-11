package org.sharedhealth.mci.domain.model;

import org.junit.Test;

public class CountryCodeValidatorTest extends BaseCodeValidatorTest<Address> {

    @Test
    public void shouldPassForValidValues() throws Exception {
        String[] validStatuses = {"004", "008", "050", "051"};
        assertValidValues(validStatuses, "countryCode", Address.class);
    }

    @Test
    public void shouldFailForInvalidValues() throws Exception {
        String[] inValidRelations = {"", "some_invalid_code", "4", "01"};
        assertInvalidValues(inValidRelations, "countryCode", Address.class);
    }
}