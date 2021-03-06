package org.sharedhealth.mci.deduplication.model;

import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;

import java.util.Set;
import java.util.UUID;

import static org.sharedhealth.mci.domain.constant.RepositoryConstants.*;
import static org.springframework.cassandra.core.PrimaryKeyType.CLUSTERED;
import static org.springframework.cassandra.core.PrimaryKeyType.PARTITIONED;

@Table(value = CF_PATIENT_DUPLICATE)
public class DuplicatePatient {

    @PrimaryKeyColumn(name = CATCHMENT_ID, ordinal = 0, type = PARTITIONED)
    private String catchment_id;

    @PrimaryKeyColumn(name = CREATED_AT, ordinal = 1, type = CLUSTERED)
    private UUID created_at;

    @Column(HEALTH_ID1)
    private String health_id1;

    @Column(HEALTH_ID2)
    private String health_id2;

    @Column(REASONS)
    private Set<String> reasons;

    public DuplicatePatient() {
    }

    public DuplicatePatient(String catchment_id, String health_id1, String health_id2) {
        this(catchment_id, health_id1, health_id2, null, null);
    }

    public DuplicatePatient(String catchment_id, String health_id1, String health_id2, Set<String> reasons, UUID created_at) {
        this.catchment_id = catchment_id;
        this.health_id1 = health_id1;
        this.health_id2 = health_id2;
        this.reasons = reasons;
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

    public Set<String> getReasons() {
        return reasons;
    }

    public void setReasons(Set<String> reasons) {
        this.reasons = reasons;
    }

    public UUID getCreated_at() {
        return created_at;
    }

    public void setCreated_at(UUID created_at) {
        this.created_at = created_at;
    }
}
