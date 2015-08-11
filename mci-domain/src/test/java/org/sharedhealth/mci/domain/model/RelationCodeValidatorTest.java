package org.sharedhealth.mci.domain.model;

import org.junit.Test;

import javax.validation.ConstraintViolation;
import java.util.Set;

import static org.junit.Assert.assertEquals;


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

    @Test
    public void shouldFailIfTypeIsNullForNonEmptyRelation() {
        Relation relation = new Relation();
        relation.setHealthId("12345678901");
        Set<ConstraintViolation<Relation>> constraintViolations = getValidator().validate(relation);
        assertEquals(1, constraintViolations.size());
        assertEquals("1001", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldFailIfOnlyTypeGiven() {
        Relation relation = new Relation();
        relation.setType("FTH");
        Set<ConstraintViolation<Relation>> constraintViolations = getValidator().validate(relation);
        assertEquals(1, constraintViolations.size());
        assertEquals("1001", constraintViolations.iterator().next().getMessage());
    }
}