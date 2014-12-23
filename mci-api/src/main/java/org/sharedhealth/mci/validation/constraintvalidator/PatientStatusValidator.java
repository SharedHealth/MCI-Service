package org.sharedhealth.mci.validation.constraintvalidator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;
import org.sharedhealth.mci.validation.constraints.PatientStatus;
import org.sharedhealth.mci.web.mapper.PatientData;

public class PatientStatusValidator implements ConstraintValidator<PatientStatus, PatientData> {

    @Override
    public void initialize(PatientStatus constraintAnnotation) {
    }

    @Override
    public boolean isValid(final PatientData value, final ConstraintValidatorContext context) {

        if (value == null) return true;

        if (StringUtils.isEmpty(value.getDateOfDeath())) return true;

        if (isDead(value.getStatus())) {
            return true;
        }

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode("dateOfDeath")
                .addConstraintViolation();
        return false;
    }

    private boolean isDead(String patientStatus) {
        return patientStatus != null && patientStatus.equals("deceased");
    }
}