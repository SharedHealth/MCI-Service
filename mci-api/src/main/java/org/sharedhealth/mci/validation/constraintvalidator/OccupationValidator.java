package org.sharedhealth.mci.validation.constraintvalidator;

import org.sharedhealth.mci.validation.constraints.Occupation;
import java.util.Arrays;
import java.util.List;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class OccupationValidator implements ConstraintValidator<Occupation, String> {

    private static final String[] occupationNumbers = {"01","02","03","04","05","06","07","08","09","10","12","13","14","15","16","17","18","19","20","21","30","31","32","33","34","35","36","37","38","39","40","42","43","44","45","46","50","51","52","53","54","55","56","58","59","60","61","63","64","70","71","72","74","75","76","77","78","79","80","81","82","83","84","85","86","87","88","89","90","91","92"};

    @Override
    public void initialize(Occupation constraintAnnotation) {

    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if(value == null){
            return true;
        }
        List<String> list = Arrays.asList(occupationNumbers);
        boolean b = list.contains(value);
        if (b == false) {
            return false;
        }
        return true;
    }
}
