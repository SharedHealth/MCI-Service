package org.sharedhealth.mci.web.mapper;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.sharedhealth.mci.utils.DateUtil;

import static org.sharedhealth.mci.web.utils.JsonConstants.CREATED_AT;
import static org.sharedhealth.mci.web.utils.JsonConstants.REQUESTED_BY;

public class PendingApprovalFieldDetails {

    @JsonProperty(REQUESTED_BY)
    private Requester requestedBy;

    private Object value;

    private String createdAt;

    public Requester getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(Requester requestedBy) {
        this.requestedBy = requestedBy;
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
        this.createdAt = DateUtil.toIsoMillisFormat(createdAt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PendingApprovalFieldDetails)) return false;

        PendingApprovalFieldDetails that = (PendingApprovalFieldDetails) o;

        if (!createdAt.equals(that.createdAt)) return false;
        if (!value.equals(that.value)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = value != null ? value.hashCode() : 0;
        result = 31 * result + (createdAt != null ? createdAt.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PendingApprovalFieldDetails{");
        sb.append("requestedBy='").append(requestedBy).append('\'');
        sb.append(", value=").append(value);
        sb.append(", createdAt='").append(createdAt).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
