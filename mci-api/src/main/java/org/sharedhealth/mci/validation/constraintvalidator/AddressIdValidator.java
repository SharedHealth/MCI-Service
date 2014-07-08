package org.sharedhealth.mci.validation.constraintvalidator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

import org.sharedhealth.mci.validation.constraints.AddressId;
import org.sharedhealth.mci.validation.AddressType;

public class AddressIdValidator implements ConstraintValidator<AddressId, String> {

    private AddressType addressType;

    @Override
    public void initialize(AddressId constraintAnnotation) {
        this.addressType = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Dummy implementation
        if(value == null) return true;
        return Pattern.compile("[\\d]{2,10}").matcher(value).matches();
    }
}
