package org.sharedhealth.mci.web.mapper;

import org.junit.Test;
import org.sharedhealth.mci.domain.model.SearchQuery;

import static org.junit.Assert.assertEquals;

public class SearchQueryTest {

    @Test
    public void shouldExtractIndividualAddressLevelsFromPresentAddress() {
        SearchQuery query = new SearchQuery();
        query.setPresent_address("112233445566");
        assertEquals("11", query.getDivisionId());
        assertEquals("22", query.getDistrictId());
        assertEquals("33", query.getUpazilaId());
        assertEquals("44", query.getCityCorporationId());
        assertEquals("55", query.getUnionOrUrbanWardId());
        assertEquals("66", query.getRuralWardId());
    }

}