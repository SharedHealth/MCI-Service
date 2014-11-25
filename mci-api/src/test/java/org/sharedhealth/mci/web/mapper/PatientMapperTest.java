package org.sharedhealth.mci.web.mapper;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PatientMapperTest {

    @Test
    public void shouldReturnFalseWhenNoIdPresent() {
        PatientMapper mapper = new PatientMapper();
        mapper.setNationalId("   ");
        mapper.setBirthRegistrationNumber("");
        mapper.setUid(null);
        assertFalse(mapper.containsMultipleIdentifier());
    }

    @Test
    public void shouldReturnFalseWhenSingleIdPresent() {
        PatientMapper mapper = new PatientMapper();
        mapper.setNationalId("100");
        mapper.setBirthRegistrationNumber("");
        mapper.setUid(null);
        assertFalse(mapper.containsMultipleIdentifier());
    }

    @Test
    public void shouldReturnTrueWhenNidAndBrnPresent() {
        PatientMapper mapper = new PatientMapper();
        mapper.setNationalId("100");
        mapper.setBirthRegistrationNumber("200");
        mapper.setUid(null);
        assertTrue(mapper.containsMultipleIdentifier());
    }

    @Test
    public void shouldReturnTrueWhenNidAndUidPresent() {
        PatientMapper mapper = new PatientMapper();
        mapper.setNationalId("100");
        mapper.setBirthRegistrationNumber(null);
        mapper.setUid("300");
        assertTrue(mapper.containsMultipleIdentifier());
    }

    @Test
    public void shouldReturnTrueWhenBrnAndUidPresent() {
        PatientMapper mapper = new PatientMapper();
        mapper.setNationalId(null);
        mapper.setBirthRegistrationNumber("200");
        mapper.setUid("300");
        assertTrue(mapper.containsMultipleIdentifier());
    }
}