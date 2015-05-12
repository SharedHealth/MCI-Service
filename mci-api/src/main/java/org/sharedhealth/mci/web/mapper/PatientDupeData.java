package org.sharedhealth.mci.web.mapper;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

import static org.sharedhealth.mci.web.utils.JsonConstants.*;

public class PatientDupeData {

    @JsonProperty(HID1)
    private String healthId1;

    @JsonProperty(HID2)
    private String healthId2;

    @JsonProperty(REASONS)
    private Set<String> reasons;

    @JsonProperty(CREATED_AT)
    private String createdAt;

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
