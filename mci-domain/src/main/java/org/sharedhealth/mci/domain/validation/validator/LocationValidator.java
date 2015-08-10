package org.sharedhealth.mci.domain.validation.validator;

import org.apache.commons.lang3.StringUtils;
import org.sharedhealth.mci.domain.model.Address;
import org.sharedhealth.mci.domain.model.LocationData;
import org.sharedhealth.mci.domain.service.LocationService;
import org.sharedhealth.mci.domain.validation.constraints.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

@Component
public class LocationValidator implements ConstraintValidator<Location, Address> {

    private static final Logger logger = LoggerFactory.getLogger(LocationValidator.class);
    private static final String BD_COUNTRY_CODE = "050";
    private static final String ERROR_CODE_REQUIRED = "1001";
    private static final String ERROR_CODE_PATTERN = "1004";
    public static final int BANGLADESH_POST_CODE_LENGTH = 4;

    private LocationService locationService;
    private String countryCode;
    private Pattern pattern;
    private boolean isRequired;

    @Autowired
    public LocationValidator(LocationService locationService) {
        this.locationService = locationService;
    }

    @Override
    public void initialize(Location constraintAnnotation) {
        this.countryCode = constraintAnnotation.country_code();
        this.pattern = Pattern.compile("[\\d]{" + BANGLADESH_POST_CODE_LENGTH + "}");
        this.isRequired = constraintAnnotation.required();
    }

    @Override
    public boolean isValid(Address value, ConstraintValidatorContext context) {

        boolean isValid = true;

        if (value == null) return true;

        String geoCode = value.getGeoCode();

        context.disableDefaultConstraintViolation();

        if(!this.isRequired && value.isEmpty()) {
            return true;
        }

        if(isInvalidAddressLine(value, context)) {
            isValid = false;
        }

        if (StringUtils.isEmpty(this.countryCode) && value.getCountryCode() != null && !value.getCountryCode().equals(BD_COUNTRY_CODE)) {
            return isValid;
        }

        if (StringUtils.isNotEmpty(this.countryCode) && value.getCountryCode() != null && !value.getCountryCode().equals(this.countryCode)) {
            isValid = false;
        }

        isValid =  isMinimumRequiredFieldsGiven(value, context, isValid);

        isValid = isValid && isValidPostCode(value.getPostCode(), context);


        if (isInvalidHierarchy(value)) {
            isValid = false;
        }

        isValid = isValid && isExistInLocationRegistry(geoCode);

        if (!isValid) {
            addConstraintViolation(context, context.getDefaultConstraintMessageTemplate());
        }

        return isValid;

    }

    private boolean isInvalidAddressLine(Address address, ConstraintValidatorContext context) {

        if (StringUtils.isNotBlank(address.getAddressLine()) || address.isEmpty()) {
            return false;
        }

        addConstraintViolation(
                context,
                address.getAddressLine() == null ? ERROR_CODE_REQUIRED : ERROR_CODE_PATTERN,
                "addressLine"
        );

        return true;
    }

    private boolean isValidPostCode(String postCode, ConstraintValidatorContext context) {

        if (isInvalidPostCodePattern(postCode)) {
            addConstraintViolation(context, ERROR_CODE_PATTERN, "postCode");
            return false;
        }

        return true;
    }

    private boolean isInvalidPostCodePattern(String postCode) {
        return postCode != null && !this.pattern.matcher(postCode).matches();
    }

    private boolean isMinimumRequiredFieldsGiven(Address value, ConstraintValidatorContext context, boolean isValid) {

        if (StringUtils.isBlank(value.getDivisionId())) {
            isValid = false;
            addConstraintViolation(context, ERROR_CODE_REQUIRED, "divisionId");
        }

        if (StringUtils.isBlank(value.getDistrictId())) {
            isValid = false;
            addConstraintViolation(context, ERROR_CODE_REQUIRED, "districtId");
        }

        if (StringUtils.isBlank(value.getUpazilaId())) {
            isValid = false;
            addConstraintViolation(context, ERROR_CODE_REQUIRED, "upazilaId");
        }

        return isValid;
    }

    private boolean isInvalidHierarchy(Address address) {
        if (StringUtils.isNotBlank(address.getRuralWardId()) && StringUtils.isBlank(address.getUnionOrUrbanWardId())) {
            return true;
        } else if (StringUtils.isNotBlank(address.getUnionOrUrbanWardId()) && StringUtils.isBlank(address.getCityCorporationId())) {
            return true;
        } else if (StringUtils.isNotBlank(address.getCityCorporationId()) && StringUtils.isBlank(address.getUpazilaId())) {
            return true;
        }

        return false;
    }

    private boolean isExistInLocationRegistry(String geoCode) {
        logger.debug("Validation testing for code : [" + geoCode + "]");

        LocationData location = locationService.findByGeoCode(geoCode);

        return location != null;

    }

    private void addConstraintViolation(ConstraintValidatorContext context, String code, String field) {
        context.buildConstraintViolationWithTemplate(code)
                .addPropertyNode(field)
                .addConstraintViolation();
    }

    private void addConstraintViolation(ConstraintValidatorContext context, String code) {
        context.buildConstraintViolationWithTemplate(code)
                .addConstraintViolation();
    }
}
