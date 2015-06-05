package org.sharedhealth.mci.web.mapper;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

import static org.sharedhealth.mci.web.utils.JsonConstants.CREATED_AT;
import static org.sharedhealth.mci.web.utils.JsonConstants.HID1;
import static org.sharedhealth.mci.web.utils.JsonConstants.HID2;
import static org.sharedhealth.mci.web.utils.JsonConstants.REASONS;

public class DuplicatePatientData {

    @JsonProperty(HID1)
    private String healthId1;

    @JsonProperty(HID2)
    private String healthId2;

    @JsonProperty(REASONS)
    private Set<String> reasons;

    @JsonProperty(CREATED_AT)
    private String createdAt;

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

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
