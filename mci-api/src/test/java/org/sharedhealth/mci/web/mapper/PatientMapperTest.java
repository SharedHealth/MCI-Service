package org.sharedhealth.mci.web.mapper;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import org.hibernate.validator.HibernateValidator;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PatientMapperTest {

    private static Validator validator;

    @BeforeClass
    public static void setUp() {
        ValidatorFactory factory = Validation.byProvider(HibernateValidator.class)
                .configure()
                .buildValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void shouldFailIfGivenNameIsBlank() {
        Set<ConstraintViolation<PatientMapper>> constraintViolations = validator.validateValue(PatientMapper.class, "givenName", "");
        assertEquals(1, constraintViolations.size());
        assertEquals("1001", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldFailIfSurNameIsBlank() {
        Set<ConstraintViolation<PatientMapper>> constraintViolations = validator.validateValue(PatientMapper.class, "surName", "");
        assertEquals(1, constraintViolations.size());
        assertEquals("1001", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldFailIfDateOfBirthIsBlank() {
        Set<ConstraintViolation<PatientMapper>> constraintViolations = validator.validateValue(PatientMapper.class, "dateOfBirth", "   ");
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
    public void shouldPassIfGenderValid() {
            Set<ConstraintViolation<PatientMapper>> constraintViolations = validator.validateValue(PatientMapper.class, "gender", "M");
            assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldFailIfGenderIsBlank() {
        Set<ConstraintViolation<PatientMapper>> constraintViolations = validator.validateValue(PatientMapper.class, "gender", null);
        assertEquals(1, constraintViolations.size());
        assertEquals("1001", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldFailIfGenderIsInvalid() {
        Set<ConstraintViolation<PatientMapper>> constraintViolations = validator.validateValue(PatientMapper.class, "gender", "5");
        assertEquals(1, constraintViolations.size());
        assertEquals("1004", constraintViolations.iterator().next().getMessage());
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
    public void shouldFailIfOccupationCodeIsLessThenOne() {
        Set<ConstraintViolation<PatientMapper>> constraintViolations = validator.validateValue(PatientMapper.class, "occupation", "00");
        assertEquals(1, constraintViolations.size());
        assertEquals("1004", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldFailIfOccupationCodeIsGreaterThen92() {
        Set<ConstraintViolation<PatientMapper>> constraintViolations = validator.validateValue(PatientMapper.class, "occupation", "93");
        assertEquals(1, constraintViolations.size());
        assertEquals("1004", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldPassIfOccupationCodeIsValid() {
        for (int i = 1; i <93; i++) {
            Set<ConstraintViolation<PatientMapper>> constraintViolations = validator.validateValue(PatientMapper.class, "occupation", String.format("%02d", i));
            assertEquals(0, constraintViolations.size());
        }
    }

    @Test
    public void shouldPassIfReligionIsValid() {
        for(int i = 1; i<8; i++) {
            Set<ConstraintViolation<PatientMapper>> constraintViolations = validator.validateValue(PatientMapper.class, "religion", Integer.toString(i));
            assertEquals(0, constraintViolations.size());
        }
    }

    @Test
    public void shouldFailIfReligionIsInvalid() {
        Set<ConstraintViolation<PatientMapper>> constraintViolations = validator.validateValue(PatientMapper.class, "religion", "9");
        assertEquals(1, constraintViolations.size());
        assertEquals("1004", constraintViolations.iterator().next().getMessage());
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
        Set<ConstraintViolation<PatientMapper>> constraintViolations = validator.validateValue(PatientMapper.class, "birthRegistrationNumber", "123456748912345644");
        assertEquals("1002", constraintViolations.iterator().next().getMessage());
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
    public void shouldFailIfFullNameBanglaIsMoreThan_120_Characters() {
        Set<ConstraintViolation<PatientMapper>> constraintViolations = validator.validateValue(PatientMapper.class, "nameBangla", "এ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডল");
        assertEquals("1002", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldFailIfGivenNameIsMoreThan_100_Characters() {
        Set<ConstraintViolation<PatientMapper>> constraintViolations = validator.validateValue(PatientMapper.class, "givenName", "janagiralamkabirkhanjahanaliahmadpuri janagiralamkabirkhanjahanaliahmadpurijanagiralamkabirkhanjahanaliahmadpurijanagiralamkabirkhanjahanaliahmadpurijanagiralamkabirkhanjahanaliahmadpurijanagiralamkabirkhanjahanaliahmadpurijanagiralamkabirkhanjahanaliahmadpurijanagiralamkabirkhanjahanaliahmadpurijanagiralamkabirkhanjahanaliahmadpurijanagiralamkabirkhanjahanaliahmadpurijanagiralamkabirkhanjahanaliahmadpurijanagiralamkabirkhanjahanaliahmadpurijanagiralamkabirkhanjahanaliahmadpurijanagiralamkabirkhanjahanaliahmadpurijanagiralamkabirkhanjahanaliahmadpuri");
        assertEquals("1002", constraintViolations.iterator().next().getMessage());
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
        Set<ConstraintViolation<PatientMapper>> constraintViolations = validator.validateValue(PatientMapper.class, "surName", "janagiralamkabirkhanjahanaliahmadpuri");
        assertEquals("1002", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldFailIfEducationLevelIsGreaterThen20() {
        Set<ConstraintViolation<PatientMapper>> constraintViolations = validator.validateValue(PatientMapper.class, "educationLevel", "20");
        assertEquals(1, constraintViolations.size());
        assertEquals("1004", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldFailIfEducationLevelIsNotTwoDigitFormat() {
        Set<ConstraintViolation<PatientMapper>> constraintViolations = validator.validateValue(PatientMapper.class, "educationLevel", "1");
        assertEquals(1, constraintViolations.size());
        assertEquals("1004", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldPassIfEducationLevelIsValid() {
        for (int i = 0; i <20; i++) {
            Set<ConstraintViolation<PatientMapper>> constraintViolations = validator.validateValue(PatientMapper.class, "educationLevel", String.format("%02d", i));
            assertEquals(0, constraintViolations.size());
        }
    }

    @Test
    public void shouldFailIfPlaceOfBirthIsMoreThan_20_AlphabeticCharacters() {
        Set<ConstraintViolation<PatientMapper>> constraintViolations = validator.validateValue(PatientMapper.class, "placeOfBirth", "DhanmondiDmondiDmondiDmondiDmondiDmondiDhanmondi");
        assertEquals("1002", constraintViolations.iterator().next().getMessage());
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
        Set<ConstraintViolation<PatientMapper>> constraintViolations = validator.validateValue(PatientMapper.class, "nationality", "bangladeshi bangladeshi bangladeshi bangladeshi bangladeshi bangladeshi ");
        assertEquals("1002", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldPassIfAliveIsValid() {
        for(int i = 1; i<3; i++) {
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
    public void shouldPassIfMaritalStatusIsValid() {
        for(int i = 1; i<6; i++) {
            Set<ConstraintViolation<PatientMapper>> constraintViolations = validator.validateValue(PatientMapper.class, "maritalStatus", Integer.toString(i));
            assertEquals(0, constraintViolations.size());
        }
    }

    @Test
    public void shouldFailIfMaritalStatusIsInvalid() {
        Set<ConstraintViolation<PatientMapper>> constraintViolations = validator.validateValue(PatientMapper.class, "maritalStatus", "8");
        assertEquals(1, constraintViolations.size());
        assertEquals("1004", constraintViolations.iterator().next().getMessage());
    }
}
