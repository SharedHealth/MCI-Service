package org.sharedhealth.mci.validation.constraintvalidator;

import org.sharedhealth.mci.validation.constraints.Length;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;


public class LengthValidator implements ConstraintValidator<Length, String> {

    private Length length;

    @Override
    public void initialize(Length length) {
        this.length = length;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        if (value == null) return true;

        String trimValue = value.trim();

        return !(trimValue.length() > length.max() || trimValue.length() < length.min());

    }
}
