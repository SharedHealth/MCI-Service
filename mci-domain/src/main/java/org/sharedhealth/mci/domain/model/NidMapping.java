package org.sharedhealth.mci.domain.model;

import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;

import static org.sharedhealth.mci.domain.constant.RepositoryConstants.*;
import static org.springframework.cassandra.core.PrimaryKeyType.CLUSTERED;
import static org.springframework.cassandra.core.PrimaryKeyType.PARTITIONED;

@Table(value = CF_NID_MAPPING)
public class NidMapping {

    @PrimaryKeyColumn(name = NATIONAL_ID, ordinal = 0, type = PARTITIONED)
    private String national_id;

    @PrimaryKeyColumn(name = HEALTH_ID, ordinal = 1, type = CLUSTERED)
    private String health_id;

    public NidMapping() {
    }

    public NidMapping(String nationalId, String healthId) {
        this.national_id = nationalId;
        this.health_id = healthId;
    }
}