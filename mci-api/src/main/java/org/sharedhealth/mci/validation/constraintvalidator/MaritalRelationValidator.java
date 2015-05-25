package org.sharedhealth.mci.validation.constraintvalidator;

import org.sharedhealth.mci.validation.constraints.MaritalRelation;
import org.sharedhealth.mci.web.mapper.PatientData;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static org.sharedhealth.mci.web.utils.MCIConstants.MARITAL_STATUS_UNMARRIED;
import static org.sharedhealth.mci.web.utils.MCIConstants.RELATION_SPOUSE;

public class MaritalRelationValidator implements ConstraintValidator<MaritalRelation, PatientData> {

    private String field;

    @Override
    public void initialize(MaritalRelation constraintAnnotation) {
        this.field = constraintAnnotation.field();
    }

    @Override
    public boolean isValid(final PatientData value, final ConstraintValidatorContext context) {

        if (value == null) return true;

        if (value.getRelationOfType(RELATION_SPOUSE) == null) return true;

        if (isNotUnmarried(value.getMaritalStatus())) {
            return true;
        }

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode(this.field)
                .addConstraintViolation();
        return false;
    }

    private boolean isNotUnmarried(String maritalStatus) {
        return !MARITAL_STATUS_UNMARRIED.equals(maritalStatus);
    }
}