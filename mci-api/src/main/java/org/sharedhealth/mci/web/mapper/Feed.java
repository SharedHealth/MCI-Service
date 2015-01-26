package org.sharedhealth.mci.web.mapper;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Feed {

    @JsonProperty
    private String author = "MCI";
    @JsonProperty
    private String title;
    @JsonProperty
    private String feedUrl;
    @JsonProperty
    private String prevUrl;
    @JsonProperty
    private String nextUrl;
    @JsonProperty
    private List<FeedEntry> entries;

    public String getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFeedUrl() {
        return feedUrl;
    }

    public void setFeedUrl(String feedUrl) {
        this.feedUrl = feedUrl;
    }

    public String getPrevUrl() {
        return prevUrl;
    }

    public void setPrevUrl(String prevUrl) {
        this.prevUrl = prevUrl;
    }

    public String getNextUrl() {
        return nextUrl;
    }

    public void setNextUrl(String nextUrl) {
        this.nextUrl = nextUrl;
    }

    public List<FeedEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<FeedEntry> entries) {
        this.entries = entries;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FeedResponse{");
        sb.append("\ntitle='").append(title).append('\'');
        sb.append(",\n author='").append(author).append('\'');
        sb.append(",\n feedUrl='").append(feedUrl).append('\'');
        sb.append(",\n prevUrl='").append(prevUrl).append('\'');
        sb.append(",\n nextUrl='").append(nextUrl).append('\'');
        sb.append(",\n entries=").append(entries);
        sb.append('}');
        return sb.toString();
    }
}