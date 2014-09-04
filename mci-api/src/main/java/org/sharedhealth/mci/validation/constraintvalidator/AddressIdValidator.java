package org.sharedhealth.mci.validation.constraintvalidator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

import org.sharedhealth.mci.validation.AddressType;
import org.sharedhealth.mci.validation.constraints.AddressId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddressIdValidator implements ConstraintValidator<AddressId, String> {

    private AddressType addressType;
    private static final Logger logger = LoggerFactory.getLogger(AddressIdValidator.class);
    @Override
    public void initialize(AddressId constraintAnnotation) {
        this.addressType = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if(value == null){
            return true;
        }
        // Dummy implementation
        if(this.addressType == AddressType.UPAZILLA){
            return Pattern.compile("[\\d]{2}").matcher(value).matches();
        }
        else if(this.addressType == AddressType.VILLAGE){
            return Pattern.compile("[\\d]{2}").matcher(value).matches();
        }
        else if(this.addressType == AddressType.DIVISION){
            return Pattern.compile("[\\d]{2}").matcher(value).matches();
        }
        else if(this.addressType == AddressType.DISTRICT){
            return Pattern.compile("[\\d]{2}").matcher(value).matches();
        }
        else if(this.addressType == AddressType.UNION){
            return Pattern.compile("[\\d]{2}").matcher(value).matches();
        }
        else if(this.addressType == AddressType.COUNTRY){
            return Pattern.compile("[\\d]{3}").matcher(value).matches();
        }
        else if(this.addressType == AddressType.WARD){
            return Pattern.compile("[\\d]{2}").matcher(value).matches();
        }
        else if(this.addressType == AddressType.CITYCORPORATION){
            return Pattern.compile("[\\d]{2}").matcher(value).matches();
        }
        else if(this.addressType == AddressType.AREAMOUJA){
            return Pattern.compile("[\\d]{3}").matcher(value).matches();
        } else{
            return true;
        }
    }
}
