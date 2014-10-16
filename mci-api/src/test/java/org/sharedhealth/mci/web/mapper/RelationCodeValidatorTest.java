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
public class RelationCodeValidatorTest extends BaseCodeValidatorTest<Relation> {

    @Test
    public void shouldPassForValidValues() throws Exception {
        String[] validRelations = {"FTH"};
        assertValidValues(validRelations, "type", Relation.class);
    }

    @Test
    public void shouldFailForInvalidValues() throws Exception {
        String[] inValidRelations = {"", "some_invalid_code"};
        assertInvalidValues(inValidRelations, "type", Relation.class);
    }
}