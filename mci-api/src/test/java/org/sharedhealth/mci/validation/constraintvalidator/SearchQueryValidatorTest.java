package org.sharedhealth.mci.validation.constraintvalidator;

import javax.validation.ConstraintViolation;
import java.util.Set;

import org.junit.Test;
import org.sharedhealth.mci.web.mapper.SearchQuery;
import org.sharedhealth.mci.web.mapper.ValidationAwareMapper;

import static org.junit.Assert.assertEquals;

public class SearchQueryValidatorTest extends ValidationAwareMapper {

    @Test
    public void shouldFailForEmptySearchQuery() throws Exception {
        SearchQuery searchQuery = getEmptySearchQuery();
        assertInvalidSearchQuery(searchQuery, "No valid search parameter given");
    }

    @Test
    public void shouldPassIfOnlyNidGiven() throws Exception {
        SearchQuery searchQuery = getEmptySearchQuery();
        searchQuery.setNid("1234567890123");
        assertValidSearchQuery(searchQuery);
    }

    @Test
    public void shouldPassIfOnlyBrnGiven() throws Exception {
        SearchQuery searchQuery = getEmptySearchQuery();
        searchQuery.setBin_brn("12345678901234567");
        assertValidSearchQuery(searchQuery);
    }

    @Test
    public void shouldPassIfOnlyUidGiven() throws Exception {
        SearchQuery searchQuery = getEmptySearchQuery();
        searchQuery.setUid("12345678912");
        assertValidSearchQuery(searchQuery);
    }

    @Test
    public void shouldPassIfOnlyPhoneNoGiven() throws Exception {
        SearchQuery searchQuery = getEmptySearchQuery();
        searchQuery.setPhone_no("12345678912");
        assertValidSearchQuery(searchQuery);
    }

    @Test
    public void shouldPassIfOtherFieldsWithAnID_FieldGiven() throws Exception {
        SearchQuery searchQuery = getEmptySearchQuery();
        searchQuery.setUid("12345678912");
        searchQuery.setGiven_name("given name");
        assertValidSearchQuery(searchQuery);
    }

    @Test
    public void givenNameIsRequiredIfSurNameIsProvidedWithAnyBusinessRuleFailed() throws Exception {
        SearchQuery searchQuery = getEmptySearchQuery();
        searchQuery.setSur_name("surname");
        assertInvalidSearchQuery(searchQuery, "1001");
    }

    @Test
    public void givenNameIsNotRequiredEventSurNameIsProvidedAlongWithSingleSearchableField() throws Exception {
        SearchQuery searchQuery = getEmptySearchQuery();
        searchQuery.setUid("12345678912");
        searchQuery.setSur_name("surname");
        assertValidSearchQuery(searchQuery);
    }

    @Test
    public void shouldPassForGivenNameAndAddress() throws Exception {
        SearchQuery searchQuery = getEmptySearchQuery();
        searchQuery.setPresent_address("123456");
        searchQuery.setGiven_name("Given name");
        searchQuery.setSur_name("surname");
        assertValidSearchQuery(searchQuery);
    }

    @Test
    public void shouldFailForSurNameWithoutGivenNameAlongWithAddress() throws Exception {
        SearchQuery searchQuery = getEmptySearchQuery();
        searchQuery.setPresent_address("123456");
        searchQuery.setSur_name("surname");
        assertInvalidSearchQuery(searchQuery, "1001");
    }

    @Test
    public void shouldPassForNameAndPhoneNumber() throws Exception {
        SearchQuery searchQuery = getEmptySearchQuery();
        searchQuery.setPhone_no("123456");
        searchQuery.setGiven_name("given name");
        assertValidSearchQuery(searchQuery);
    }

    @Test
    public void phoneNoIsRequiredIfOptionalPhoneNumberBlockGiven() throws Exception {
        SearchQuery searchQuery = getEmptySearchQuery();
        searchQuery.setCountry_code("1234");
        assertInvalidSearchQuery(searchQuery, "1001");
    }

    @Test
    public void addressShouldComplyWithValidPattern() throws Exception {
        SearchQuery searchQuery = getEmptySearchQuery();
        searchQuery.setGiven_name("given name");
        assertSearchQueryWithInvalidAddress(searchQuery);
        assertSearchQueryWithValidAddress(searchQuery);
    }

    private void assertSearchQueryWithInvalidAddress(SearchQuery searchQuery) {

        String[] inValidAddress = {"", "somevalue", "12", "1234", "12345", "1234567", "123456789", "12345678901", "1234567890121"};
        for (String address : inValidAddress) {
            searchQuery.setPresent_address(address);
            assertInvalidSearchQuery(searchQuery, "1002");
        }
    }

    private void assertSearchQueryWithValidAddress(SearchQuery searchQuery) {
        String[] validAddress = {"123456", "12345678", "1234567890", "123456789012"};
        for (String address : validAddress) {
            searchQuery.setPresent_address(address);
            assertValidSearchQuery(searchQuery);
        }
    }

    private void assertInvalidSearchQuery(SearchQuery searchQuery, String expectedErrorCode) {
        Set<ConstraintViolation<SearchQuery>> constraintViolations = validator.validate(searchQuery);
        assertEquals(1, constraintViolations.size());
        assertEquals(expectedErrorCode, constraintViolations.iterator().next().getMessage());
    }

    private void assertValidSearchQuery(SearchQuery searchQuery) {
        Set<ConstraintViolation<SearchQuery>> constraintViolations = validator.validate(searchQuery);
        assertEquals(0, constraintViolations.size());
    }

    @Test
    public void shouldPassForValidSearchQuery() throws Exception {
        SearchQuery searchQuery = getFullSearchQuery();
        assertValidSearchQuery(searchQuery);
    }

    private SearchQuery getFullSearchQuery() {
        SearchQuery searchQuery = getEmptySearchQuery();
        searchQuery.setNid("1234567890123");
        searchQuery.setBin_brn("12345678901234567");
        searchQuery.setUid("12345678912");
        searchQuery.setGiven_name("given name");
        searchQuery.setSur_name("surname");
        searchQuery.setCountry_code("123");
        searchQuery.setArea_code("02");
        searchQuery.setPhone_no("1234567890");
        searchQuery.setExtension("101");
        searchQuery.setPresent_address("111111");

        return searchQuery;
    }

    private SearchQuery getEmptySearchQuery() {
        return new SearchQuery();
    }
}