package org.sharedhealth.mci.web.model;

import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;

import static org.sharedhealth.mci.domain.constant.RepositoryConstants.*;
import static org.springframework.cassandra.core.PrimaryKeyType.PARTITIONED;


@Table(value = CF_ORG_HEALTH_ID)
public class OrgHealthId {

    @PrimaryKeyColumn(name = HEALTH_ID, ordinal = 0, type = PARTITIONED)
    private String healthId;

    @Column(ALLOCATED_FOR)
    private String allocatedFor;

    @Column(USED_AT)
    private String usedAt;

    @Column(IS_USED)
    private boolean isUsed;


    public OrgHealthId(String healthId, String allocatedFor, String usedAt) {
        this.healthId = healthId;
        this.allocatedFor = allocatedFor;
        this.usedAt = usedAt;
        this.isUsed = false;
    }

    public String getHealthId() {
        return healthId;
    }

    public String getAllocatedFor() {
        return allocatedFor;
    }

    public String getUsedAt() {
        return usedAt;
    }

    public boolean isUsed() {
        return isUsed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OrgHealthId that = (OrgHealthId) o;

        if (isUsed != that.isUsed) return false;
        if (healthId != null ? !healthId.equals(that.healthId) : that.healthId != null) return false;
        if (allocatedFor != null ? !allocatedFor.equals(that.allocatedFor) : that.allocatedFor != null) return false;
        return !(usedAt != null ? !usedAt.equals(that.usedAt) : that.usedAt != null);

    }

    @Override
    public int hashCode() {
        int result = healthId != null ? healthId.hashCode() : 0;
        result = 31 * result + (allocatedFor != null ? allocatedFor.hashCode() : 0);
        result = 31 * result + (usedAt != null ? usedAt.hashCode() : 0);
        result = 31 * result + (isUsed ? 1 : 0);
        return result;
    }
}

