package org.sharedhealth.mci.deduplication.config.model;

import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;

import java.util.Set;

import static org.sharedhealth.mci.domain.constant.RepositoryConstants.*;
import static org.springframework.cassandra.core.PrimaryKeyType.PARTITIONED;

@Table(value = CF_PATIENT_DUPLICATE_IGNORED)
public class DuplicatePatientIgnored {

    @PrimaryKeyColumn(name = HEALTH_ID1, ordinal = 0, type = PARTITIONED)
    private String health_id1;

    @PrimaryKeyColumn(name = HEALTH_ID2, ordinal = 1, type = PARTITIONED)
    private String health_id2;

    @Column(REASONS)
    private Set<String> reasons;

    public DuplicatePatientIgnored() {
    }

    public DuplicatePatientIgnored(String healthId1, String healthId2, Set<String> reasons) {
        this.health_id1 = healthId1;
        this.health_id2 = healthId2;
        this.reasons = reasons;
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
}
