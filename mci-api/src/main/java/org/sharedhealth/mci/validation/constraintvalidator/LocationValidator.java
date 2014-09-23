package org.sharedhealth.mci.validation.constraintvalidator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;
import org.sharedhealth.mci.validation.constraints.Location;
import org.sharedhealth.mci.web.mapper.Address;
import org.sharedhealth.mci.web.service.LocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LocationValidator implements ConstraintValidator<Location, Address> {

    private static final Logger logger = LoggerFactory.getLogger(LocationValidator.class);
    private static final String BD_COUNTRY_CODE = "050";
    private static final String ERROR_CODE_REQUIRED = "1001";
    private static final String ERROR_CODE_DEPENDENT = "1005";

    private LocationService locationService;

    @Autowired
    public LocationValidator(LocationService locationService) {
        this.locationService = locationService;
    }

    @Override
    public void initialize(Location constraintAnnotation) {

    }

    @Override
    public boolean isValid(Address value, ConstraintValidatorContext context) {

        if (value == null) return true;

        String geoCode = value.getGeoCode();


        if (value.getCountryCode() != null && !value.getCountryCode().equals(BD_COUNTRY_CODE)) {
            return true;
        }

        boolean isValid = true;

        context.disableDefaultConstraintViolation();

        if (StringUtils.isBlank(value.getDivisionId())) {
            isValid = false;
            addConstraintViolation(context, ERROR_CODE_REQUIRED, "divisionId");
        }

        if (StringUtils.isBlank(value.getDistrictId())) {
            isValid = false;
            addConstraintViolation(context, ERROR_CODE_REQUIRED, "districtId");
        }

        if (StringUtils.isBlank(value.getUpazilaOrThana())) {
            isValid = false;
            addConstraintViolation(context, ERROR_CODE_REQUIRED, "upazilaOrThana");
        } else if (StringUtils.isNotBlank(value.getUpazillaId()) && StringUtils.isNotBlank(value.getThanaId())) {
            isValid = false;
            addConstraintViolation(context, ERROR_CODE_DEPENDENT, "upazilaAndThana");
        }

        if (StringUtils.isNotBlank(value.getUnionId()) && StringUtils.isNotBlank(value.getWardId())) {
            isValid = false;
            addConstraintViolation(context, ERROR_CODE_DEPENDENT + "unionAndWard");
        }

        isValid = isValid && isExistInLocationRegistry(geoCode);

        if(!isValid) {
            addConstraintViolation(context, context.getDefaultConstraintMessageTemplate());
        }

        return isValid;

    }

    private boolean isExistInLocationRegistry(String geoCode) {
        logger.debug("Validation testing for code : [" + geoCode + "]");
        try {
            org.sharedhealth.mci.web.mapper.Location location = locationService.findByGeoCode(geoCode).get();

            if (!StringUtils.isBlank(location.getGeoCode())) {
                return true;
            }

        } catch (Exception e) {
            logger.debug("Validation error for : [" + geoCode + "]");
        }

        return false;
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
