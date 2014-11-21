package org.sharedhealth.mci.web.model;

import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;

import static org.sharedhealth.mci.web.infrastructure.persistence.PatientQueryBuilder.*;
import static org.springframework.cassandra.core.PrimaryKeyType.CLUSTERED;
import static org.springframework.cassandra.core.PrimaryKeyType.PARTITIONED;

@Table(value = CF_NAME_MAPPING)
public class NameMapping {

    @PrimaryKeyColumn(name = DIVISION_ID, ordinal = 0, type = PARTITIONED)
    private String divisionId;

    @PrimaryKeyColumn(name = DISTRICT_ID, ordinal = 1, type = PARTITIONED)
    private String districtId;

    @PrimaryKeyColumn(name = UPAZILLA_ID, ordinal = 2, type = PARTITIONED)
    private String upazilaId;

    @PrimaryKeyColumn(name = GIVEN_NAME, ordinal = 3, type = CLUSTERED)
    private String givenName;

    @PrimaryKeyColumn(name = SUR_NAME, ordinal = 4, type = CLUSTERED)
    private String surname;

    @PrimaryKeyColumn(name = HEALTH_ID, ordinal = 5, type = CLUSTERED)
    private String healthId;

    @Column("city_corp_id")
    private String cityCorpId;

    @Column("ward_id")
    private String wardId;

    @Column("rural_ward_id")
    private String ruralWardId;

    public NameMapping(String divisionId, String districtId, String upazilaId, String givenName, String surname, String healthId) {
        this.divisionId = divisionId;
        this.districtId = districtId;
        this.upazilaId = upazilaId;
        this.givenName = givenName;
        this.surname = surname;
        this.healthId = healthId;
    }

    public void setCityCorpId(String cityCorpId) {
        this.cityCorpId = cityCorpId;
    }

    public void setWardId(String wardId) {
        this.wardId = wardId;
    }

    public void setRuralWardId(String ruralWardId) {
        this.ruralWardId = ruralWardId;
    }
}