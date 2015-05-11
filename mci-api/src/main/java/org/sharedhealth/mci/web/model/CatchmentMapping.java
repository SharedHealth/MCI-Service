package org.sharedhealth.mci.web.model;

import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;

import java.util.UUID;

import static org.sharedhealth.mci.web.infrastructure.persistence.RepositoryConstants.*;
import static org.sharedhealth.mci.web.utils.JsonConstants.CATCHMENT_ID;
import static org.springframework.cassandra.core.PrimaryKeyType.CLUSTERED;
import static org.springframework.cassandra.core.PrimaryKeyType.PARTITIONED;

@Table(value = CF_CATCHMENT_MAPPING)
public class CatchmentMapping {

    @PrimaryKeyColumn(name = CATCHMENT_ID, ordinal = 0, type = PARTITIONED)
    private String catchment_id;

    @PrimaryKeyColumn(name = LAST_UPDATED, ordinal = 2, type = CLUSTERED)
    private UUID last_updated;

    @Column(HEALTH_ID)
    private String health_id;

    public CatchmentMapping() {
    }

    public CatchmentMapping(String catchmentId, UUID lastUpdated, String healthId) {
        this.catchment_id = catchmentId;
        this.last_updated = lastUpdated;
        this.health_id = healthId;
    }

    public String getCatchmentId() {
        return catchment_id;
    }

    public void setCatchmentId(String catchmentId) {
        this.catchment_id = catchmentId;
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

    public void setHealthId(String health_id) {
        this.health_id = health_id;
    }
}
