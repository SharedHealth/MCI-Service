package org.sharedhealth.mci.web.model;

import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;

import static org.sharedhealth.mci.web.infrastructure.persistence.RepositoryConstants.*;
import static org.springframework.cassandra.core.PrimaryKeyType.CLUSTERED;
import static org.springframework.cassandra.core.PrimaryKeyType.PARTITIONED;

@Table(value = CF_BRN_MAPPING)
public class BrnMapping {

    @PrimaryKeyColumn(name = BIN_BRN, ordinal = 0, type = PARTITIONED)
    private String bin_brn;

    @PrimaryKeyColumn(name = HEALTH_ID, ordinal = 1, type = CLUSTERED)
    private String health_id;

    public BrnMapping() {
    }

    public BrnMapping(String brn, String healthId) {
        this.bin_brn = brn;
        this.health_id = healthId;
    }
}