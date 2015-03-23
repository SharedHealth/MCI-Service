package org.sharedhealth.mci.web.mapper;

import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public abstract class BaseCodeValidatorTest<T> {

    @Autowired
    private Validator validator;

    public void assertValidValues(String[] values, String property, Class<T> tClass) throws Exception {
        for (String value : values) {
            Set<ConstraintViolation<T>> constraintViolations = validator.validateValue(tClass, property, value);
            assertEquals(0, constraintViolations.size());
        }
    }

    public void assertInvalidValues(String[] inValidValues, String property, Class<T> tClass) throws Exception {

        for (String code : inValidValues) {
            Set<ConstraintViolation<T>> constraintViolations = validator.validateValue(tClass, property, code);
            assertEquals(1, constraintViolations.size());
            assertEquals("1004", constraintViolations.iterator().next().getMessage());
        }
    }

    public Validator getValidator() {
        return validator;
    }
}