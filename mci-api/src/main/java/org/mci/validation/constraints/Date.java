package org.mci.validation.constraints;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import org.mci.validation.constraintvalidator.DateValidator;

@Target({FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = DateValidator.class)
@Documented
public @interface Date {

    String message() default "Must provide date of format {format}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String format() default "yyyy-MM-dd";
}
