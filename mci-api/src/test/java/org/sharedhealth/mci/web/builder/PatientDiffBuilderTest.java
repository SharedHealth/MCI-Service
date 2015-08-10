package org.sharedhealth.mci.web.builder;

import org.junit.Test;
import org.sharedhealth.mci.domain.diff.PatientDiffBuilder;
import org.sharedhealth.mci.domain.model.Address;
import org.sharedhealth.mci.domain.model.PatientData;
import org.sharedhealth.mci.domain.model.PhoneNumber;
import org.sharedhealth.mci.domain.model.Relation;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.sharedhealth.mci.domain.constant.JsonConstants.*;
import static org.sharedhealth.mci.domain.diff.PatientDiffBuilder.EMPTY_VALUE;

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
    public void shouldReturnEmptyMapWhenNewPatientHasNoChanges() {
        PatientData patient1 = new PatientData();
        patient1.setGivenName("Harry");
        patient1.setSurName("Potter");

        PatientData patient2 = new PatientData();
        patient2.setGivenName("Harry");

        Map<String, Map<String, Object>> diff = new PatientDiffBuilder(patient1, patient2).build();
        assertNotNull(diff);
        assertEquals(0, diff.size());
    }

    @Test
    public void shouldReturnDiffWhenPatientsHaveOnlyRelationsDiff() {
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
    public void shouldReturnDiffWhenPatientsHaveMultipleDiffs() {
        PatientData patient1 = new PatientData();
        patient1.setGivenName("Harry");
        patient1.setReligion("00");
        Address permanentAddress1 = new Address("10", "20", "30");
        patient1.setPermanentAddress(permanentAddress1);

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
        Address presentAddress2 = new Address("10", "20", "30");
        patient2.setAddress(presentAddress2);

        PhoneNumber phoneNumber2 = new PhoneNumber();
        phoneNumber2.setNumber("111222333");
        phoneNumber2.setExtension("456");
        patient2.setPhoneNumber(phoneNumber2);

        PhoneNumber primaryContactNumber2 = new PhoneNumber();
        primaryContactNumber2.setCountryCode("01");
        patient2.setPrimaryContactNumber(primaryContactNumber2);

        Map<String, Map<String, Object>> diff = new PatientDiffBuilder(patient1, patient2).build();
        assertNotNull(diff);
        assertEquals(4, diff.size());

        assertChangeSet(diff, GIVEN_NAME, "Harry", "Potter");
        assertChangeSet(diff, PHONE_NUMBER, phoneNumber1, phoneNumber2);
        assertChangeSet(diff, PRIMARY_CONTACT_NUMBER, primaryContactNumber1, primaryContactNumber2);
        assertChangeSet(diff, PRESENT_ADDRESS, EMPTY_VALUE, presentAddress2);
    }

    private void assertChangeSet(Map<String, Map<String, Object>> diff, String key, Object oldValue, Object newValue) {
        Map<String, Object> changeSet = diff.get(key);
        assertNotNull(changeSet);
        assertEquals(oldValue, changeSet.get(OLD_VALUE));
        assertEquals(newValue, changeSet.get(NEW_VALUE));
    }
}