package org.sharedhealth.mci.web.model;

import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKey;
import org.springframework.data.cassandra.mapping.Table;


@Table(value = "lr_markers")
public class LRMarker {

    @PrimaryKey("type")
    private String type;

    @Column("last_feed_url")
    private String lastFeedUrl;

    public LRMarker(String type, String lastFeedUrl) {
        this.type = type;
        this.lastFeedUrl = lastFeedUrl;
    }

    public LRMarker() {

    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLastFeedUrl() {
        return lastFeedUrl;
    }

    public void setLastFeedUrl(String lastFeedUrl) {
        this.lastFeedUrl = lastFeedUrl;
    }

}