package org.sharedhealth.mci.web.model;

import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;

import static org.sharedhealth.mci.web.infrastructure.persistence.RepositoryConstants.CF_HOUSEHOLD_CODE_MAPPING;
import static org.sharedhealth.mci.web.infrastructure.persistence.RepositoryConstants.HEALTH_ID;
import static org.sharedhealth.mci.web.utils.JsonConstants.HOUSEHOLD_CODE;
import static org.springframework.cassandra.core.PrimaryKeyType.CLUSTERED;
import static org.springframework.cassandra.core.PrimaryKeyType.PARTITIONED;

@Table(value = CF_HOUSEHOLD_CODE_MAPPING)
public class HouseholdCodeMapping {

    @PrimaryKeyColumn(name = HOUSEHOLD_CODE, ordinal = 0, type = PARTITIONED)
    private String household_code;

    @PrimaryKeyColumn(name = HEALTH_ID, ordinal = 1, type = CLUSTERED)
    private String health_id;

    public HouseholdCodeMapping() {
    }

    public HouseholdCodeMapping(String household_code, String health_id) {
        this.household_code = household_code;
        this.health_id = health_id;
    }
}