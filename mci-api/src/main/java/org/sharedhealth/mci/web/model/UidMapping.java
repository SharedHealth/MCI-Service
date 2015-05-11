package org.sharedhealth.mci.web.model;

import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;

import static org.sharedhealth.mci.web.infrastructure.persistence.RepositoryConstants.*;
import static org.springframework.cassandra.core.PrimaryKeyType.CLUSTERED;
import static org.springframework.cassandra.core.PrimaryKeyType.PARTITIONED;

@Table(value = CF_UID_MAPPING)
public class UidMapping {

    @PrimaryKeyColumn(name = UID, ordinal = 0, type = PARTITIONED)
    private String uid;

    @PrimaryKeyColumn(name = HEALTH_ID, ordinal = 1, type = CLUSTERED)
    private String health_id;

    public UidMapping() {
    }

    public UidMapping(String uid, String healthId) {
        this.uid = uid;
        this.health_id = healthId;
    }
}