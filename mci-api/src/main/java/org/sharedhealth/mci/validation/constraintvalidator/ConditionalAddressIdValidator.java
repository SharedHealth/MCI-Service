package org.sharedhealth.mci.validation.constraintvalidator;

import org.sharedhealth.mci.validation.constraints.ConditionalAddessId;
import org.sharedhealth.mci.web.mapper.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.StringUtils;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ConditionalAddressIdValidator implements ConstraintValidator<ConditionalAddessId, Address> {

    private static final Logger logger = LoggerFactory.getLogger(ConditionalAddressIdValidator.class);

    @Override
    public void initialize(ConditionalAddessId constraintAnnotation) {

    }

    @Override
    public boolean isValid(final Address value, final ConstraintValidatorContext context) {
        if (value != null) {

            if (StringUtils.isNotBlank(value.getWardId()) && StringUtils.isNotBlank(value.getUnionId())) {
                logger.debug("It is word address debug[" + value.getWardId() + "]");
                logger.debug("It is uinon address debug[" + value.getUnionId() + "]");
                return false;
            }

            if (StringUtils.isNotBlank(value.getThanaId()) && StringUtils.isNotBlank(value.getUpazillaId())) {
                logger.debug("It is word address debug[" + value.getWardId() + "]");
                logger.debug("It is uinon address debug[" + value.getUnionId() + "]");
                return false;
            }

            if (StringUtils.isBlank(value.getThanaId()) && StringUtils.isBlank(value.getUpazillaId())) {
                logger.debug("It is word address debug[" + value.getWardId() + "]");
                logger.debug("It is uinon address debug[" + value.getUnionId() + "]");
                return false;
            }
        }

        return true;
    }
}