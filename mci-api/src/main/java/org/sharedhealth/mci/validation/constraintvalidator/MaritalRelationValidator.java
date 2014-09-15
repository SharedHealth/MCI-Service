package org.sharedhealth.mci.validation.constraintvalidator;
import org.sharedhealth.mci.validation.constraints.MaritalRelation;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.sharedhealth.mci.web.mapper.PatientMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class MaritalRelationValidator implements ConstraintValidator<MaritalRelation, PatientMapper> {

    private static final Logger logger = LoggerFactory.getLogger(MaritalRelationValidator.class);
    private String maritalStatus;
    private String relationalStatus;

    @Override
    public void initialize(MaritalRelation constraintAnnotation) {
        maritalStatus = constraintAnnotation.maritalStatus();
        relationalStatus = constraintAnnotation.relationalStatus();
    }

    @Override
    public boolean isValid(final PatientMapper value, final ConstraintValidatorContext context) {
          if(value != null) {
              try {
                  if(value.getMaritalStatus().equals(1)){
                     /* if(value.getRelation("spouse") != null){
                          return false;
                      }*/
                      return false;
                  }

              } catch (final Exception ignore) {
                  logger.debug("empty block does not allow");
              }

          }
        return true;
    }
}