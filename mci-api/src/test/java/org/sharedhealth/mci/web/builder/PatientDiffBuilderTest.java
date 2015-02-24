package org.sharedhealth.mci.web.builder;

import org.junit.Test;
import org.sharedhealth.mci.web.mapper.Address;
import org.sharedhealth.mci.web.mapper.PatientData;
import org.sharedhealth.mci.web.mapper.PhoneNumber;
import org.sharedhealth.mci.web.mapper.Relation;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.sharedhealth.mci.web.builder.PatientDiffBuilder.EMPTY_VALUE;
import static org.sharedhealth.mci.web.utils.JsonConstants.*;

public class PatientDiffBuilderTest {

    @Test
    public void shouldReturnEmptyMapWhenPatientsHaveNoDiff() {
        Relation relation1 = new Relation();
        relation1.setType("X");
        relation1.setHealthId("X1");
        relation1.setGivenName("XName");
        Relation relation2 = new Relation();
        relation2.setType("Y");
        relation2.setHealthId("Y1");
        relation2.setGivenName("YName");

        PatientData patient1 = new PatientData();
        patient1.setHealthId(new String("h100"));
        patient1.setGivenName("Harry");
        patient1.setRelations(asList(relation1, relation2));

        PatientData patient2 = new PatientData();
        patient2.setHealthId("h100");
        patient2.setGivenName("Harry");
        patient2.setRelations(asList(relation2, relation1));

        Map<String, Map<String, Object>> diff = new PatientDiffBuilder(patient1, patient2).build();
        assertNotNull(diff);
        assertEquals(0, diff.size());
    }

    @Test
    public void shouldReturnDiffWhenPatientsHaveOnlyRelationsDiffAndNewPatientDoesNotHaveRelations() {
        Relation relation1 = new Relation();
        relation1.setType("X");
        relation1.setHealthId("X1");
        relation1.setGivenName("XName");
        Relation relation2 = new Relation();
        relation2.setType("Y");
        relation2.setHealthId("Y1");
        relation2.setGivenName("YName");
        List<Relation> relations = asList(relation1, relation2);

        PatientData patient1 = new PatientData();
        patient1.setGivenName("Harry");
        patient1.setRelations(relations);

        PatientData patient2 = new PatientData();
        patient2.setGivenName("Harry");

        Map<String, Map<String, Object>> diff = new PatientDiffBuilder(patient1, patient2).build();
        assertNotNull(diff);
        assertEquals(1, diff.size());

        Map<String, Object> changeSet = diff.get(RELATIONS);
        assertNotNull(changeSet);
        assertEquals(relations, changeSet.get(OLD_VALUE));
        assertEquals(EMPTY_VALUE, changeSet.get(NEW_VALUE));
    }

    @Test
    public void shouldReturnDiffWhenPatientsHaveOnlyRelationsDiffAndOldPatientDoesNotHaveRelations() {
        PatientData patient1 = new PatientData();
        patient1.setGivenName("Harry");

        Relation relation1 = new Relation();
        relation1.setType("X");
        relation1.setHealthId("X1");
        relation1.setGivenName("XName");
        Relation relation2 = new Relation();
        relation2.setType("Y");
        relation2.setHealthId("Y1");
        relation2.setGivenName("YName");
        List<Relation> relations = asList(relation1, relation2);

        PatientData patient2 = new PatientData();
        patient2.setGivenName("Harry");
        patient2.setRelations(relations);

        Map<String, Map<String, Object>> diff = new PatientDiffBuilder(patient1, patient2).build();
        assertNotNull(diff);
        assertEquals(1, diff.size());

        Map<String, Object> changeSet = diff.get(RELATIONS);
        assertNotNull(changeSet);
        assertEquals(EMPTY_VALUE, changeSet.get(OLD_VALUE));
        assertEquals(relations, changeSet.get(NEW_VALUE));
    }

    @Test
    public void shouldReturnDiffWhenPatientsHaveMultipleDiff() {
        PatientData patient1 = new PatientData();
        patient1.setGivenName("Harry");
        patient1.setReligion("00");
        patient1.setPermanentAddress(new Address("10", "20", "30"));

        PhoneNumber phoneNumber1 = new PhoneNumber();
        phoneNumber1.setNumber("100200300");
        phoneNumber1.setExtension("123");
        patient1.setPhoneNumber(phoneNumber1);

        PhoneNumber primaryContactNumber1 = new PhoneNumber();
        primaryContactNumber1.setCountryCode("91");
        patient1.setPrimaryContactNumber(primaryContactNumber1);

        PatientData patient2 = new PatientData();
        patient2.setGivenName("Potter");
        patient2.setSurName("");
        patient2.setReligion("00");
        patient2.setAddress(new Address("10", "20", "30"));

        PhoneNumber phoneNumber2 = new PhoneNumber();
        phoneNumber2.setNumber("111222333");
        phoneNumber2.setExtension("456");
        patient2.setPhoneNumber(phoneNumber2);

        PhoneNumber primaryContactNumber2 = new PhoneNumber();
        primaryContactNumber2.setCountryCode("01");
        patient2.setPrimaryContactNumber(primaryContactNumber2);

        Map<String, Map<String, Object>> diff = new PatientDiffBuilder(patient1, patient2).build();
        assertNotNull(diff);
        assertEquals(10, diff.size());

        assertChangeSet(diff, GIVEN_NAME, "Harry", "Potter");

        assertChangeSet(diff, PHONE_NUMBER + "." + NUMBER, "100200300", "111222333");
        assertChangeSet(diff, PHONE_NUMBER + "." + EXTENSION, "123", "456");
        assertChangeSet(diff, PRIMARY_CONTACT_NUMBER + "." + COUNTRY_CODE, "91", "01");

        assertChangeSet(diff, PERMANENT_ADDRESS + "." + DIVISION_ID, "10", EMPTY_VALUE);
        assertChangeSet(diff, PERMANENT_ADDRESS + "." + DISTRICT_ID, "20", EMPTY_VALUE);
        assertChangeSet(diff, PERMANENT_ADDRESS + "." + UPAZILA_ID, "30", EMPTY_VALUE);

        assertChangeSet(diff, PRESENT_ADDRESS + "." + DIVISION_ID, EMPTY_VALUE, "10");
        assertChangeSet(diff, PRESENT_ADDRESS + "." + DISTRICT_ID, EMPTY_VALUE, "20");
        assertChangeSet(diff, PRESENT_ADDRESS + "." + UPAZILA_ID, EMPTY_VALUE, "30");
    }

    private void assertChangeSet(Map<String, Map<String, Object>> diff, String key, String oldValue, String newValue) {
        Map<String, Object> changeSet = diff.get(key);
        assertNotNull(changeSet);
        assertEquals(oldValue, changeSet.get(OLD_VALUE));
        assertEquals(newValue, changeSet.get(NEW_VALUE));
    }
}