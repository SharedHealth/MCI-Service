package org.sharedhealth.mci.web.mapper;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PatientMapperTest {

    @Test
    public void shouldReturnFalseWhenNoIdPresent() {
        PatientData mapper = new PatientData();
        mapper.setNationalId("   ");
        mapper.setBirthRegistrationNumber("");
        mapper.setUid(null);
        assertFalse(mapper.containsMultipleIdentifier());
    }

    @Test
    public void shouldReturnFalseWhenSingleIdPresent() {
        PatientData mapper = new PatientData();
        mapper.setNationalId("100");
        mapper.setBirthRegistrationNumber("");
        mapper.setUid(null);
        assertFalse(mapper.containsMultipleIdentifier());
    }

    @Test
    public void shouldReturnTrueWhenNidAndBrnPresent() {
        PatientData mapper = new PatientData();
        mapper.setNationalId("100");
        mapper.setBirthRegistrationNumber("200");
        mapper.setUid(null);
        assertTrue(mapper.containsMultipleIdentifier());
    }

    @Test
    public void shouldReturnTrueWhenNidAndUidPresent() {
        PatientData mapper = new PatientData();
        mapper.setNationalId("100");
        mapper.setBirthRegistrationNumber(null);
        mapper.setUid("300");
        assertTrue(mapper.containsMultipleIdentifier());
    }

    @Test
    public void shouldReturnTrueWhenBrnAndUidPresent() {
        PatientData mapper = new PatientData();
        mapper.setNationalId(null);
        mapper.setBirthRegistrationNumber("200");
        mapper.setUid("300");
        assertTrue(mapper.containsMultipleIdentifier());
    }
}