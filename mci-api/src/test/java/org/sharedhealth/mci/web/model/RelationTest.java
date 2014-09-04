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

public class RelationTest {

    private static Validator validator;

    @BeforeClass
    public static void setUp() {
        ValidatorFactory factory = Validation.byProvider(HibernateValidator.class)
                .configure()
                .buildValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void shouldFailIf_Marriage_Id_LengthIsNotEqual_8() {
        Set<ConstraintViolation<Relation>> constraintViolations = validator.validateValue(Relation.class, "marriageId", "1");
        assertEquals(1, constraintViolations.size());
        assertEquals("1029", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldFailIf_Marriage_Id_ContainSpecialCharacter() {
        Set<ConstraintViolation<Relation>> constraintViolations = validator.validateValue(Relation.class, "marriageId", "123456*8901");
        assertEquals(1, constraintViolations.size());
        assertEquals("1029", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldPassIf_Marriage_Id_Is_8_DigitAlphaNumeric() {
        Set<ConstraintViolation<Relation>> constraintViolations = validator.validateValue(Relation.class, "marriageId", "MM001177");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldPassIfMaritalStatusIsValid() {
        for(int i = 1; i<6; i++) {
            Set<ConstraintViolation<Relation>> constraintViolations = validator.validateValue(Relation.class, "maritalStatus", Integer.toString(i));
            assertEquals(0, constraintViolations.size());
        }
    }

    @Test
    public void shouldFailIfMaritalStatusIsInvalid() {
        Set<ConstraintViolation<Relation>> constraintViolations = validator.validateValue(Relation.class, "maritalStatus", "8");
        assertEquals(1, constraintViolations.size());
        assertEquals("1028", constraintViolations.iterator().next().getMessage());
    }
}
