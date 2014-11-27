package org.sharedhealth.mci.validation.constraintvalidator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;
import org.sharedhealth.mci.validation.constraints.RelationType;
import org.sharedhealth.mci.web.mapper.Relation;

public class RelationTypeValidator implements ConstraintValidator<RelationType, Relation> {

    private String field;

    @Override
    public void initialize(RelationType constraintAnnotation) {
        this.field = constraintAnnotation.field();
    }

    @Override
    public boolean isValid(final Relation value, final ConstraintValidatorContext context) {

        if (value == null) return true;

        if (value.isEmpty()) return true;

        if (StringUtils.isNotBlank(value.getType())) {
            return true;
        }

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode(this.field)
                .addConstraintViolation();

        return false;
    }
}