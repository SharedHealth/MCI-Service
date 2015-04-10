package org.sharedhealth.mci.validation.constraintvalidator;

import org.sharedhealth.mci.validation.constraints.ProviderUrl;
import org.sharedhealth.mci.web.config.MCIProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static java.util.regex.Pattern.compile;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.sharedhealth.mci.utils.StringUtil.ensureSuffix;

@Component
public class ProviderUrlValidator implements ConstraintValidator<ProviderUrl, String> {

    private static final String START_MARKER = "^";
    private static final String END_MARKER = "$";
    private static final String PROVIDER_ID_MATCHER = "([0-9]+)";
    private static final String JSON = ".json";

    @Autowired
    private MCIProperties properties;

    @Override
    public void initialize(ProviderUrl constraintAnnotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (isNotBlank(value)) {
            String providerReferencePath = properties.getProviderRegistryUrl();
            String regex = START_MARKER + ensureSuffix(providerReferencePath, "/") + PROVIDER_ID_MATCHER + JSON + END_MARKER;
            return compile(regex).matcher(value).matches();
        }
        return true;
    }
}
