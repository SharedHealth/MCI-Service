package org.sharedhealth.mci.web.mapper;

import org.hibernate.validator.HibernateValidator;
import org.junit.BeforeClass;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

public class ValidationAwareMapper {

    protected static Validator validator;

    @BeforeClass
    public static void setUp() {
        ValidatorFactory factory = Validation.byProvider(HibernateValidator.class)
                .configure()
                .buildValidatorFactory();
        validator = factory.getValidator();
    }

}
