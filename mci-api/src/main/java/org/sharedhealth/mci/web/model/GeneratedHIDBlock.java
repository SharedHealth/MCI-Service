package org.sharedhealth.mci.web.model;

import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;

import java.util.UUID;

import static com.datastax.driver.core.utils.UUIDs.timeBased;
import static org.sharedhealth.mci.domain.constant.RepositoryConstants.*;
import static org.springframework.cassandra.core.PrimaryKeyType.CLUSTERED;
import static org.springframework.cassandra.core.PrimaryKeyType.PARTITIONED;

@Table(value = CF_GENERATED_HID_BLOCKS)
public class GeneratedHIDBlock {

    @PrimaryKeyColumn(name = SERIES_NO, ordinal = 0, type = PARTITIONED)
    private Long seriesNo;

    @PrimaryKeyColumn(name = GENERATED_FOR, ordinal = 1, type = CLUSTERED)
    private String generatedFor;

    @PrimaryKeyColumn(name = GENERATED_AT, ordinal = 2, type = CLUSTERED)
    private UUID generatedAt;

    @Column(BEGINS_AT)
    private Long beginsAt;

    @Column(ENDS_AT)
    private Long endsAt;

    @Column(TOTAL_HIDS)
    private Long totalHIDs;

    @Column(REQUESTED_BY)
    private String requestedBy;

    public GeneratedHIDBlock(Long seriesNo, String generatedFor, Long beginsAt, Long endsAt,
                             Long totalHIDs, String requestedBy) {
        this.seriesNo = seriesNo;
        this.beginsAt = beginsAt;
        this.endsAt = endsAt;
        this.generatedFor = generatedFor;
        this.totalHIDs = totalHIDs;
        this.generatedAt = timeBased();
        this.requestedBy = requestedBy;
    }

    public Long getSeriesNo() {
        return seriesNo;
    }

    public Long getBeginsAt() {
        return beginsAt;
    }

    public Long getEndsAt() {
        return endsAt;
    }

    public String getGeneratedFor() {
        return generatedFor;
    }

    public UUID getGeneratedAt() {
        return generatedAt;
    }

    public Long getTotalHIDs() {
        return totalHIDs;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GeneratedHIDBlock that = (GeneratedHIDBlock) o;

        if (seriesNo != null ? !seriesNo.equals(that.seriesNo) : that.seriesNo != null) return false;
        if (generatedFor != null ? !generatedFor.equals(that.generatedFor) : that.generatedFor != null) return false;
        if (generatedAt != null ? !generatedAt.equals(that.generatedAt) : that.generatedAt != null) return false;
        if (beginsAt != null ? !beginsAt.equals(that.beginsAt) : that.beginsAt != null) return false;
        if (endsAt != null ? !endsAt.equals(that.endsAt) : that.endsAt != null) return false;
        if (totalHIDs != null ? !totalHIDs.equals(that.totalHIDs) : that.totalHIDs != null) return false;
        return !(requestedBy != null ? !requestedBy.equals(that.requestedBy) : that.requestedBy != null);

    }

    @Override
    public int hashCode() {
        int result = seriesNo != null ? seriesNo.hashCode() : 0;
        result = 31 * result + (generatedFor != null ? generatedFor.hashCode() : 0);
        result = 31 * result + (generatedAt != null ? generatedAt.hashCode() : 0);
        result = 31 * result + (beginsAt != null ? beginsAt.hashCode() : 0);
        result = 31 * result + (endsAt != null ? endsAt.hashCode() : 0);
        result = 31 * result + (totalHIDs != null ? totalHIDs.hashCode() : 0);
        result = 31 * result + (requestedBy != null ? requestedBy.hashCode() : 0);
        return result;
    }
}
