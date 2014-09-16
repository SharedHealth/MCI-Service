package org.sharedhealth.mci.validation.constraintvalidator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

import org.sharedhealth.mci.validation.AddressType;
import org.sharedhealth.mci.validation.constraints.AddressId;

public class AddressIdValidator implements ConstraintValidator<AddressId, String> {

    private AddressType addressType;

    @Override
    public void initialize(AddressId constraintAnnotation) {
        this.addressType = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        if (value == null) {
            return true;
        }

        switch (this.addressType) {
            case DIVISION:
                return Pattern.compile("[\\d]{2}").matcher(value).matches();
            case DISTRICT:
                return Pattern.compile("[\\d]{2}").matcher(value).matches();
            case UPAZILLA:
                return Pattern.compile("[\\d]{2}").matcher(value).matches();
            case UNION:
                return Pattern.compile("[\\d]{2}").matcher(value).matches();
            case WARD:
                return Pattern.compile("[\\d]{2}").matcher(value).matches();
            case COUNTRY:
                return Pattern.compile("[\\d]{3}").matcher(value).matches();
            case CITYCORPORATION:
                return Pattern.compile("[\\d]{2}").matcher(value).matches();
        }

        return false;
    }
}
