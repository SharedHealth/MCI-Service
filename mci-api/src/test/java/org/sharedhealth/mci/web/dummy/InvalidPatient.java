package org.sharedhealth.mci.web.dummy;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.sharedhealth.mci.web.mapper.PatientData;

public class InvalidPatient extends PatientData
{
    public InvalidPatient(){}

    @JsonProperty("invalid_property")
    public String invalidProperty = "some thing";
}
