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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GeneratedHidRange that = (GeneratedHidRange) o;

        if (allocatedAt != null ? !allocatedAt.equals(that.allocatedAt) : that.allocatedAt != null) return false;
        if (allocatedFor != null ? !allocatedFor.equals(that.allocatedFor) : that.allocatedFor != null) return false;
        if (beginsAt != null ? !beginsAt.equals(that.beginsAt) : that.beginsAt != null) return false;
        if (blockBeginsAt != null ? !blockBeginsAt.equals(that.blockBeginsAt) : that.blockBeginsAt != null)
            return false;
        if (endsAt != null ? !endsAt.equals(that.endsAt) : that.endsAt != null) return false;
        if (requestedBy != null ? !requestedBy.equals(that.requestedBy) : that.requestedBy != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = blockBeginsAt != null ? blockBeginsAt.hashCode() : 0;
        result = 31 * result + (beginsAt != null ? beginsAt.hashCode() : 0);
        result = 31 * result + (endsAt != null ? endsAt.hashCode() : 0);
        result = 31 * result + (allocatedFor != null ? allocatedFor.hashCode() : 0);
        result = 31 * result + (allocatedAt != null ? allocatedAt.hashCode() : 0);
        result = 31 * result + (requestedBy != null ? requestedBy.hashCode() : 0);
        return result;
    }
}
