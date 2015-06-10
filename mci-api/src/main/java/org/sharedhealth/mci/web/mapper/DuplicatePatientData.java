package org.sharedhealth.mci.web.mapper;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DuplicatePatientData)) return false;

        DuplicatePatientData that = (DuplicatePatientData) o;

        if (!healthId1.equals(that.healthId1)) return false;
        if (!healthId2.equals(that.healthId2)) return false;
        if (!reasons.equals(that.reasons)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = healthId1.hashCode();
        result = 31 * result + healthId2.hashCode();
        result = 31 * result + reasons.hashCode();
        return result;
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
