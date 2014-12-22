package org.sharedhealth.mci.web.mapper;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.sharedhealth.mci.utils.DateUtil;

import static org.sharedhealth.mci.web.utils.JsonConstants.CREATED_AT;
import static org.sharedhealth.mci.web.utils.JsonConstants.FACILITY_ID;

public class PendingApprovalFieldDetails {

    @JsonProperty(FACILITY_ID)
    private String facilityId;

    private Object value;

    @JsonProperty(CREATED_AT)
    private long createdAt;

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

    public String getCreatedAt() {
        return DateUtil.toIsoFormat(this.createdAt);
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PendingApprovalFieldDetails)) return false;

        PendingApprovalFieldDetails that = (PendingApprovalFieldDetails) o;

        if (createdAt != that.createdAt) return false;
        if (!facilityId.equals(that.facilityId)) return false;
        if (!value.equals(that.value)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = facilityId.hashCode();
        result = 31 * result + value.hashCode();
        result = 31 * result + (int) (createdAt ^ (createdAt >>> 32));
        return result;
    }
}
