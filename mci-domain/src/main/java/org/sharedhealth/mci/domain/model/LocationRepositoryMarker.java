package org.sharedhealth.mci.domain.model;

import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKey;
import org.springframework.data.cassandra.mapping.Table;


@Table(value = "lr_markers")
public class LocationRepositoryMarker {

    @PrimaryKey("type")
    private String type;

    @Column("last_feed_url")
    private String lastFeedUrl;

    public LocationRepositoryMarker(String type, String lastFeedUrl) {
        this.type = type;
        this.lastFeedUrl = lastFeedUrl;
    }

    public LocationRepositoryMarker() {

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