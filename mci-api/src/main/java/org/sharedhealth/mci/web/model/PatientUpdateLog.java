package org.sharedhealth.mci.web.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.sharedhealth.mci.utils.DateUtil;
import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import static org.sharedhealth.mci.web.infrastructure.persistence.PatientRepositoryConstants.*;
import static org.springframework.cassandra.core.PrimaryKeyType.CLUSTERED;
import static org.springframework.cassandra.core.PrimaryKeyType.PARTITIONED;

@Table(value = CF_PATIENT_UPDATE_LOG)
@JsonIgnoreProperties({"year", "changeSet", "eventTime"})
public class PatientUpdateLog {

    @PrimaryKeyColumn(name = YEAR, ordinal = 0, type = PARTITIONED)
    private int year;

    @PrimaryKeyColumn(name = EVENT_TIME, ordinal = 1, type = CLUSTERED)
    private Date eventTime;

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

    public Date getEventTime() {
        return eventTime;
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
    public String getEeventTimeAsString() {
        if(this.eventTime == null) return null;
        return DateUtil.toIsoFormat(this.eventTime.getTime());
    }

    public void setEventTime(Date eventTime) {
        this.eventTime = eventTime;
        this.setYear(eventTime);
    }

    private void setYear(Date eventTime) {
        this.year = DateUtil.getYear(eventTime);
    }
}
