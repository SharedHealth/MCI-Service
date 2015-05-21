package org.sharedhealth.mci.web.mapper;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static org.sharedhealth.mci.web.infrastructure.persistence.RepositoryConstants.CHANGE_SET;
import static org.sharedhealth.mci.web.utils.JsonConstants.*;

public class PatientUpdateLogData {

    @JsonProperty(HEALTH_ID)
    private String healthId;

    @JsonProperty(EVENT_TIME)
    @JsonInclude(NON_EMPTY)
    private String eventTime;

    @JsonProperty(CHANGE_SET)
    @JsonInclude(NON_EMPTY)
    private Map<String, Map<String, Object>> changeSet;

    @JsonProperty(REQUESTED_BY)
    @JsonInclude(NON_EMPTY)
    private Map<String, Set<Requester>> requestedBy;

    @JsonProperty(APPROVED_BY)
    @JsonInclude(NON_EMPTY)
    private Requester approvedBy;

    public String getHealthId() {
        return healthId;
    }

    public void setHealthId(String healthId) {
        this.healthId = healthId;
    }

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
}
