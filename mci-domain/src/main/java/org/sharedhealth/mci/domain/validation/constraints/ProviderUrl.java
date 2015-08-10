package org.sharedhealth.mci.domain.validation.constraints;


import org.sharedhealth.mci.domain.validation.validator.ProviderUrlValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.sharedhealth.mci.domain.constant.ErrorConstants.ERROR_CODE_INVALID;

@Target({FIELD, METHOD})
@Retention(RUNTIME)
@Constraint(validatedBy = ProviderUrlValidator.class)
public @interface ProviderUrl {

    String message() default ERROR_CODE_INVALID;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
