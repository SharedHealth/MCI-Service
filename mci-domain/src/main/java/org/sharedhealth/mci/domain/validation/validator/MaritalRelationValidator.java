package org.sharedhealth.mci.domain.validation.validator;

import org.sharedhealth.mci.domain.model.PatientData;
import org.sharedhealth.mci.domain.validation.constraints.MaritalRelation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static org.sharedhealth.mci.domain.constant.MCIConstants.MARITAL_STATUS_UNMARRIED;
import static org.sharedhealth.mci.domain.constant.MCIConstants.RELATION_SPOUSE;

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