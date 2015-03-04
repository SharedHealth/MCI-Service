package org.sharedhealth.mci.web.mapper;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

import static org.sharedhealth.mci.web.utils.JsonConstants.CHANGE_SET;
import static org.sharedhealth.mci.web.utils.JsonConstants.EVENT_TIME;

public class PatientAuditLogData {

    @JsonProperty(EVENT_TIME)
    private String eventTime;

    @JsonProperty(CHANGE_SET)
    private Map<String, Map<String, Object>> changeSet;

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
}
