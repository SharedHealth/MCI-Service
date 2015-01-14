package org.sharedhealth.mci.validation.constraintvalidator;

import org.sharedhealth.mci.validation.constraints.Date;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.text.ParseException;
import java.text.SimpleDateFormat;

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

        SimpleDateFormat sdf = new SimpleDateFormat(this.format);
        sdf.setLenient(false);

        try {

            testDate = new java.util.Date(sdf.parse(value).getTime());
            System.out.println(testDate);

        } catch (ParseException e) {
            //The date pattern is invalid.;
            return false;
        }

        return sdf.format(testDate).equals(value);
    }
}
