package org.sharedhealth.mci.web.mapper;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import static org.sharedhealth.mci.utils.DateUtil.string2Date;

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
    public boolean equals(Object rhs) {
        return EqualsBuilder.reflectionEquals(this, rhs);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
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
        return string2Date(since);
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
