package org.sharedhealth.mci.domain.validation.validator;

import org.sharedhealth.mci.domain.model.PhoneNumber;
import org.sharedhealth.mci.domain.validation.constraints.PhoneBlock;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Component
public class PhoneBlockValidator implements ConstraintValidator<PhoneBlock, PhoneNumber> {

    private static final String ERROR_CODE_REQUIRED = "1001";

    @Override
    public void initialize(PhoneBlock constraintAnnotation) {
    }

    @Override
    public boolean isValid(PhoneNumber value, ConstraintValidatorContext context) {

        if (value == null) return true;

        if (null != value.getNumber() || value.isEmpty()) {
            return true;
        }

        context.disableDefaultConstraintViolation();

        addConstraintViolation(context, ERROR_CODE_REQUIRED, "number");

        return false;
    }

    private void addConstraintViolation(ConstraintValidatorContext context, String code, String field) {
        context.buildConstraintViolationWithTemplate(code)
                .addPropertyNode(field)
                .addConstraintViolation();
    }
}
