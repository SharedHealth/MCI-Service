package org.sharedhealth.mci.web.mapper;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PendingApprovalResponse {

    @JsonProperty("pending_approval")
    private List<Map<String, String>> pendingApprovals;
    private UUID lastItemId;

    public List<Map<String, String>> getPendingApprovals() {
        return pendingApprovals;
    }

    public void setPendingApprovals(List<Map<String, String>> pendingApprovals) {
        this.pendingApprovals = pendingApprovals;
    }

    public UUID getLastItemId() {
        return lastItemId;
    }

    public void setLastItemId(UUID lastItemId) {
        this.lastItemId = lastItemId;
    }
}
