package org.sharedhealth.mci.validation.constraintvalidator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
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

        if(value == null) return true;

        if(StringUtils.isBlank(value)) return false;

        if (StringUtils.isNotBlank(code.regexp()) && !Pattern.compile(code.regexp()).matcher(value).matches()) {
            return false;
        }

        MasterData masterData = masterDataService.findByKey(code.type(), value);

        return masterData != null;
    }
}
