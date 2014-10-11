package org.sharedhealth.mci.validation.constraintvalidator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import java.util.HashMap;

import org.sharedhealth.mci.validation.constraints.Code;
import org.sharedhealth.mci.web.service.SettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CodeValidator implements ConstraintValidator<Code, String> {

    private Code code;

    private SettingService settingService;

    @Autowired
    public CodeValidator(SettingService settingService) {
        this.settingService = settingService;
    }

    @Override
    public void initialize(Code constraintAnnotation) {
        this.code = constraintAnnotation;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        HashMap<String, String> setting = settingService.getSettingAsHashMapByKey(code.type());

        return setting.get(value) != null;
    }
}
