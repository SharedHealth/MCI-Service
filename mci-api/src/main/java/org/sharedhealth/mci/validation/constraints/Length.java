package org.sharedhealth.mci.validation.constraints;

import org.sharedhealth.mci.validation.constraintvalidator.DateValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.sharedhealth.mci.validation.constraintvalidator.LengthValidator;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = LengthValidator.class)
@Documented
public @interface Length {
    String message() default "invalid";
    int lengthSize() default 0;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
