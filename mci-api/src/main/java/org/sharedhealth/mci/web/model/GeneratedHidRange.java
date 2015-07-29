package org.sharedhealth.mci.web.model;

import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;

import static org.sharedhealth.mci.web.infrastructure.persistence.RepositoryConstants.BEGINS_AT;
import static org.sharedhealth.mci.web.infrastructure.persistence.RepositoryConstants.CF_GENERATED_HID_RANGE;
import static org.sharedhealth.mci.web.infrastructure.persistence.RepositoryConstants.ENDS_AT;
import static org.springframework.cassandra.core.PrimaryKeyType.PARTITIONED;

@Table(value = CF_GENERATED_HID_RANGE)
public class GeneratedHidRange {

    @PrimaryKeyColumn(name = BEGINS_AT, ordinal = 0, type = PARTITIONED)
    private Long beginsAt;

    @Column(ENDS_AT)
    private Long endsAt;

    public GeneratedHidRange(Long beginsAt, Long endsAt) {
        this.beginsAt = beginsAt;
        this.endsAt = endsAt;
    }

    public Long getBeginsAt() {
        return beginsAt;
    }

    public Long getEndsAt() {
        return endsAt;
    }
}
