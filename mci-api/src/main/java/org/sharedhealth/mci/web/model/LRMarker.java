package org.sharedhealth.mci.web.model;

import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKey;
import org.springframework.data.cassandra.mapping.Table;


@Table(value = "lr_markers")
public class LRMarker {

    @PrimaryKey("type")
    private String type;

    @Column("last_sync")
    private String lastSync;

    @Column("offset")
    private int offset;

    public LRMarker(String type, String lastSync, int offset) {
        this.type = type;
        this.lastSync = lastSync;
        this.offset = offset;
    }

    public LRMarker() {

    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLastSync() {
        return lastSync;
    }

    public void setLastSync(String lastSync) {
        this.lastSync = lastSync;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }
}