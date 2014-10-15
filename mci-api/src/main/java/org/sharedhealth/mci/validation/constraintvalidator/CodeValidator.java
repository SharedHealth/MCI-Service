package org.sharedhealth.mci.validation.constraintvalidator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.sharedhealth.mci.validation.constraints.Code;
import org.sharedhealth.mci.web.model.MasterData;
import org.sharedhealth.mci.web.service.MasterDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CodeValidator implements ConstraintValidator<Code, String> {

    private Code code;

    private MasterDataService masterDataService;

    @Autowired
    public CodeValidator(MasterDataService dataService) {
        this.masterDataService = dataService;
    }

    @Override
    public void initialize(Code constraintAnnotation) {
        this.code = constraintAnnotation;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        MasterData masterData = masterDataService.findByKey(code.type(), value);

        return masterData != null;
    }
}
