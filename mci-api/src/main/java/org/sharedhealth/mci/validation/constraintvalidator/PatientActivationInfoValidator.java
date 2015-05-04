package org.sharedhealth.mci.validation.constraintvalidator;

import org.apache.commons.lang3.StringUtils;
import org.sharedhealth.mci.validation.constraints.PatientActivationInfoBlock;
import org.sharedhealth.mci.web.mapper.PatientActivationInfo;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PatientActivationInfoValidator implements ConstraintValidator<PatientActivationInfoBlock, PatientActivationInfo> {
    @Override
    public void initialize(PatientActivationInfoBlock constraintAnnotation) {

    }

    @Override
    public boolean isValid(PatientActivationInfo value, ConstraintValidatorContext context) {
        if (null == value) {
            return true;
        }

        if (null == value.getActive()) {
            if (StringUtils.isBlank(value.getMergedWith())) {
                return true;
            }
            return false;
        }

        if (value.getActive() && StringUtils.isNotBlank(value.getMergedWith())) {
            return false;
        }
        if (!value.getActive() && StringUtils.isBlank(value.getMergedWith())) {
            return false;
        }
        return true;
    }
}
