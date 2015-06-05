package org.sharedhealth.mci.validation.constraintvalidator;

import org.apache.commons.lang3.StringUtils;
import org.sharedhealth.mci.utils.DateUtil;
import org.sharedhealth.mci.validation.constraints.Date;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class DateValidator implements ConstraintValidator<Date, String> {

    private String format;

    @Override
    public void initialize(Date constraintAnnotation) {
        this.format = constraintAnnotation.format();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        if (value == null || value.trim().equals("")) {
            return true;
        }

        java.util.Date testDate;

        if (StringUtils.isNotBlank(this.format)) {
            testDate = DateUtil.parseDate(value, this.format);
        } else {
            testDate = DateUtil.parseDate(value);
        }

        return null != testDate;
    }
}