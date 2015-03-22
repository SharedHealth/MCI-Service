package org.sharedhealth.mci.validation.constraints;

import org.sharedhealth.mci.validation.constraintvalidator.PatientStatusBlockValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = PatientStatusBlockValidator.class)
@Documented
public @interface PatientStatusBlock {
    String message() default "Invalid patient status block";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
