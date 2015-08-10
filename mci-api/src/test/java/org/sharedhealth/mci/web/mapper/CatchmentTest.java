package org.sharedhealth.mci.web.mapper;

import org.junit.Test;
import org.sharedhealth.mci.domain.model.Catchment;

import java.util.List;

import static java.util.Arrays.asList;
import static org.apache.commons.collections4.CollectionUtils.isEqualCollection;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CatchmentTest {

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenMandatoryLevelsNotSet() {
        new Catchment("10", "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenCityCorpSetWithoutProperHierarchy() {
        new Catchment("10", "20").setCityCorpId("40");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenUnionSetWithoutProperHierarchy() {
        new Catchment("10", "20").setUnionOrUrbanWardId("50");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenRuralWardSetWithoutProperHierarchy() {
        new Catchment("10", "20").setRuralWardId("60");
    }

    @Test
    public void shouldCreateIdFromAllLevels() {
        Catchment catchment = new Catchment("10", "20", "30", "40", "50", "60");
        assertEquals("A10B20C30D40E50F60", catchment.getId());
    }

    @Test
    public void shouldCreateIdFromMandatoryLevels() {
        Catchment catchment = new Catchment("10", "20");
        assertEquals("A10B20", catchment.getId());
    }

    @Test
    public void shouldCreateIdFromTopFewLevels() {
        Catchment catchment = new Catchment("10", "20");
        catchment.setUpazilaId("30");
        catchment.setCityCorpId("40");
        assertEquals("A10B20C30D40", catchment.getId());
    }

    @Test
    public void shouldCreateAllPossibleIds() {
        Catchment catchment = new Catchment("10", "20");
        catchment.setUpazilaId("30");
        catchment.setCityCorpId("40");
        catchment.setUnionOrUrbanWardId("50");
        catchment.setRuralWardId("60");
        List<String> ids = catchment.getAllIds();
        assertEquals(5, ids.size());
        List<String> expected = asList("A10B20", "A10B20C30", "A10B20C30D40", "A10B20C30D40E50", "A10B20C30D40E50F60");
        assertTrue(isEqualCollection(expected, ids));
    }

    @Test
    public void shouldCreateIdFromCatchmentStringWithAllLevels() {
        Catchment catchment = new Catchment("102030405060");
        assertEquals("A10B20C30D40E50F60", catchment.getId());
    }

    @Test
    public void shouldCreateIdFromCatchmentStringWithMandatoryLevels() {
        Catchment catchment = new Catchment("1020");
        assertEquals("A10B20", catchment.getId());
    }

    @Test
    public void shouldCreateIdFromCatchmentStringWithTopFewLevels() {
        Catchment catchment = new Catchment("10203040");
        assertEquals("A10B20C30D40", catchment.getId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenCatchmentStringDoesNotContainMandatoryLevels() {
        new Catchment("10");
    }
}