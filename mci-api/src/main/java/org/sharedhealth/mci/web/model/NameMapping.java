package org.sharedhealth.mci.web.model;

import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;

import static org.sharedhealth.mci.web.infrastructure.persistence.RepositoryConstants.*;
import static org.springframework.cassandra.core.PrimaryKeyType.CLUSTERED;
import static org.springframework.cassandra.core.PrimaryKeyType.PARTITIONED;

@Table(value = CF_NAME_MAPPING)
public class NameMapping {

    @PrimaryKeyColumn(name = DIVISION_ID, ordinal = 0, type = PARTITIONED)
    private String division_id;

    @PrimaryKeyColumn(name = DISTRICT_ID, ordinal = 1, type = PARTITIONED)
    private String district_id;

    @PrimaryKeyColumn(name = UPAZILA_ID, ordinal = 2, type = PARTITIONED)
    private String upazila_id;

    @PrimaryKeyColumn(name = GIVEN_NAME, ordinal = 3, type = CLUSTERED)
    private String given_name;

    @PrimaryKeyColumn(name = SUR_NAME, ordinal = 4, type = CLUSTERED)
    private String sur_name;

    @PrimaryKeyColumn(name = HEALTH_ID, ordinal = 5, type = CLUSTERED)
    private String health_id;

    public NameMapping() {
    }

    public NameMapping(String divisionId, String districtId, String upazilaId, String givenName, String surname, String healthId) {
        this.division_id = divisionId;
        this.district_id = districtId;
        this.upazila_id = upazilaId;
        this.given_name = givenName;
        this.sur_name = surname;
        this.health_id = healthId;
    }
}