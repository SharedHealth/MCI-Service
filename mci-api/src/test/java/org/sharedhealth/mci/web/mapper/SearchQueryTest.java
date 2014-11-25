package org.sharedhealth.mci.web.mapper;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SearchQueryTest {

    @Test
    public void shouldExtractIndividualAddressLevelsFromPresentAddress() {
        SearchQuery query = new SearchQuery();
        query.setPresent_address("112233");
        assertEquals("11", query.getDivisionId());
        assertEquals("22", query.getDistrictId());
        assertEquals("33", query.getUpazilaId());
    }

}