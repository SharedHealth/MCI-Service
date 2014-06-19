package org.mci.web.model;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import org.hibernate.validator.HibernateValidator;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;

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
        assertEquals("may not be empty", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldFailIfLastNameIsBlank() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "lastName", "");
        assertEquals(1, constraintViolations.size());
        assertEquals("may not be empty", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldFailIfDateOfBirthIsBlank() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "dateOfBirth", "   ");
        assertEquals(1, constraintViolations.size());
        assertEquals("may not be empty", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldFailIfDateOfBirthIsInvalidDate() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "dateOfBirth", "1999-02-30");
        assertEquals(1, constraintViolations.size());
        assertEquals("Must provide date of format yyyy-MM-dd", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldPassIfDateOfBirthIsValidDate() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "dateOfBirth", "1983-09-21");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldPassIfGenderValid() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "gender", "3");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldFailIfGenderIsBlank() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "gender", null);
        assertEquals(1, constraintViolations.size());
        assertEquals("may not be empty", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldFailIfGenderIsInvalid() {
        Set<ConstraintViolation<Patient>> constraintViolations = validator.validateValue(Patient.class, "gender", "5");
        assertEquals(1, constraintViolations.size());
        assertEquals("must match \"[1-3]{1}\"", constraintViolations.iterator().next().getMessage());
    }
}
