package org.sharedhealth.mci.validation.constraintvalidator;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.mci.web.config.EnvironmentMock;
import org.sharedhealth.mci.web.config.WebMvcConfigTest;
import org.sharedhealth.mci.web.mapper.Relation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(initializers = EnvironmentMock.class, classes = WebMvcConfigTest.class)
public class CodeValidatorTest {

    @Autowired
    private Validator validator;

    @Test
    public void shouldPassForValidRelationType() throws Exception {
        String[] validRelations = {"FTH", "MTH", "SPS", "CHILD"};
        for (String relation : validRelations) {
            Set<ConstraintViolation<Relation>> constraintViolations = validator.validateValue(Relation.class, "type", relation);
            assertEquals(0, constraintViolations.size());
        }
    }

    @Test
    public void shouldFailForInvalidRelationType() throws Exception {
        String[] inValidRelations = {"", "somevalue"};
        for (String relation : inValidRelations) {
            Set<ConstraintViolation<Relation>> constraintViolations = validator.validateValue(Relation.class, "type", relation);
            assertEquals(1, constraintViolations.size());
            assertEquals("1004", constraintViolations.iterator().next().getMessage());
        }
    }
}