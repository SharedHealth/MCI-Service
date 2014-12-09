package org.sharedhealth.mci.validation.constraintvalidator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;
import org.sharedhealth.mci.validation.constraints.SearchQueryConstraint;
import org.sharedhealth.mci.web.mapper.SearchQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchQueryValidator implements ConstraintValidator<SearchQueryConstraint, SearchQuery> {

    private static final Logger logger = LoggerFactory.getLogger(SearchQueryValidator.class);
    private static final String ERROR_CODE_INCOMPLETE = "1006";
    private static final String ERROR_CODE_REQUIRED = "1001";

    @Override
    public void initialize(SearchQueryConstraint constraintAnnotation) {

    }

    @Override
    public boolean isValid(final SearchQuery value, final ConstraintValidatorContext context) {

        boolean isValid;

        context.disableDefaultConstraintViolation();

        isValid = isRequiredFieldMissing(value, context);

        return isValid && isBusinessRulesValidationPassed(value, context);

    }

    private boolean isBusinessRulesValidationPassed(SearchQuery searchQuery, ConstraintValidatorContext context) {

        if (searchQuery.isEmpty()) {
            addConstraintViolation(context, ERROR_CODE_REQUIRED);
            return false;
        }

        final boolean isValid = isSingleSearchableFieldsGiven(searchQuery) || isNameAndAddressGiven(searchQuery);

        if(!isValid) {
            addConstraintViolation(context, ERROR_CODE_INCOMPLETE);
        }

        return isValid;
    }

    private boolean isNameAndAddressGiven(SearchQuery value) {
        return value.getPresent_address() != null && value.getGiven_name() != null;
    }

    private boolean isRequiredFieldMissing(SearchQuery searchQuery, ConstraintValidatorContext context) {
        boolean isValid = true;

        if (searchQuery.getGiven_name() == null && isGivenNameRequired(searchQuery)) {
            isValid = false;
            addConstraintViolation(context, ERROR_CODE_REQUIRED, "given_name");
        }

        if (searchQuery.getPhone_no() == null && isPhoneNoRequired(searchQuery)) {
            isValid = false;
            addConstraintViolation(context, ERROR_CODE_REQUIRED, "phone_no");
        }

        return isValid;
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