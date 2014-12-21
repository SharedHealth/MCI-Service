package org.sharedhealth.mci.web.handler;

import org.junit.Test;
import org.sharedhealth.mci.web.mapper.Address;
import org.sharedhealth.mci.web.mapper.Location;
import org.sharedhealth.mci.web.mapper.PatientData;
import org.sharedhealth.mci.web.model.PendingApprovalRequest;

import java.text.ParseException;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PatientFilterTest {

    @Test
    public void shouldMapFieldsToBeApproved() throws ParseException {
        Properties properties = new Properties();
        String genderKey = "gender";
        properties.setProperty(genderKey, "NA");
        PatientData patientUpdated = buildPatientData();
        PatientData patientExisting = buildPatientData();
        PatientData patientToBeSaved = new PatientData();
        patientExisting.setGender("M");
        patientUpdated.setGender("F");

        PatientFilter patientFilter = new PatientFilter(properties, patientExisting, patientUpdated,patientToBeSaved);
        PendingApprovalRequest pendingApprovalRequest = patientFilter.filter();

        assertEquals("F", pendingApprovalRequest.getFields().get(genderKey));
        assertEquals(patientToBeSaved.getGender(), patientExisting.getGender());
    }

    @Test
    public void shouldNotUpdateNonUpdateableFields() throws ParseException {
        Properties properties = new Properties();
        String dobKey = "date_of_birth";
        properties.setProperty(dobKey, "NU");
        PatientData patientUpdated = buildPatientData();
        PatientData patientExisting = buildPatientData();
        PatientData patientToBeSaved = new PatientData();
        patientExisting.setDateOfBirth("2000-02-10");
        patientUpdated.setDateOfBirth("2000-02-10");

        PatientFilter patientFilter = new PatientFilter(properties, patientExisting, patientUpdated,patientToBeSaved);
        PendingApprovalRequest pendingApprovalRequest = patientFilter.filter();

        assertNull(pendingApprovalRequest);
        assertEquals(patientToBeSaved.getDateOfBirth(), patientExisting.getDateOfBirth());
    }

    @Test
    public void shouldUpdateFieldsWhichNeedNotBeApprovedOrNotMarkedAsNonUpdateable() throws ParseException {
        Properties properties = new Properties();
        String dobKey = "date_of_birth";
        PatientData patientUpdated = buildPatientData();
        PatientData patientExisting = buildPatientData();
        PatientData patientToBeSaved = new PatientData();
        patientExisting.setDateOfBirth("2000-02-10");
        patientUpdated.setDateOfBirth("2001-02-10");

        PatientFilter patientFilter = new PatientFilter(properties, patientExisting, patientUpdated,patientToBeSaved);
        PendingApprovalRequest pendingApprovalRequest = patientFilter.filter();

        assertNull(pendingApprovalRequest);
        assertEquals(patientToBeSaved.getDateOfBirth(), patientUpdated.getDateOfBirth());
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

        Address presentAdress = new Address();
        presentAdress.setAddressLine("house-10");
        presentAdress.setDivisionId("10");
        presentAdress.setDistrictId("04");
        presentAdress.setUpazilaId("09");
        presentAdress.setCityCorporationId("20");
        presentAdress.setVillage("10");
        presentAdress.setRuralWardId("01");
        presentAdress.setCountryCode("050");

        patient.setPermanentAddress(presentAdress);

        Location location = new Location();
        location = new Location();

        location.setGeoCode("1004092001");
        location.setDivisionId("10");
        location.setDistrictId("04");
        location.setUpazilaId("09");
        location.setPaurashavaId("20");
        location.setUnionId("01");
        return patient;
    }

}