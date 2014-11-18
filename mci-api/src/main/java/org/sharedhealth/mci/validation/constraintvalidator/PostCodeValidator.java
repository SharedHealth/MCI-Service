package org.sharedhealth.mci.validation.constraintvalidator;

import org.apache.commons.lang3.StringUtils;
import org.sharedhealth.mci.validation.constraints.PostCode;
import org.sharedhealth.mci.web.mapper.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Component
public class PostCodeValidator implements ConstraintValidator<PostCode, Address> {

    private static final Logger logger = LoggerFactory.getLogger(PostCodeValidator.class);
    private static final String BD_COUNTRY_CODE = "050";
    private static final String ERROR_CODE_PATTERN = "1004";
    public static final int BANGLADESH_POST_CODE_LENGTH = 4;

    private String countryCode;


    @Override
    public void initialize(PostCode constraintAnnotation) {
        this.countryCode = constraintAnnotation.country_code();
    }

    @Override
    public boolean isValid(Address value, ConstraintValidatorContext context) {

        boolean isValid = true;

        if (value == null) return true;

        context.disableDefaultConstraintViolation();

        if (value.getCountryCode() != null && value.getCountryCode().equals(BD_COUNTRY_CODE) &&
                value.getPostCode() != null && StringUtils.length(value.getPostCode()) != BANGLADESH_POST_CODE_LENGTH) {
            isValid = false;
            addConstraintViolation(context, ERROR_CODE_PATTERN, "postCode");
        }

        return isValid;

    }

    private void addConstraintViolation(ConstraintValidatorContext context, String code, String field) {
        context.buildConstraintViolationWithTemplate(code)
                .addPropertyNode(field)
                .addConstraintViolation();
    }


}
