package org.sharedhealth.mci.deduplication.config.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.sharedhealth.mci.domain.model.PatientSummaryData;
import org.sharedhealth.mci.domain.model.ResponseWithAdditionalInfo;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.sharedhealth.mci.domain.constant.JsonConstants.*;
import static org.sharedhealth.mci.domain.util.DateUtil.toIsoMillisFormat;

public class DuplicatePatientData implements ResponseWithAdditionalInfo {

    @JsonProperty(PATIENT1)
    private PatientSummaryData patient1;

    @JsonProperty(PATIENT2)
    private PatientSummaryData patient2;

    @JsonProperty(REASONS)
    private Set<String> reasons;

    @JsonIgnore
    private UUID createdAt;

    public DuplicatePatientData() {
    }

    public DuplicatePatientData(PatientSummaryData patient1, PatientSummaryData patient2,
                                Set<String> reasons, UUID createdAt) {
        this.patient1 = patient1;
        this.patient2 = patient2;
        this.reasons = reasons;
        this.createdAt = createdAt;
    }

    public PatientSummaryData getPatient1() {
        return patient1;
    }

    public void setPatient1(PatientSummaryData patient1) {
        this.patient1 = patient1;
    }

    public PatientSummaryData getPatient2() {
        return patient2;
    }

    public void setPatient2(PatientSummaryData patient2) {
        this.patient2 = patient2;
    }

    public Set<String> getReasons() {
        return reasons;
    }

    public void setReasons(Set<String> reasons) {
        this.reasons = reasons;
    }

    public void addReason(String reason) {
        if (this.reasons == null) {
            this.reasons = new HashSet<>();
        }
        this.reasons.add(reason);
    }

    public UUID getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(UUID createdAt) {
        this.createdAt = createdAt;
    }

    @JsonProperty(CREATED_AT)
    public String getCreatedAtDateString() {
        return toIsoMillisFormat(getCreatedAt());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DuplicatePatientData{");
        sb.append(", patient1=").append(patient1);
        sb.append(", patient2=").append(patient2);
        sb.append(", reasons=").append(reasons);
        sb.append(", createdAt='").append(createdAt).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public UUID getModifiedAt() {
        return getCreatedAt();
    }
}
