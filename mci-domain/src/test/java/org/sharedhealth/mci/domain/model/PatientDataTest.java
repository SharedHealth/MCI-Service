package org.sharedhealth.mci.domain.model;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.sharedhealth.mci.domain.validation.group.RequiredGroup;

import javax.validation.ConstraintViolation;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.sharedhealth.mci.domain.constant.JsonConstants.*;

public class PatientDataTest extends ValidationAwareMapper {

    @Test
    public void shouldFailIfGivenNameIsBlank() {
        Set<ConstraintViolation<PatientData>> constraintViolations = validator.validateValue(PatientData.class, "givenName", "");
        assertEquals(1, constraintViolations.size());
        assertEquals("1002", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldFailIfGivenNameIsNull() {
        Set<ConstraintViolation<PatientData>> constraintViolations = validator.validateValue(PatientData.class, "givenName", null,
                RequiredGroup.class);
        assertEquals(1, constraintViolations.size());
        assertEquals("1001", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldNotFailIfSurNameIsNull() {
        Set<ConstraintViolation<PatientData>> constraintViolations = validator.validateValue(PatientData.class, "surName", null);
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldNotFailIfSurNameIsBlank() {
        Set<ConstraintViolation<PatientData>> constraintViolations = validator.validateValue(PatientData.class, "surName", "");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldFailIfDateOfBirthIsBlank() {
        Set<ConstraintViolation<PatientData>> constraintViolations = validator.validateValue(PatientData.class, "dateOfBirth", "   ");
        assertEquals(1, constraintViolations.size());
        assertEquals("1002", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldFailIfDateOfBirthIsInvalidDate() {
        Set<ConstraintViolation<PatientData>> constraintViolations = validator.validateValue(PatientData.class, "dateOfBirth",
                "1997-07-17 00:20");
        assertEquals(1, constraintViolations.size());
        assertEquals("1002", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldPassIfDateOfBirthIsValidDate() {
        Set<ConstraintViolation<PatientData>> constraintViolations = validator.validateValue(PatientData.class, "dateOfBirth",
                "1983-09-21");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldFailIfGenderIsBlank() {
        Set<ConstraintViolation<PatientData>> constraintViolations = validator.validateValue(PatientData.class, "gender", null,
                RequiredGroup.class);
        assertEquals(1, constraintViolations.size());
        assertEquals("1001", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldFailIfNationalIdIsInvalid() {
        Set<ConstraintViolation<PatientData>> constraintViolations = validator.validateValue(PatientData.class, "nationalId", "1");
        assertEquals(1, constraintViolations.size());
        assertEquals("1002", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldPassIfNationalIdIs_13_DigitLong() {
        Set<ConstraintViolation<PatientData>> constraintViolations = validator.validateValue(PatientData.class, "nationalId",
                "1234567890123");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldPassIfNationalIdIs_17_DigitLong() {
        Set<ConstraintViolation<PatientData>> constraintViolations = validator.validateValue(PatientData.class, "nationalId",
                "12345678901234567");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldFailIf_UID_LengthIsNotEqual_11() {
        Set<ConstraintViolation<PatientData>> constraintViolations = validator.validateValue(PatientData.class, "uid", "1");
        assertEquals(1, constraintViolations.size());
        assertEquals("1002", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldFailIf_UUID_ContainSpecialCharacter() {
        Set<ConstraintViolation<PatientData>> constraintViolations = validator.validateValue(PatientData.class, "uid", "123456*8901");
        assertEquals(1, constraintViolations.size());
        assertEquals("1002", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldPassIf_UID_Is_11_DigitAlphaNumeric() {
        Set<ConstraintViolation<PatientData>> constraintViolations = validator.validateValue(PatientData.class, "uid", "UID45678901");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldPassIfBirthRegistrationNumberIsValid() {
        Set<ConstraintViolation<PatientData>> constraintViolations = validator.validateValue(PatientData.class,
                "birthRegistrationNumber", "12345674891234567");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldFailIfBirthRegistrationNumberIsLessThan17() {
        Set<ConstraintViolation<PatientData>> constraintViolations = validator.validateValue(PatientData.class,
                "birthRegistrationNumber", "1234567489123456");
        assertEquals("1002", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldFailIfBirthRegistrationNumberIsMoreThan17() {
        assertLengthViolation("birthRegistrationNumber", 17);
    }

    @Test
    public void shouldFailIfContainSpecialCharacter() {
        Set<ConstraintViolation<PatientData>> constraintViolations = validator.validateValue(PatientData.class,
                "birthRegistrationNumber", "123456748*12345644");
        assertEquals("1002", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldPassIfFullNameBanglaIsValid() {
        Set<ConstraintViolation<PatientData>> constraintViolations = validator.validateValue(PatientData.class, "nameBangla", "এ বি এম " +
                "আখতার হোসেন মন্ডল");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldFailIfFullNameBanglaIsMoreThan_125_Characters() {
        assertLengthViolation("nameBangla", 125);
    }

    @Test
    public void shouldFailIfGivenNameIsMoreThan_100_Characters() {
        assertLengthViolation("givenName", 100);
    }

    @Test
    public void shouldPassIfGivenNameIsValid() {
        Set<ConstraintViolation<PatientData>> constraintViolations = validator.validateValue(PatientData.class, "givenName", "imran");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldPassIfSurNameIsValid() {
        Set<ConstraintViolation<PatientData>> constraintViolations = validator.validateValue(PatientData.class, "surName", "imran");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldFailIfSurNameIsMoreThan_25_Characters() {
        assertLengthViolation("surName", 25);
    }

    @Test
    public void shouldFailIfSurNameContainMultipleWord() {
        Set<ConstraintViolation<PatientData>> constraintViolations = validator.validateValue(PatientData.class, "surName", "part1 part2");
        assertEquals(1, constraintViolations.size());
        assertEquals("1002", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldFailIfPlaceOfBirthIsMoreThan_20_AlphabeticCharacters() {
        assertLengthViolation("placeOfBirth", 20);
    }

    @Test
    public void shouldFailIfPlaceOfBirthIsContainSpecialCharacterAndNumericCharacter() {
        Set<ConstraintViolation<PatientData>> constraintViolations = validator.validateValue(PatientData.class, "placeOfBirth", "rr;");
        assertEquals("1002", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldPassIfPlaceOfBirthIsValid() {
        Set<ConstraintViolation<PatientData>> constraintViolations = validator.validateValue(PatientData.class, "placeOfBirth", "Dhaka");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldPassIfNationalityIsValid() {
        Set<ConstraintViolation<PatientData>> constraintViolations = validator.validateValue(PatientData.class, "nationality",
                "bangladeshi");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldFailIfNationalityIsMoreThan_50_Characters() {
        assertLengthViolation("nationality", 50);
    }

    @Test
    public void shouldFailIfPrimaryContactIsMoreThan_100_Characters() {
        assertLengthViolation("primaryContact", 100);
    }

    @Test
    public void shouldPassIfPrimaryContactIsValid() {
        Set<ConstraintViolation<PatientData>> constraintViolations = validator.validateValue(PatientData.class, "primaryContact", "imran");
        assertEquals(0, constraintViolations.size());
    }

    private void assertLengthViolation(String field, int length) {
        Set<ConstraintViolation<PatientData>> constraintViolations = validator.validateValue(PatientData.class, field, StringUtils.repeat
                ("a", length + 1));
        assertEquals("1002", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldPassIfPatientConfidentialIsValid() {
        String[] validStatus = {"yes", "no", "Yes", "nO"};

        for (String status : validStatus) {
            Set<ConstraintViolation<PatientData>> constraintViolations = validator.validateValue(PatientData.class, "confidential", status);
            assertEquals(0, constraintViolations.size());
        }
    }

    @Test
    public void shouldFailIfPatientConfidentialIsInvalid() {
        String[] inValidStatus = {"", "somevalue", "ayes", "noa", "ayesd", "dnoa"};

        for (String status : inValidStatus) {
            Set<ConstraintViolation<PatientData>> constraintViolations = validator.validateValue(PatientData.class, "confidential", status);
            assertEquals(1, constraintViolations.size());
            assertEquals("1004", constraintViolations.iterator().next().getMessage());
        }
    }

    @Test
    public void shouldPassIfPatientHouseholdCodeIsValid() {
        String[] validStatus = {"", "1", "22", "333", "4444"};

        for (String status : validStatus) {
            Set<ConstraintViolation<PatientData>> constraintViolations = validator.validateValue(PatientData.class, "householdCode",
                    status);
            assertEquals(0, constraintViolations.size());
        }
    }

    @Test
    public void shouldFailIfPatientHouseholdCodeIsInvalid() {
        String[] inValidStatus = {"alphabet", "numberinend1", "1a2", "a2i", "1number"};

        for (String status : inValidStatus) {
            Set<ConstraintViolation<PatientData>> constraintViolations = validator.validateValue(PatientData.class, "householdCode",
                    status);
            assertEquals(1, constraintViolations.size());
            assertEquals("1004", constraintViolations.iterator().next().getMessage());
        }
    }

    @Test
    public void shouldRetrieveFieldValueFromJsonKey() {
        PatientData patient = new PatientData();
        patient.setGivenName("Harry");
        patient.setSurName("Potter");

        Address address = new Address("1", "2", "3");
        address.setAddressLine("House no. 10");
        patient.setAddress(address);

        assertEquals(patient.getGivenName(), patient.getValue(GIVEN_NAME));
        assertEquals(patient.getAddress(), patient.getValue(PRESENT_ADDRESS));
    }

    @Test
    public void shouldRetrieveNonEmptyFieldNames() {
        PatientData patient = new PatientData();
        patient.setGivenName("Harry");
        patient.setSurName("Potter");

        Address address = new Address("1", "2", "3");
        address.setAddressLine("House no. 10");
        patient.setAddress(address);

        List<String> fieldNames = patient.findNonEmptyFieldNames();
        assertEquals(3, fieldNames.size());
        assertTrue(fieldNames.contains(GIVEN_NAME));
        assertTrue(fieldNames.contains(SUR_NAME));
        assertTrue(fieldNames.contains(PRESENT_ADDRESS));
    }

    @Test
    public void shouldReturnTrueIfPatientBelongsToCatchment() {
        Catchment catchment = new Catchment("11", "22", "33");

        Address address = new Address("11", "22", "33");
        address.setCityCorporationId("44");
        address.setUnionOrUrbanWardId("55");
        PatientData patient = new PatientData();
        patient.setAddress(address);

        assertTrue(patient.belongsTo(catchment));
    }
//
//    @Test
//    public void shouldPopulateRequester() {
//        UserProfile facilityProfile = new UserProfile(FACILITY_TYPE, "f100", asList("3026", "4019"));
//        UserProfile providerProfile = new UserProfile(PROVIDER_TYPE, "p100", asList("1001"));
//        UserProfile adminProfile = new UserProfile(ADMIN_TYPE, "a100", asList("2020"));
//
//        UserInfo userInfo = new UserInfo("102", "ABC", "abc@mail", 1, true, "111100",
//                new ArrayList<>(asList(MCI_USER_GROUP)), asList(adminProfile, providerProfile, facilityProfile));
//
//        PatientData patient = new PatientData();
//        patient.setProvider("p000");
//        UserInfo.UserInfoProperties properties = userInfo.getProperties();
//        patient.setRequester(
//                properties.getFacilityId(), properties.getProviderId(), properties.getAdminId()
//                , properties.getName());
//
//        Requester requester = patient.getRequester();
//        assertNotNull(requester);
//
//        RequesterDetails provider = requester.getProvider();
//        assertNotNull(provider);
//        assertEquals("p100", provider.getId());
//        assertNull(provider.getName());
//
//        RequesterDetails facility = requester.getFacility();
//        assertNotNull(facility);
//        assertEquals("f100", facility.getId());
//        assertNull(facility.getName());
//    }
}
