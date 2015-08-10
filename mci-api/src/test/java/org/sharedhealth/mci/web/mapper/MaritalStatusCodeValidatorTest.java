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
public class MaritalStatusCodeValidatorTest extends BaseCodeValidatorTest<PatientData> {

    @Test
    public void shouldPassForValidValues() throws Exception {
        String[] validStatuses = {"1", "2", "3", "4", "5"};
        assertValidValues(validStatuses, "maritalStatus", PatientData.class);
    }

    @Test
    public void shouldFailForInvalidValues() throws Exception {
        String[] inValidRelations = {"", "some_invalid_code", "6", "0"};
        assertInvalidValues(inValidRelations, "maritalStatus", PatientData.class);
    }
}