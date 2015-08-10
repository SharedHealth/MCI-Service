package org.sharedhealth.mci.domain.validation.validator;

import org.sharedhealth.mci.domain.config.MCIProperties;
import org.sharedhealth.mci.domain.validation.constraints.ProviderUrl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.sharedhealth.mci.domain.util.StringUtil.ensureSuffix;

@Component
public class ProviderUrlValidator implements ConstraintValidator<ProviderUrl, String> {

    private static final String START_MARKER = "^";
    private static final String END_MARKER = "$";
    private static final String PROVIDER_ID_MATCHER = "([0-9]+)";
    private static final String JSON = ".json";
    private Pattern providerUrlPattern;

    @Autowired
    public ProviderUrlValidator(MCIProperties properties) {
        String providerReferencePath = properties.getProviderRegistryUrl();
        String regex = START_MARKER + ensureSuffix(providerReferencePath, "/") + PROVIDER_ID_MATCHER + JSON + END_MARKER;
        this.providerUrlPattern = compile(regex);
    }

    @Override
    public void initialize(ProviderUrl constraintAnnotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return !isNotBlank(value) || providerUrlPattern.matcher(value).matches();
    }
}
