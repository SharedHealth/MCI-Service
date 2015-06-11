package org.sharedhealth.mci.web.mapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashSet;
import java.util.Set;

import static org.sharedhealth.mci.web.utils.JsonConstants.*;

public class DuplicatePatientData {

    @JsonProperty(HID1)
    private String healthId1;

    @JsonProperty(HID2)
    private String healthId2;

    @JsonProperty(REASONS)
    private Set<String> reasons;

    @JsonProperty(CREATED_AT)
    private String createdAt;

    @JsonIgnore
    private Catchment catchment1;

    @JsonIgnore
    private Catchment catchment2;

    public DuplicatePatientData() {
    }

    public DuplicatePatientData(String healthId1, String healthId2, Set<String> reasons, String createdAt) {
        this.healthId1 = healthId1;
        this.healthId2 = healthId2;
        this.reasons = reasons;
        this.createdAt = createdAt;
    }

    public String getHealthId1() {
        return healthId1;
    }

    public void setHealthId1(String healthId1) {
        this.healthId1 = healthId1;
    }

    public String getHealthId2() {
        return healthId2;
    }

    public void setHealthId2(String healthId2) {
        this.healthId2 = healthId2;
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

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Catchment getCatchment1() {
        return catchment1;
    }

    public void setCatchment1(Catchment catchment1) {
        this.catchment1 = catchment1;
    }

    public Catchment getCatchment2() {
        return catchment2;
    }

    public void setCatchment2(Catchment catchment2) {
        this.catchment2 = catchment2;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DuplicatePatientData{");
        sb.append("healthId1='").append(healthId1).append('\'');
        sb.append(", healthId2='").append(healthId2).append('\'');
        sb.append(", reasons=").append(reasons);
        sb.append(", createdAt='").append(createdAt).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
