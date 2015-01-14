package org.sharedhealth.mci.web.model;


import java.util.Calendar;
import java.util.Date;

import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;

import static org.sharedhealth.mci.web.infrastructure.persistence.PatientQueryBuilder.CF_PATIENT_UPDATE_LOG;
import static org.sharedhealth.mci.web.infrastructure.persistence.PatientQueryBuilder.CHANGE_SET;
import static org.sharedhealth.mci.web.infrastructure.persistence.PatientQueryBuilder.EVENT_TIME;
import static org.sharedhealth.mci.web.infrastructure.persistence.PatientQueryBuilder.HEALTH_ID;
import static org.sharedhealth.mci.web.infrastructure.persistence.PatientQueryBuilder.YEAR;
import static org.springframework.cassandra.core.PrimaryKeyType.CLUSTERED;
import static org.springframework.cassandra.core.PrimaryKeyType.PARTITIONED;

@Table(value = CF_PATIENT_UPDATE_LOG)
public class PatientUpdateLog {

    @PrimaryKeyColumn(name = YEAR, ordinal = 0, type = PARTITIONED)
    private int year;

    @PrimaryKeyColumn(name = EVENT_TIME, ordinal = 1, type = CLUSTERED)
    private Date eventTime;

    @PrimaryKeyColumn(name = HEALTH_ID, ordinal = 2, type = CLUSTERED)
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

    public void setEventTime(Date eventTime) {
        this.eventTime = eventTime;
        this.setYear(eventTime);
    }

    private void setYear(Date eventTime) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(eventTime);
        this.year = cal.get(Calendar.YEAR);
    }
}
