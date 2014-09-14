package org.sharedhealth.mci.validation.constraintvalidator;
import org.sharedhealth.mci.validation.constraints.MaritalRelation;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class MaritalRelationValidator implements ConstraintValidator<MaritalRelation, Object> {

    private static final Logger logger = LoggerFactory.getLogger(MaritalRelationValidator.class);

    @Override
    public void initialize(MaritalRelation constraintAnnotation) {

    }

    @Override
    public boolean isValid(final Object value, final ConstraintValidatorContext context) {
          if(value != null) {
              try {
                  logger.debug("try to find patientmapper object[" +value+ "]");
              } catch (final Exception ignore) {
                  logger.debug("empty block does not allow");
              }


          }
        return true;
    }
}