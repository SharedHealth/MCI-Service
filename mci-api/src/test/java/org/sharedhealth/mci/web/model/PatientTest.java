package org.sharedhealth.mci.web.model;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;
import org.hibernate.validator.HibernateValidator;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class PatientTest {

    private static Validator validator;

    @BeforeClass
    public static void setUp() {
        ValidatorFactory factory = Validation.byProvider(HibernateValidator.class)
                .configure()
                .buildValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void shouldFailIfFirstNameIsBlank() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "firstName", "");
        assertEquals(1, constraintViolations.size());
        assertEquals("1004", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldFailIfLastNameIsBlank() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "lastName", "");
        assertEquals(1, constraintViolations.size());
        assertEquals("1006", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldFailIfDateOfBirthIsBlank() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "dateOfBirth", "   ");
        assertEquals(1, constraintViolations.size());
        assertEquals("1007", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldFailIfDateOfBirthIsInvalidDate() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "dateOfBirth", "1999-02-30");
        assertEquals(1, constraintViolations.size());
        assertEquals("1008", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldPassIfDateOfBirthIsValidDate() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "dateOfBirth", "1983-09-21");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldPassIfGenderValid() {
        for (int i = 1; i < 4; i++) {
            Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "gender", Integer.toString(i));
            assertEquals(0, constraintViolations.size());
        }
    }

    @Test
    public void shouldFailIfGenderIsBlank() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "gender", null);
        assertEquals(1, constraintViolations.size());
        assertEquals("1009", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldFailIfGenderIsInvalid() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "gender", "5");
        assertEquals(1, constraintViolations.size());
        assertEquals("1010", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldFailIfNationalIdIsInvalid() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "nationalId", "1");
        assertEquals(1, constraintViolations.size());
        assertEquals("1001", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldPassIfNationalIdIs_13_DigitLong() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "nationalId", "1234567890123");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldPassIfNationalIdIs_17_DigitLong() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "nationalId", "12345678901234567");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldFailIf_UID_LengthIsNotEqual_11() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "uid", "1");
        assertEquals(1, constraintViolations.size());
        assertEquals("1027", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldFailIf_UUID_ContainSpecialCharacter() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "uid", "123456*8901");
        assertEquals(1, constraintViolations.size());
        assertEquals("1027", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldPassIf_UID_Is_11_DigitAlphaNumeric() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "uid", "UID45678901");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldFailIfOccupationCodeIsLessThenOne() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "occupation", "00");
        assertEquals(1, constraintViolations.size());
        assertEquals("1011", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldFailIfOccupationCodeIsGreaterThen92() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "occupation", "93");
        assertEquals(1, constraintViolations.size());
        assertEquals("1011", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldPassIfOccupationCodeIsValid() {
        for (int i = 1; i <93; i++) {
            Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "occupation", String.format("%02d", i));
            assertEquals(0, constraintViolations.size());
        }
    }

    @Test
    public void shouldPassIfMaritalStatusIsValid() {
        for(int i = 1; i<6; i++) {
            Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "maritalStatus", Integer.toString(i));
            assertEquals(0, constraintViolations.size());
        }
    }

    @Test
    public void shouldFailIfMaritalStatusIsInvalid() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "maritalStatus", "8");
        assertEquals(1, constraintViolations.size());
        assertEquals("1028", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldPassIfReligionIsValid() {
        for(int i = 1; i<8; i++) {
            Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "religion", Integer.toString(i));
            assertEquals(0, constraintViolations.size());
        }
    }

    @Test
    public void shouldFailIfReligionIsInvalid() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "religion", "9");
        assertEquals(1, constraintViolations.size());
        assertEquals("1031", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldPassIfBirthRegistrationNumberIsValid() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "birthRegistrationNumber", "12345674891234567");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldFailIfBirthRegistrationNumberIsLessThan17() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "birthRegistrationNumber", "1234567489123456");
        assertEquals("1002", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldFailIfBirthRegistrationNumberIsMoreThan17() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "birthRegistrationNumber", "123456748912345644");
        assertEquals("1002", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldFailIfContainSpecialCharacter() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "birthRegistrationNumber", "123456748*12345644");
        assertEquals("1002", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldPassIfFullNameBanglaIsValid() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "fullNameBangla", "এ বি এম আখতার হোসেন মন্ডল");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldFailIfFullNameBanglaIsMoreThan_120_Characters() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "fullNameBangla", "এ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডল");
        assertEquals("1003", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldFailIfFirstNameIsMoreThan_25_Characters() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "firstName", "janagiralamkabirkhanjahanaliahmadpuri");
        assertEquals("1036", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldPassIfFirstNameIsValid() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "firstName", "imran");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldPassIfLastNameIsValid() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "lastName", "imran");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldFailIfLastNameIsMoreThan_25_Characters() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "lastName", "janagiralamkabirkhanjahanaliahmadpuri");
        assertEquals("1037", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldPassIfMiddleNameIsValid() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "middleName", "imran");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldFailIfMiddleNameIsMoreThan_25_Characters() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "middleName", "janagiralamkabirkhanjahanaliahmadpuri");
        assertEquals("1005", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldFailIfEducationLevelIsGreaterThen20() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "educationLevel", "20");
        assertEquals(1, constraintViolations.size());
        assertEquals("1012", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldFailIfEducationLevelIsNotTwoDigitFormat() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "educationLevel", "1");
        assertEquals(1, constraintViolations.size());
        assertEquals("1012", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldPassIfEducationLevelIsValid() {
        for (int i = 0; i <20; i++) {
            Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "educationLevel", String.format("%02d", i));
            assertEquals(0, constraintViolations.size());
        }
    }

    @Test
    public void shouldPassIfFathersNameBanglaIsValid() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "fathersNameBangla", "এ বি এম আখতার হোসেন মন্ডল");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldFailIfFathersNameBanglaIsMoreThan_120_Characters() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "fathersNameBangla", "এ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডল");
        assertEquals("1013", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldFailIfFathersFirstNameIsMoreThan_25_Characters() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "fathersFirstName", "janagiralamkabirkhanjahanaliahmadpuri");
        assertEquals("1014", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldPassIfFathersFirstNameIsValid() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "fathersFirstName", "imran");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldFailIfFathersMiddleNameIsMoreThan_25_Characters() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "fathersMiddleName", "janagiralamkabirkhanjahanaliahmadpuri");
        assertEquals("1015", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldPassIfFathersMiddleNameIsValid() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "fathersMiddleName", "imran");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldFailIfFathersLastNameIsMoreThan_25_Characters() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "fathersLastName", "janagiralamkabirkhanjahanaliahmadpuri");
        assertEquals("1016", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldPassIfFathersLastNameIsValid() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "fathersLastName", "imran");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldFailIfFathers_UID_LengthIsNotEqual_11() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "fathersUid", "1");
        assertEquals(1, constraintViolations.size());
        assertEquals("1017", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldFailIf_Fathers_UID_ContainSpecialCharacter() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "fathersUid", "123456*8901");
        assertEquals(1, constraintViolations.size());
        assertEquals("1017", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldPassIf_Fathers_UID_Is_11_DigitAlphaNumeric() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "fathersUid", "UID45678901");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldFailIfFathersNationalIdIsInvalid() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "fathersNid", "1");
        assertEquals(1, constraintViolations.size());
        assertEquals("1018", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldPassIfFathersNationalIdIs_13_DigitLong() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "fathersNid", "1234567890123");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldPassIfFathersNationalIdIs_17_DigitLong() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "fathersNid", "12345678901234567");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldPassIfFathersBirthRegistrationNumberIsValid() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "fathersBrn", "12345674891234567");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldFailIfFathersBirthRegistrationNumberIsLessThan17() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "fathersBrn", "1234567489123456");
        assertEquals("1019", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldFailIfFathersBirthRegistrationNumberIsMoreThan17() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "fathersBrn", "123456748912345644");
        assertEquals("1019", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldPassIfMothersNameBanglaIsValid() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "mothersNameBangla", "আনোয়ারা খাতুন");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldFailIfMothersNameBanglaIsMoreThan_120_Characters() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "mothersNameBangla", "এ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডল");
        assertEquals("1020", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldFailIfMothersFirstNameIsMoreThan_25_Characters() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "mothersFirstName", "janagiralamkabirkhanjahanaliahmadpuri");
        assertEquals("1021", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldPassIfMothersFirstNameIsValid() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "mothersFirstName", "annowara");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldFailIfMothersMiddleNameIsMoreThan_25_Characters() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "mothersMiddleName", "janagiralamkabirkhanjahanaliahmadpuri");
        assertEquals("1022", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldPassIfMothersMiddleNameIsValid() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "mothersMiddleName", "anowara");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldFailIfMothersLastNameIsMoreThan_25_Characters() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "mothersLastName", "janagiralamkabirkhanjahanaliahmadpuri");
        assertEquals("1023", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldPassIfMothersLastNameIsValid() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "mothersLastName", "khatun");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldFailIfMothers_UID_LengthIsNotEqual_11() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "mothersUid", "1");
        assertEquals(1, constraintViolations.size());
        assertEquals("1024", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldFailIf_Mothers_UID_ContainSpecialCharacter() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "mothersUid", "123456*8901");
        assertEquals(1, constraintViolations.size());
        assertEquals("1024", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldPassIf_Mothers_UID_Is_11_DigitAlphaNumeric() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "mothersUid", "UID45678901");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldFailIfMothersNationalIdIsInvalid() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "mothersNid", "1");
        assertEquals(1, constraintViolations.size());
        assertEquals("1025", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldPassIfMothersNationalIdIs_13_DigitLong() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "mothersNid", "1234567890123");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldPassIfMothersNationalIdIs_17_DigitLong() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "mothersNid", "12345678901234567");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldPassIfMothersBirthRegistrationNumberIsValid() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "mothersBrn", "12345674891234567");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldFailIfMothersBirthRegistrationNumberIsLessThan17() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "mothersBrn", "1234567489123456");
        assertEquals("1026", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldFailIfMothersBirthRegistrationNumberIsMoreThan17() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "mothersBrn", "123456748912345644");
        assertEquals("1026", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldFailIfPlaceOfBirthIsMoreThan_7_AlphabeticCharacters() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "placeOfBirth", "DhanmondiDhanmondi");
        assertEquals("1038", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldFailIfPlaceOfBirthIsContainSpecialCharacterAndNumericCharacter() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "placeOfBirth", "9;");
        assertEquals("1038", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldPassIfPlaceOfBirthIsValid() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "placeOfBirth", "Dhaka");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldFailIf_Marriage_Id_LengthIsNotEqual_8() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "marriageId", "1");
        assertEquals(1, constraintViolations.size());
        assertEquals("1029", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldFailIf_Marriage_Id_ContainSpecialCharacter() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "marriageId", "123456*8901");
        assertEquals(1, constraintViolations.size());
        assertEquals("1029", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldPassIf_Marriage_Id_Is_8_DigitAlphaNumeric() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "marriageId", "MM001177");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldFailIfSpouseNameIsMoreThan_100_Characters() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "spouseName", "janagiralamkabirkhanjahanaliahmahanaliahmahanaliahmahanaliahmahanaliahmadpurijanagiralamkabirkhanjahanaliahmahanaliahmahanaliahmahanaliahmahanaliahmadpurijanagiralamkabirkhanjahanaliahmahanaliahmahanaliahmahanaliahmahanaliahmadpuri");
        assertEquals("1030", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldPassIfSpouseNameIsValid() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "spouseName", "imran");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldPassIfSpouseNameBanglaIsValid() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "spouseNameBangla", "এ বি এম আখতার হোসেন মন্ডল");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldFailIfSpouseNameBanglaIsMoreThan_120_Characters() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "spouseNameBangla", "এ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডলএ বি এম আখতার হোসেন মন্ডল");
        assertEquals("1039", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldFailIfSpouseUidNidIsInvalid() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "spouseUidNid", "1");
        assertEquals(1, constraintViolations.size());
        assertEquals("1040", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldPassIfSpouseUidNidIs_10_DigitLong() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "spouseUidNid", "1234567890");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldPassIfSpouseUidNidIs_17_DigitLong() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "spouseUidNid", "12345678901234567");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldPassIfNationalityIsValid() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "nationality", "bangladeshi");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldFailIfNationalityIsMoreThan_50_Characters() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "nationality", "bangladeshi bangladeshi bangladeshi bangladeshi bangladeshi bangladeshi ");
        assertEquals("1033", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldPassIfAliveIsValid() {
        for(int i = 1; i<3; i++) {
            Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "isAlive", Integer.toString(i));
            assertEquals(0, constraintViolations.size());
        }
    }

    @Test
    public void shouldFailIfAliveIsInvalid() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "isAlive", "3");
        assertEquals(1, constraintViolations.size());
        assertEquals("1041", constraintViolations.iterator().next().getMessage());
    }

}
