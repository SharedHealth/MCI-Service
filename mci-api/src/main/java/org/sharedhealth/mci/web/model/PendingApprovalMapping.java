package org.sharedhealth.mci.web.model;


import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;

import java.util.UUID;

import static org.sharedhealth.mci.web.infrastructure.persistence.PatientQueryBuilder.*;
import static org.springframework.cassandra.core.PrimaryKeyType.CLUSTERED;
import static org.springframework.cassandra.core.PrimaryKeyType.PARTITIONED;

@Table(value = CF_PENDING_APPROVAL_MAPPING)
public class PendingApprovalMapping {
    @PrimaryKeyColumn(name = DIVISION_ID, ordinal = 0, type = PARTITIONED)
    private String division_id;

    @PrimaryKeyColumn(name = DISTRICT_ID, ordinal = 1, type = PARTITIONED)
    private String district_id;

    @PrimaryKeyColumn(name = UPAZILA_ID, ordinal = 2, type = PARTITIONED)
    private String upazila_id;

    @PrimaryKeyColumn(name = LAST_UPDATED, ordinal = 3, type = CLUSTERED)
    private UUID last_updated;

    @Column(HEALTH_ID)
    private String health_id;

    public String getDivisionId() {
        return division_id;
    }

    public void setDivisionId(String divisionId) {
        this.division_id = divisionId;
    }

    public String getDistrictId() {
        return district_id;
    }

    public void setDistrictId(String districtId) {
        this.district_id = districtId;
    }

    public String getUpazilaId() {
        return upazila_id;
    }

    public void setUpazilaId(String upazilaId) {
        this.upazila_id = upazilaId;
    }

    public UUID getLastUpdated() {
        return last_updated;
    }

    public void setLastUpdated(UUID lastUpdated) {
        this.last_updated = lastUpdated;
    }

    public String getHealthId() {
        return health_id;
    }

    public void setHealthId(String healthId) {
        this.health_id = healthId;
    }
}
