package org.sharedhealth.mci.web.mapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import static org.sharedhealth.mci.web.utils.ErrorConstants.ERROR_CODE_PATTERN;
import static org.sharedhealth.mci.web.utils.ErrorConstants.ERROR_CODE_REQUIRED;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DuplicatePatientMergeData {

    @NotNull(message = ERROR_CODE_REQUIRED)
    @Pattern(regexp = "^(1000|1001)", message = ERROR_CODE_PATTERN)
    private String action;

    @NotNull
    @Valid
    private PatientData patient1;

    @NotNull
    @Valid
    private PatientData patient2;

    public DuplicatePatientMergeData() {
    }

    public DuplicatePatientMergeData(String action, PatientData patient1, PatientData patient2) {
        this.action = action;
        this.patient1 = patient1;
        this.patient2 = patient2;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public PatientData getPatient1() {
        return patient1;
    }

    public void setPatient1(PatientData patient1) {
        this.patient1 = patient1;
    }

    public PatientData getPatient2() {
        return patient2;
    }

    public void setPatient2(PatientData patient2) {
        this.patient2 = patient2;
    }
}
