package org.sharedhealth.mci.web.model;


import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;

import java.util.UUID;

import static org.sharedhealth.mci.web.infrastructure.persistence.PatientQueryBuilder.*;
import static org.springframework.cassandra.core.PrimaryKeyType.CLUSTERED;
import static org.springframework.cassandra.core.PrimaryKeyType.PARTITIONED;

@Table(value = CF_APPROVAL_MAPPING)
public class ApprovalMapping {
    @PrimaryKeyColumn(name = DIVISION_ID, ordinal = 0, type = PARTITIONED)
    private String divisionId;

    @PrimaryKeyColumn(name = DISTRICT_ID, ordinal = 1, type = PARTITIONED)
    private String districtId;

    @PrimaryKeyColumn(name = UPAZILLA_ID, ordinal = 2, type = PARTITIONED)
    private String upazilaId;

    @PrimaryKeyColumn(name = CREATED_AT, ordinal = 3, type = CLUSTERED)
    private UUID createdAt;

    @Column(HEALTH_ID)
    private String healthId;

    public String getDivisionId() {
        return divisionId;
    }

    public void setDivisionId(String divisionId) {
        this.divisionId = divisionId;
    }

    public String getDistrictId() {
        return districtId;
    }

    public void setDistrictId(String districtId) {
        this.districtId = districtId;
    }

    public String getUpazilaId() {
        return upazilaId;
    }

    public void setUpazilaId(String upazilaId) {
        this.upazilaId = upazilaId;
    }

    public UUID getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(UUID createdAt) {
        this.createdAt = createdAt;
    }

    public String getHealthId() {
        return healthId;
    }

    public void setHealthId(String healthId) {
        this.healthId = healthId;
    }
}
