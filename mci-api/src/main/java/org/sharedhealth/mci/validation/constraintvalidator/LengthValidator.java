package org.sharedhealth.mci.validation.constraintvalidator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.sharedhealth.mci.validation.constraints.Length;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LengthValidator implements ConstraintValidator<Length, String> {

    private Length length;
    private static final Logger logger = LoggerFactory.getLogger(LengthValidator.class);

    @Override
    public void initialize(Length length) {
        this.length = length;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

     if(value != null) {
        logger.debug(" Name  [" + value + "]");
        logger.debug(" length [" + length.lengthSize() + "]");
        String trimvalue = value.trim();
        logger.debug(" trim value [" + trimvalue + "]");
        logger.debug(" trim lenght [" + trimvalue.length() + "]");
        if (trimvalue.length() > (int) length.lengthSize()) {
            return false;
        }
    }
        return true;

    }
}
