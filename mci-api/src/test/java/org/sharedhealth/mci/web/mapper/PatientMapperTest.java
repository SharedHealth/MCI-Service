package org.sharedhealth.mci.web.mapper;

import javax.validation.ConstraintViolation;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.sharedhealth.mci.validation.group.RequiredGroup;

import static org.junit.Assert.assertEquals;

public class PatientMapperTest extends ValidationAwareMapper{

    @Test
    public void shouldFailIfGivenNameIsBlank() {
        Set<ConstraintViolation<PatientMapper>> constraintViolations = validator.validateValue(PatientMapper.class, "givenName", "");
        assertEquals(1, constraintViolations.size());
        assertEquals("1002", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldFailIfGivenNameIsNull() {
        Set<ConstraintViolation<PatientMapper>> constraintViolations = validator.validateValue(PatientMapper.class, "givenName", null, RequiredGroup.class);
        assertEquals(1, constraintViolations.size());
        assertEquals("1001", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldFailIfSurNameIsBlank() {
        Set<ConstraintViolation<PatientMapper>> constraintViolations = validator.validateValue(PatientMapper.class, "surName", null, RequiredGroup.class);
        assertEquals(1, constraintViolations.size());
        assertEquals("1001", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldFailIfDateOfBirthIsBlank() {
        Set<ConstraintViolation<PatientMapper>> constraintViolations = validator.validateValue(PatientMapper.class, "dateOfBirth", "   ", RequiredGroup.class);
        assertEquals(1, constraintViolations.size());
        assertEquals("1001", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldFailIfDateOfBirthIsInvalidDate() {
        Set<ConstraintViolation<PatientMapper>> constraintViolations = validator.validateValue(PatientMapper.class, "dateOfBirth", "1999-02-30");
        assertEquals(1, constraintViolations.size());
        assertEquals("1002", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldPassIfDateOfBirthIsValidDate() {
        Set<ConstraintViolation<PatientMapper>> constraintViolations = validator.validateValue(PatientMapper.class, "dateOfBirth", "1983-09-21");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldFailIfGenderIsBlank() {
        Set<ConstraintViolation<PatientMapper>> constraintViolations = validator.validateValue(PatientMapper.class, "gender", null, RequiredGroup.class);
        assertEquals(1, constraintViolations.size());
        assertEquals("1001", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldFailIfNationalIdIsInvalid() {
        Set<ConstraintViolation<PatientMapper>> constraintViolations = validator.validateValue(PatientMapper.class, "nationalId", "1");
        assertEquals(1, constraintViolations.size());
        assertEquals("1002", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldPassIfNationalIdIs_13_DigitLong() {
        Set<ConstraintViolation<PatientMapper>> constraintViolations = validator.validateValue(PatientMapper.class, "nationalId", "1234567890123");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldPassIfNationalIdIs_17_DigitLong() {
        Set<ConstraintViolation<PatientMapper>> constraintViolations = validator.validateValue(PatientMapper.class, "nationalId", "12345678901234567");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldFailIf_UID_LengthIsNotEqual_11() {
        Set<ConstraintViolation<PatientMapper>> constraintViolations = validator.validateValue(PatientMapper.class, "uid", "1");
        assertEquals(1, constraintViolations.size());
        assertEquals("1002", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldFailIf_UUID_ContainSpecialCharacter() {
        Set<ConstraintViolation<PatientMapper>> constraintViolations = validator.validateValue(PatientMapper.class, "uid", "123456*8901");
        assertEquals(1, constraintViolations.size());
        assertEquals("1002", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldPassIf_UID_Is_11_DigitAlphaNumeric() {
        Set<ConstraintViolation<PatientMapper>> constraintViolations = validator.validateValue(PatientMapper.class, "uid", "UID45678901");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldPassIfBirthRegistrationNumberIsValid() {
        Set<ConstraintViolation<PatientMapper>> constraintViolations = validator.validateValue(PatientMapper.class, "birthRegistrationNumber", "12345674891234567");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldFailIfBirthRegistrationNumberIsLessThan17() {
        Set<ConstraintViolation<PatientMapper>> constraintViolations = validator.validateValue(PatientMapper.class, "birthRegistrationNumber", "1234567489123456");
        assertEquals("1002", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldFailIfBirthRegistrationNumberIsMoreThan17() {
        assertLengthViolation("birthRegistrationNumber", 17);
    }

    @Test
    public void shouldFailIfContainSpecialCharacter() {
        Set<ConstraintViolation<PatientMapper>> constraintViolations = validator.validateValue(PatientMapper.class, "birthRegistrationNumber", "123456748*12345644");
        assertEquals("1002", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldPassIfFullNameBanglaIsValid() {
        Set<ConstraintViolation<PatientMapper>> constraintViolations = validator.validateValue(PatientMapper.class, "nameBangla", "এ বি এম আখতার হোসেন মন্ডল");
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
        Set<ConstraintViolation<PatientMapper>> constraintViolations = validator.validateValue(PatientMapper.class, "givenName", "imran");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldPassIfSurNameIsValid() {
        Set<ConstraintViolation<PatientMapper>> constraintViolations = validator.validateValue(PatientMapper.class, "surName", "imran");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldFailIfSurNameIsMoreThan_25_Characters() {
        assertLengthViolation("surName", 25);
    }

    @Test
    public void shouldFailIfPlaceOfBirthIsMoreThan_20_AlphabeticCharacters() {
        assertLengthViolation("placeOfBirth", 20);
    }

    @Test
    public void shouldFailIfPlaceOfBirthIsContainSpecialCharacterAndNumericCharacter() {
        Set<ConstraintViolation<PatientMapper>> constraintViolations = validator.validateValue(PatientMapper.class, "placeOfBirth", "rr;");
        assertEquals("1002", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldPassIfPlaceOfBirthIsValid() {
        Set<ConstraintViolation<PatientMapper>> constraintViolations = validator.validateValue(PatientMapper.class, "placeOfBirth", "Dhaka");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldPassIfNationalityIsValid() {
        Set<ConstraintViolation<PatientMapper>> constraintViolations = validator.validateValue(PatientMapper.class, "nationality", "bangladeshi");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldFailIfNationalityIsMoreThan_50_Characters() {
        assertLengthViolation("nationality", 50);
    }

    @Test
    public void shouldPassIfAliveIsValid() {
        for (int i = 0; i < 2; i++) {
            Set<ConstraintViolation<PatientMapper>> constraintViolations = validator.validateValue(PatientMapper.class, "isAlive", Integer.toString(i));
            assertEquals(0, constraintViolations.size());
        }
    }

    @Test
    public void shouldFailIfAliveIsInvalid() {
        Set<ConstraintViolation<PatientMapper>> constraintViolations = validator.validateValue(PatientMapper.class, "isAlive", "3");
        assertEquals(1, constraintViolations.size());
        assertEquals("1004", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldFailIfPrimaryContactIsMoreThan_100_Characters() {
        assertLengthViolation("primaryContact", 100);
    }

    @Test
    public void shouldPassIfPrimaryContactIsValid() {
        Set<ConstraintViolation<PatientMapper>> constraintViolations = validator.validateValue(PatientMapper.class, "primaryContact", "imran");
        assertEquals(0, constraintViolations.size());
    }

    private void assertLengthViolation(String field, int length) {
        Set<ConstraintViolation<PatientMapper>> constraintViolations = validator.validateValue(PatientMapper.class, field, StringUtils.repeat("a", length + 1));
        assertEquals("1002", constraintViolations.iterator().next().getMessage());
    }

}
