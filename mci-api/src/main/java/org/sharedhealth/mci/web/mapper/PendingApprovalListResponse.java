package org.sharedhealth.mci.web.mapper;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.sharedhealth.mci.web.utils.JsonConstants.LAST_ITEM_ID;
import static org.sharedhealth.mci.web.utils.JsonConstants.PENDING_APPROVALS;

public class PendingApprovalListResponse {

    @JsonProperty(PENDING_APPROVALS)
    private List<Map<String, String>> pendingApprovals;

    @JsonProperty(LAST_ITEM_ID)
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
