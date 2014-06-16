package org.mci.web.model;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.hibernate.validator.HibernateValidator;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AddressValidationTest {

    private static Validator validator;

    @BeforeClass
    public static void setUp() {
        ValidatorFactory factory = Validation.byProvider(HibernateValidator.class)
                .configure()
                .buildValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void shouldFailIfAddressLineSizeLessThan3() {
        Set<ConstraintViolation<Address>> constraintViolations = validator.validateValue(Address.class, "addressLine", "ab");

        assertEquals(1, constraintViolations.size());
        assertEquals("size must be between 3 and 20", constraintViolations.iterator().next().getMessage()
        );
    }

    @Test
    public void shouldPassIfAddressLineSize3() {
        Set<ConstraintViolation<Address>> constraintViolations = validator.validateValue(Address.class, "addressLine", "abc");
        assertEquals(0, constraintViolations.size());
    }
}
