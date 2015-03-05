package org.sharedhealth.mci.web.mapper;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.sharedhealth.mci.web.utils.JsonConstants.*;

public class PatientAuditLogData {

    @JsonProperty(EVENT_TIME)
    private String eventTime;

    @JsonProperty(CHANGE_SET)
    private Map<String, Map<String, Object>> changeSet;

    @JsonProperty(REQUESTED_BY)
    private Map<String, Set<String>> requestedBy;

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

    public Map<String, Set<String>> getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(Map<String, Set<String>> requestedBy) {
        this.requestedBy = requestedBy;
    }

    public void addRequestedBy(String fieldName, Set<String> requesters) {
        if (this.requestedBy == null) {
            this.requestedBy = new HashMap<>();
        }
        this.requestedBy.put(fieldName, requesters);
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }
}
