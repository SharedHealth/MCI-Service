package org.sharedhealth.mci.web.mapper;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

import static org.sharedhealth.mci.web.utils.JsonConstants.*;

public class PatientAuditLogData {

    @JsonProperty(EVENT_TIME)
    private String eventTime;

    @JsonProperty(CHANGE_SET)
    private Map<String, Map<String, Object>> changeSet;

    @JsonProperty(REQUESTED_BY)
    private String requestedBy;

    @JsonProperty(APPROVED_BY)
    private String approvedBy;

    public String getEventTime() {
        return eventTime;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }

    public Map<String, Map<String, Object>> getChangeSet() {
        return changeSet;
    }

    public void setChangeSet(Map<String, Map<String, Object>> changeSet) {
        this.changeSet = changeSet;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(String requestedBy) {
        this.requestedBy = requestedBy;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }
}
