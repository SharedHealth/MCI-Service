package org.sharedhealth.mci.web.infrastructure.persistence;


import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;

import java.util.Date;
import java.util.Map;

import static org.springframework.cassandra.core.PrimaryKeyType.PARTITIONED;

@Table(value = "approval")
public class Approval {
    @PrimaryKeyColumn(name = "health_id", ordinal = 0, type = PARTITIONED)
    private String health_id;
    @Column("approval_fields")
    private Map<String, String> fieldsToApprovedMaps;
    @PrimaryKeyColumn(name = "datetime", ordinal = 0, type = PARTITIONED)
    private Date datetime;
    @PrimaryKeyColumn(name = "facility_id", ordinal = 0, type = PARTITIONED)
    private String facility_id;


    public String getHealth_id() {
        return health_id;
    }

    public void setHealth_id(String health_id) {
        this.health_id = health_id;
    }

    public Map<String, String> getFieldsToApprovedMaps() {
        return fieldsToApprovedMaps;
    }

    public void setFieldsToApprovedMaps(Map<String, String> fieldsToApprovedMaps) {
        this.fieldsToApprovedMaps = fieldsToApprovedMaps;
    }

    public Date getDatetime() {
        return datetime;
    }

    public void setDatetime(Date datetime) {
        this.datetime = datetime;
    }

    public String getFacility_id() {
        return facility_id;
    }

    public void setFacility_id(String facility_id) {
        this.facility_id = facility_id;
    }



}
