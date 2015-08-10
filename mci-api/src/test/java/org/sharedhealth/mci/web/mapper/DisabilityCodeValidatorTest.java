package org.sharedhealth.mci.web.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.mci.domain.model.PatientData;
import org.sharedhealth.mci.web.config.EnvironmentMock;
import org.sharedhealth.mci.web.config.WebMvcConfigTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(initializers = EnvironmentMock.class, classes = WebMvcConfigTest.class)
public class DisabilityCodeValidatorTest extends BaseCodeValidatorTest<PatientData> {

    @Test
    public void shouldPassForValidValues() throws Exception {
        String[] validStatuses = {"0", "1", "2", "3"};
        assertValidValues(validStatuses, "disability", PatientData.class);
    }

    @Test
    public void shouldFailForInvalidValues() throws Exception {
        String[] inValidRelations = {"", "some_invalid_code", "6", "01"};
        assertInvalidValues(inValidRelations, "disability", PatientData.class);
    }
}