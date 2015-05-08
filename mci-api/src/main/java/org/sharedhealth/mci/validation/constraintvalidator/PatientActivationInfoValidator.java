package org.sharedhealth.mci.validation.constraintvalidator;

import org.apache.commons.lang3.StringUtils;
import org.sharedhealth.mci.validation.constraints.PatientActivationInfoBlock;
import org.sharedhealth.mci.web.mapper.PatientActivationInfo;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PatientActivationInfoValidator implements ConstraintValidator<PatientActivationInfoBlock, PatientActivationInfo> {
    private static final String ERROR_CODE_REQUIRED = "1001";
    @Override
    public void initialize(PatientActivationInfoBlock constraintAnnotation) {

    }

    @Override
    public boolean isValid(PatientActivationInfo value, ConstraintValidatorContext context) {
        if (null == value) {
            return true;
        }

        context.disableDefaultConstraintViolation();
        addConstraintViolation(context, ERROR_CODE_REQUIRED, "activation");

        if (null == value.getActivated()) {
            if (StringUtils.isBlank(value.getMergedWith())) {
                return true;
            }
            return false;
        }

        if (value.getActivated() && StringUtils.isNotBlank(value.getMergedWith())) {
            return false;
        }

        return true;
    }

    private void addConstraintViolation(ConstraintValidatorContext context, String code, String field) {
        context.buildConstraintViolationWithTemplate(code)
                .addPropertyNode(field)
                .addConstraintViolation();
    }
}
