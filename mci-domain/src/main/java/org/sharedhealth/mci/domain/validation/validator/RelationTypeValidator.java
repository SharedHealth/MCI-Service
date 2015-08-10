package org.sharedhealth.mci.domain.validation.validator;

import org.apache.commons.lang3.StringUtils;
import org.sharedhealth.mci.domain.model.Relation;
import org.sharedhealth.mci.domain.validation.constraints.RelationType;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class RelationTypeValidator implements ConstraintValidator<RelationType, Relation> {

    private String field;

    @Override
    public void initialize(RelationType constraintAnnotation) {
        this.field = constraintAnnotation.field();
    }

    @Override
    public boolean isValid(final Relation value, final ConstraintValidatorContext context) {

        if (value == null) return true;

        if (StringUtils.isNotBlank(value.getType()) && (!value.isEmpty() || StringUtils.isNotBlank(value.getId()))) {
            return true;
        }

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode(this.field)
                .addConstraintViolation();

        return false;
    }
}