package org.sharedhealth.mci.domain.model;

import org.junit.runner.RunWith;
import org.sharedhealth.mci.domain.config.EnvironmentMock;
import org.sharedhealth.mci.domain.config.TestWebMvcConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(initializers = EnvironmentMock.class, classes = TestWebMvcConfig.class)
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