package org.sharedhealth.mci.domain.validation.validator;

import org.apache.commons.lang3.StringUtils;
import org.sharedhealth.mci.domain.model.MasterData;
import org.sharedhealth.mci.domain.service.MasterDataService;
import org.sharedhealth.mci.domain.validation.constraints.Code;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component
public class CodeValidator implements ConstraintValidator<Code, String> {

    private Code code;

    private MasterDataService masterDataService;

    private Pattern pattern;

    @Autowired
    public CodeValidator(MasterDataService dataService) {
        this.masterDataService = dataService;
    }

    @Override
    public void initialize(Code constraintAnnotation) {
        this.code = constraintAnnotation;

        if (isNotBlank(code.regexp())) {
            this.pattern = Pattern.compile(this.code.regexp());
        }
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        if (value == null) return true;

        if (StringUtils.isBlank(value)) return this.code.allowBlank();

        if (this.pattern != null && !this.pattern.matcher(value).matches()) {
            return false;
        }

        MasterData masterData = masterDataService.findByKey(code.type(), value);

        return masterData != null;
    }
}
