package org.sharedhealth.mci.validation.constraints;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.sharedhealth.mci.validation.constraintvalidator.PhoneBlockValidator;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = PhoneBlockValidator.class)
@Documented
public @interface PhoneBlock {

    String message() default "Invalid phone block";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
