package org.sharedhealth.mci.web.mapper;

import com.fasterxml.jackson.annotation.JsonProperty;

import static org.sharedhealth.mci.utils.DateUtil.toIsoFormat;
import static org.sharedhealth.mci.web.utils.JsonConstants.CREATED_AT;
import static org.sharedhealth.mci.web.utils.JsonConstants.FACILITY_ID;

public class PendingApprovalFieldDetails {

    @JsonProperty(FACILITY_ID)
    private String facilityId;

    private Object value;

    private String createdAt;

    public String getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(String facilityId) {
        this.facilityId = facilityId;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @JsonProperty(CREATED_AT)
    public String getCreatedAt() {
        return this.createdAt;
    }

    @JsonProperty(CREATED_AT)
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = toIsoFormat(createdAt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PendingApprovalFieldDetails)) return false;

        PendingApprovalFieldDetails that = (PendingApprovalFieldDetails) o;

        if (!createdAt.equals(that.createdAt)) return false;
        if (!facilityId.equals(that.facilityId)) return false;
        if (!value.equals(that.value)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = facilityId.hashCode();
        result = 31 * result + value.hashCode();
        result = 31 * result + createdAt.hashCode();
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PendingApprovalFieldDetails{");
        sb.append("facilityId='").append(facilityId).append('\'');
        sb.append(", value=").append(value);
        sb.append(", createdAt='").append(createdAt).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
