package org.sharedhealth.mci.domain.validation.constraints;


import org.sharedhealth.mci.domain.validation.validator.CodeValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = CodeValidator.class)
@Documented
public @interface Code {
    String message() default "invalid";

    String type();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String regexp() default "";

    boolean allowBlank() default false;
}
