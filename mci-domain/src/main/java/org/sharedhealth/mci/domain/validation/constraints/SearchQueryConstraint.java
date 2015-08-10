package org.sharedhealth.mci.domain.validation.constraints;


import org.sharedhealth.mci.domain.validation.validator.SearchQueryValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = SearchQueryValidator.class)
@Documented
public @interface SearchQueryConstraint {
    String message() default "1006";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};


    @Target({TYPE, ANNOTATION_TYPE})
    @Retention(RUNTIME)
    @Documented
    @interface List {
        SearchQueryConstraint[] value();
    }
}
