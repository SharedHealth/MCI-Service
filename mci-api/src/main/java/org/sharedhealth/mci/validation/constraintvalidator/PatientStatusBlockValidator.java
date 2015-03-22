package org.sharedhealth.mci.validation.constraintvalidator;

import org.apache.commons.lang3.StringUtils;
import org.sharedhealth.mci.validation.constraints.PatientStatusBlock;
import org.sharedhealth.mci.web.mapper.PatientStatus;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static org.sharedhealth.mci.web.utils.PatientDataConstants.PATIENT_STATUS_DEAD;

public class PatientStatusBlockValidator implements ConstraintValidator<PatientStatusBlock, PatientStatus> {

    @Override
    public void initialize(PatientStatusBlock constraintAnnotation) {
    }

    @Override
    public boolean isValid(final PatientStatus value, final ConstraintValidatorContext context) {

        if (value == null) return true;

        if (StringUtils.isEmpty(value.getDateOfDeath())) return true;

        if (isDead(value.getType())) {
            return true;
        }

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode("dateOfDeath")
                .addConstraintViolation();
        return false;
    }

    private boolean isDead(String type) {
        return type != null && type.equals(PATIENT_STATUS_DEAD);
    }
}