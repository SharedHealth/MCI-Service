package org.sharedhealth.mci.web.mapper;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.sharedhealth.mci.domain.model.Requester;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.sharedhealth.mci.domain.constant.JsonConstants.*;

public class PatientAuditLogData {

    @JsonProperty(EVENT_TIME)
    private String eventTime;

    @JsonProperty(CHANGE_SET)
    private Map<String, Map<String, Object>> changeSet;

    @JsonProperty(REQUESTED_BY)
    private Map<String, Set<Requester>> requestedBy;

    @JsonProperty(APPROVED_BY)
    private Requester approvedBy;

    @JsonProperty(HID)
    private String healthId;

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

    public Map<String, Set<Requester>> getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(Map<String, Set<Requester>> requestedBy) {
        this.requestedBy = requestedBy;
    }

    public void addRequestedBy(String fieldName, Set<Requester> requesters) {
        if (this.requestedBy == null) {
            this.requestedBy = new HashMap<>();
        }
        this.requestedBy.put(fieldName, requesters);
    }

    public Requester getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(Requester approvedBy) {
        this.approvedBy = approvedBy;
    }

    public void setHealthId(String healthId) {
        this.healthId = healthId;
    }

    public String getHealthId() {
        return healthId;
    }
}
