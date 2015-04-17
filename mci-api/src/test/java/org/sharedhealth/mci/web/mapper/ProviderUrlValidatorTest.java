package org.sharedhealth.mci.web.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.mci.web.config.EnvironmentMock;
import org.sharedhealth.mci.web.config.WebMvcConfigTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(initializers = EnvironmentMock.class, classes = WebMvcConfigTest.class)
public class ProviderUrlValidatorTest extends BaseCodeValidatorTest<PatientData> {

    @Test
    public void shouldPassForValidValues() throws Exception {
        String[] values = {"http://localhost:9997/api/1.0/providers/100123.json"};
        assertValidValues(values, "provider", PatientData.class);
    }

    @Test
    public void shouldFailForInvalidValues() throws Exception {
        String[] values = {"https://localhost:9997/api/1.0/providers/100123.json"};
        assertInvalidValues(values, "provider", PatientData.class);
    }
}