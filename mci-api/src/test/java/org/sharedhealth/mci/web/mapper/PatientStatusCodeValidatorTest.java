package org.sharedhealth.mci.web.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.mci.web.config.EnvironmentMock;
import org.sharedhealth.mci.web.config.WebMvcConfigTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.validation.ConstraintViolation;
import java.util.Set;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(initializers = EnvironmentMock.class, classes = WebMvcConfigTest.class)
public class PatientStatusCodeValidatorTest extends BaseCodeValidatorTest<PatientStatus> {

    @Test
    public void shouldPassForValidValues() throws Exception {
        String[] validStatus = {"1","2","3"};
        assertValidValues(validStatus, "type", PatientStatus.class);
    }

    @Test
    public void shouldFailForInvalidValues() throws Exception {
        PatientStatus patientStatus = new PatientStatus();
        patientStatus.setType("4");
        Set<ConstraintViolation<PatientStatus>> constraintViolations = getValidator().validate(patientStatus);
        assertEquals(1, constraintViolations.size());
        assertEquals("1004", constraintViolations.iterator().next().getMessage());
    }
}