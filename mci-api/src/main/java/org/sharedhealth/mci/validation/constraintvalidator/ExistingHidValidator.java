package org.sharedhealth.mci.validation.constraintvalidator;

import org.apache.commons.lang3.StringUtils;
import org.sharedhealth.mci.validation.constraints.ExistingHid;
import org.sharedhealth.mci.web.mapper.PatientData;
import org.sharedhealth.mci.web.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Component
public class ExistingHidValidator implements ConstraintValidator<ExistingHid, String> {
    private static final String ERROR_CODE_REQUIRED = "1001";

    private PatientService patientService;

    @Autowired
    public ExistingHidValidator(PatientService patientService) {
        this.patientService = patientService;
    }

    @Override
    public void initialize(ExistingHid constraintAnnotation) {

    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StringUtils.isBlank(value)) {
            return true;
        }

        context.disableDefaultConstraintViolation();
        addConstraintViolation(context, ERROR_CODE_REQUIRED, "hid");

        PatientData patientData = patientService.findByHealthId(value);
        Boolean isActive = patientData.getPatientActivationInfo().getActivated();
        return isActive == null || isActive;
    }

    private void addConstraintViolation(ConstraintValidatorContext context, String code, String field) {
        context.buildConstraintViolationWithTemplate(code)
                .addPropertyNode(field)
                .addConstraintViolation();
    }
}
