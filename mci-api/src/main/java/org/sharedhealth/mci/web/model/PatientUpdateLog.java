package org.sharedhealth.mci.web.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.sharedhealth.mci.utils.DateUtil;
import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import static org.sharedhealth.mci.web.infrastructure.persistence.PatientRepositoryConstants.*;
import static org.springframework.cassandra.core.PrimaryKeyType.CLUSTERED;
import static org.springframework.cassandra.core.PrimaryKeyType.PARTITIONED;

@Table(value = CF_PATIENT_UPDATE_LOG)
@JsonIgnoreProperties({YEAR, "changeSet", EVENT_ID})
@JsonPropertyOrder({HEALTH_ID, UPDATED_AT, CHANGE_SET})
public class PatientUpdateLog {

    @PrimaryKeyColumn(name = YEAR, ordinal = 0, type = PARTITIONED)
    private int year;

    @PrimaryKeyColumn(name = EVENT_ID, ordinal = 1, type = CLUSTERED)
    @JsonProperty(EVENT_ID)
    private UUID eventId;

    @PrimaryKeyColumn(name = HEALTH_ID, ordinal = 2, type = CLUSTERED)
    @JsonProperty(HEALTH_ID)
    private String healthId;

    @Column(CHANGE_SET)
    private String changeSet;

    public String getHealthId() {
        return healthId;
    }

    public void setHealthId(String healthId) {
        this.healthId = healthId;
    }

    public String getChangeSet() {
        return changeSet;
    }

    public void setChangeSet(String changeSet) {
        this.changeSet = changeSet;
    }

    public int getYear() {
        return year;
    }

    @JsonProperty(CHANGE_SET)
    public Map getChangeSetMap() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(this.changeSet, Map.class);
        } catch (IOException e) {
            return null;
        }
    }

    @JsonProperty(UPDATED_AT)
    public String getEventTimeAsString() {
        if(this.eventId == null) return null;
        return DateUtil.toIsoFormat(eventId);
    }


    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
        this.year = DateUtil.getYear(eventId);
    }
}
