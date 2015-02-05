package org.sharedhealth.mci.web.mapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

import static org.sharedhealth.mci.utils.DateUtil.parseDate;


@JsonIgnoreProperties(ignoreUnknown = true)
public class PaginationQuery {
    @JsonProperty("last")
    private String last;

    @org.sharedhealth.mci.validation.constraints.Date(format = "yyyy-MM-dd", message = "1002")
    private String since;

    @JsonProperty("limit")
    private int limit;

    @JsonProperty("maximum_limitt")
    private int maximum_limit;

    public PaginationQuery() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PaginationQuery)) return false;

        PaginationQuery that = (PaginationQuery) o;

        if (limit != that.limit) return false;
        if (maximum_limit != that.maximum_limit) return false;
        if (last != null ? !last.equals(that.last) : that.last != null) return false;
        if (since != null ? !since.equals(that.since) : that.since != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = last != null ? last.hashCode() : 0;
        result = 31 * result + (since != null ? since.hashCode() : 0);
        result = 31 * result + limit;
        result = 31 * result + maximum_limit;
        return result;
    }

    public String getLast() {
        return last;
    }

    public void setLast(String last) {
        this.last = last;
    }

    public String getSince() {
        return since;
    }

    public Date getDateSince() {
        return parseDate(since);
    }

    public void setSince(String since) {
        this.since = since;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getMaximum_limit() {
        return maximum_limit;
    }

    public void setMaximum_limit(int maximum_limit) {
        this.maximum_limit = maximum_limit;
    }
}
