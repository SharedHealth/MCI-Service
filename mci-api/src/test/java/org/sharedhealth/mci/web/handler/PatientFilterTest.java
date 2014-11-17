package org.sharedhealth.mci.web.handler;

import org.junit.Test;
import org.sharedhealth.mci.web.infrastructure.persistence.Approval;
import org.sharedhealth.mci.web.mapper.Address;
import org.sharedhealth.mci.web.mapper.Location;
import org.sharedhealth.mci.web.mapper.PatientMapper;

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
        PatientMapper patientDtoUpdated = createPatientDto();
        PatientMapper patientDtoExisting = createPatientDto();
        PatientMapper patientDtoToBeSaved = new PatientMapper();
        patientDtoExisting.setGender("M");
        patientDtoUpdated.setGender("F");

        PatientFilter patientFilter = new PatientFilter(properties, patientDtoExisting, patientDtoUpdated,patientDtoToBeSaved);
        Approval approval = patientFilter.filter();

        assertEquals("F", approval.getFieldsToApprovedMaps().get(genderKey));
        assertEquals(patientDtoToBeSaved.getGender(), patientDtoExisting.getGender());
    }

    @Test
    public void shouldNotUpdateNonUpdateableFields() throws ParseException {
        Properties properties = new Properties();
        String dobKey = "date_of_birth";
        properties.setProperty(dobKey, "NU");
        PatientMapper patientDtoUpdated = createPatientDto();
        PatientMapper patientDtoExisting = createPatientDto();
        PatientMapper patientDtoToBeSaved = new PatientMapper();
        patientDtoExisting.setDateOfBirth("2000-02-10");
        patientDtoUpdated.setDateOfBirth("2000-02-10");

        PatientFilter patientFilter = new PatientFilter(properties, patientDtoExisting, patientDtoUpdated,patientDtoToBeSaved);
        Approval approval = patientFilter.filter();

        assertNull(approval);
        assertEquals(patientDtoToBeSaved.getDateOfBirth(), patientDtoExisting.getDateOfBirth());
    }

    @Test
    public void shouldUpdateFieldsWhichNeedNotBeApprovedOrNotMarkedAsNonUpdateable() throws ParseException {
        Properties properties = new Properties();
        String dobKey = "date_of_birth";
        PatientMapper patientDtoUpdated = createPatientDto();
        PatientMapper patientDtoExisting = createPatientDto();
        PatientMapper patientDtoToBeSaved = new PatientMapper();
        patientDtoExisting.setDateOfBirth("2000-02-10");
        patientDtoUpdated.setDateOfBirth("2001-02-10");

        PatientFilter patientFilter = new PatientFilter(properties, patientDtoExisting, patientDtoUpdated,patientDtoToBeSaved);
        Approval approval = patientFilter.filter();

        assertNull(approval);
        assertEquals(patientDtoToBeSaved.getDateOfBirth(), patientDtoUpdated.getDateOfBirth());
    }

    private PatientMapper createPatientDto() throws ParseException {
       PatientMapper patientDto = new PatientMapper();
        patientDto.setNationalId("1234567890123");
        patientDto.setBirthRegistrationNumber("12345678901234567");
        patientDto.setGivenName("Scott");
        patientDto.setSurName("Tiger");
        patientDto.setGender("M");
        patientDto.setDateOfBirth("2014-12-01");

        Address address = new Address();
        address.setAddressLine("house-10");
        address.setDivisionId("10");
        address.setDistrictId("04");
        address.setUpazillaId("09");
        address.setCityCorporationId("20");
        address.setVillage("10");
        address.setWardId("01");
        address.setCountryCode("050");

        patientDto.setAddress(address);

        Address presentAdress = new Address();
        presentAdress.setAddressLine("house-10");
        presentAdress.setDivisionId("10");
        presentAdress.setDistrictId("04");
        presentAdress.setUpazillaId("09");
        presentAdress.setCityCorporationId("20");
        presentAdress.setVillage("10");
        presentAdress.setWardId("01");
        presentAdress.setCountryCode("050");

        patientDto.setPermanentAddress(presentAdress);

        Location location = new Location();
        location = new Location();

        location.setGeoCode("1004092001");
        location.setDivisionId("10");
        location.setDistrictId("04");
        location.setUpazillaId("09");
        location.setPaurashavaId("20");
        location.setUnionId("01");
        return patientDto;
    }

}