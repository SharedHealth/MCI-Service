package org.sharedhealth.mci.validation.constraintvalidator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;
import org.sharedhealth.mci.validation.constraints.SearchQueryConstraint;
import org.sharedhealth.mci.web.mapper.SearchQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class SearchQueryValidator implements ConstraintValidator<SearchQueryConstraint, SearchQuery> {

    private static final Logger logger = LoggerFactory.getLogger(SearchQueryValidator.class);
    private static final String ERROR_CODE_REQUIRED = "1001";
    private static final String ERROR_CODE_PATTERN = "1002";

    @Override
    public void initialize(SearchQueryConstraint constraintAnnotation) {

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
            addConstraintViolation(context, "No valid search parameter given");
            return false;
        }

        final boolean isValid = isSingleSearchableFieldsGiven(searchQuery) || isNameAndAddressGiven(searchQuery);

        if (!isValid) {
            registerProperErrorMessage(searchQuery, context);
        }

        return isValid;
    }

    private void registerProperErrorMessage(SearchQuery searchQuery, ConstraintValidatorContext context) {
        String msg = "Incomplete search criteria!";
            if(isBlank(searchQuery.getGiven_name()) && isGivenNameRequired(searchQuery)) {
                msg =  "Please enter a valid name";
            }else if(isNotBlank(searchQuery.getGiven_name()) || isNotBlank(searchQuery.getGiven_name())){
                msg =  "Please provide a valid ID, Address or Phone number";
            }else if(isNotBlank(searchQuery.getPresent_address())){
                msg =  "Please provide a valid ID, Name or Phone number";
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
            addConstraintViolation(context, ERROR_CODE_REQUIRED, "given_name");
        }

        if (searchQuery.getPhone_no() == null && isPhoneNoRequired(searchQuery)) {
            isValid = false;
            addConstraintViolation(context, ERROR_CODE_REQUIRED, "phone_no");
        }

        if (isInvalidAddressPattern(searchQuery.getPresent_address())) {
            isValid = false;
            addConstraintViolation(context, ERROR_CODE_PATTERN, "present_address");
        }

        return isValid;
    }

    private boolean isInvalidAddressPattern(String present_address) {
        return present_address != null  && !present_address.matches("[\\d]{6}|[\\d]{8}|[\\d]{10}|[\\d]{12}");
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