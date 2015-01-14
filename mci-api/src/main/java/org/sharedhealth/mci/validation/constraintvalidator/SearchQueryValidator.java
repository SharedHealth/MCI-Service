package org.sharedhealth.mci.validation.constraintvalidator;

import org.apache.commons.lang3.StringUtils;
import org.sharedhealth.mci.validation.constraints.SearchQueryConstraint;
import org.sharedhealth.mci.web.mapper.SearchQuery;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.sharedhealth.mci.web.utils.JsonConstants.*;

public class SearchQueryValidator implements ConstraintValidator<SearchQueryConstraint, SearchQuery> {

    private Pattern pattern;

    private static final String GEOCODE_VALID_PATTERN = "[\\d]{6}|[\\d]{8}|[\\d]{10}|[\\d]{12}";
    private static final String ERROR_CODE_REQUIRED = "1001";
    private static final String ERROR_CODE_PATTERN = "1002";
    private static final String ERROR_EMPTY_SEARCH_QUERY = "No valid search parameter given";
    private static final String ERROR_INCOMPLETE_SEARCH_CRITERIA = "Incomplete search criteria!";
    private static final String ERROR_GIVEN_NAME_REQUIRED = "Please enter a valid name";
    private static final String ERROR_ID_ADDRESS_OR_PHONE_NUMBER_REQUIRED = "Please provide a valid ID, Address or Phone number";
    private static final String ERROR_ID_NAME_OR_PHONE_NUMBER_REQUIRED = "Please provide a valid ID, Name or Phone number";

    @Override
    public void initialize(SearchQueryConstraint constraintAnnotation) {
        this.pattern = Pattern.compile(GEOCODE_VALID_PATTERN);
    }

    @Override
    public boolean isValid(final SearchQuery value, final ConstraintValidatorContext context) {

        boolean isValid;

        context.disableDefaultConstraintViolation();

        isValid = isValidDataGiven(value, context);

        return isValid && isBusinessRulesValidationPassed(value, context);

    }

    private boolean isBusinessRulesValidationPassed(SearchQuery searchQuery, ConstraintValidatorContext context) {

        if (searchQuery.isEmpty()) {
            addConstraintViolation(context, ERROR_EMPTY_SEARCH_QUERY);
            return false;
        }

        final boolean isValid = isSingleSearchableFieldsGiven(searchQuery) || isNameAndAddressGiven(searchQuery);

        if (!isValid) {
            registerProperErrorMessage(searchQuery, context);
        }

        return isValid;
    }

    private void registerProperErrorMessage(SearchQuery searchQuery, ConstraintValidatorContext context) {
        String msg = ERROR_INCOMPLETE_SEARCH_CRITERIA;
        if (isBlank(searchQuery.getGiven_name()) && isGivenNameRequired(searchQuery)) {
            msg = ERROR_GIVEN_NAME_REQUIRED;
        } else if (isNotBlank(searchQuery.getGiven_name()) || isNotBlank(searchQuery.getGiven_name())) {
            msg = ERROR_ID_ADDRESS_OR_PHONE_NUMBER_REQUIRED;
        } else if (isNotBlank(searchQuery.getPresent_address())) {
            msg = ERROR_ID_NAME_OR_PHONE_NUMBER_REQUIRED;
        }

        addConstraintViolation(context, msg);
    }

    private boolean isNameAndAddressGiven(SearchQuery value) {
        return value.getPresent_address() != null && value.getGiven_name() != null;
    }

    private boolean isValidDataGiven(SearchQuery searchQuery, ConstraintValidatorContext context) {
        boolean isValid = true;

        if (searchQuery.getGiven_name() == null && isGivenNameRequired(searchQuery)) {
            isValid = false;
            addConstraintViolation(context, ERROR_CODE_REQUIRED, GIVEN_NAME);
        }

        if (searchQuery.getPhone_no() == null && isPhoneNoRequired(searchQuery)) {
            isValid = false;
            addConstraintViolation(context, ERROR_CODE_REQUIRED, PHONE_NO);
        }

        if (isInvalidAddressPattern(searchQuery.getPresent_address())) {
            isValid = false;
            addConstraintViolation(context, ERROR_CODE_PATTERN, PRESENT_ADDRESS);
        }

        return isValid;
    }

    private boolean isInvalidAddressPattern(String geoCode) {
        return geoCode != null && !this.pattern.matcher(geoCode).matches();
    }

    private void addConstraintViolation(ConstraintValidatorContext context, String code) {
        context.buildConstraintViolationWithTemplate(code)
                .addConstraintViolation();
    }

    private void addConstraintViolation(ConstraintValidatorContext context, String code, String field) {
        context.buildConstraintViolationWithTemplate(code)
                .addPropertyNode(field)
                .addConstraintViolation();
    }

    private boolean isGivenNameRequired(SearchQuery searchQuery) {
        return StringUtils.isNotBlank(searchQuery.getSur_name()) && !isSingleSearchableFieldsGiven(searchQuery);
    }

    private boolean isPhoneNoRequired(SearchQuery searchQuery) {
        return isOptionalPhoneBlockValuesGiven(searchQuery);
    }

    private boolean isOptionalPhoneBlockValuesGiven(SearchQuery searchQuery) {

        return StringUtils.isNotBlank(searchQuery.getCountry_code()) ||
                StringUtils.isNotBlank(searchQuery.getArea_code()) ||
                StringUtils.isNotBlank(searchQuery.getExtension());

    }

    private boolean isSingleSearchableFieldsGiven(SearchQuery searchQuery) {

        return searchQuery.getNid() != null ||
                searchQuery.getBin_brn() != null ||
                searchQuery.getUid() != null ||
                searchQuery.getPhone_no() != null;

    }
}