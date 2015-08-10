package org.sharedhealth.mci.deduplication.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.sharedhealth.mci.domain.model.PatientData;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import static org.sharedhealth.mci.domain.constant.ErrorConstants.ERROR_CODE_PATTERN;
import static org.sharedhealth.mci.domain.constant.ErrorConstants.ERROR_CODE_REQUIRED;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DuplicatePatientMergeData {

    @NotNull(message = ERROR_CODE_REQUIRED)
    @Pattern(regexp = "^(RETAIN_ALL|MERGE)", message = ERROR_CODE_PATTERN)
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
