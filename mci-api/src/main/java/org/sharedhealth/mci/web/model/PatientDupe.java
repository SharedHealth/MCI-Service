package org.sharedhealth.mci.web.model;

import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;

import java.util.UUID;

import static org.sharedhealth.mci.web.infrastructure.persistence.RepositoryConstants.*;
import static org.springframework.cassandra.core.PrimaryKeyType.CLUSTERED;
import static org.springframework.cassandra.core.PrimaryKeyType.PARTITIONED;

@Table(value = CF_PATIENT_DUPE)
public class PatientDupe {

    @PrimaryKeyColumn(name = CATCHMENT_ID, ordinal = 0, type = PARTITIONED)
    private String catchment_id;

    @PrimaryKeyColumn(name = HEALTH_ID1, ordinal = 1, type = CLUSTERED)
    private String health_id1;

    @PrimaryKeyColumn(name = HEALTH_ID2, ordinal = 2, type = CLUSTERED)
    private String health_id2;

    @PrimaryKeyColumn(name = REASON, ordinal = 3, type = CLUSTERED)
    private String reason;

    @Column(CREATED_AT)
    private UUID created_at;

    public PatientDupe() {
    }

    public PatientDupe(String catchment_id, String health_id1, String health_id2, String reason, UUID created_at) {
        this.catchment_id = catchment_id;
        this.health_id1 = health_id1;
        this.health_id2 = health_id2;
        this.reason = reason;
        this.created_at = created_at;
    }

    public String getCatchment_id() {
        return catchment_id;
    }

    public void setCatchment_id(String catchment_id) {
        this.catchment_id = catchment_id;
    }

    public String getHealth_id1() {
        return health_id1;
    }

    public void setHealth_id1(String health_id1) {
        this.health_id1 = health_id1;
    }

    public String getHealth_id2() {
        return health_id2;
    }

    public void setHealth_id2(String health_id2) {
        this.health_id2 = health_id2;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public UUID getCreated_at() {
        return created_at;
    }

    public void setCreated_at(UUID created_at) {
        this.created_at = created_at;
    }
}
