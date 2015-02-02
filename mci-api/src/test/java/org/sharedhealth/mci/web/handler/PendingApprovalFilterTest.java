package org.sharedhealth.mci.web.handler;

import org.junit.Test;
import org.sharedhealth.mci.web.mapper.*;

import java.text.ParseException;
import java.util.Properties;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import static com.datastax.driver.core.utils.UUIDs.unixTimestamp;
import static java.util.Arrays.asList;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.junit.Assert.*;
import static org.sharedhealth.mci.utils.DateUtil.toIsoFormat;
import static org.sharedhealth.mci.web.utils.JsonConstants.*;

public class PendingApprovalFilterTest {

    @Test
    public void shouldAddFieldsToPendingApprovalsWhenMarkedForApproval() throws ParseException {
        Properties properties = new Properties();
        properties.setProperty(GENDER, "NA");

        PatientData updateRequest = buildPatientData();
        updateRequest.setGender("F");

        PatientData existingPatient = buildPatientData();
        existingPatient.setGender("M");

        PendingApprovalFilter pendingApprovalFilter = new PendingApprovalFilter();
        pendingApprovalFilter.setProperties(properties);
        PatientData newPatient = pendingApprovalFilter.filter(existingPatient, updateRequest);

        assertPendingApprovals(existingPatient, newPatient, 1);
    }

    @Test
    public void shouldAddFieldsToExistingPendingApprovalsWhenMarkedForApproval() throws ParseException {
        Properties properties = new Properties();
        properties.setProperty(GENDER, "NA");

        PatientData updateRequest = buildPatientData();
        updateRequest.setGender("F");

        PatientData existingPatient = buildPatientData();
        existingPatient.setGender("M");

        PendingApprovalFilter pendingApprovalFilter = new PendingApprovalFilter();
        pendingApprovalFilter.setProperties(properties);

        PatientData newPatient = pendingApprovalFilter.filter(existingPatient, updateRequest);
        assertPendingApprovals(existingPatient, newPatient, 1);

        updateRequest = buildPatientData();
        updateRequest.setGender("O");

        newPatient = pendingApprovalFilter.filter(newPatient, updateRequest);
        TreeSet<PendingApproval> pendingApprovals = newPatient.getPendingApprovals();
        assertNotNull(pendingApprovals);
        assertEquals(1, pendingApprovals.size());

        PendingApproval pendingApproval = pendingApprovals.iterator().next();
        assertEquals(GENDER, pendingApproval.getName());

        TreeMap<UUID, PendingApprovalFieldDetails> fieldDetailsMap = pendingApproval.getFieldDetails();
        assertNotNull(fieldDetailsMap);
        assertEquals(2, fieldDetailsMap.size());

        for (PendingApprovalFieldDetails fieldDetails : fieldDetailsMap.values()) {
            assertTrue(asList("F", "O").contains(fieldDetails.getValue()));
        }

        assertEquals(existingPatient.getGender(), newPatient.getGender());
    }

    @Test
    public void shouldAddFieldsToPendingApprovalsWhenMarkedForApprovalAndExistingValueIsNull() throws ParseException {
        Properties properties = new Properties();
        properties.setProperty(GENDER, "NA");
        properties.setProperty(PHONE_NUMBER, "NA");
        properties.setProperty(PRESENT_ADDRESS, "NA");
        properties.setProperty(PERMANENT_ADDRESS, "NA");

        PatientData existingPatient = buildPatientData();
        existingPatient.setGender(null);
        existingPatient.setPhoneNumber(null);
        existingPatient.setAddress(null);
        existingPatient.setPermanentAddress(null);

        PatientData updateRequest = buildPatientData();
        updateRequest.setGender("F");
        PhoneNumber phoneNumber = new PhoneNumber();
        phoneNumber.setNumber("123");
        updateRequest.setPhoneNumber(phoneNumber);
        Address presentAddress = new Address("10", "20", "30");
        updateRequest.setAddress(presentAddress);
        Address permanentAddress = new Address("11", "22", "33");
        updateRequest.setPermanentAddress(permanentAddress);

        PendingApprovalFilter pendingApprovalFilter = new PendingApprovalFilter();
        pendingApprovalFilter.setProperties(properties);
        PatientData newPatient = pendingApprovalFilter.filter(existingPatient, updateRequest);

        assertPendingApprovals(existingPatient, newPatient, 4);
    }

    @Test
    public void shouldAddFieldsToPendingApprovalsWhenMarkedForApprovalButValueSameAsExisting() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(PHONE_NUMBER, "NA");
        properties.setProperty(PRESENT_ADDRESS, "NA");
        properties.setProperty(PERMANENT_ADDRESS, "NA");

        PatientData existingPatient = buildPatientData();

        PhoneNumber existingPhoneNumber = new PhoneNumber();
        existingPhoneNumber.setCountryCode("");
        existingPhoneNumber.setAreaCode("");
        existingPhoneNumber.setNumber("123");
        existingPhoneNumber.setExtension("");
        existingPatient.setPhoneNumber(existingPhoneNumber);

        Address existingPresentAddress = new Address("10", "20", "30");
        existingPresentAddress.setCityCorporationId("");
        existingPresentAddress.setUnionOrUrbanWardId("");
        existingPresentAddress.setRuralWardId("");
        existingPatient.setAddress(existingPresentAddress);

        Address existingPermanentAddress = new Address("11", "22", "33");
        existingPermanentAddress.setCityCorporationId("");
        existingPermanentAddress.setUnionOrUrbanWardId("");
        existingPermanentAddress.setRuralWardId("");
        existingPatient.setPermanentAddress(existingPermanentAddress);

        PatientData updateRequest = buildPatientData();
        PhoneNumber phoneNumberToBeUpdated = new PhoneNumber();
        phoneNumberToBeUpdated.setNumber("123");
        updateRequest.setPhoneNumber(phoneNumberToBeUpdated);
        updateRequest.setAddress(new Address("10", "20", "30"));
        updateRequest.setPermanentAddress(new Address("11", "22", "33"));

        PendingApprovalFilter pendingApprovalFilter = new PendingApprovalFilter();
        pendingApprovalFilter.setProperties(properties);
        PatientData newPatient = pendingApprovalFilter.filter(existingPatient, updateRequest);

        assertTrue(isEmpty(newPatient.getPendingApprovals()));
        assertEquals(existingPatient.getPhoneNumber(), newPatient.getPhoneNumber());
        assertEquals(existingPatient.getAddress(), newPatient.getAddress());
        assertEquals(existingPatient.getPermanentAddress(), newPatient.getPermanentAddress());
    }

    @Test
    public void shouldNotAddFieldsToPendingApprovalsWhenNotMarkedNonUpdateable() throws ParseException {
        Properties properties = new Properties();
        properties.setProperty(DATE_OF_BIRTH, "NU");

        PatientData existingPatient = buildPatientData();
        existingPatient.setDateOfBirth("2000-02-10");

        PatientData updateRequest = buildPatientData();
        updateRequest.setDateOfBirth("2000-02-10");

        PendingApprovalFilter pendingApprovalFilter = new PendingApprovalFilter();
        pendingApprovalFilter.setProperties(properties);
        PatientData newPatient = pendingApprovalFilter.filter(existingPatient, updateRequest);

        assertTrue(isEmpty(newPatient.getPendingApprovals()));
        assertEquals(existingPatient.getDateOfBirth(), newPatient.getDateOfBirth());
    }

    @Test
    public void shouldUpdateFieldsThatAreNeitherMarkedForApprovalNorMarkedAsNonUpdateable() throws ParseException {
        Properties properties = new Properties();

        PatientData existingPatient = buildPatientData();
        existingPatient.setDateOfBirth("2000-02-10");

        PatientData updateRequest = buildPatientData();
        updateRequest.setDateOfBirth("2001-02-10");

        PendingApprovalFilter pendingApprovalFilter = new PendingApprovalFilter();
        pendingApprovalFilter.setProperties(properties);
        PatientData newPatient = pendingApprovalFilter.filter(existingPatient, updateRequest);

        assertTrue(isEmpty(newPatient.getPendingApprovals()));
        assertEquals("2001-02-10", newPatient.getDateOfBirth());
    }

    private void assertPendingApprovals(PatientData existingPatient, PatientData newPatient, int pendingApprovalsCount) {
        TreeSet<PendingApproval> pendingApprovals = newPatient.getPendingApprovals();
        assertNotNull(pendingApprovals);
        assertEquals(pendingApprovalsCount, pendingApprovals.size());

        for (PendingApproval pendingApproval : pendingApprovals) {
            if (GENDER.equals(pendingApproval.getName())) {
                assertFieldDetails(pendingApproval.getFieldDetails(), "F");
                assertEquals(existingPatient.getGender(), newPatient.getGender());

            } else if (PHONE_NUMBER.equals(pendingApproval.getName())) {
                PhoneNumber phoneNumber = new PhoneNumber();
                phoneNumber.setNumber("123");
                assertFieldDetails(pendingApproval.getFieldDetails(), phoneNumber);
                assertEquals(existingPatient.getPhoneNumber(), newPatient.getPhoneNumber());

            } else if (PRESENT_ADDRESS.equals(pendingApproval.getName())) {
                assertFieldDetails(pendingApproval.getFieldDetails(), new Address("10", "20", "30"));
                assertEquals(existingPatient.getAddress(), newPatient.getAddress());

            } else if (PERMANENT_ADDRESS.equals(pendingApproval.getName())) {
                assertFieldDetails(pendingApproval.getFieldDetails(), new Address("11", "22", "33"));
                assertEquals(existingPatient.getPermanentAddress(), newPatient.getPermanentAddress());

            } else {
                fail("Invalid pending approval.");
            }
        }
    }

    private void assertFieldDetails(TreeMap<UUID, PendingApprovalFieldDetails> fieldDetailsMap, Object value) {
        assertNotNull(fieldDetailsMap);
        assertEquals(1, fieldDetailsMap.size());
        PendingApprovalFieldDetails fieldDetails = fieldDetailsMap.values().iterator().next();
        assertEquals(value, fieldDetails.getValue());
        assertNotNull(fieldDetails.getFacilityId());
        long expectedCreatedAt = unixTimestamp(fieldDetailsMap.keySet().iterator().next());
        assertEquals(toIsoFormat(expectedCreatedAt), fieldDetails.getCreatedAt());
    }

    private PatientData buildPatientData() throws ParseException {
        PatientData patient = new PatientData();
        patient.setNationalId("1234567890123");
        patient.setBirthRegistrationNumber("12345678901234567");
        patient.setGivenName("Scott");
        patient.setSurName("Tiger");
        patient.setGender("M");
        patient.setDateOfBirth("2014-12-01");

        Address address = new Address();
        address.setAddressLine("house-10");
        address.setDivisionId("10");
        address.setDistrictId("04");
        address.setUpazilaId("09");
        address.setCityCorporationId("20");
        address.setVillage("10");
        address.setRuralWardId("01");
        address.setCountryCode("050");

        patient.setAddress(address);

        Address presentAddress = new Address();
        presentAddress.setAddressLine("house-10");
        presentAddress.setDivisionId("10");
        presentAddress.setDistrictId("04");
        presentAddress.setUpazilaId("09");
        presentAddress.setCityCorporationId("20");
        presentAddress.setVillage("10");
        presentAddress.setRuralWardId("01");
        presentAddress.setCountryCode("050");

        patient.setPermanentAddress(presentAddress);

        LocationData location = new LocationData();

        location.setGeoCode("1004092001");
        location.setDivisionId("10");
        location.setDistrictId("04");
        location.setUpazilaId("09");
        location.setCityCorporationId("20");
        location.setUnionOrUrbanWardId("01");
        return patient;
    }

}