package org.mci.web.annotation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class AddressIdValidator implements ConstraintValidator<AddressId, String> {

    private AddressType addressType;

    @Override
    public void initialize(AddressId constraintAnnotation) {
        this.addressType = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Dummy implementation
        return Pattern.compile("[\\d]{2,10}").matcher(value).matches();
    }
}
