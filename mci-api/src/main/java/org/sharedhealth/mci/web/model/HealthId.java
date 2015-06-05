package org.sharedhealth.mci.web.model;

import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;

import static org.sharedhealth.mci.web.infrastructure.persistence.RepositoryConstants.CF_HEALTH_ID;
import static org.sharedhealth.mci.web.infrastructure.persistence.RepositoryConstants.HID;
import static org.sharedhealth.mci.web.infrastructure.persistence.RepositoryConstants.RESERVED_FOR;
import static org.sharedhealth.mci.web.infrastructure.persistence.RepositoryConstants.STATUS;
import static org.springframework.cassandra.core.PrimaryKeyType.PARTITIONED;

@Table(value = CF_HEALTH_ID)
public class HealthId {
    public static HealthId NULL_HID = new HealthId("00000000000", "MCI", 0);
    @PrimaryKeyColumn(name = HID, ordinal = 0, type = PARTITIONED)
    private String hid;

    @Column(RESERVED_FOR)
    private String reservedFor;

    @Column(STATUS)
    private Integer status;

    public HealthId(String hid) {
        this(hid, "MCI", 0);
    }

    public HealthId() {

    }

    public HealthId(String hid, String reservedFor, Integer status){
        this.hid = hid;
        this.reservedFor = reservedFor;
        this.status = status;
    }

    public String getHid() {
        return hid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HealthId)) return false;

        HealthId healthId = (HealthId) o;

        if (!hid.equals(healthId.hid)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return hid.hashCode();
    }
}