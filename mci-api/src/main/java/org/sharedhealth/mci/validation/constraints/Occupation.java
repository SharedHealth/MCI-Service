package org.sharedhealth.mci.validation.constraints;

import org.sharedhealth.mci.validation.constraintvalidator.OccupationValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = OccupationValidator.class)
@Documented
public @interface Occupation {
    String message() default "invalid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
