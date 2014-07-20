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

}
