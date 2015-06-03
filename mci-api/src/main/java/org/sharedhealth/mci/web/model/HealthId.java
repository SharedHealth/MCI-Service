package org.sharedhealth.mci.web.model;

import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;

import static org.sharedhealth.mci.web.infrastructure.persistence.RepositoryConstants.CF_HID;
import static org.sharedhealth.mci.web.infrastructure.persistence.RepositoryConstants.HID;
import static org.springframework.cassandra.core.PrimaryKeyType.PARTITIONED;

@Table(value = CF_HID)
public class HealthId {
    public static HealthId NULL_HID = new HealthId("00000000000");
    @PrimaryKeyColumn(name = HID, ordinal = 0, type = PARTITIONED)
    private String hid;

    public HealthId() {
    }

    public HealthId(String hid){
        this.hid = hid;
    }

    public String getHid() {
        return hid;
    }
}