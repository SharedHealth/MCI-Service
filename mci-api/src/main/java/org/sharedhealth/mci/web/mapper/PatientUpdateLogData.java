package org.sharedhealth.mci.web.mapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @JsonIgnore
    public Object getOldValueFromChangeSet(String key) {
        if (changeSet == null) {
            return null;
        }
        Map<String, Object> changeSetMap = changeSet.get(key);
        if (changeSetMap == null) {
            return null;
        }
        return changeSetMap.get(OLD_VALUE);
    }

    @JsonIgnore
    public Catchment getOldCatchmentFromChangeSet() {
        Object oldValue = getOldValueFromChangeSet(PRESENT_ADDRESS);
        if (oldValue != null) {
            return new Catchment((Map<String, String>) oldValue);
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PatientUpdateLogData)) return false;

        PatientUpdateLogData that = (PatientUpdateLogData) o;

        if (approvedBy != null ? !approvedBy.equals(that.approvedBy) : that.approvedBy != null) return false;
        if (changeSet != null ? !changeSet.equals(that.changeSet) : that.changeSet != null) return false;
        if (eventTime != null ? !eventTime.equals(that.eventTime) : that.eventTime != null) return false;
        if (healthId != null ? !healthId.equals(that.healthId) : that.healthId != null) return false;
        if (requestedBy != null ? !requestedBy.equals(that.requestedBy) : that.requestedBy != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = healthId != null ? healthId.hashCode() : 0;
        result = 31 * result + (eventTime != null ? eventTime.hashCode() : 0);
        result = 31 * result + (changeSet != null ? changeSet.hashCode() : 0);
        result = 31 * result + (requestedBy != null ? requestedBy.hashCode() : 0);
        result = 31 * result + (approvedBy != null ? approvedBy.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PatientUpdateLogData{");
        sb.append("healthId='").append(healthId).append('\'');
        sb.append(", eventTime='").append(eventTime).append('\'');
        sb.append(", changeSet=").append(changeSet);
        sb.append(", requestedBy=").append(requestedBy);
        sb.append(", approvedBy=").append(approvedBy);
        sb.append('}');
        return sb.toString();
    }
}
