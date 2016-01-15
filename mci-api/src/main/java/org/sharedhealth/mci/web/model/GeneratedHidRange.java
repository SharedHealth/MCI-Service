package org.sharedhealth.mci.web.model;

import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;

import java.util.UUID;

import static com.datastax.driver.core.utils.UUIDs.timeBased;
import static org.sharedhealth.mci.domain.constant.RepositoryConstants.*;
import static org.springframework.cassandra.core.PrimaryKeyType.PARTITIONED;

@Table(value = CF_GENERATED_HID_RANGE)
public class GeneratedHidRange {

    @PrimaryKeyColumn(name = BLOCK_BEGINS_AT, ordinal = 0, type = PARTITIONED)
    private Long blockBeginsAt;
    
    @PrimaryKeyColumn(name = BEGINS_AT, ordinal = 1, type = PARTITIONED)
    private Long beginsAt;
    
    @Column(ENDS_AT)
    private Long endsAt;
    
    @Column(ALLOCATED_FOR)
    private String allocatedFor;

    @Column(ALLOCATED_AT)
    private UUID allocatedAt;
    
    @Column(REQUESTED_BY)
    private String requestedBy;

    public GeneratedHidRange(Long blockBeginsAt, Long beginsAt, Long endsAt,
                             String allocatedFor, String requestedBy) {
        this.blockBeginsAt = blockBeginsAt;
        this.beginsAt = beginsAt;
        this.endsAt = endsAt;
        this.allocatedFor = allocatedFor;
        this.allocatedAt = timeBased();
        this.requestedBy = requestedBy;
    }

    public Long getBlockBeginsAt() {
        return blockBeginsAt;
    }

    public Long getBeginsAt() {
        return beginsAt;
    }

    public Long getEndsAt() {
        return endsAt;
    }

    public String getAllocatedFor() {
        return allocatedFor;
    }

    public UUID getAllocatedAt() {
        return allocatedAt;
    }

    public String getRequestedBy() {
        return requestedBy;
    }
}
