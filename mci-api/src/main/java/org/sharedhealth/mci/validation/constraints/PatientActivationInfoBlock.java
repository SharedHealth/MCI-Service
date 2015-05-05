package org.sharedhealth.mci.validation.constraints;

import org.sharedhealth.mci.validation.constraintvalidator.PatientActivationInfoValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = PatientActivationInfoValidator.class)
@Documented
public @interface PatientActivationInfoBlock {
    String message() default "Invalid active info";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
