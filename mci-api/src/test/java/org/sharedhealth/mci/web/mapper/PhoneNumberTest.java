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

public class PhoneNumberTest {
    private static Validator validator;

    @BeforeClass
    public static void setUp() {
        ValidatorFactory factory = Validation.byProvider(HibernateValidator.class)
                .configure()
                .buildValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void ShouldFailIFCountryCodeIsInvalid(){
        Set<ConstraintViolation<PhoneNumber>> constraintViolations = validator.validateValue(PhoneNumber.class, "countryCode", "adfdfdsdfdfdfdfdfdffdfdfdfdf");
        assertEquals("1002", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void ShouldPassIFCountryCodeIsvalid(){
        Set<ConstraintViolation<PhoneNumber>> constraintViolations = validator.validateValue(PhoneNumber.class, "countryCode", "88");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void ShouldFailIFAreaCodeIsInvalid(){
        Set<ConstraintViolation<PhoneNumber>> constraintViolations = validator.validateValue(PhoneNumber.class, "areaCode", "adfdfdsdfdfdfdfdfdffdfdfdfdf");
        assertEquals("1002", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void ShouldPassIFAreaCodeIsvalid(){
        Set<ConstraintViolation<PhoneNumber>> constraintViolations = validator.validateValue(PhoneNumber.class, "areaCode", "02");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void ShouldFailIFExtensionIsInvalid(){
        Set<ConstraintViolation<PhoneNumber>> constraintViolations = validator.validateValue(PhoneNumber.class, "extension", "adfdfdsdfdfdfdfdfdff");
        assertEquals("1002", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void ShouldPassIFExtensionIsvalid(){
        Set<ConstraintViolation<PhoneNumber>> constraintViolations = validator.validateValue(PhoneNumber.class, "extension", "122");
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void ShouldFailIFPhoneNoIsMorThan12Digit(){
        Set<ConstraintViolation<PhoneNumber>> constraintViolations = validator.validateValue(PhoneNumber.class, "number", "1234567890123455666");
        assertEquals("1002", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void ShouldFailIFPhoneNoIsInvalid(){
        Set<ConstraintViolation<PhoneNumber>> constraintViolations = validator.validateValue(PhoneNumber.class, "number", "aaa");
        assertEquals("1002", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void ShouldPassIFPhoneNoIsvalid(){
        Set<ConstraintViolation<PhoneNumber>> constraintViolations = validator.validateValue(PhoneNumber.class, "number", "1234566788");
        assertEquals(0, constraintViolations.size());
    }
}
